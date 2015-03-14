package miniJava.CodeGenerator;

public abstract class RuntimeEntity {
	public int size;
	public RuntimeEntity(){
		this.size = 1;
	}
	
	public RuntimeEntity(int size){
		this.size = size;
	}
}
