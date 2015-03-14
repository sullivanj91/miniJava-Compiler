package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

	public class ErrorType extends Type{
		
	public ErrorType(TypeKind t, SourcePosition posn){
        super(t, posn);
	}

	public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitErrorType(this, o);
	}
    
}
