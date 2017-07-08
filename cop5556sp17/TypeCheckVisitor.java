package cop5556sp17;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import java.util.ArrayList;
import java.util.List;

import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.*;


public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {

		TypeName chainType = (TypeName)binaryChain.getE0().visit(this, arg);
		TypeName chainElemType = (TypeName)binaryChain.getE1().visit(this, arg);
		Token op = binaryChain.getArrow();
				
		
		if(op.isKind(ARROW)){
			if(chainType.equals(URL) && chainElemType.equals(IMAGE)){
				binaryChain.setTypeName(IMAGE);
			}
			else if(chainType.equals(FILE) && chainElemType.equals(IMAGE)){
				binaryChain.setTypeName(IMAGE);
			}
			else if(chainType.equals(FRAME) && (binaryChain.getE1() instanceof FrameOpChain) && (binaryChain.getE1().getFirstToken().isKind(KW_XLOC) || binaryChain.getE1().getFirstToken().isKind(KW_YLOC))){
				binaryChain.setTypeName(INTEGER);
			}
			else if(chainType.equals(FRAME) && (binaryChain.getE1() instanceof FrameOpChain) && (binaryChain.getE1().getFirstToken().isKind(KW_SHOW) || binaryChain.getE1().getFirstToken().isKind(KW_HIDE) || binaryChain.getE1().getFirstToken().isKind(KW_MOVE))){
				binaryChain.setTypeName(FRAME);
			}
			else if(chainType.equals(IMAGE) && (binaryChain.getE1() instanceof ImageOpChain) && (binaryChain.getE1().getFirstToken().isKind(OP_WIDTH) || binaryChain.getE1().getFirstToken().isKind(OP_HEIGHT))){
				binaryChain.setTypeName(INTEGER);
			}
			else if(chainType.equals(IMAGE) && chainElemType.equals(FRAME)){
				binaryChain.setTypeName(FRAME);
			}
			else if(chainType.equals(IMAGE) && chainElemType.equals(FILE)){
				binaryChain.setTypeName(NONE);
			}
			else if(chainType.equals(IMAGE) && (binaryChain.getE1() instanceof FilterOpChain) && (binaryChain.getE1().getFirstToken().isKind(OP_GRAY) || binaryChain.getE1().getFirstToken().isKind(OP_BLUR) || binaryChain.getE1().getFirstToken().isKind(OP_CONVOLVE))){
				binaryChain.setTypeName(IMAGE);
			}
			else if(chainType.equals(IMAGE) && (binaryChain.getE1() instanceof ImageOpChain) && (binaryChain.getE1().getFirstToken().isKind(KW_SCALE) )){
				binaryChain.setTypeName(IMAGE);
			}
			else if(chainType.equals(IMAGE) && (binaryChain.getE1() instanceof IdentChain) && (chainElemType.equals(IMAGE)) ){
				binaryChain.setTypeName(IMAGE);		//HW6
			}
			else if(chainType.equals(INTEGER) && (binaryChain.getE1() instanceof IdentChain) && (chainElemType.equals(INTEGER)) ){
				binaryChain.setTypeName(INTEGER);	//HW6
			}
			else{
				throw new TypeCheckException("visitBinaryChain error: Token="+binaryChain.getFirstToken().getText()+". Global pos="+binaryChain.getFirstToken().pos+". "+ binaryChain.getFirstToken().getLinePos());
			}
			
		}
		else if(op.isKind(BARARROW)){
			if(chainType.equals(IMAGE) && (binaryChain.getE1() instanceof FilterOpChain) && (binaryChain.getE1().getFirstToken().isKind(OP_GRAY) || binaryChain.getE1().getFirstToken().isKind(OP_BLUR) || binaryChain.getE1().getFirstToken().isKind(OP_CONVOLVE))){
				binaryChain.setTypeName(IMAGE);
			}
			else{
				throw new TypeCheckException("visitBinaryChain error: Token="+binaryChain.getFirstToken().getText()+". Global pos="+binaryChain.getFirstToken().pos+". "+ binaryChain.getFirstToken().getLinePos());
			}
		}
		else{
			throw new TypeCheckException("visitBinaryChain error: Token="+binaryChain.getFirstToken().getText()+". Global pos="+binaryChain.getFirstToken().pos+". "+ binaryChain.getFirstToken().getLinePos());
		}
		
		return binaryChain.getTypeName();
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		
        
        TypeName t0 = (TypeName) binaryExpression.getE0().visit(this, arg);
        TypeName t1 = (TypeName) binaryExpression.getE1().visit(this, arg);
        
        Scanner.Kind opkind = binaryExpression.getOp().kind;
        
        if(t0.equals(INTEGER) && (opkind.equals(PLUS)||opkind.equals(MINUS)) && t1.equals(INTEGER)){
        	binaryExpression.setType(INTEGER);
        }
        else if(t0.equals(IMAGE) && (opkind.equals(PLUS)||opkind.equals(MINUS)) && t1.equals(IMAGE)){
        	binaryExpression.setType(IMAGE);
        }
        else if(t0.equals(INTEGER) && (opkind.equals(TIMES)||opkind.equals(DIV)) && t1.equals(INTEGER)){
        	binaryExpression.setType(INTEGER);
        }
        else if(t0.equals(IMAGE) && (opkind.equals(DIV)) && t1.equals(INTEGER)){	//Implemented at HW6
        	binaryExpression.setType(IMAGE);
        }
        else if(t0.equals(IMAGE) && (opkind.equals(MOD)) && t1.equals(INTEGER)){	//Implemented at HW6
        	binaryExpression.setType(IMAGE);
        }
        else if(t0.equals(INTEGER) && (opkind.equals(MOD)) && t1.equals(INTEGER)){	//Implemented at HW6
        	binaryExpression.setType(INTEGER);
        }
        else if(t0.equals(INTEGER) && (opkind.equals(TIMES)) && t1.equals(IMAGE)){
        	binaryExpression.setType(IMAGE);
        }
        else if(t0.equals(IMAGE) && (opkind.equals(TIMES)) && t1.equals(INTEGER)){
        	binaryExpression.setType(IMAGE);
        }
        else if(t0.equals(INTEGER) && (opkind.equals(LT)||opkind.equals(GT)||opkind.equals(LE)||opkind.equals(GE)) && t1.equals(INTEGER)){
        	binaryExpression.setType(BOOLEAN);
        }
        else if(t0.equals(BOOLEAN) && (opkind.equals(LT)||opkind.equals(GT)||opkind.equals(LE)||opkind.equals(GE)) && t1.equals(BOOLEAN)){
        	binaryExpression.setType(BOOLEAN);
        }
        else if(t0.equals(t1) && (opkind.equals(AND)||opkind.equals(OR)) ){
        	binaryExpression.setType(BOOLEAN);	//Implemented at HW5/HW6
        }
        
        else if(opkind.equals(EQUAL)||opkind.equals(NOTEQUAL)){
        	if(t0.equals(t1)){
        		binaryExpression.setType(BOOLEAN);
        	}        	
        	else{
        		throw new TypeCheckException("visitBinaryExpression error : Token="+binaryExpression.getFirstToken().getText()+". Global pos="+binaryExpression.getFirstToken().pos+". "+ binaryExpression.getFirstToken().getLinePos());
        	}
        }
        else{
        	throw new TypeCheckException("visitBinaryExpression error : Token="+binaryExpression.getFirstToken().getText()+". Global pos="+binaryExpression.getFirstToken().pos+". "+ binaryExpression.getFirstToken().getLinePos());
        }
        
        
        
        return binaryExpression.getType();
		
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		//System.out.println("visitBlock");
		symtab.enterScope();
		for(Dec d : block.getDecs()){
			d.visit(this, arg);
		}
		for(Statement s : block.getStatements()){
			s.visit(this, arg);
		}
		symtab.leaveScope();
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		booleanLitExpression.setType(BOOLEAN);
		return booleanLitExpression.getType();

	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		int len = filterOpChain.getArg().getExprList().size();
		if(len!=0){
			throw new TypeCheckException("visitFilterOpChain error: Token="+filterOpChain.getFirstToken().getText()+". Global pos="+filterOpChain.getFirstToken().pos+". "+ filterOpChain.getFirstToken().getLinePos());
		}
		
		filterOpChain.getArg().visit(this, arg);	//***** CHECK if required or not
		filterOpChain.setTypeName(IMAGE);
		
		return filterOpChain.getTypeName();
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		Token op = frameOpChain.getFirstToken();
		if(op.isKind(KW_SHOW) || op.isKind(KW_HIDE)){
			if(frameOpChain.getArg().getExprList().size() != 0){
				throw new TypeCheckException("FrameOpChain tuple length not 0: Token="+frameOpChain.getFirstToken().getText()+". Global pos="+frameOpChain.getFirstToken().pos+". "+ frameOpChain.getFirstToken().getLinePos());
			}
			frameOpChain.setTypeName(NONE);
		}
		else if(op.isKind(KW_XLOC) || op.isKind(KW_YLOC)){
			if(frameOpChain.getArg().getExprList().size() != 0){
				throw new TypeCheckException("FrameOpChain tuple length not 0: Token="+frameOpChain.getFirstToken().getText()+". Global pos="+frameOpChain.getFirstToken().pos+". "+ frameOpChain.getFirstToken().getLinePos());
			}
			frameOpChain.setTypeName(INTEGER);
		}
		else if(op.isKind(KW_MOVE)){
			if(frameOpChain.getArg().getExprList().size() != 2){
				throw new TypeCheckException("FrameOpChain tuple length not 2: Token="+frameOpChain.getFirstToken().getText()+". Global pos="+frameOpChain.getFirstToken().pos+". "+ frameOpChain.getFirstToken().getLinePos());
			}
			frameOpChain.setTypeName(NONE);
		}
		else{
			throw new TypeCheckException("FrameOpChain operator incorrect: Token="+frameOpChain.getFirstToken().getText()+". Global pos="+frameOpChain.getFirstToken().pos+". "+ frameOpChain.getFirstToken().getLinePos());
		}
		
		frameOpChain.getArg().visit(this, arg);
		
		return frameOpChain.getTypeName();
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		Dec d = symtab.lookup(identChain.getFirstToken().getText());
		if(d==null){
			throw new TypeCheckException("visitIdentChain Symtab lookup failed: Token="+identChain.getFirstToken().getText()+". Global pos="+identChain.getFirstToken().pos+". "+ identChain.getFirstToken().getLinePos());
		}
		identChain.setDec(d);	//added in HW6
		identChain.setTypeName(d.getTypeName());
		
		return identChain.getTypeName();
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {

		Dec d = symtab.lookup(identExpression.getFirstToken().getText());
		if(d==null){
			throw new TypeCheckException("visitIdentExpression Symtab lookup failed: Token="+identExpression.getFirstToken().getText()+". Global pos="+identExpression.getFirstToken().pos+". "+ identExpression.getFirstToken().getLinePos());
		}
		identExpression.setType(d.getTypeName());
		identExpression.setDec(d);
		
		return identExpression.getType();
		
		//return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		TypeName t = (TypeName)ifStatement.getE().visit(this, arg);
		if(!t.equals(BOOLEAN)){
			throw new TypeCheckException("visitIfStatement error. Expr not boolean: Token="+ifStatement.getFirstToken().getText()+". Global pos="+ifStatement.getFirstToken().pos+". "+ ifStatement.getFirstToken().getLinePos());
		}
		ifStatement.getB().visit(this, arg);
			
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {		
		
		intLitExpression.setType(INTEGER);
		return intLitExpression.getType();
		
		//return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		TypeName tn = (TypeName)sleepStatement.getE().visit(this, arg);
		if(!tn.equals(INTEGER)){
			throw new TypeCheckException("visitSleepStatement error: Token="+sleepStatement.getFirstToken().getText()+". Global pos="+sleepStatement.getFirstToken().pos+". "+ sleepStatement.getFirstToken().getLinePos());
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		TypeName t = (TypeName)whileStatement.getE().visit(this, arg);
		if(!t.equals(BOOLEAN)){
			throw new TypeCheckException("visitWhileStatement error. Expr not boolean: "+whileStatement.getFirstToken().getText()+". Global pos="+whileStatement.getFirstToken().pos+". "+ whileStatement.getFirstToken().getLinePos());
		}
		whileStatement.getB().visit(this, arg);
		
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		boolean symtabinsert = symtab.insert(declaration.getIdent().getText(), declaration);
		if(!symtabinsert){
			throw new TypeCheckException("visitDec symtabinsert error: Token="+declaration.getFirstToken().getText()+". Global pos="+declaration.getFirstToken().pos+". "+ declaration.getFirstToken().getLinePos());
		}
		declaration.setTypeName(Type.getTypeName(declaration.getFirstToken()));		
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {

		for(ParamDec pd : program.getParams()){
			pd.visit(this, arg);
		}
		program.getB().visit(this, arg);
		
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		TypeName tn_identlval = (TypeName)assignStatement.getVar().visit(this, arg);
		TypeName tn_exp = (TypeName)assignStatement.getE().visit(this, arg);
		
		if(!tn_identlval.equals(tn_exp)){
			throw new TypeCheckException("visitAssignmentStatement error: Token="+assignStatement.getFirstToken().getText()+". Global pos="+assignStatement.getFirstToken().pos+". "+ assignStatement.getFirstToken().getLinePos());
		}
		
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		Dec d = symtab.lookup(identX.getText());
		if(d==null){
			throw new TypeCheckException("visitIdentLValue Symtab lookup failed: Token="+identX.getFirstToken().getText()+". Global pos="+identX.getFirstToken().pos+". "+ identX.getFirstToken().getLinePos());
		}
		//identX.setTypeName(d.getTypeName());
		identX.setDec(d);
		return d.getTypeName();
		
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		boolean symtabinsert = symtab.insert(paramDec.getIdent().getText(), paramDec);
		if(!symtabinsert){
			throw new TypeCheckException("visitParamDec error: Token= "+paramDec.getFirstToken().getText()+". Global pos="+paramDec.getFirstToken().pos+". "+ paramDec.getFirstToken().getLinePos());
		}
		paramDec.setTypeName(Type.getTypeName(paramDec.getFirstToken()));		
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		constantExpression.setType(INTEGER);
		return constantExpression.getType();
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		
		Token op = imageOpChain.getFirstToken();
		if(op.isKind(OP_WIDTH) || op.isKind(OP_HEIGHT)){
			if(imageOpChain.getArg().getExprList().size() != 0){
				throw new TypeCheckException("ImageOpChain tuple length not 0: Token="+imageOpChain.getFirstToken().getText()+". Global pos="+imageOpChain.getFirstToken().pos+". "+ imageOpChain.getFirstToken().getLinePos());
			}
			imageOpChain.setTypeName(INTEGER);
		}
		else if(op.isKind(KW_SCALE)){
			if(imageOpChain.getArg().getExprList().size() != 1){
				throw new TypeCheckException("ImageOpChain tuple length not 1: Token="+imageOpChain.getFirstToken().getText()+". Global pos="+imageOpChain.getFirstToken().pos+". "+ imageOpChain.getFirstToken().getLinePos());
			}
			imageOpChain.setTypeName(IMAGE);
		}		
		else{
			throw new TypeCheckException("ImageOpChain operator incorrect: Token="+imageOpChain.getFirstToken().getText()+". Global pos="+imageOpChain.getFirstToken().pos+". "+ imageOpChain.getFirstToken().getLinePos());
		}
		
		imageOpChain.getArg().visit(this, arg);
		
		return imageOpChain.getTypeName();
		
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		
		List<Expression> list_expr = tuple.getExprList();
		boolean all_expr_int=true;
		
		for(Expression ex : list_expr){
			if(!ex.visit(this, arg).equals(INTEGER)){
				all_expr_int = false;				
			}
		}
		
		if(!all_expr_int){
			throw new TypeCheckException("visitTuple error: Token="+tuple.getFirstToken().getText()+". Global pos="+tuple.getFirstToken().pos+". "+ tuple.getFirstToken().getLinePos());
		}
		
		return null;
	}


}
