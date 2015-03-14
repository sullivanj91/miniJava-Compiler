package miniJava.CodeGenerator;



import java.util.HashMap;
import java.util.Map;

import mJAM.*;
import mJAM.Machine.Op;
import mJAM.Machine.Prim;
import mJAM.Machine.Reg;
import miniJava.StdEnvironment;
import miniJava.AbstractSyntaxTrees.*;
import miniJava.AbstractSyntaxTrees.Package;

public class Generator implements Visitor<Object, Object>{
	
	int mainAddr = -1;
	int localCount = 3;
	Map<String,Integer> dict = new HashMap<String, Integer>();
	
	public Generator(){		
	}
	
	public void generate(AST ast){
		ast.visit(this, null);
	}

	@Override
	public Object visitPackage(Package prog, Object arg) {
		Machine.initCodeGen();
		int patchme_Jump = Machine.nextInstrAddr();
		Machine.emit(Op.JUMP, Reg.CB, 0);
		for(int i=0; i < prog.classDeclList.size(); i++){
			//generate jump
			//visit classes
			//when visit main set address of main address
			//use this to patch correctly. 
			int classSize = prog.classDeclList.get(i).methodDeclList.size() + prog.classDeclList.get(i).fieldDeclList.size();
			RuntimeEntity entity = new KnownValue(classSize, 0);
			prog.classDeclList.get(i).entity = entity;
		}for(int j=0; j<prog.classDeclList.size();j++){
			prog.classDeclList.get(j).visit(this, null);
		}
		Machine.patch(patchme_Jump, mainAddr);
		Machine.emit(Op.HALT, 0, 0, 0);
		return null;
	}

	//create runtime entity for how big each class is. 
	public Object visitClassDecl(ClassDecl cd, Object arg) {
		this.localCount = 3;
		for(int i=0; i<cd.fieldDeclList.size(); i++){
			cd.fieldDeclList.get(i).visit(this, i);
		}
		for(int i=0; i<cd.methodDeclList.size(); i++){
			cd.methodDeclList.get(i).visit(this, null);
		}		
		return null;
	}

	@Override
	public Object visitFieldDecl(FieldDecl fd, Object arg) {
		// TODO Auto-generated method stub
		fd.type.visit(this, null);
		fd.entity = new UnknownValue((int) arg);
		return null;
	}

	@Override
	public Object visitMethodDecl(MethodDecl md, Object arg) {
		if (md.name.equals("main")){
			this.mainAddr = Machine.nextInstrAddr();
			this.localCount = 0;
			int params = md.parameterDeclList.size();
			md.entity = new KnownValue(params, 0);
			for(int i=0; i<md.parameterDeclList.size();i++){
				md.parameterDeclList.get(i).visit(this, -i-1);//is this right?
			}
			for(int i=0; i<md.statementList.size(); i++){
				md.statementList.get(i).visit(this, localCount);
			}
			Machine.emit(Op.HALT, 0, 0, 0);
		}else{			
			int params = md.parameterDeclList.size();
			md.entity = new KnownValue(params, 0);
			int j = Machine.nextInstrAddr();
			if(dict.get(md.name) != null){
				int h = dict.get(md.name);
				Machine.patch(h, j);
			}			
			for(int i=0; i<md.parameterDeclList.size();i++){
				md.parameterDeclList.get(i).visit(this, -i-1);//is this right?
			}
			for(int i=0; i<md.statementList.size(); i++){
				md.statementList.get(i).visit(this, localCount);
			}if(md.returnExp != null){
				md.returnExp.visit(this, null);
				Machine.emit(Op.RETURN, 1, 1, params);
			}else{
				Machine.emit(Op.RETURN, 0, 0, params);
			}
			
		}
		return null;
	}

	@Override
	public Object visitParameterDecl(ParameterDecl pd, Object arg) {//is this right?
		int i = (int) arg;
		pd.entity = new UnknownValue(i);
//		Machine.emit(Op.PUSH, 1);
		return null;
	}

	@Override
	public Object visitVarDecl(VarDecl decl, Object arg) {
		int i = (int) arg;		
		decl.type.visit(this, null);//how is this supposed to work? from slides...
		decl.entity = new UnknownValue(i);// do i need to do this?
		return null;
	}

	@Override
	public Object visitBaseType(BaseType type, Object arg) {
		
		return null;
	}

	@Override
	public Object visitClassType(ClassType type, Object arg) {
		// TODO Auto-generated method stub
		int size = type.classDecl.fieldDeclList.size() + type.classDecl.methodDeclList.size();
		type.classDecl.entity = new UnknownValue(size, localCount);
		return null;
	}

