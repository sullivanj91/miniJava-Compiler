package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class DeRef extends Reference{
	
	public DeRef(Reference ref, MemberRef mref, SourcePosition posn){
		super(posn);
		this.ref = ref;
		this.mref = mref;
	}
	
	
	public <A,R> R visit(Visitor<A,R> v, A o) {
		return v.visitDeRef(this, o);
	}
	public Reference ref;
	public MemberRef mref;
}
