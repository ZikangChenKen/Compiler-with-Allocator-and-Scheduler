package FrontEnd;

public class Renamer {
    Parser parser;

    public int maxLive;

    public int VRName;

    /**
     * Constructor for Renamer.
     * @param parser
     */
    public Renamer(Parser parser) {
        this.parser = parser;
    }

    /**
     * Do the renaming algorithm.
     */
    public void rename() {
        VRName = 0;
        maxLive = 0;
        int maxSR = parser.maxSR;
        int[] SRToVR = new int[maxSR + 1];
        int[] LU = new int[maxSR + 1];
        for (int i = 0; i <= maxSR; i++) {
            SRToVR[i] = -1;
            LU[i] = -1; // Infinity
        }
        int index = parser.irList.size() - 1;
        IR ir = this.parser.irList.getTail();
        while (index >= 0) {
            // calculate maxLive.
            int cnt = parser.maxSR;
            for (int num: SRToVR) {
                if (num == -1) {
                    cnt--;
                }
            }
            maxLive = Math.max(maxLive, cnt);

            // Defines.
            if (ir.operands[2][0] != -1) {
                if (SRToVR[ir.operands[2][0]] == -1) {
                    SRToVR[ir.operands[2][0]] = VRName++;
                }
                ir.operands[2][1] = SRToVR[ir.operands[2][0]];
                ir.operands[2][3] = LU[ir.operands[2][0]];
                SRToVR[ir.operands[2][0]] = -1;
                LU[ir.operands[2][0]] = -1;
            }

            // Uses
            if (ir.opcode != Opcode.loadI && ir.opcode != Opcode.output) {
                if (ir.operands[0][0] != -1) {
                    if (SRToVR[ir.operands[0][0]] == -1) {
                        SRToVR[ir.operands[0][0]] = VRName++;
                    }
                    ir.operands[0][1] = SRToVR[ir.operands[0][0]];
                    ir.operands[0][3] = LU[ir.operands[0][0]];
                    LU[ir.operands[0][0]] = index;
                } 
                
                if (ir.operands[1][0] != -1) {
                    if (SRToVR[ir.operands[1][0]] == -1) {
                        SRToVR[ir.operands[1][0]] = VRName++;
                    }
                    ir.operands[1][1] = SRToVR[ir.operands[1][0]];
                    ir.operands[1][3] = LU[ir.operands[1][0]];
                    LU[ir.operands[1][0]] = index;
                }
            }
            index--;
            ir = ir.getPrev();
        }
    }

//    /**
//     * Replace SR with VR.
//     */
//    public void replace() {
//        for (IR ir : parser.getIRList()) {
//            String str = "";
//            if (ir.opcode == Opcode.loadI) {
//                str += "loadI" + "\t";
//                str += ir.operands[0][0]; // Constant
//                str += "\t=>\tr";
//                str += ir.operands[2][1];
//            } else if (ir.opcode == Opcode.store) {
//                str += "store" + "\tr";
//                str += ir.operands[0][1];
//                str += "\t=>\tr";
//                str += ir.operands[1][1];
//            } else if (ir.opcode == Opcode.output) {
//                str += "output" + "\t";
//                str += ir.operands[0][0]; // Constant
//            } else if (ir.opcode == Opcode.nop) {
//                continue;
//            } else {
//                str += ir.opcode + "\tr" + ir.operands[0][1];
//                if (ir.operands[1][0] != -1) {
//                    // if exists second operands.
//                    str += ", r";
//                    str += ir.operands[1][1];
//                }
//                str += "\t=>\tr";
//                str += ir.operands[2][1];
//            }
//            System.out.println(str);
//        }
//
//    }
}
