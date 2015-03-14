package miniJava.ContextualAnalyzer;

import miniJava.StdEnvironment;
import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.*;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.SyntacticAnalyzer.SourcePosition;

public class Checker implements Visitor<Object, Type>{
	
	public void check(AST ast) {
	    ast.visit(this, null);
	    if (reporter.numErrors > 0){
	    	System.exit(4);
	    }
	  }

	  /////////////////////////////////////////////////////////////////////////////

	  public Checker (ErrorReporter reporter, IdentificationTable idTable) {
	    this.reporter = reporter;
	    this.idTable = idTable;
	    establishStdEnvironment();
	  }	  

	private IdentificationTable idTable;
	  private static SourcePosition dummyPos = new SourcePosition();
	  private ErrorReporter reporter;

	public Type visitPackage(Package prog, Object arg) {
		for(int i=0; i < prog.classDeclList.size(); i++){
			if (prog.classDeclList.get(i).duplicated)
			      reporter.reportError ("identifier \"%\" already declared",
			    		  prog.classDeclList.get(i).name, prog.classDeclList.get(i).posn);			
			prog.classDeclList.get(i).visit(this,null);
		}
		return null;
	}

	
	public Type visitClassDecl(ClassDecl cd, Object arg) {
		idTable.openScope();
		for(int j=0; j < cd.fieldDeclList.size(); j++){
//			if (cd.fieldDeclList.get(j).duplicated)
//			      reporter.reportError ("identifier \"%\" already declared",
//			    		  cd.fieldDeclList.get(j).name, cd.fieldDeclList.get(j).posn);
			cd.fieldDeclList.get(j).visit(this,null);
		}
		for(int j=0; j < cd.methodDeclList.size(); j++){
			if (cd.methodDeclList.get(j).duplicated)
			      reporter.reportError ("identifier \"%\" already declared",
			    		  cd.methodDeclList.get(j).name, cd.methodDeclList.get(j).posn);
			cd.methodDeclList.get(j).visit(this,null);
		}
		idTable.closeScope();
		return null;
	}

	
	public Type visitFieldDecl(FieldDecl fd, Object arg) {
		fd.type.visit(this, null);
//		if (fd.duplicated)
//		      reporter.reportError ("identifier \"%\" already declared",
//		                            fd.name, fd.posn);
		return null;
	}

	
	public Type visitMethodDecl(MethodDecl md, Object arg) {
		md.type.visit(this, null);		
		if (md.name.equals("main")){
			if (!md.type.equals(StdEnvironment.voidType)){
				reporter.reportError ("main method \"%\" must have type void",
                        md.name, md.posn);
			}
			ArrayType testT = (ArrayType) md.parameterDeclList.get(0).type;
			ClassType testC = (ClassType) testT.eltType;
			if(!testC.className.equals("String")){
				reporter.reportError ("main method \"%\" has wrong param type",
                        md.name, md.posn);	
			}
		}
		idTable.openScope();
		if (md.duplicated)
		      reporter.reportError ("identifier \"%\" already declared",
		                            md.name, md.posn);		
		for(int i=0; i < md.parameterDeclList.size(); i++){			
			if (md.parameterDeclList.get(i).duplicated)
			      reporter.reportError ("identifier \"%\" already declared",
			    		  md.parameterDeclList.get(i).name, md.parameterDeclList.get(i).posn);
			md.parameterDeclList.get(i).visit(this,null); 
		}
		idTable.openScope();
		for(int i=0; i < md.statementList.size(); i++){ 
			md.statementList.get(i).visit(this,null); 			
		}
		Type eType;
		if(md.returnExp == null){
			eType = StdEnvironment.voidType;
		}else{
			eType = md.returnExp.visit(this, null);
		}
		if (! md.type.equals(eType))
		      reporter.reportError ("body of function \"%\" has wrong type",
		                            md.name, null);
		idTable.closeScope();
		idTable.closeScope();
		return null;
	}

	
	public Type visitParameterDecl(ParameterDecl pd, Object arg) {
		pd.type.visit(this, null);
		if (pd.type.equals(StdEnvironment.voidType))
		      reporter.reportError ("parameter \"%\" cannot have void Type",
		                            pd.name, pd.posn);
		return null;
	}

	
	public Type visitVarDecl(VarDecl decl, Object arg) {//check on scope rules here and shadowing make changes to idtable to handle scope rules
		decl.type.visit(this, null);
		if (decl.duplicated)						//also check if more error checking is needed here
		      reporter.reportError ("identifier \"%\" already declared",
		                            decl.name, decl.posn);
		return decl.type;
	}

	
	public Type visitBaseType(BaseType type, Object arg) {		
		return null;
	}

	
	public Type visitClassType(ClassType type, Object arg) {
		//do I handle String class in parser by making it unsupported?
		if(type.classDecl == null){
			reporter.reportError ("declaration \"%\" not declared",
                    type.className, type.posn);
		}else if(type.classDecl.type == null){
			return null;
		}
		else if(! type.classDecl.type.equals(type)){
			reporter.reportError ("declaration \"%\" has wrong type",
                    type.classDecl.name, type.classDecl.posn);
		}
		return null;
	}

	
	public Type visitArrayType(ArrayType type, Object arg) {
		type.eltType.visit(this, null);
		return null;
	}
	public Type visitUnsupportedType(UnsupportedType type, Object arg) {
		return StdEnvironment.unsupportedType;
	}

