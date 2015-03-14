package miniJava.ContextualAnalyzer;

import miniJava.ErrorReporter;
import miniJava.StdEnvironment;
import miniJava.AbstractSyntaxTrees.*;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.SyntacticAnalyzer.SourcePosition;

public class Identify implements Visitor<Object,Object>{
	
	private IdentificationTable idTable;
	  private static SourcePosition dummyPos = new SourcePosition();
	  private ErrorReporter reporter;
	  private Checker checker;
	  
	  public void id(AST ast) {
		    ast.visit(this, null);
		    checker = new Checker(reporter, idTable);
		    checker.check(ast);
		    if (reporter.numErrors > 0){
		    	System.exit(4);
		    }
	  }
	  
	  public Identify (ErrorReporter reporter) {
		    this.reporter = reporter;
		    this.idTable = new IdentificationTable ();
		    establishStdEnvironment();
		  }

	@Override
	public Object visitPackage(Package prog, Object arg) { // how do i handle references to other class objects's fields or methods and duplication?
		boolean mainPresent = false;
		for(int i=0; i < prog.classDeclList.size(); i++){
			idTable.enter(prog.classDeclList.get(i).name, prog.classDeclList.get(i));
		}
		for(int i=0; i < prog.classDeclList.size(); i++){						
			idTable.openScope();
			for(int j=0; j < prog.classDeclList.get(i).fieldDeclList.size(); j++){
				idTable.enter(prog.classDeclList.get(i).fieldDeclList.get(j).name, prog.classDeclList.get(i).fieldDeclList.get(j));
			}
			for(int j=0; j < prog.classDeclList.get(i).methodDeclList.size(); j++){
				idTable.enter(prog.classDeclList.get(i).methodDeclList.get(j).name, prog.classDeclList.get(i).methodDeclList.get(j));
				if(prog.classDeclList.get(i).methodDeclList.get(j).name.equals("main")){
					mainPresent = true;
				}
			}			
			idTable.closeScope();			
		}
		if(!mainPresent){
			reporter.reportError ("main method \"%\" does not exist",
                    prog.toString(), prog.posn);
		}
		for(int i=0; i < prog.classDeclList.size(); i++){
			prog.classDeclList.get(i).visit(this,null);
		}
		return null;
	}

	@Override
	public Object visitClassDecl(ClassDecl cd, Object arg) {
		idTable.openScope();
		for(int j=0; j < cd.fieldDeclList.size(); j++){
			cd.fieldDeclList.get(j).visit(this,null); 
		}
		
		for(int j=0; j < cd.methodDeclList.size(); j++){
			cd.methodDeclList.get(j).visit(this,null);			
		}		
		idTable.closeScope();
		return null;
	}

	@Override
	public Object visitFieldDecl(FieldDecl fd, Object arg) {
		fd.type.visit(this, null);
		return null;
	}

	@Override
	public Object visitMethodDecl(MethodDecl md, Object arg) {
		md.type.visit(this, null);		
		if(md.name.equals("main")){
			if (md.isPrivate || !md.isStatic || md.parameterDeclList.size() > 1){
				reporter.reportError ("main method \"%\" incorrect",
	                    md.toString(), md.posn);
			}
		}
		idTable.openScope();
		for(int i=0; i < md.parameterDeclList.size(); i++){
			idTable.enter(md.parameterDeclList.get(i).name, md.parameterDeclList.get(i));
			md.parameterDeclList.get(i).visit(this,null); 
		}		
		idTable.openScope();
		for(int i=0; i < md.statementList.size(); i++){ 
			md.statementList.get(i).visit(this,null); 			
		}
		if(md.returnExp != null){
			md.returnExp.visit(this, null);
		}
		idTable.closeScope();
		idTable.closeScope();
		return null;
	}

	@Override
	public Object visitParameterDecl(ParameterDecl pd, Object arg) {
		pd.type.visit(this, null);
		return null;
	}

