package miniJava.SyntacticAnalyzer;
import miniJava.AbstractSyntaxTrees.*;
import miniJava.AbstractSyntaxTrees.Package;


public class Parser {
	
	private Scanner scanner;
	private Token token;
	
	public Parser(Scanner scanner){
		this.scanner = scanner;
	}
	public AST parse() {
		token = scanner.scan();
		Package pack = parsePro();
		return pack;
	}
	private void acceptIt() {
        accept(token.kind);
    }

    private void accept(int expectedToken) {
        if (token.kind == expectedToken) {
//            System.out.println("accepting: " + token.spelling);
            token = scanner.scan();
        }
        else
            parseError("expecting '" + expectedToken +
                       "' but found '" + token.spelling + "'");
    }
    private void parseError(String e) {
        System.out.println("Parse error: " + e);
        System.exit(4);
    }
    
    // Program ::= (ClassDeclaration)* eot
	private Package parsePro(){
		ClassDeclList cdl = new ClassDeclList();
		while (token.kind == Token.CLASS){
			ClassDecl cd = parseClassDec();
			cdl.add(cd);
		}
		Package pack = new Package(cdl, null);
		accept(Token.EOT);
		return pack;
	}
	//ClassDeclaration ::= class id {(FieldDec | MethodDec)* }
	private ClassDecl parseClassDec(){
		accept(Token.CLASS);
		String cn = token.spelling;
		accept(Token.IDENTIFIER);
		accept(Token.LCURLY);
		FieldDeclList fdl = new FieldDeclList();
		MethodDeclList mdl = new MethodDeclList();		
		//move private, public, static up here
		while(token.kind == Token.PRIVATE || token.kind == Token.PUBLIC || token.kind == Token.STATIC || 
				token.kind == Token.INT || token.kind == Token.BOOLEAN || token.kind == Token.VOID || token.kind == Token.IDENTIFIER){
			//ask about how to tell diference between field and method here
			parseDeclarators(fdl, mdl);
		}		
		accept(Token.RCURLY);
		ClassDecl cd = new ClassDecl(cn, fdl, mdl, null);
		return cd;
	}
	private void parseDeclarators(FieldDeclList fdl, MethodDeclList mdl){
		boolean isPrivate = false;
		boolean isStatic = false;
		if (token.kind == Token.PRIVATE){
			isPrivate = true;
			acceptIt();			
		}else if (token.kind == Token.PUBLIC){
			acceptIt();
		}
		if (token.kind == Token.STATIC){
			isStatic = true;
			acceptIt();
		}
		Type type = parseType();
		String name = token.spelling;
		accept(Token.IDENTIFIER);
		FieldDecl fd = new FieldDecl(isPrivate, isStatic, type, name, null);
		if (token.kind == Token.LPAREN){
			acceptIt();
			//first create field declaration no matter what, then if LParen, use fd to create method declaration 
			parseMethod(fd, mdl);
		}else{
			accept(Token.SEMICOLON);
			fdl.add(fd);
		}
	}
	private Type parseType(){
		if (token.kind == Token.BOOLEAN){
			acceptIt();
			BaseType type = new BaseType(TypeKind.BOOLEAN, null);
			return type;
		}else if (token.kind == Token.VOID){
			acceptIt();
			BaseType type = new BaseType(TypeKind.VOID, null);
			return type;			
		}else if (token.kind == Token.INT){
			acceptIt();
			Type type = new BaseType(TypeKind.INT, null);
			if(token.kind == Token.LBRACKET){
				acceptIt();
				type = new ArrayType(type, null);
				accept(Token.RBRACKET);
			}
			return type;
		}else if(token.kind == Token.IDENTIFIER){
			String name = token.spelling;
			acceptIt();
			Type type;
			if(name == "String"){
				type = new UnsupportedType(TypeKind.UNSUPPORTED, null); //might need to remove this
			}else{
				type = new ClassType(name, null);
			}
			if(token.kind == Token.LBRACKET){
				acceptIt();
				type = new ArrayType(type, null);
				accept(Token.RBRACKET);
			}
			return type;
		}else{
			Type type = new BaseType(TypeKind.UNSUPPORTED, null);
			parseError("expecting Type token'" +
                    "' but found '" + token + "'");
			return type;
		}
	}
	private void parseMethod(FieldDecl fd, MethodDeclList mdl){
		ParameterDeclList pdl = new ParameterDeclList();
		StatementList stl = new StatementList();
		Expression exp;
		if (token.kind == Token.INT || token.kind == Token.BOOLEAN || token.kind == Token.VOID || token.kind == Token.IDENTIFIER){
			parseParamList(pdl);
		}
		accept(Token.RPAREN); 
		accept(Token.LCURLY);
		while(token.kind == Token.LCURLY || token.kind == Token.INT || token.kind == Token.BOOLEAN || token.kind == Token.VOID || 
				token.kind == Token.IF || token.kind == Token.WHILE || token.kind == Token.THIS || token.kind == Token.IDENTIFIER){
			Statement st = parseStatement();
			stl.add(st);
		}
		if (token.kind == Token.RETURN){
			acceptIt();
			exp = parseExpression();
			accept(Token.SEMICOLON);
		}else{
			exp = null;
		}
		MethodDecl md = new MethodDecl(fd, pdl, stl, exp, null);
		mdl.add(md);
		accept(Token.RCURLY);
	}
	private void parseParamList(ParameterDeclList pdl){
		Type type = parseType();
		String name = token.spelling;
		ParameterDecl pd = new ParameterDecl(type, name, null);
		pdl.add(pd);
		accept(Token.IDENTIFIER);
		while(token.kind == Token.COMMA){
			acceptIt();
			type = parseType();
			name = token.spelling;
			pd = new ParameterDecl(type, name, null);
			pdl.add(pd);
			accept(Token.IDENTIFIER);
		}
	}
	private Statement parseStatement(){
		Statement st;
		//{Statement*}
		if (token.kind == Token.LCURLY){
			acceptIt();
			StatementList stl = new StatementList();
			while(token.kind == Token.LCURLY || token.kind == Token.INT || token.kind == Token.BOOLEAN || token.kind == Token.VOID || 
					token.kind == Token.IF || token.kind == Token.WHILE || token.kind == Token.THIS || token.kind == Token.IDENTIFIER){
				st = parseStatement();
				stl.add(st);
			}
			accept(Token.RCURLY);
			st = new BlockStmt(stl, null);
			return st;
		}
		//Type id = Expression ;
		else if (token.kind == Token.INT || token.kind == Token.BOOLEAN || token.kind == Token.VOID){
			Type type = parseType();
			String name = token.spelling;
			VarDecl vd = new VarDecl(type, name, null);
			accept(Token.IDENTIFIER);
			accept(Token.BECOMES);
			Expression exp = parseExpression();
			st = new VarDeclStmt(vd, exp, null);
			accept(Token.SEMICOLON);
			return st;
		}
		//Reference = Expression ;
		else if (token.kind == Token.THIS || token.kind == Token.IDENTIFIER){
			String classSpell = token.spelling;
			Reference ref = parseReference();			
			if (token.kind == Token.LBRACKET){
				acceptIt();
				//handles var decl issue
				if(token.kind == Token.RBRACKET){
					acceptIt();
					Type type1 = new ClassType(classSpell, null);
					Type type2 = new ArrayType(type1, null);
					VarDecl vd = new VarDecl(type2, token.spelling, null);
					accept(Token.IDENTIFIER);
					accept(Token.BECOMES);
					Expression exp = parseExpression();
					st = new VarDeclStmt(vd, exp, null);
					accept(Token.SEMICOLON);
					return st;
				}else{
					Expression exp1 = parseExpression();
					IndexedRef indRef = new IndexedRef(ref, exp1, null);
					accept(Token.RBRACKET);
					accept(Token.BECOMES);
					Expression exp2 = parseExpression();
					st = new AssignStmt(indRef, exp2, null);
					accept(Token.SEMICOLON);
					return st;
				}
				
			}else if(token.kind == Token.LPAREN){
				acceptIt();
				ExprList exl = new ExprList();
				if (token.kind == Token.THIS || token.kind == Token.IDENTIFIER || token.kind == Token.UNOP ||
						token.kind == Token.LPAREN || token.kind == Token.INTLITERAL || token.kind == Token.TRUE || token.kind == Token.FALSE || token.kind == Token.NEW){
					parseArgList(exl);
				}
				accept(Token.RPAREN);
				st = new CallStmt(ref, exl, null);
				accept(Token.SEMICOLON);
				return st;
			}//handles Var Declar crossover
			else if(token.kind == Token.IDENTIFIER){
				Type type1 = new ClassType(classSpell, null);
				String name = token.spelling;
				VarDecl vd = new VarDecl(type1, name, null);
				accept(Token.IDENTIFIER);
				accept(Token.BECOMES);
				Expression exp = parseExpression();
				st = new VarDeclStmt(vd, exp, null);
				accept(Token.SEMICOLON);
				return st;
				
			}else{				
				accept(Token.BECOMES);
				Expression exp = parseExpression();
				st = new AssignStmt(ref, exp, null);
				accept(Token.SEMICOLON);
				return st;
			}
		}
		//if(expresstion)statement
		else if(token.kind == Token.IF){
			acceptIt();
			accept(Token.LPAREN);
			Expression exp = parseExpression();
			accept(Token.RPAREN);
			Statement st1 = parseStatement();
			if (token.kind == Token.ELSE){
				acceptIt();
				Statement st2 = parseStatement();
				st = new IfStmt(exp, st1, st2, null);
				return st;
			}else{
				st = new IfStmt(exp, st1, null);
				return st;
			}
		}
		//while loop
		else{
			accept(Token.WHILE);
			accept(Token.LPAREN);
			Expression exp = parseExpression();
			accept(Token.RPAREN);
			Statement st1 = parseStatement();
			st = new WhileStmt(exp, st1, null);
			return st;
		}
	}
	private Expression parseExpression(){
		Expression exp = parseAnd();
		while(token.spelling.equals("||")){
			Operator o = new Operator(token.spelling, null);
			acceptIt();
			Expression exp2 = parseAnd();
			exp = new BinaryExpr(o, exp, exp2, null);
		}
		return exp;		
	}
	private Reference parseReference(){
		IdentifierList idl = new IdentifierList();
		Reference ref;
		if (token.kind == Token.THIS){
			acceptIt();
			while(token.kind == Token.DOT){
				acceptIt();
				String name = token.spelling;
				Identifier id = new Identifier(name, null);
				idl.add(id);
				accept(Token.IDENTIFIER);
			}
			ref = new QualifiedRef(true, idl, null);
			return ref;
		}else{
			String name = token.spelling;
			Identifier id = new Identifier(name, null);
			idl.add(id);
			accept(Token.IDENTIFIER);
			while(token.kind == Token.DOT){
				acceptIt();
				name = token.spelling;
				id = new Identifier(name, null);
				idl.add(id);
				accept(Token.IDENTIFIER);
			}
			ref = new QualifiedRef(false, idl, null);
			return ref;
		}
	}
	private void parseArgList(ExprList exl){
		Expression exp = parseExpression();
		exl.add(exp);
		while(token.kind == Token.COMMA){
			acceptIt();
			exp = parseExpression();
			exl.add(exp);
		}
	}
	private Expression parseAnd(){
		Expression exp = parseEqual();
		while(token.spelling.equals("&&")){
			Operator o = new Operator(token.spelling, null);
			acceptIt();
			Expression exp2 = parseEqual();
			exp = new BinaryExpr(o, exp, exp2, null);
		}
		return exp;
	}
	private Expression parseEqual(){
		Expression exp = parseRela();
		while(token.spelling.equals("==") || token.spelling.equals("!=")){
			Operator o = new Operator(token.spelling, null);
			acceptIt();
			Expression exp2 = parseRela();
			exp = new BinaryExpr(o, exp, exp2, null);
		}
		return exp;
	}
	private Expression parseRela(){
		Expression exp = parseAdd();
		while(token.spelling.equals("<=") || token.spelling.equals(">=") || token.spelling.equals(">") || token.spelling.equals("<")){
			Operator o = new Operator(token.spelling, null);
			acceptIt();
			Expression exp2 = parseAdd();
			exp = new BinaryExpr(o, exp, exp2, null);
		}
		return exp;
	}
	private Expression parseAdd(){
		Expression exp = parseMult();
		while(token.spelling.equals("+") || token.spelling.equals("-")){
			Operator o = new Operator(token.spelling, null);
			acceptIt();
			Expression exp2 = parseMult();
			exp = new BinaryExpr(o, exp, exp2, null);
		}
		return exp;
	}
	private Expression parseMult(){
		Expression exp = parseTer();
		while(token.spelling.equals("*") || token.spelling.equals("/")){
			Operator o = new Operator(token.spelling, null);
			acceptIt();
			Expression exp2 = parseTer();
			exp = new BinaryExpr(o, exp, exp2, null);
		}
		return exp;
	}
	private Expression parseUnop(){
		Expression exp = parseTer();
		while(token.kind == Token.UNOP){
			Operator o = new Operator(token.spelling, null);
			acceptIt();
			Expression exp2 = parseTer();
			exp = new BinaryExpr(o, exp, exp2, null);
		}
		return exp;
	}
	private Expression parseTer(){
		Expression exp;
		if (token.kind == Token.THIS || token.kind == Token.IDENTIFIER){
			Reference ref = parseReference();
			//Reference [Expression]
			if(token.kind == Token.LBRACKET){
				acceptIt();
				Expression exp1 = parseExpression();
				IndexedRef indRef = new IndexedRef(ref, exp1, null);
				accept(Token.RBRACKET);
				exp = new RefExpr(indRef, null);
				return exp;
			}
			//Reference (ArgumentList)
			else if(token.kind == Token.LPAREN){
				acceptIt();
				ExprList exl = new ExprList();
				if (token.kind == Token.THIS || token.kind == Token.IDENTIFIER || token.kind == Token.UNOP ||
						token.kind == Token.LPAREN || token.kind == Token.INTLITERAL || token.kind == Token.TRUE || token.kind == Token.FALSE || token.kind == Token.NEW){
					parseArgList(exl);
				}
				accept(Token.RPAREN);
				exp = new CallExpr(ref, exl, null);
				return exp;
			}else{
				exp = new RefExpr(ref, null);
				return exp;
			}			
		}
		//unop Expression
		else if(token.kind == Token.UNOP){
			Operator o = new Operator(token.spelling, null);
			acceptIt();
			Expression exp1 = parseTer();
			exp = new UnaryExpr(o, exp1, null);
			return exp;
		}
		//num | true | false
		else if(token.kind == Token.INTLITERAL	|| token.kind == Token.TRUE || token.kind == Token.FALSE){
			Literal lit;
			if(token.kind == Token.INTLITERAL){
				lit = new IntLiteral(token.spelling, null);
			}else{
				lit = new BooleanLiteral(token.spelling, null);
			}						
			acceptIt();
			exp = new LiteralExpr(lit, null);
			return exp;
		}
		//(Expression)
		else if (token.kind == Token.LPAREN){
			acceptIt();
			exp = parseExpression();
			accept(Token.RPAREN);
			return exp;
		}
		//new()
		else{
			accept(Token.NEW);
			if(token.kind == Token.IDENTIFIER){
				String name = token.spelling;
				ClassType type = new ClassType(name, null);
				acceptIt();
				if(token.kind == Token.LPAREN){
					acceptIt();
					exp = new NewObjectExpr(type, null);
					accept(Token.RPAREN);
				}else{					
					accept(Token.LBRACKET);
					//ArrayType array = new ArrayType(type, null);
					Expression exp1 = parseExpression();
					exp = new NewArrayExpr(type, exp1, null);
					accept(Token.RBRACKET);
				}
			}else{
				Type type = new BaseType(TypeKind.INT, null);
				accept(Token.INT);
				accept(Token.LBRACKET);
				//ArrayType array = new ArrayType(type, null);
				Expression exp1 = parseExpression();
				exp = new NewArrayExpr(type, exp1, null);
				accept(Token.RBRACKET);
			}
			return exp;
		}
	}
}
