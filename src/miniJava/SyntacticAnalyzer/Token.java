package miniJava.SyntacticAnalyzer;


public class Token {
	
	protected int kind;
	protected String spelling;

	public Token(int kind, String spelling) {
		if (kind == Token.IDENTIFIER) {
		      int currentKind = firstReservedWord;
		      boolean searching = true;

		      while (searching) {
		        int comparison = tokenTable[currentKind].compareTo(spelling);
		        if (comparison == 0) {
		          this.kind = currentKind;
		          searching = false;
		        } else if (comparison > 0 || currentKind == lastReservedWord) {
		          this.kind = Token.IDENTIFIER;
		          searching = false;
		        } else {
		          currentKind ++;
		        }
		      }
		} else		
		  this.kind = kind;
		
	    this.spelling = spelling;
	}
	
	public static String spell (int kind) {
	    return tokenTable[kind];
	}
	
	public static final int

    // literals, identifiers, operators...
    INTLITERAL		= 0,
    CHARLITERAL		= 1,
    IDENTIFIER		= 2,
    OPERATOR		= 3,
    UNOP			= 4,

    // reserved words - must be in alphabetical order...
    ARRAY		= 5,    
    BOOLEAN		= 6,
    CLASS		= 7,
    ELSE		= 8,
    FALSE		= 9,
    IF			= 10,
    INT			= 11,
    NEW			= 12,
    PRIVATE		= 13,
    PUBLIC		= 14,
    RETURN		= 15,
    STATIC		= 16,
    THIS		= 17,
    TRUE		= 18,
    VOID		= 19,
    WHILE		= 20,

    // punctuation...
    DOT			= 21,
    COLON		= 22,
    SEMICOLON	= 23,
    COMMA		= 24,
    BECOMES		= 25,

    // brackets...
    LPAREN		= 26,
    RPAREN		= 27,
    LBRACKET	= 28,
    RBRACKET	= 29,
    LCURLY		= 30,
    RCURLY		= 31,

    // special tokens...
    EOT			= 32,
    ERROR		= 33;
	
	private static String[] tokenTable = new String[] {
	    "<int>",
	    "<char>",
	    "<identifier>",
	    "<operator>",
	    "<unop>",
	    "array",
	    "boolean",
	    "class",
	    "else",
	    "false",
	    "if",
	    "int",
	    "new",
	    "private",
	    "public",
	    "return",
	    "static",
	    "this",
	    "true",
	    "void",
	    "while",
	    ".",
	    ":",
	    ";",
	    ",",
	    "=",
	    "(",
	    ")",
	    "[",
	    "]",
	    "{",
	    "}",
	    "",
	    "<error>"
	  };

	  private final static int	firstReservedWord = Token.ARRAY,
	  				lastReservedWord  = Token.WHILE;

}