	@Override
	public Object visitVarDecl(VarDecl decl, Object arg) {
		// TODO Auto-generated method stub
		idTable.enter(decl.name, decl);
		decl.type.visit(this, null);
		return null;
	}

	@Override
	public Object visitBaseType(BaseType type, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitClassType(ClassType type, Object arg) {
		// TODO Auto-generated method stub
		Declaration decl = idTable.retrieveClass(type.className);
		try{
			if(type.className.equals("String")){
				type.classDecl = (ClassDecl) decl;
				decl.type = StdEnvironment.unsupportedType;
			}else{
				type.classDecl = (ClassDecl) decl;
				decl.type = type;
			}			
		}catch (ClassCastException e){
			reporter.reportError ("declaration \"%\" is not class type",
                  type.className, type.posn);
		}catch (NullPointerException e){
			reporter.reportError ("not \"%\" declared",
	                  type.className, type.posn);
		}
		return null;
	}

	@Override
	public Object visitArrayType(ArrayType type, Object arg) {
		type.eltType.visit(this, null);
		return null;
	}

	@Override
	public Object visitUnsupportedType(UnsupportedType type, Object arg) {
		return null;
	}

	@Override
	public Object visitErrorType(ErrorType type, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitBlockStmt(BlockStmt stmt, Object arg) {
		idTable.openScope();
		for(int i=0; i<stmt.sl.size(); i++){
			stmt.sl.get(i).visit(this,null);
		}
		idTable.closeScope();
		return null;
	}

	@Override
	public Object visitVardeclStmt(VarDeclStmt stmt, Object arg) {
		// TODO Auto-generated method stub
		stmt.varDecl.visit(this, null); 
		stmt.initExp.visit(this, null);
		return null;
	}

	@Override
	public Object visitAssignStmt(AssignStmt stmt, Object arg) {
		// TODO Auto-generated method stub
		stmt.ref.visit(this, null);
		stmt.val.visit(this, null);
		return null;
	}

	@Override
	public Object visitCallStmt(CallStmt stmt, Object arg) {
		// TODO Auto-generated method stub
		stmt.methodRef.visit(this, null);
		for(int i=0; i< stmt.argList.size(); i++){
			stmt.argList.get(i).visit(this, null);
		}
		return null;
	}

	@Override
	public Object visitIfStmt(IfStmt stmt, Object arg) {
		// TODO Auto-generated method stub
		idTable.openScope();
		stmt.cond.visit(this, null);
		stmt.thenStmt.visit(this, null);
		if(stmt.elseStmt != null){
			stmt.elseStmt.visit(this, null);
		}
		idTable.closeScope();
		return null;
	}

	@Override
	public Object visitWhileStmt(WhileStmt stmt, Object arg) {
		// TODO Auto-generated method stub
		idTable.openScope();//Is this right?
		stmt.cond.visit(this, null);
		stmt.body.visit(this, null);
		idTable.closeScope();
		return null;
	}

	@Override
	public Object visitUnaryExpr(UnaryExpr expr, Object arg) {
		// TODO Auto-generated method stub
		expr.operator.visit(this, null);
		expr.expr.visit(this, null);
		return null;
	}

	@Override
	public Object visitBinaryExpr(BinaryExpr expr, Object arg) {
		// TODO Auto-generated method stub
		expr.left.visit(this, null);
		expr.operator.visit(this, null);
		expr.right.visit(this, null);
		return null;
	}

	@Override
	public Object visitRefExpr(RefExpr expr, Object arg) {
		// TODO Auto-generated method stub
		expr.ref.visit(this, null);
		return null;
	}

	@Override
	public Object visitCallExpr(CallExpr expr, Object arg) {
		// TODO Auto-generated method stub
		expr.functionRef.visit(this, null);
		for(int i=0; i< expr.argList.size(); i++){
			expr.argList.get(i).visit(this, null);
		}
		return null;
	}

	@Override
	public Object visitLiteralExpr(LiteralExpr expr, Object arg) {
		// TODO Auto-generated method stub
		expr.literal.visit(this, null);
		return null;
	}

	@Override
	public Object visitNewObjectExpr(NewObjectExpr expr, Object arg) {
		// TODO Auto-generated method stub
		expr.classtype.visit(this, null);
		return null;
	}

	@Override
	public Object visitNewArrayExpr(NewArrayExpr expr, Object arg) {
		// TODO Auto-generated method stub
		expr.eltType.visit(this, null);
		expr.sizeExpr.visit(this, null);
		return null;
	}

	@Override
	public Object visitQualifiedRef(QualifiedRef ref, Object arg) {
		// TODO Auto-generated method stub
		int size = ref.qualifierList.size();
		for(int i=0; i<ref.qualifierList.size(); i++){
			ref.qualifierList.get(i).visit(this, null);
		}
		Reference dref = parseDeRef(ref, size);
		if (dref == null){
			reporter.reportError ("parameter \"%\" cannot have void Type",
                    ref.toString(), ref.posn);
		}else{
			dref.visit(this, null);
		}		
		return null;
	}

	@Override
	public Object visitIndexedRef(IndexedRef ref, Object arg) {
		// TODO Auto-generated method stub
		ref.ref.visit(this, null);
		ref.indexExpr.visit(this, null);
		return null;
	}

	@Override
	public Object visitDeRef(DeRef ref, Object arg) {
		// TODO Auto-generated method s
		
		return null;
	}

	@Override
	public Object visitMemberRef(MemberRef ref, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitLocalRef(LocalRef ref, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitThisRef(ThisRef ref, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitClassRef(ClassRef ref, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIdentifier(Identifier id, Object arg) {
		// TODO Auto-generated method stub
		Declaration decl = idTable.retrieve(id.spelling);
		id.decl = decl;
		return null;
	}

	@Override
	public Object visitOperator(Operator op, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIntLiteral(IntLiteral num, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitBooleanLiteral(BooleanLiteral bool, Object arg) {
		// TODO Auto-generated method stub
		return null;
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
			Declaration decl = idTable.retrieve(ref.qualifierList.get(size - 1).spelling);
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
//		 StdEnvironment.integerType = new BaseType(TypeKind.INT, null);
//		 StdEnvironment.booleanType = new BaseType(TypeKind.BOOLEAN, null);
//		 StdEnvironment.voidType = new BaseType(TypeKind.VOID, null);
//		 StdEnvironment.string = new UnsupportedType(TypeKind.UNSUPPORTED, null); //this how to do it?
//		 StdEnvironment.errorType = new ErrorType(TypeKind.ERROR, null);
		 
		 UnsupportedType String = new UnsupportedType(TypeKind.UNSUPPORTED, null);
		 Declaration StringD = new ClassDecl("String",null, null, null);
		 idTable.enter("String", StringD);
		 
		 ClassType system = new ClassType("System",null);
		 ClassType printStr = new ClassType("_PrintStream",null); 
		 
		 
		 MemberDecl println = new FieldDecl(false, false, StdEnvironment.voidType, "println", null);
		 ParameterDecl param = new ParameterDecl(StdEnvironment.integerType, "n", null);
		 ParameterDeclList paramList = new ParameterDeclList();
		 paramList.add(param);
		 MethodDecl PrintLine = new MethodDecl(println, paramList, null, null, null);
		 idTable.enter("println", PrintLine);
		 FieldDecl print = new FieldDecl(false, true, printStr, "out", null);
		 idTable.enter("out", print);
		 FieldDeclList fdl = new FieldDeclList();
		 fdl.add(print);
		 MethodDeclList mdl = new MethodDeclList();
		 mdl.add(PrintLine);
		 
		 ClassDecl systemD = new ClassDecl("System", fdl, null, null);
		 idTable.enter("System", systemD);
		 ClassDecl printD = new ClassDecl("_PrintStream", null, mdl, null);
		 idTable.enter("_PrintStream", printD);
		
	}

}
