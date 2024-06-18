package FrontEnd;

import java.io.IOException;
import java.util.*;

public class Parser {
    private Scanner scanner;
    public int error;
    private Token token;
    public IRList irList;

    public int maxSR;
    public int index;

    public Parser(Scanner scanner) throws IOException {
        this.scanner = scanner;
        this.maxSR = -1;
        this.index = 0;
        this.irList = new IRList();
    }

    public void parseIR() throws IOException {
        token = this.scanner.nextToken();
        int res;
        while (token.getCategory() != 100) {
            switch(token.getCategory()) {
                case Category.MEMOP:
                    res = this.parseMEMOPIR(token.getLexeme());
                    break;
                case Category.LOADI:
                    res = this.parseLOADIIR(token.getLexeme());
                    break;
                case Category.ARITHOP:
                    res = this.parseARITHOPIR(token.getLexeme());
                    break;
                case Category.OUTPUT:
                    res = this.parseOUTPUTIR(token.getLexeme());
                    break;
                case Category.NOP:
                    res = this.parseNOPIR(token.getLexeme());
                    break;
                case Category.EOL:
                    token = this.scanner.nextToken();
                    continue;
                case -1:
                    res = -1;
                    token = this.scanner.nextToken();
                    continue;
                default:
                    System.err.println("Unknown opcode at line " + token.getLineNum());
                    res = -1;
                    break;
            }
            if (res == -1) {
                this.skipToEOL();
            }
            token = this.scanner.nextToken();
        }
    }

    private void skipToEOL() throws IOException {
        while (token.getCategory() != 200 && token.getCategory() != 100) {
            token = this.scanner.nextToken();
        }
    }

    private int parseMEMOPIR(String opcode) throws IOException {
        String operand1;
        String operand3;
        String temp;
        IR ir = new IR(opcode.trim());
        ir.lineNum = token.getLineNum();
        ir.index = this.index++;
        token = scanner.nextToken();
        if (token == null || token.getCategory() != Category.REGISTER) {
            errorMsg("Missing source register in load or store.", token.getLineNum());
            this.error++;
            return -1;
        } else {
            temp = token.getLexeme().trim();
            operand1 = temp.substring(1);
            ir.operands[0][0] = Integer.parseInt(operand1);
            this.maxSR = Math.max(this.maxSR, Integer.parseInt(operand1));
            token = scanner.nextToken();
            if (token == null || token.getCategory() != Category.INTO) {
                errorMsg("Missing '=>' in load or store.", token.getLineNum());
                this.error++;
                return -1;
            } else {
                token = scanner.nextToken();
                if (token == null || token.getCategory() != Category.REGISTER) {
                    errorMsg("Missing target register in load or store.", token.getLineNum());
                    this.error++;
                    return -1;
                } else {
                    temp = token.getLexeme().trim();
                    operand3 = temp.substring(1);
                    if (ir.opcode == Opcode.load) {
                        ir.operands[2][0] = Integer.parseInt(operand3);
                    } else {
                        ir.operands[1][0] = Integer.parseInt(operand3);
                    }
                    this.maxSR = Math.max(this.maxSR, Integer.parseInt(operand3));
                    token = scanner.nextToken();
                    if (token == null || token.getCategory() != Category.EOL) {
                        errorMsg("Extra token at end of line.", token.getLineNum());
                        this.error++;
                        return -1;
                    } else {
                        this.irList.append(ir);
                        return 0;
                    }
                }
            }
        }
    }

    private int parseLOADIIR(String opcode) throws IOException {
        String operand1;
        String operand3;
        String temp;
        IR ir = new IR(opcode.trim());
        ir.lineNum = token.getLineNum();
        ir.index = this.index++;
        token = scanner.nextToken();
        if (token == null || token.getCategory() != Category.CONSTANT) {
            errorMsg("Missing constant in loadI.", token.getLineNum());
            this.error++;
            return -1;
        } else {
            operand1 = token.getLexeme().trim();
            ir.operands[0][0] = Integer.parseInt(operand1);
            token = scanner.nextToken();
            if (token == null || token.getCategory() != Category.INTO) {
                errorMsg("Missing '=>' in loadI.", token.getLineNum());
                this.error++;
                return -1;
            } else {
                token = scanner.nextToken();
                if (token == null || token.getCategory() != Category.REGISTER) {
                    errorMsg("Missing target register in loadI.", token.getLineNum());
                    this.error++;
                    return -1;
                } else {
                    temp = token.getLexeme().trim();
                    operand3 = temp.substring(1);
                    ir.operands[2][0] = Integer.parseInt(operand3);
                    this.maxSR = Math.max(this.maxSR, Integer.parseInt(operand3));
                    token = scanner.nextToken();
                    if (token == null || token.getCategory() != Category.EOL) {
                        errorMsg("Extra token at end of line.", token.getLineNum());
                        this.error++;
                        return -1;
                    } else {
                        this.irList.append(ir);
                        return 0;
                    }
                }
            }
        }
    }

