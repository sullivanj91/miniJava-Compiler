package miniJava.SyntacticAnalyzer;



public class Scanner {
	
	 private char currentChar;
	 private SourceFile sourceFile;
	 private StringBuffer currentSpelling;
	 private boolean currentlyScanningToken;
	 
	 private boolean isLetter(char c) {
		    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z' || c == '_');
	 }

	 private boolean isDigit(char c) {
		    return (c >= '0' && c <= '9');
	 }
	 
	 public Scanner(SourceFile source) {
		    sourceFile = source;
		    currentChar = sourceFile.getSource();
	 }
	 
	 private void takeIt() {
		 if (currentlyScanningToken)
		    currentSpelling.append(currentChar);
		 	currentChar = sourceFile.getSource();
	 }
	 
	 //TODO: make this scan through java comments
	 private void scanSeparator() {
		    switch (currentChar) {		    
		    case ' ': case '\n': case '\r': case '\t':
		      takeIt();
		      break;
		    }
	}
	 
	private void scanComment(){
		if(currentChar == '/'){		
			takeIt();
	        while (currentChar != '\r' && currentChar != '\n'){
	          takeIt();
	          if (currentChar == SourceFile.eot){					
					System.exit(0);
				}
	        }
	        takeIt();	        
		}else{		
			while (currentChar != '*'){
				takeIt();
				if (currentChar == SourceFile.eot){
					System.out.println("Unclosed Comment Error");
					System.exit(4);
				}
			}	
			takeIt();
			if (currentChar == '/'){
				takeIt();				
			}else {
				scanComment();
			}
		}
	}
	 
	 private int scanToken() {
		 while(true){

		    switch (currentChar) {

		    case 'a':  case 'b':  case 'c':  case 'd':  case 'e':
		    case 'f':  case 'g':  case 'h':  case 'i':  case 'j':
		    case 'k':  case 'l':  case 'm':  case 'n':  case 'o':
		    case 'p':  case 'q':  case 'r':  case 's':  case 't':
		    case 'u':  case 'v':  case 'w':  case 'x':  case 'y':
		    case 'z':  	
		    case 'A':  case 'B':  case 'C':  case 'D':  case 'E':
		    case 'F':  case 'G':  case 'H':  case 'I':  case 'J':
		    case 'K':  case 'L':  case 'M':  case 'N':  case 'O':
		    case 'P':  case 'Q':  case 'R':  case 'S':  case 'T':
		    case 'U':  case 'V':  case 'W':  case 'X':  case 'Y':
		    case 'Z':
		      takeIt();
		      while (isLetter(currentChar) || isDigit(currentChar))
		        takeIt();
		      return Token.IDENTIFIER;

		    case '0':  case '1':  case '2':  case '3':  case '4':
		    case '5':  case '6':  case '7':  case '8':  case '9':
		      takeIt();
		      while (isDigit(currentChar))
		        takeIt();
		      return Token.INTLITERAL;

		    case '+':  case '*': 		    
		      takeIt();		      
		      return Token.OPERATOR;
		      
		    case '-':
		    	takeIt();
		    	if(currentChar == '-'){
		    		takeIt();
		    		return Token.ERROR;
		    	}else{
			    	return Token.UNOP;
		    	}
		    	
		    case '!':
		    	takeIt();
		    	if (currentChar == '='){
		    		takeIt();
		    		return Token.OPERATOR;
		    	}else {
		    		return Token.UNOP;
		    	}
		      
		    //handles comments  
		    case '/':
		    	takeIt();
		    	if (currentChar == '/'){
		    		scanComment();
		    		currentSpelling = new StringBuffer("");
		    		break;
		    	}else if (currentChar == '*'){
		    		scanComment();
		    		currentSpelling = new StringBuffer("");
		    		break;
		    	}else{
		    		return Token.OPERATOR;
		    	}
		      
		    case '<':  case '>':
		    	takeIt();
		    	if (currentChar == '='){
		    		takeIt();
		    		return Token.OPERATOR;
		    	}else{
		    		return Token.OPERATOR;
		    	}

		    case '\'':
		      takeIt();
		      takeIt(); // the quoted character
		      if (currentChar == '\'') {
		      	takeIt();
		        return Token.CHARLITERAL;
		      } else
		        return Token.ERROR;

		    case '.':
		      takeIt();
		      return Token.DOT;

		    case '=':
		      takeIt();
		      if (currentChar == '=') {
		        takeIt();
		        return Token.OPERATOR;
		      } else
		        return Token.BECOMES;
		      
		    case '&':
			      takeIt();
			      if (currentChar == '&') {
			        takeIt();
			        return Token.OPERATOR;
			      } else
			        return Token.ERROR;
			      
		    case '|':
			      takeIt();
			      if (currentChar == '|') {
			        takeIt();
			        return Token.OPERATOR;
			      } else
			        return Token.ERROR;

		    case ';':
		      takeIt();
		      return Token.SEMICOLON;
		      
		    case ':':
			      takeIt();
			      return Token.COLON;

		    case ',':
		      takeIt();
		      return Token.COMMA;		

		    case '(':
		      takeIt();
		      return Token.LPAREN;

		    case ')':
		      takeIt();
		      return Token.RPAREN;

		    case '[':
		      takeIt();
		      return Token.LBRACKET;

		    case ']':
		      takeIt();
		      return Token.RBRACKET;

		    case '{':
		      takeIt();
		      return Token.LCURLY;

		    case '}':
		      takeIt();
		      return Token.RCURLY;

		    case SourceFile.eot:
		      return Token.EOT;

		    default:
		    	if(currentChar == ' ' || currentChar == '\n' || currentChar == '\r' || currentChar == '\t'){
		    		takeIt();
		    		currentSpelling = new StringBuffer("");
				      break;
		    	}else{
		    		takeIt();
				    return Token.ERROR;
		    	} 	      
		    }
		 }
	 }
	 
	 public Token scan () {
		    Token tok;
		    int kind;

		    currentlyScanningToken = false;
		    while (currentChar == ' '
		           || currentChar == '\n'
		           || currentChar == '\r'
		           || currentChar == '\t')
		      scanSeparator();

		    currentlyScanningToken = true;
		    currentSpelling = new StringBuffer("");

		    kind = scanToken();

		    tok = new Token(kind, currentSpelling.toString());		   
		    return tok;
		  }

}
