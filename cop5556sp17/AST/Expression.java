package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;

public abstract class Expression extends ASTNode {
	
	protected Expression(Token firstToken) {
		super(firstToken);
	}

	private TypeName type;	//added in HW4
	public TypeName getType(){
		return type;
	}
	public void setType(TypeName tn){
		type=tn;
	}
	
	@Override
	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;

}