	public Type visitErrorType(ErrorType type, Object arg) {
		return StdEnvironment.errorType;
	}

	
	public Type visitBlockStmt(BlockStmt stmt, Object arg) {
		idTable.openScope();
		for(int i=0; i<stmt.sl.size(); i++){
			stmt.sl.get(i).visit(this,null);
		}
		idTable.closeScope();
		return null;
	}

	
	public Type visitVardeclStmt(VarDeclStmt stmt, Object arg) {
		Type dType = stmt.varDecl.visit(this, null);
		Type eType = stmt.initExp.visit(this, null);
		if(! dType.equals(eType)){ //need new equals method
			reporter.reportError ("varDecl statement \"%\" has wrong type",
                    stmt.varDecl.name, stmt.posn);
		}
		return null;
	}

	
	public Type visitAssignStmt(AssignStmt stmt, Object arg) {
		Type rType = stmt.ref.visit(this, null);
		Type eType = stmt.val.visit(this, null); //check if more error checking is needed for references.
//		if(rType == null){
//			reporter.reportError ("reference \"%\" not declared",
//                    stmt.ref.toString(), stmt.ref.posn);
//		}
//		if(! rType.equals(eType)){
//			reporter.reportError ("assign statement \"%\" has wrong type",
//                    stmt.ref.toString(), stmt.posn);
//		}
		return null;
	}

	
	public Type visitCallStmt(CallStmt stmt, Object arg) {
		Type rType = stmt.methodRef.visit(this, null);
		for(int i=0; i< stmt.argList.size(); i++){
			Type eType = stmt.argList.get(i).visit(this, null);
//			if(! eType.equals(rType)){
//				reporter.reportError ("call statement \"%\" has wrong type",  //check if this is sufficent considering static access issues
//	                    stmt.methodRef.toString(), stmt.posn);
//			}
		}
		return null;
	}

	
	public Type visitIfStmt(IfStmt stmt, Object arg) {
		idTable.openScope();
		Type eType = stmt.cond.visit(this, null);
		if (! eType.equals(StdEnvironment.booleanType)) 
		      reporter.reportError("Boolean expression expected here", "", stmt.cond.posn);
		stmt.thenStmt.visit(this, null);
		if(stmt.elseStmt != null){
			stmt.elseStmt.visit(this, null);
		}
		idTable.closeScope();
		return null;
	}

	
	public Type visitWhileStmt(WhileStmt stmt, Object arg) {
		idTable.openScope();//Is this right?
		Type eType = stmt.cond.visit(this, null);
		if (! eType.equals(StdEnvironment.booleanType)) 
		      reporter.reportError("Boolean expression expected here", "", stmt.cond.posn);
		stmt.body.visit(this, null);
		idTable.closeScope();
		return null;
	}

	
	public Type visitUnaryExpr(UnaryExpr expr, Object arg) {
		Type oType = expr.operator.visit(this, null);
		Type eType = expr.expr.visit(this, null);
		if(! oType.equals(eType)){
			reporter.reportError ("Unaryexpr \"%\" has type mismatch",
                    expr.toString(), expr.posn);
		}
		return eType;
	}

	
	public Type visitBinaryExpr(BinaryExpr expr, Object arg) {
		Type eType1 = expr.left.visit(this, null);
		Type oType = expr.operator.visit(this, null);
		Type eType2 = expr.right.visit(this, null);
//		if(! eType1.equals(eType2)){
//			reporter.reportError ("bin expr \"%\" has mismatch type",  
//                    expr.toString(), expr.posn);
//		}
//		else if(! oType.equals(eType1)){
//			reporter.reportError ("bin expr \"%\" has type mismatch",
//                    expr.toString(), expr.posn);
//		}
		return oType;
	}

	
	public Type visitRefExpr(RefExpr expr, Object arg) {//static access errors here too
		Type rType = expr.ref.visit(this, null);
		return rType;
	}

	
	public Type visitCallExpr(CallExpr expr, Object arg) {//static errors?
		Type rType = expr.functionRef.visit(this, null);
		for(int i=0; i< expr.argList.size(); i++){
			expr.argList.get(i).visit(this, null);
		}
		return rType;
	}

	
	public Type visitLiteralExpr(LiteralExpr expr, Object arg) {
		Type lType = expr.literal.visit(this, null);		
		return lType;
	}

	
	public Type visitNewObjectExpr(NewObjectExpr expr, Object arg) {
		expr.classtype.visit(this, null);			//do i need to do a binding here? what do I return?
		return expr.classtype;
	}

	
	public Type visitNewArrayExpr(NewArrayExpr expr, Object arg) {
		expr.eltType.visit(this, null);
		Type sType = expr.sizeExpr.visit(this, null);
		if (! sType.equals(StdEnvironment.integerType)){//this may be wrong
			reporter.reportError("Integer expression expected here", "", expr.posn);
		}
		return expr.eltType;
	}

	
	public Type visitQualifiedRef(QualifiedRef ref, Object arg) {//TODO: Split this up into 5 classes
		int size = ref.qualifierList.size();
		Reference dref = parseDeRef(ref, size);
		if(dref == null){			
			return StdEnvironment.errorType;
		}
		Type rType = dref.visit(this, null);
		return rType;
	}
	public Type visitDeRef(DeRef ref, Object arg) { // not sure what to do here
		Type rType = ref.ref.visit(this, null);
		Type mType = ref.mref.visit(this, null);
//		if(isStatic == null){
//			return rType;
//		}else if(isStatic.equals(StdEnvironment.errorType) && rType != null){
//			reporter.reportError("PA3 no static access", "", ref.posn);
//		}		
		return mType;
	}

