package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class UnsupportedType extends Type{
	
	public UnsupportedType(TypeKind t, SourcePosition posn){
	        super(t, posn);
	}
	    
	public <A,R> R visit(Visitor<A,R> v, A o) {
	        return v.visitUnsupportedType(this, o);
	}
	

}