    private int parseARITHOPIR(String opcode) throws IOException {
        String operand1;
        String operand2;
        String operand3;
        String temp;
        IR ir = new IR(opcode.trim());
        ir.lineNum = token.getLineNum();
        ir.index = this.index++;
        token = scanner.nextToken();
        if (token == null || token.getCategory() != Category.REGISTER) {
            errorMsg("First register missing.", token.getLineNum());
            this.error++;
            return -1;
        } else {
            temp = token.getLexeme().trim();
            operand1 = temp.substring(1);
            ir.operands[0][0] = Integer.parseInt(operand1);
            this.maxSR = Math.max(this.maxSR, Integer.parseInt(operand1));
            token = scanner.nextToken();
            if (token == null || token.getCategory() != Category.COMMA) {
                errorMsg("Comma missing.", token.getLineNum());
                this.error++;
                return -1;
            } else {
                token = scanner.nextToken();
                if (token == null || token.getCategory() != Category.REGISTER) {
                    errorMsg("Second register missing", token.getLineNum());
                    this.error++;
                    return -1;
                } else {
                    temp = token.getLexeme().trim();
                    operand2 = temp.substring(1);
                    ir.operands[1][0] = Integer.parseInt(operand2);
                    this.maxSR = Math.max(this.maxSR, Integer.parseInt(operand2));
                    token = scanner.nextToken();
                    if (token == null || token.getCategory() != Category.INTO) {
                        errorMsg("Missing =>.", token.getLineNum());
                        this.error++;
                        return -1;
                    } else {
                        token = scanner.nextToken();
                        if (token == null || token.getCategory() != Category.REGISTER) {
                            errorMsg("Third register missing.", token.getLineNum());
                            this.error++;
                            return -1;
                        } else {
                            temp = token.getLexeme().trim();
                            operand3 = temp.substring(1);
                            ir.operands[2][0] = Integer.parseInt(operand3);
                            this.maxSR = Math.max(this.maxSR, Integer.parseInt(operand3));
                            token = scanner.nextToken();
                            if (token == null || token.getCategory() != Category.EOL) {
                                errorMsg("Extra token at end of line.", token.getLineNum());
                                this.error++;
                                return -1;
                            } else {
                                this.irList.append(ir);
                                return 0;
                            }
                        }
                    }
                }
            }
        }
    }

    private int parseOUTPUTIR(String opcode) throws IOException {
        String operand1;
        IR ir = new IR(opcode.trim());
        ir.lineNum = token.getLineNum();
        ir.index = this.index++;
        token = scanner.nextToken();
        if (token == null || token.getCategory() != Category.CONSTANT) {
            errorMsg("Missing constant in output.", token.getLineNum());
            this.error++;
            return -1;
        } else {
            operand1 = token.getLexeme().trim();
            ir.operands[0][0] = Integer.parseInt(operand1);
            token = scanner.nextToken();
            if (token == null || token.getCategory() != Category.EOL) {
                errorMsg("Extra token at end of line.", token.getLineNum());
                this.error++;
                return -1;
            } else {
                this.irList.append(ir);
                return 0;
            }
        }
    }

    private int parseNOPIR(String opcode) throws IOException {
        IR ir = new IR(opcode.trim());
        ir.lineNum = token.getLineNum();
        ir.index = this.index++;
        Token EOL = scanner.nextToken();
        if (EOL == null || EOL.getCategory() != Category.EOL) {
            errorMsg("Extra token at end of line.", EOL.getLineNum());
            this.error++;
            return -1;
        } else {
            this.irList.append(ir);
            return 0;
        }
    }

    private void errorMsg(String msg, int line) {
        System.err.println("ERROR " + line + ":\t" + msg);
    }

    public IRList getIRList() {
        return this.irList;
    }
}
