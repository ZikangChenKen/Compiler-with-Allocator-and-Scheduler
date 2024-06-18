package FrontEnd;

public class Node {
    private int priority;
    private IR ir;
    private boolean notReady;
    private boolean ready;
    private boolean active;
    private boolean retire;
    private int lineNum;
    private int startCycle;
    private int counter; // used to determine if the node is ready.

    public Node(int lineNum, int priority, IR ir) {
        this.lineNum = lineNum;
        this.priority = priority;
        this.ir = ir;
        this.notReady = true;
        this.ready = false;
        this.active = false;
        this.retire = false;
        this.startCycle = -1;
        this.counter = 0;
    }

    @Override
    public String toString() {
        return "(" + lineNum + ")";
    }

    public boolean equals(Node node) {
        return this.lineNum == node.lineNum;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public boolean isNotReady() {
        return notReady;
    }

    public void setNotReady(boolean notReady) {
        this.notReady = notReady;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public IR getIr() {
        return ir;
    }

    public void setIr(IR ir) {
        this.ir = ir;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    public int getStartCycle() {
        return startCycle;
    }

    public void setStartCycle(int startCycle) {
        this.startCycle = startCycle;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean isRetire() {
        return retire;
    }

    public void setRetire(boolean retire) {
        this.retire = retire;
    }
}
