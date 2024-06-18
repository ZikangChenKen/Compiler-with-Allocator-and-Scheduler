package FrontEnd;

import java.util.*;

public class Scheduler {
    private int ops;
    private Renamer renamer;
    private IRList irList;
    private Node[] VR2Node;
//    private int[] distance;
//    private int[] descendant;
    private List<Node> storedNode;
    private List<IR[]> res;
    private Map<Node, List<Edge>> graph;
    private Map<Node, List<Edge>> revGraph;
    private Map<Integer, Set<Integer>> dependency;
    private Node last_load;
    private Node last_store;
    private Node last_output;
    private List<Node> loadAndOutput;

    public static final int[] latencyTable = new int[10];
    private static final int DATA = 0;
    private static final int SERIAL = 1;
    private static final int CONFLICT = 2;

    public Scheduler(Renamer renamer) {
        this.renamer = renamer;
        this.irList = this.renamer.parser.getIRList();
        this.VR2Node = new Node[this.renamer.VRName];
        this.storedNode = new ArrayList<>();
        this.ops = this.irList.getTail().lineNum + 1;

        this.graph = new HashMap<>();
        this.revGraph = new HashMap<>();
        this.dependency = new HashMap<>();
//        this.distance = new int[this.ops];
//        this.descendant = new int[this.ops];
        this.res = new ArrayList<>();
        this.loadAndOutput = new ArrayList<>();

        Arrays.fill(latencyTable, 1);
//        Arrays.fill(distance, -1);
//        Arrays.fill(descendant, -1);
        latencyTable[0] = 5;
        latencyTable[1]= 5;
        latencyTable[5] = 3;
    }

    public void schedule() {
        this.graphBuilder();
        this.revGraphBuilder();
//        for (Node node : graph.keySet()) {
//            this.calcPath(node);
//        }

        // Set the priority
        for (Node node : graph.keySet()) {
            this.calcPriority(node);
        }

//        // Set the priority
//        for (Node node : graph.keySet()) {
//            node.setPriority(10 * this.distance[node.getLineNum()] + this.descendant[node.getLineNum()]);
//        }

        int cycle = 1;
        PriorityQueue<Node> ready = new PriorityQueue<>((a, b) -> (b.getPriority() - a.getPriority()));

        // Put all leaf nodes to readyQueue
        for (Map.Entry<Node, List<Edge>> entry : this.graph.entrySet()) {
            if (entry.getValue().size() == 0) {
                ready.add(entry.getKey());
                entry.getKey().setReady(true);
            }
        }

        Set<Node> active = new HashSet<>();
        Set<Node> temp = new HashSet<>();

        while (!(active.isEmpty() && ready.isEmpty())) {
            // Pick an operation o for each functional unit, move o from ready to active
            boolean f0 = false;
            boolean f1 = false;
            boolean output = false; // determine if output has been chosen
            IR[] curIRs = new IR[2];

            // functional unit0
            while (!f0 && !ready.isEmpty()) {
                Node node = ready.poll();
                if (node.getIr().opcode != Opcode.mult) {
                    active.add(node);
                    node.setActive(true);
                    node.setStartCycle(cycle);
                    curIRs[0] = node.getIr();
                    f0 = true;
                    if (node.getIr().opcode == Opcode.output) {
                        output = true;
                    }
                } else {
                    temp.add(node);
                }
            }
            if (!f0) {
                // add nop
                IR ir = new IR("nop");
                curIRs[0] = ir;
            }

            // add all mult back to ready and clear the temp.
            ready.addAll(temp);
            temp.clear();

            // functional unit 1
            while (!f1 && !ready.isEmpty()) {
                Node node = ready.poll();
                if (node.getIr().opcode != Opcode.load && node.getIr().opcode != Opcode.store && (!output || node.getIr().opcode != Opcode.output)) {
                    active.add(node);
                    node.setActive(true);
                    node.setStartCycle(cycle);
                    curIRs[1] = node.getIr();
                    f1 = true;
                } else {
                    temp.add(node);
                }
            }
            if (!f1) {
                // add nop
                IR ir = new IR("nop");
                curIRs[1] = ir;
            }

            // add all mult back to ready and clear the temp.
            ready.addAll(temp);
            temp.clear();

            // add the chosen IRs and increment cycle count.
            this.res.add(curIRs);
            cycle++;

            Set<Node> remove = new HashSet<>();
            // find each op o in active that retires
            for (Node node : active) {
                if (cycle - node.getStartCycle() == Scheduler.latencyTable[node.getIr().opcode.ordinal()]) {
                    node.setRetire(true);
                    remove.add(node);
                    // for each op d that depends on o
                    for (Edge edge : this.revGraph.get(node)) {
                        Node pred = edge.getPred();
                        boolean toAdd = true;
                        for (Edge otherEdge : this.graph.get(pred)) {
                            if (!otherEdge.getPred().isRetire()) {
                                toAdd = false;
                                break;
                            }
                        }
                        if (!pred.isReady() && toAdd) {
                            ready.add(pred);
                            pred.setReady(true);
                        }
                    }
                }
            }
            active.removeAll(remove);

            // for each multi-cycle operation in active, check ops that depend on o for early releases
            for (Node node : active) {
                Opcode opcode = node.getIr().opcode;
                if (opcode == Opcode.load || opcode == Opcode.store || opcode == Opcode.mult) {
                    for (Edge edge : this.revGraph.get(node)) {
                        if (edge.getType() == SERIAL) {
                            Node pred = edge.getPred();
                            boolean toAdd = true;
                            for (Edge otherEdge : this.graph.get(pred)) {
                                if (otherEdge.getPred() != node && !otherEdge.getPred().isRetire()) {
                                    toAdd = false;
                                    break;
                                }
                            }
                            if (!pred.isReady() && toAdd) {
                                ready.add(pred);
                                pred.setReady(true);
                            }
                        }
                    }
                }
            }
        }
        this.print();
    }

