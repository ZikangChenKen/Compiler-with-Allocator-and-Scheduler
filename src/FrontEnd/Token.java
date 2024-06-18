package FrontEnd;

public class Token {
    private int category;
    private String lexeme;
    private int lineNum;
    public static String[] categoryArr = {"MEMOP", "LOADI", "ARITHOP", "OUTPUT", "NOP", "CONST", "REG", "COMMA", "INTO"};

    public Token(int category, String lexeme, int lineNum) {
        this.category = category;
        this.lexeme = lexeme;
        // Check if the token is EOL.
        if (this.category == 200) {
            this.lineNum = lineNum - 1;
        } else {
            this.lineNum = lineNum;
        }
    }

    public int getCategory() {
        return category;
    }

    public String getLexeme() {
        return lexeme;
    }

    public int getLineNum() {
        return lineNum;
    }

    public String toString() {
        switch (this.category) {
            case 100:
                // EOF
                return this.lineNum + ": " + "< ENDFILE, \"\" >";
            case 200:
                // EOL
                return this.lineNum + ": " + "< NEWLINE, \"\\n\" >";
            case -1:
                // Error indicator
                return this.lineNum + ": " + "< INVALID, \"" + this.lexeme.trim() + "\" >";
            default:
                return this.lineNum + ": " + "< " + Token.categoryArr[this.category] + ", \"" + this.lexeme.trim() + "\" >";
        }
    }
}
