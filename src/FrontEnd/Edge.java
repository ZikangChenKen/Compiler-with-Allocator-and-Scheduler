package FrontEnd;

public class Edge {
    private Node pred;
    private int type;
    private int data;
    private int weight;

    public Edge(Node pred, int type) {
        this.pred = pred;
        this.type = type;
        this.data = -1;
        if (type == 1) {
            this.weight = 1;
        }
    }

    public int getWeight() {
        if (this.weight == 0) {
            this.weight = Scheduler.latencyTable[this.pred.getIr().opcode.ordinal()];
        }
        return this.weight;
    }

    @Override
    public String toString() {
        return this.pred.toString();
    }

    public Node getPred() {
        return pred;
    }

    public void setPred(Node pred) {
        this.pred = pred;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
