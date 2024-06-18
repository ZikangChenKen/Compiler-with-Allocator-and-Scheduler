package FrontEnd;

public class IRList {
    private int size;
    private IR head;
    private IR tail;

    public IRList() {
        this.size = 0;
        this.head = null;
        this.tail = null;
    }

    public int size() {
        return this.size;
    }

    public IR getHead() {
        return this.head;
    }

    public IR getTail() {
        return this.tail;
    }

    /*
     * Append IR to the end of the list
     */
    public void append(IR ir) {
        if (this.head == null) {
            this.head = ir;
        } else {
            this.tail.setNext(ir);
            ir.setPrev(this.tail);
        }
        this.tail = ir;
        this.size++;
    }

    public void remove(IR ir) {
        IR prev = ir.getPrev();
        IR next = ir.getNext();

        if (prev == null) {
            this.head = next;
        } else {
            prev.setNext(next);
        }

        if (next == null) {
            this.tail = prev;
        } else {
            next.setPrev(prev);
        }

        this.size--;
    }

    /*
     * Insert ir1 in front of ir2
     */
    public void insertBefore(IR ir1, IR ir2) {
        if (ir2.getPrev() == null) {
            this.head = ir1;
            ir1.setNext(ir2);
            ir2.setPrev(ir1);
        } else {
            IR oldPrev = ir2.getPrev();
            oldPrev.setNext(ir1);
            ir1.setPrev(oldPrev);
            ir2.setPrev(ir1);
            ir1.setNext(ir2);
        }
        this.size++;
    }
}
