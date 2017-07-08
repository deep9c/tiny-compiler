package cop5556sp17.AST;

import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Token;


public abstract class Chain extends Statement {
	
	public Chain(Token firstToken) {
		super(firstToken);
	}
	
	private TypeName type;	//added in HW4
	public TypeName getTypeName(){
		return type;
	}
	public void setTypeName(TypeName tn){
		type=tn;
	}
	
	private boolean left;		//added in HW6
	public boolean getLeft() {
		return left;
	}
	public void setLeft(boolean left) {
		this.left = left;
	}
	
	private Dec dec;		//added in HW6
	public Dec getDec() {
		return dec;
	}
	public void setDec(Dec dec) {
		this.dec = dec;
	}
	
}
