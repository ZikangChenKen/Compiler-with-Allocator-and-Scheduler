package FrontEnd;

import java.util.*;

public class Allocator {
    Renamer renamer;
    Parser parser;
    private int k;
    private int reserved;
    private int PRNum;
    private int VRNum;
    private int[] VRToPR;
    private int[] PRToVR;
    private int[] VRToSpillLoc;
    private int[] PRNU;
    private int[] PRFlag;
    private int[] VRToConstant;
    private int[] VRIsLoadI;
    private int[] VRSkipped;
    private int SpillLoc;
    private Stack<Integer> unused;
    private IR ir;


    public Allocator(Parser parser, int k) {
        this.renamer = new Renamer(parser);
        renamer.rename();
        this.parser = parser;
        this.k = k;
        this.VRNum = renamer.VRName;
        this.PRNum = k - 1;
        this.reserved = k - 1;
        this.SpillLoc  = 32768;
        this.unused = new Stack<>();

        this.VRToPR = new int[this.VRNum];
        this.PRToVR = new int[this.PRNum];
        this.VRToSpillLoc = new int[this.VRNum];
        this.PRNU = new int[this.PRNum];
        this.PRFlag = new int[this.PRNum];
        this.VRToConstant = new int[this.VRNum];
        this.VRSkipped = new int[this.VRNum];
        this.VRIsLoadI = new int[this.VRNum];

        this.ir = this.parser.irList.getHead();

        for (int i = this.PRNum - 1; i >= 0; i--) {
            this.unused.push(i);
        }
        for (int i = 0; i < this.VRNum; i++) {
            this.VRToPR[i] = -1;
            this.VRToSpillLoc[i] = -1;
        }
        for (int i = 0; i < this.PRNum; i++) {
            this.PRToVR[i] = -1;
            this.PRNU[i] = -1;
        }
    }

    public void allocate() {
        while (ir != null) {
            for (int i = 0; i < this.PRNum; i++) {
                this.PRFlag[i] = 0;
            }

            if (ir.opcode == Opcode.loadI) {
                int vr = ir.operands[2][1];
                this.VRIsLoadI[vr] = 1;
                this.VRSkipped[vr] = 1;
                this.VRToConstant[vr] = ir.operands[0][0];
                this.parser.irList.remove(ir);
                ir = ir.getNext();
                continue;
            }

            if (ir.operands[0][1] >= 0) {
                int pr = this.VRToPR[ir.operands[0][1]];
                if (pr == -1) {
                    // invalid
                    ir.operands[0][2] = this.getPR(ir.operands[0][1], ir.operands[0][3]);
                    this.restore(ir.operands[0][1], ir.operands[0][2]);

                } else {
                    ir.operands[0][2] = pr;
                    this.PRNU[pr] = ir.operands[0][3];
                }
                this.PRFlag[ir.operands[0][2]] = 1;
            }

            if (ir.operands[1][1] >= 0) {
                int pr = this.VRToPR[ir.operands[1][1]];
                if (pr == -1) {
                    ir.operands[1][2] = this.getPR(ir.operands[1][1], ir.operands[1][3]);
                    this.restore(ir.operands[1][1], ir.operands[1][2]);
                } else {
                    ir.operands[1][2] = pr;
                    this.PRNU[pr] = ir.operands[1][3];
                }
                this.PRFlag[ir.operands[1][2]] = 1;
            }

            if (ir.operands[0][1] >= 0) {
                if (ir.operands[0][3] == -1 && this.PRToVR[ir.operands[0][2]] != -1) {
                    this.free(ir.operands[0][2]);
                }
            }

            if (ir.operands[1][1] >= 0) {
                if (ir.operands[1][3] == -1 && this.PRToVR[ir.operands[1][2]] != -1) {
                    this.free(ir.operands[1][2]);
                }
            }

            // Flag clear.
            for (int i = 0; i < this.PRNum; i++) {
                this.PRFlag[i] = 0;
            }

            if (ir.operands[2][1] >= 0) {
                ir.operands[2][2] = this.getPR(ir.operands[2][1], ir.operands[2][3]);
                this.PRFlag[ir.operands[2][2]] = 1;
            }
            ir = ir.getNext();
        }
    }

