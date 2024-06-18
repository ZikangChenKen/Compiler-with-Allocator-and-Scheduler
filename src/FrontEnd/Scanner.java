package FrontEnd;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Scanner {

    private int[][] transTable = new int[42][25];
    private BufferedReader reader;
    private String line;
    /**
     * Set contains all accepted states.
     */
    private Set<Integer> accepted = new HashSet<>(Arrays.asList(5, 7, 11, 12, 18, 24, 27, 33, 35, 36, 37, 38, 39));
    private int pos;
    private int lineNum;
    public boolean EOF;
    public int error;

    /**
     * Constructor for scanner
     * @throws IOException
     */
    public Scanner(BufferedReader reader) throws IOException {
        this.pos = 0;
        this.lineNum = 1;
        this.EOF = false;
        this.fillTransTable();
        this.reader = reader;
        this.line = this.reader.readLine();
    }

    /**
     * Doing roll back.
     */
    private void rollBack() {
        if (this.pos > 0) this.pos--;
    }

    public Token nextToken() throws IOException {
        int state = 0;
        StringBuilder lexeme = new StringBuilder();
        Stack<Integer> stack = new Stack<>();
        // "Bad" as 1314.
        stack.push(1314);
        while (state != -1) {
            // Check if it is a comment.
            if (state == 41) {
                this.pos = this.line.length() + 1;
                return new Token(200, "\n", this.lineNum + 1);
            }
            char c = nextChar();
            // Check EOF.
            if (state == 0 && c == '\0') {
                this.EOF = true;
                return new Token(100, "", this.lineNum);
            }
            lexeme.append(c);
            if (accepted.contains(state)) {
                stack.clear();
            }
            stack.push(state);
            int cPos = charClass(c);
            if (cPos != -1) {
                state = transTable[state][cPos];
            } else {
                state = -1;
            }
        }
        StringBuilder error = new StringBuilder();
        while (!accepted.contains(state) && state != 1314) {
            state = stack.pop();
            if (lexeme.length() != 0) {
                error.insert(0, lexeme.charAt(lexeme.length() - 1));
                lexeme.deleteCharAt(lexeme.length() - 1);
            }
            this.rollBack();
        }
        if (accepted.contains(state)) {
            return new Token(stateToCategory(state), lexeme.toString(), this.lineNum);
        } else {
            this.pos = this.line.length();
            this.error++;
            System.err.println("ERROR " + this.lineNum + ": " + "\"" +  error.toString().trim() + "\"" + " is not a valid word.");
            return new Token(-1, error.toString().trim(), this.lineNum);
        }
    }

    /**
     * Find the next character to scan.
     * @return
     */
    private char nextChar() throws IOException {
        char c;
        if (this.line == null) {
            // EOF
            c = '\0';
        } else if (this.pos == this.line.length()) {
            // EOL
            c = '\n';
            this.pos++;
        } else if (this.pos == this.line.length() + 1) {
            // Need to read a new line
            this.line = this.reader.readLine();
            this.lineNum++;
            this.pos = 0;
            // Would truncate this char so does not matter what it is.
            c = ' ';
        }else {
            c = this.line.charAt(this.pos);
            this.pos++;
        }
        return c;
    }

    /**
     * Fill the transition table.
     */
    private void fillTransTable() {
        for (int i = 0; i <= 41; i++) {
            for (int j = 0; j <= 24; j++) {
                // Set default as error transition.
                this.transTable[i][j] = -1;
            }
        }
        this.transTable[0][0] = 36;
        this.transTable[0][1] = 34;
        this.transTable[0][3] = 22;
        this.transTable[0][10] = 8;
        this.transTable[0][11] = 19;
        this.transTable[0][12] = 25;
        this.transTable[0][13] = 28;
        this.transTable[0][15] = 13;
        this.transTable[0][16] = 1;
        this.transTable[0][20] = 38;
        this.transTable[0][21] = 37;
        this.transTable[0][23] = 40;
        this.transTable[0][24] = 0;
        this.transTable[1][17] = 2;
        this.transTable[1][18] = 6;
        this.transTable[2][13] = 3;
        this.transTable[3][15] = 4;
        this.transTable[4][6] = 5;
        this.transTable[6][4] = 7;
        this.transTable[8][13] = 9;
        this.transTable[8][16] = 14;
        this.transTable[9][3] = 10;
        this.transTable[10][5] = 11;
        this.transTable[11][19] = 12;
        this.transTable[13][16] = 14;
        this.transTable[13][20] = 39;
        this.transTable[14][8] = 15;
        this.transTable[15][9] = 16;
        this.transTable[16][7] = 17;
        this.transTable[17][17] = 18;
        this.transTable[19][18] = 20;
        this.transTable[20][10] = 21;
        this.transTable[21][17] = 18;
        this.transTable[22][5] = 23;
        this.transTable[23][5] = 24;
        this.transTable[25][13] = 26;
        this.transTable[26][14] = 27;
        this.transTable[28][18] = 29;
        this.transTable[29][17] = 30;
        this.transTable[30][14] = 31;
        this.transTable[31][18] = 32;
        this.transTable[32][17] = 33;
        this.transTable[34][2] = 35;
        this.transTable[38][20] = 38;
        this.transTable[39][20] = 39;
        this.transTable[40][23] = 41;
    }

    /**
     * Convert the state to the category
     * @param state
     * @return
     */
    private int stateToCategory(int state) {
        switch (state) {
            case 5:
            case 11:
                return 0; // MEMOP
            case 12:
                return 1; // LOADI
            case 7:
            case 18:
            case 24:
                return 2; // ARITHOP
            case 33:
                return 3; // OUTPUT
            case 27:
                return 4; // NOP
            case 38:
                return 5; // CONST
            case 39:
                return 6; // REG
            case 36:
                return 7; // COMMA
            case 35:
                return 8; // INTO
            case 37:
                return 200; // NEWLINE
            default:
                return -1;
        }
    }

    /**
     * Each valid character's position in the transition table.
     * @param c
     * @return Each character's corresponding number in the table
     */
    private int charClass(char c) {
        switch (c) {
            case ',':
                return 0;
            case '=':
                return 1;
            case '>':
                return 2;
            case 'a':
                return 3;
            case 'b':
                return 4;
            case 'd':
                return 5;
            case 'e':
                return 6;
            case 'f':
                return 7;
            case 'h':
                return 8;
            case 'i':
                return 9;
            case 'l':
                return 10;
            case 'm':
                return 11;
            case 'n':
                return 12;
            case 'o':
                return 13;
            case 'p':
                return 14;
            case 'r':
                return 15;
            case 's':
                return 16;
            case 't':
                return 17;
            case 'u':
                return 18;
            case 'I':
                return 19;
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return 20;
            case '\n':
                return 21;
            case '\0':
                return 22;
            case '/':
                return 23;
            case ' ':
            case '\t':
                return 24;
            default:
                return -1;
        }

    }
}