	@Override
	public Object visitArrayType(ArrayType type, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitUnsupportedType(UnsupportedType type, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitErrorType(ErrorType type, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitBlockStmt(BlockStmt stmt, Object arg) {
		for(int i=0; i<stmt.sl.size();i++){
			stmt.sl.get(i).visit(this,null);
		}
		return null;
	}

	@Override
	public Object visitVardeclStmt(VarDeclStmt stmt, Object arg) {
		// TODO Auto-generated method stub
		int offset = (int) arg;
		stmt.varDecl.visit(this, offset);
//		Machine.emit(Op.LOAD, Reg.LB, offset);
		stmt.initExp.visit(this, offset);
//		Machine.emit(Op.STORE, Reg.LB, offset);
		this.localCount++;
		return null;
	}

	@Override
	public Object visitAssignStmt(AssignStmt stmt, Object arg) {
		int d = (int) stmt.ref.visit(this, 1);
		stmt.val.visit(this, null);
		if(d == -49){
			Machine.emit(Prim.fieldupd);
			return null;
		}else if(d == -50){
			Machine.emit(Prim.arrayupd);
			return null;
		}
		Machine.emit(Op.STORE, Reg.LB, d);
		return null;
	}

	@Override
	public Object visitCallStmt(CallStmt stmt, Object arg) {		
		for(int i=0; i<stmt.argList.size();i++){
			stmt.argList.get(i).visit(this, null);
		}
		int d = (int) stmt.methodRef.visit(this, -3);
		//need a jump to 0 keep track of calls made with no addr, store jump location in list and what calling,  store method location in entity then patch
		if(d == -1){
			Machine.emit(Prim.putint);
//			Machine.emit(Op.RETURN, 0, 0, 1); 
//			Machine.emit(Op.JUMP, 0, Reg.CP, 0);
		}else{
			int j = Machine.nextInstrAddr();
			Machine.emit(Op.JUMP, Reg.CB, j+ 1);
			Machine.emit(Op.CALL, Reg.CB, j + 1);			
		}	
		return null;
	}

	@Override
	public Object visitIfStmt(IfStmt stmt, Object arg) {
//		Machine.emit(Op.JUMP, Reg.CB, 0); do i need this?
		stmt.cond.visit(this, null);
		int j = Machine.nextInstrAddr();
		Machine.emit(Op.JUMPIF, 0, Reg.CB, 0);
		stmt.thenStmt.visit(this, null);
		int k = Machine.nextInstrAddr();
		Machine.emit(Op.JUMP, Reg.CB, k);
		int h = Machine.nextInstrAddr();
		stmt.elseStmt.visit(this, null);
		int l = Machine.nextInstrAddr();
		Machine.patch(j, h);
		Machine.patch(k, l);
		return null;
	}

	@Override
	public Object visitWhileStmt(WhileStmt stmt, Object arg) {
		int i = Machine.nextInstrAddr();
		Machine.emit(Op.JUMP, Reg.CB, 0);
		int j = Machine.nextInstrAddr();
		stmt.body.visit(this, null);
		int h = Machine.nextInstrAddr();
		Machine.patch(i, h);
		stmt.cond.visit(this, null);		
		Machine.emit(Op.JUMPIF, 1, Reg.CB, j);
		return null;
	}

	@Override
	public Object visitUnaryExpr(UnaryExpr expr, Object arg) {
		expr.expr.visit(this, null);
		if(expr.operator.spelling.equals("-")){
			Machine.emit(Prim.neg);
		}else if(expr.operator.spelling.equals("!")){
			Machine.emit(Prim.not);
		}
		return null;
	}

	@Override
	public Object visitBinaryExpr(BinaryExpr expr, Object arg) {
		expr.left.visit(this, null);
		expr.right.visit(this, null);
		expr.operator.visit(this, null);
		return null;
	}

	@Override
	public Object visitRefExpr(RefExpr expr, Object arg) {
		int d = (int) expr.ref.visit(this, null);
		if(d == -49){
			return null;
		}else if(d == -50){
			return null;
		}
		Machine.emit(Op.LOAD, Reg.LB, d);
		return null;
	}

	@Override
	public Object visitCallExpr(CallExpr expr, Object arg) {		
		for(int j=0;j<expr.argList.size();j++){
			expr.argList.get(j).visit(this, null);
		}
		int i = (int) expr.functionRef.visit(this, -3);
		int j = Machine.nextInstrAddr();
		Machine.emit(Op.JUMP, Reg.CB, j+ 1);
		Machine.emit(Op.CALL, Reg.CB, j + 1);
		
		return null;
	}

	@Override
	public Object visitLiteralExpr(LiteralExpr expr, Object arg) {
		expr.literal.visit(this, null);
		return null;
	}

	@Override
	public Object visitNewObjectExpr(NewObjectExpr expr, Object arg) {
		RuntimeEntity entity = (RuntimeEntity) expr.classtype.classDecl.entity;
		int size = entity.size;
		Machine.emit(Op.LOADL, -1);
		Machine.emit(Op.LOADL, size);
		Machine.emit(Prim.newobj);
//		Machine.emit(Op.STORE, Reg.LB, localCount);
		return null;
	}

	@Override
	public Object visitNewArrayExpr(NewArrayExpr expr, Object arg) {
		expr.sizeExpr.visit(this, null);
//		Machine.emit(Op.LOADL, size);
		Machine.emit(Prim.newarr);
		return null;
	}

	@Override
	public Object visitQualifiedRef(QualifiedRef ref, Object arg) {
		if(!ref.thisRelative){
			if(ref.qualifierList.get(0).spelling.equals("System")){
				return -1;
			}
		}		
		int size = ref.qualifierList.size();
		Reference dref = parseDeRef(ref, size);		
		int d = (int) dref.visit(this, arg);
		return d;
	}

	@Override
	public Object visitIndexedRef(IndexedRef ref, Object arg) {
		if(arg == null){
			int i = (int) ref.ref.visit(this, null);
			Machine.emit(Op.LOAD, Reg.LB, i);
			ref.indexExpr.visit(this, null);
			Machine.emit(Prim.arrayref);
			
		}else{
			int i = (int) ref.ref.visit(this, null);
			Machine.emit(Op.LOAD, Reg.LB, i);
			ref.indexExpr.visit(this, null);
		}
		return -50;
	}
	
	public Object visitDeRef(DeRef ref, Object arg){
		if(arg == null){
			if(ref.ref instanceof DeRef){
				ref.ref.visit(this, null);
				int d = (int) ref.mref.visit(this, ref.ref);
				Machine.emit(Op.LOADL, d);
				Machine.emit(Prim.fieldref);
			}else{
				int i = (int) ref.ref.visit(this, null);
				if(i==-49){
					int d = (int) ref.mref.visit(this, ref.ref);
					Machine.emit(Op.LOADL, d);
					Machine.emit(Prim.fieldref);
				}else if(i==-51){
					int d = (int) ref.mref.id.visit(this, ref);
					Machine.emit(Op.LOADL, d);
					Machine.emit(Prim.fieldref);
				}
				else{
					int d = (int) ref.mref.visit(this, ref.ref);
					Machine.emit(Op.LOAD, Reg.LB, i);
					Machine.emit(Op.LOADL, d);
					Machine.emit(Prim.fieldref);
				}				
			}
		}else if((int) arg == -3){
			int i = (int) ref.ref.visit(this, null);
			String mName = ref.mref.id.spelling;
			Machine.emit(Op.LOAD, Reg.LB, i);
			int j = Machine.nextInstrAddr();
			dict.put(mName, j + 1);
		}
		else{
			if(ref.ref instanceof DeRef){
				ref.ref.visit(this, null);
				int d = (int) ref.mref.visit(this, ref.ref);
				Machine.emit(Op.LOADL, d);
				
			}else{
				int i = (int) ref.ref.visit(this, null);
				if(i==-51){
					int d = (int) ref.mref.id.visit(this, ref);
					Machine.emit(Op.LOADL, d);
				}else{
					int d = (int) ref.mref.visit(this, ref.ref);
					Machine.emit(Op.LOAD, Reg.LB, i);
					Machine.emit(Op.LOADL, d);
				}				
			}
		}
		return -49;
	}

//	@Override
//	public Object visitDeRef(DeRef ref, Object arg) {
//		if(arg == null){
//			int i = (int) ref.ref.visit(this, null);
//			if(i== -2){
//				DeRef dref = (DeRef) ref.ref;
//				int d = (int) ref.mref.visit(this, dref.ref);
//				Machine.emit(Op.LOADL, d);
//				Machine.emit(Prim.fieldref);
//			}else{
//				if(ref.mref.id.spelling.equals("length")){
//					
//				}else{
//					int d = (int) ref.mref.visit(this, ref.ref);
//					Machine.emit(Op.LOAD, Reg.LB, i);
//					Machine.emit(Op.LOADL, d);
//					Machine.emit(Prim.fieldref);
//				}				
//			}
//			
//		}else{
//			int i = (int) ref.ref.visit(this, null);
//			if(i == -2){
//				DeRef dref = (DeRef) ref.ref;
//				int d = (int) ref.mref.visit(this, dref.ref);
//				Machine.emit(Op.LOADL, d);
//			}else{
//				int d = (int) ref.mref.visit(this, ref.ref);
//				Machine.emit(Op.LOAD, Reg.LB, i);
//				Machine.emit(Op.LOADL, d);
//			}
//			
//		}		
//		return -2;
//	}

	@Override
	public Object visitMemberRef(MemberRef ref, Object arg) {
		ClassType ct;
		if(arg instanceof DeRef){
			DeRef cr = (DeRef) arg;
			ct = (ClassType) cr.mref.id.decl.type;
		}else{
			ClassRef cr = (ClassRef) arg; 
			ct = (ClassType) cr.id.decl.type;
		}
		ClassDecl cd = ct.classDecl;
		int index = 0;
		for(int i=0; i<cd.fieldDeclList.size();i++){
			if(ref.id.spelling.equals(cd.fieldDeclList.get(i).name)){
				index = i;
				return index;
			}
		}for(int j=0; j<cd.methodDeclList.size(); j++){
			if(ref.id.spelling.equals(cd.methodDeclList.get(j).name)){
				index = j;
				return index;
			}
		}
		
		return 0;
	}

	@Override
	public Object visitLocalRef(LocalRef ref, Object arg) {
		if(arg == null){
			int d = (int) ref.id.visit(this, 1);
			return d;			
		}else if((int) arg == -3){
			String mName = ref.id.spelling;
			Machine.emit(Op.LOADA, Reg.OB, 0);
			int j = Machine.nextInstrAddr();
			dict.put(mName, j + 1);
			return -4;
		}
		else{
			int d = (int) ref.id.visit(this, null);
			return d;
		}				
	}

	@Override
	public Object visitThisRef(ThisRef ref, Object arg) {
		// TODO Auto-generated method stub
		Machine.emit(Op.LOADA, Reg.OB, 0);
		return -51;
	}

	@Override
	public Object visitClassRef(ClassRef ref, Object arg) {
		int i = (int) ref.id.visit(this, 1);
		return i;
	}

	@Override
	public Object visitIdentifier(Identifier id, Object arg) {
		if (id.decl instanceof FieldDecl){
			Machine.emit(Op.LOADA, Reg.OB, 0);
			UnknownValue luv = (UnknownValue) id.decl.entity;
			Machine.emit(Op.LOADL, luv.addr);
			if(arg != null){
				Machine.emit(Prim.fieldref);
			}
			return -49;
		}else{
			UnknownValue uv = (UnknownValue) id.decl.entity;
			return uv.addr;
		}
		
		
	}

	@Override
	public Object visitOperator(Operator op, Object arg) {
		if(op.spelling.equals("*")){
			Machine.emit(Prim.mult);
		}else if(op.spelling.equals("+")){
			Machine.emit(Prim.add);
		}else if(op.spelling.equals("-")){
			Machine.emit(Prim.sub);
		}else if(op.spelling.equals("/")){
			Machine.emit(Prim.div);
		}else if(op.spelling.equals("==")){
			Machine.emit(Prim.eq);			
		}else if(op.spelling.equals("!=")){
			Machine.emit(Prim.ne);
		}else if(op.spelling.equals("<")){
			Machine.emit(Prim.lt);
		}else if(op.spelling.equals("<=")){
			Machine.emit(Prim.le);
		}else if(op.spelling.equals(">")){
			Machine.emit(Prim.gt);
		}else if(op.spelling.equals(">=")){
			Machine.emit(Prim.ge);
		}
		return null;
	}

	@Override
	public Object visitIntLiteral(IntLiteral num, Object arg) {
		// TODO Auto-generated method stub
		int Num = Integer.parseInt(num.spelling);
		Machine.emit(Op.LOADL, Num);
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
			if(size > 0){
				MemberRef mref = new MemberRef(ref.qualifierList.get(size - 1), null);
				size--;
				Reference dref = parseDeRef(ref, size);
				return new DeRef(dref, mref, null);
			}
			Reference tref = new ThisRef(ref.posn);
			return tref;
		}else{
			Declaration decl = ref.qualifierList.get(size - 1).decl;
			if(decl == null){
				return null;
			}
			else if(decl.type.typeKind == TypeKind.CLASS){
				Reference cref = new ClassRef(ref.qualifierList.get(size - 1), null);
				return cref;
			}else{
				Reference lref = new LocalRef(ref.qualifierList.get(size - 1), null);
				return lref;
			}		
		}
	}

}
