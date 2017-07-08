package cop5556sp17.AST;

import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Token;

public abstract class Statement extends ASTNode {

	public Statement(Token firstToken) {
		super(firstToken);
	}

	public TypeName type;	//added in HW4
	
	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;

}
