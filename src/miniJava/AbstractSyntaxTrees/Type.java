/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

abstract public class Type extends AST {
    
    public Type(TypeKind typ, SourcePosition posn){
        super(posn);
        typeKind = typ;
    }
    
    public TypeKind typeKind;
    
    public boolean equals(Type cType){
    	if(cType == null){
    		return true;
    	}else if (this.typeKind == TypeKind.ARRAY){
    		ArrayType eltype = (ArrayType) this;
    		if (eltype.eltType.typeKind == cType.typeKind){
    			return true;
    		}else{
    			return false;
    		}
    	}
    	else if(this.typeKind == cType.typeKind){
    		return true;
    	}else return false;
    }
}

        