    /**
     * Constructs the dependence graph.
     */
    private void graphBuilder() {
        IR ir = this.irList.getHead();
        while (ir != null) {
            // Skip NOP
            if (ir.opcode != Opcode.nop) {
                //TODO changed priority from 0 to -1.
                Node node = new Node(ir.lineNum, -1, ir);
                this.graph.put(node, new ArrayList<>());
                this.dependency.put(ir.lineNum, new HashSet<>());

                // if o defines some VR
                if (ir.operands[2][1] != -1) {
                    this.VR2Node[ir.operands[2][1]] = node;
                }

                // For each VR used in o, add an edge from o to the node in map
                if (ir.operands[0][1] != -1) {
                    Node pred = this.VR2Node[ir.operands[0][1]];
                    Edge edge = new Edge(pred, DATA);
                    edge.setData(ir.operands[0][1]);
                    this.graph.get(node).add(edge);
                    this.dependency.get(ir.lineNum).add(pred.getLineNum());
                }

                if (ir.operands[1][1] != -1) {
                    Node pred = this.VR2Node[ir.operands[1][1]];
                    Edge edge = new Edge(pred, DATA);
                    edge.setData(ir.operands[0][1]);
                    if (!this.dependency.get(ir.lineNum).contains(pred.getLineNum())) {
                        this.graph.get(node).add(edge);
                        this.dependency.get(ir.lineNum).add(pred.getLineNum());
                    }
                }

                // if o is a load, store or output
                if (ir.opcode == Opcode.load) {
                    if (this.last_store != null) {
                        Edge conflict = new Edge(this.last_store, CONFLICT);
                        if (!this.dependency.get(ir.lineNum).contains(this.last_store.getLineNum())) {
                            this.graph.get(node).add(conflict);
                        }
                    }
                    this.last_load = node;
                    this.loadAndOutput.add(node);

                } else if (ir.opcode == Opcode.store) {
                    if (this.last_store != null) {
                        Edge serial = new Edge(this.last_store, SERIAL);
                        if (!this.dependency.get(ir.lineNum).contains(this.last_store.getLineNum())) {
                            this.graph.get(node).add(serial);
                        }
                    }
                    for (Node item : this.loadAndOutput) {
                        Edge serial = new Edge(item, SERIAL);
                        if (!this.dependency.get(ir.lineNum).contains(item.getLineNum())) {
                            this.graph.get(node).add(serial);
                        }
                    }
                    this.last_store = node;
                } else if (ir.opcode == Opcode.output) {
                    if (this.last_store != null) {
                        Edge conflict = new Edge(this.last_store, CONFLICT);
                        if (!this.dependency.get(ir.lineNum).contains(this.last_store.getLineNum())) {
                            this.graph.get(node).add(conflict);
                        }
                    }
                    if (this.last_output != null) {
                        Edge serial = new Edge(this.last_output, SERIAL);
                        if (!this.dependency.get(ir.lineNum).contains(this.last_output.getLineNum())) {
                            this.graph.get(node).add(serial);
                        }
                    }
                    this.last_output = node;
                    this.loadAndOutput.add(node);
                }
            }
            ir = ir.getNext();
        }
    }

    /**
     * Constructs the reversed dependence graph.
     */
    private void revGraphBuilder() {
        for (Node node : this.graph.keySet()) {
            this.revGraph.put(node, new ArrayList<>());
        }

        for (Node node : this.graph.keySet()) {
            List<Edge> edgeList = this.graph.get(node);
            for (Edge edge : edgeList) {
                Edge revEdge = new Edge(node, edge.getType());
                revEdge.setData(edge.getData());
                revEdge.setWeight(edge.getWeight());
                this.revGraph.get(edge.getPred()).add(revEdge);
            }
        }
    }

//    /**
//     * Calculate the maximum latency-weighted path to a root for each node in the reversed graph
//     * @param node
//     * @return
//     */
//    private int calcPath(Node node) {
//        if (this.distance[node.getLineNum()] != -1) {
//            return this.distance[node.getLineNum()];
//        }
//
//        int max = 0;
//        Node pre = null;
//        for (Edge edge : this.revGraph.get(node)) {
//            int curDist = this.calcPath(edge.getPred()) + edge.getWeight();
//            if (curDist > max) {
//                max = curDist;
//                pre = edge.getPred();
//            }
//        }
//        this.distance[node.getLineNum()] = max;
//        if (pre == null) {
//            this.descendant[node.getLineNum()] = 0;
//        } else {
//            this.descendant[node.getLineNum()] = this.descendant[pre.getLineNum()] + 1;
//        }
//        return max;
//    }

    private int calcPriority(Node node) {
        if (node.getPriority() != -1) {
            return node.getPriority();
        }

        int max = 0;
        for (Edge edge : this.revGraph.get(node)) {
            int curDist = this.calcPriority(edge.getPred()) + edge.getWeight();
            if (curDist > max) {
                max = curDist;
            }
        }
        node.setPriority(max);
        return max;
    }

    private void print() {
        for (IR[] irs : this.res) {
            System.out.println("[ " + irs[0].IR2ILOC(1) + "; " + irs[1].IR2ILOC(1) + "]");
        }
    }
}
