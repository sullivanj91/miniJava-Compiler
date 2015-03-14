package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class ThisRef extends Reference{
	
	public ThisRef(SourcePosition posn){
		super(posn);
	}	
	
	public <A,R> R visit(Visitor<A,R> v, A o) {
		return v.visitThisRef(this, o);
	}
}
