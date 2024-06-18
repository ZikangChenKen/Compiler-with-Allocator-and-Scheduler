package FrontEnd;

public class IR {

    private IR prev;
    private IR next;
    public Opcode opcode;
    public int[][] operands = new int[3][4];
    public int lineNum = -1;
    public int index;

    public IR(String opcode) {
        for (int i = 0; i < 3; i ++) {
            for (int j = 0; j < 4; j++) {
                operands[i][j] = -1;
            }
        }
        this.opcode = String2Opcode(opcode);
    }

    public Opcode String2Opcode(String op) {
        switch (op) {
            case "load":
                return Opcode.load;
            case "store":
                return Opcode.store;
            case "loadI":
                return Opcode.loadI;
            case "add":
                return Opcode.add;
            case "sub":
                return Opcode.sub;
            case "mult":
                return Opcode.mult;
            case "lshift":
                return Opcode.lshift;
            case "rshift":
                return Opcode.rshift;
            case "output":
                return Opcode.output;
            case "nop":
                return Opcode.nop;

        }
        return null;
    }

    public IR getPrev() {
        return this.prev;
    }

    public IR getNext() {
        return this.next;
    }

    public void setPrev(IR prev) {
        this.prev = prev;
    }

    public void setNext(IR next) {
        this.next = next;
    }

    public String toString() {
        return (this.lineNum + "\t" +
                this.opcode + "\t" +
                this.operands[0][0] + "\t" + this.operands[0][1] + "\t" + this.operands[0][3] + "\t" +
                this.operands[1][0] + "\t" + this.operands[1][1] + "\t" + this.operands[1][3] + "\t" +
                this.operands[2][0] + "\t" + this.operands[2][1] + "\t" + this.operands[2][3]);
    }

    /**
     * convert IR to ILOC code
     * @param type
     * @return
     */
    public String IR2ILOC(int type) {
        String res = "";
        if (this.opcode == Opcode.store) {
            res += this.opcode.toString() + "\tr";
            res += this.operands[0][type];
            res += "\t=>\tr";
            res += this.operands[1][type];
        } else if (this.opcode == Opcode.loadI) {
            res += this.opcode.toString() + "\t";
            res += this.operands[0][0];
            res += "\t=>\tr";
            res += this.operands[2][type];
        } else if (this.opcode == Opcode.output) {
            res += this.opcode.toString() + "\t";
            res += this.operands[0][0];
        } else if (this.opcode == Opcode.nop) {
            res += "nop";
        } else {
            res += this.opcode.toString() + "\tr" + this.operands[0][type];
            if (this.operands[1][0] != -1) { // There's a second operation
                res += ", r";
                res += this.operands[1][type];
            }
            res += "\t=>\tr";
            res += this.operands[2][type];
        }
        return res;
    }
}