    public void spill(int pr) {
        int vr = this.PRToVR[pr];
        if (this.VRIsLoadI[vr] == 1) {
            this.VRToPR[vr] = -1;
            this.VRSkipped[vr] = 1;
            return;
        }

        // Insert loadI and store.
        IR loadI = new IR("loadI");
        loadI.operands[0][0] = this.SpillLoc;
        loadI.operands[2][2] = this.reserved;
        this.parser.irList.insertBefore(loadI, ir);

        IR store = new IR("store");
        store.operands[0][2] = pr;
        store.operands[1][2] = this.reserved;
        this.parser.irList.insertBefore(store, ir);

        // Update the spillLoc
        this.VRToSpillLoc[vr] = this.SpillLoc;
        this.VRToPR[vr] = -1;
        this.SpillLoc += 4;
    }

    public void restore(int vr, int pr) {
        if (this.VRToSpillLoc[vr] != -1) {
            // Insert loadI.
            IR loadI = new IR("loadI");
            loadI.operands[0][0] = this.VRToSpillLoc[vr];
            loadI.operands[2][2] = this.reserved;
            this.parser.irList.insertBefore(loadI, ir);

            // Insert load.
            IR load = new IR("load");
            load.operands[0][2] = this.reserved;
            load.operands[2][2] = pr;
            this.parser.irList.insertBefore(load, ir);

            this.VRToSpillLoc[vr] = -1;
            return;
        }

        if (this.VRSkipped[vr] == 1) {
            IR temp = new IR("loadI");
            temp.operands[0][0] = this.VRToConstant[vr];
            temp.operands[2][1] = vr;
            temp.operands[2][2] = pr;
            this.parser.irList.insertBefore(temp, ir);
            this.VRSkipped[vr] = 0;
        }
    }

    public int getPR(int vr, int nu) {
        int res = -1;

        if (!this.unused.isEmpty()) {
            // Assign a free pr
            res = this.unused.pop();
        } else {
            int far = -2;
            for (int i = 0; i < this.PRNum; i++) {
                if (this.PRNU[i] > far && this.PRFlag[i] == 0) {
                    res = i;
                    far = this.PRNU[i];
                }
            }
            this.spill(res);
        }
        this.PRNU[res] = nu;
        this.VRToPR[vr] = res;
        this.PRToVR[res] = vr;
        return res;
    }

    public void free(int pr) {
        this.VRToPR[this.PRToVR[pr]] = -1;
        this.PRToVR[pr] = -1;
        this.PRNU[pr] = -1;
        this.unused.push(pr);
    }

    /**
     * Replace SR with VR.
     */
    public void print(IR ir) {
        String str = "";
        if (ir.opcode == Opcode.loadI) {
            str += "loadI" + "\t";
            str += ir.operands[0][0]; // Constant
            str += "\t=>\tr";
            str += ir.operands[2][2];
        } else if (ir.opcode == Opcode.store) {
            str += "store" + "\tr";
            str += ir.operands[0][2];
            str += "\t=>\tr";
            str += ir.operands[1][2];
        } else if (ir.opcode == Opcode.output) {
            str += "output" + "\t";
            str += ir.operands[0][0]; // Constant
        } else if (ir.opcode == Opcode.nop) {

        } else {
            str += ir.opcode + "\tr" + ir.operands[0][2];
            if (ir.operands[1][0] != -1) {
                // if exists second operands.
                str += ", r";
                str += ir.operands[1][2];
            }
            str += "\t=>\tr";
            str += ir.operands[2][2];
        }
        System.out.println(str);
    }
}