	public Type visitThisRef(ThisRef ref, Object arg) {
		// TODO Auto-generated method stub		
		return StdEnvironment.thistype;
	}

	public Type visitClassRef(ClassRef ref, Object arg) {
		// TODO Auto-generated method stub
		Type type = ref.id.visit(this,null);
		return type;
	}
	public Type visitMemberRef(MemberRef ref, Object arg) {
		// TODO Auto-generated method stub
		Type type = ref.id.visit(this, null);
		return type;
	}

	public Type visitLocalRef(LocalRef ref, Object arg) {
		// TODO Auto-generated method stub
		Type type = ref.id.visit(this, null);
		if(!ref.id.decl.type.equals(type)){
			
		}
		return type;
	}

	
	public Type visitIndexedRef(IndexedRef ref, Object arg) {
		Type iType = ref.ref.visit(this, null);
		Type sType = ref.indexExpr.visit(this, null);
		if (! sType.equals(StdEnvironment.integerType)){//this may be wrong
			reporter.reportError("Integer expression expected here", "", ref.posn);
		}
		return iType;
	}

	
	public Type visitIdentifier(Identifier id, Object arg) {
		if(id.decl == null){
			reporter.reportError ("identification \"%\" not declared",
                    id.spelling, id.posn);
			return null;
		}
		return id.decl.type;
	}

	
	public Type visitOperator(Operator op, Object arg) {
		//do i need to do anything here?
		Type rtype;
		switch (op.spelling){
		case "!": case "||": case "&&": case "==": case "!=": case "<": case "<=": case ">": case ">=":
			rtype = StdEnvironment.booleanType;
			return rtype;
			
		case "+": case "-": case "*": case "/":
			rtype = StdEnvironment.integerType;
			return rtype;
			
		default:
			rtype = null;
			return rtype;
		}
		
	}

	
	public Type visitIntLiteral(IntLiteral num, Object arg) {
		
		return StdEnvironment.integerType;//how to do this
	}

	
	public Type visitBooleanLiteral(BooleanLiteral bool, Object arg) {
		
		return StdEnvironment.booleanType;//how to do this
	}
	
