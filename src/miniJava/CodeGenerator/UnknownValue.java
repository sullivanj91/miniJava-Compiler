package miniJava.CodeGenerator;

public class UnknownValue extends RuntimeEntity{
	public int addr;
	
	public UnknownValue(int addr){
		super();
		this.addr = addr;
	}
	public UnknownValue(int size, int addr){
		super(size);
		this.addr = addr;
	}
}
