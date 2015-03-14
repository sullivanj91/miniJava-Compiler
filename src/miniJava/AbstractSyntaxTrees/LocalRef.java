package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class LocalRef extends Reference{
	public LocalRef(Identifier id, SourcePosition posn){
		super(posn);
		this.id = id;
	}
	
	
	public <A,R> R visit(Visitor<A,R> v, A o) {
		return v.visitLocalRef(this, o);
	}
	public Identifier id;
}