	private Reference parseDeRef(QualifiedRef ref, int size){
		if(size > 1){
			MemberRef mref = new MemberRef(ref.qualifierList.get(size - 1), null);
			size--;
			Reference dref = parseDeRef(ref, size);
			return new DeRef(dref, mref, null);
		}
		else if (ref.thisRelative){
			Reference tref = new ThisRef(ref.posn);
			return tref;
		}else{
			Declaration decl = ref.qualifierList.get(size - 1).decl;
			if(decl == null){
				return null;
			}
			else if(decl.type == null){
				Reference cref = new ClassRef(ref.qualifierList.get(size - 1), null);
				return cref;
			}else{
				Reference lref = new LocalRef(ref.qualifierList.get(size - 1), null);
				return lref;
			}		
		}
	}
	
	private void establishStdEnvironment() {
		 StdEnvironment.integerType = new BaseType(TypeKind.INT, null);
		 StdEnvironment.booleanType = new BaseType(TypeKind.BOOLEAN, null);
		 StdEnvironment.voidType = new BaseType(TypeKind.VOID, null);
		 StdEnvironment.string = new UnsupportedType(TypeKind.UNSUPPORTED, null); //this how to do it?
		 StdEnvironment.errorType = new ErrorType(TypeKind.ERROR, null);
		 StdEnvironment.thistype = new ClassType("this", null);
		 
//		 ClassType String = new ClassType("String", null);
//		 ClassDecl StringD = new ClassDecl("String", null, null, null);
//		 idTable.enter("String", StringD);
//		 
////		 ClassType system = new ClassType("System",null);
////		 ClassType printStr = new ClassType("_PrintStream",null); 
////		 
////		 
////		 MemberDecl println = new FieldDecl(false, false, StdEnvironment.voidType, "println", null);
////		 ParameterDecl param = new ParameterDecl(StdEnvironment.integerType, "n", null);
////		 ParameterDeclList paramList = new ParameterDeclList();
////		 paramList.add(param);
////		 MethodDecl PrintLine = new MethodDecl(println, paramList, null, null, null);
////		 FieldDecl print = new FieldDecl(false, true, printStr, "out", null);
//		 FieldDeclList fdl = new FieldDeclList();
//		 fdl.add(print);
//		 MethodDeclList mdl = new MethodDeclList();
//		 mdl.add(PrintLine);
//		 
//		 ClassDecl systemD = new ClassDecl("System", fdl, null, null);
//		 idTable.enter("System", systemD);
//		 ClassDecl printD = new ClassDecl("_PrintStream", null, mdl, null);
//		 idTable.enter("_PrintStream", printD);
		
	}
	//check if you did scope in idtable right. split up references.

	

	
	

}
