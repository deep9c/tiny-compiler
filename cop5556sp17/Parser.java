package cop5556sp17;

import java.util.ArrayList;
import java.util.List;
import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.*;

public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	
	/*@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}*/

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	Program parse() throws SyntaxException {
		Program pr = program();
		matchEOF();
		return pr;
	}

	Expression expression() throws SyntaxException {
		////System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+" called: "+t.getText()+"...."+t.pos);
		Token ftoken = t;
		Expression e0 = null;
		Expression e1 = null;
		
		e0 = term();
		while(t.isKind(LT) || t.isKind(LE) || t.isKind(GT) || t.isKind(GE) || t.isKind(EQUAL) || t.isKind(NOTEQUAL) ){
			Token op = t;
			consume();
			e1 = term();
			e0 = new BinaryExpression(ftoken,e0,op,e1);
		}
		return e0;
	}

	Expression term() throws SyntaxException {
		////System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+" called: "+t.getText()+"...."+t.pos);
		Token ftoken = t;
		Expression e0 = null;
		Expression e1 = null;
		
		e0 = elem();
		while(t.isKind(PLUS) || t.isKind(MINUS) || t.isKind(OR) ){
			Token op = t;
			consume();
			e1 = elem();
			e0 = new BinaryExpression(ftoken,e0,op,e1);
		}
		return e0;
	}

	Expression elem() throws SyntaxException {
		////System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+" called: "+t.getText()+"...."+t.pos);
		Token ftoken = t;
		Expression e0 = null;
		Expression e1 = null;
		
		e0 = factor();
		//Kind kind = t.kind;
		while(t.isKind(TIMES) || t.isKind(DIV) || t.isKind(AND) || t.isKind(MOD)){
			Token op = t;
			consume();
			e1 = factor();
			e0 = new BinaryExpression(ftoken,e0,op,e1);
		}
		return e0;
		
	}

	Expression factor() throws SyntaxException {
		//System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+" called: "+t.getText()+"...."+t.pos);
		Token ftoken = t;
		Expression ep = null;
		
		Kind kind = t.kind;
		switch (kind) {
		case IDENT: {
			ep = new IdentExpression(ftoken);
			consume();			
		}
			break;
		case INT_LIT: {
			ep = new IntLitExpression(ftoken);
			consume();
		}
			break;
		case KW_TRUE:
		case KW_FALSE: {
			ep = new BooleanLitExpression(ftoken);
			consume();
		}
			break;
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			ep = new ConstantExpression(ftoken);
			consume();
		}
			break;
		case LPAREN: {
			consume();
			ep = expression();
			match(RPAREN);
		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal factor .. text='"+t.getText()+"'.. (pos= " + t.pos+") .. "+ t.getLinePos());
		}
		return ep;
	}

	Block block() throws SyntaxException {
		//System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+" called: "+t.getText()+"...."+t.pos);
		Token ftoken = t;
		ArrayList<Dec> declist = new ArrayList<Dec>();
		ArrayList<Statement> stlist = new ArrayList<Statement>();
		
		if(t.isKind(LBRACE)){
			consume();
			while(t.isKind(KW_INTEGER) || t.isKind(KW_BOOLEAN) || t.isKind(KW_IMAGE) || t.isKind(KW_FRAME) || t.isKind(OP_SLEEP) || t.isKind(KW_WHILE) || t.isKind(KW_IF) || t.isKind(IDENT) || t.isKind(KW_SHOW) || t.isKind(KW_HIDE) || t.isKind(KW_MOVE) || t.isKind(KW_XLOC) || t.isKind(KW_YLOC) || t.isKind(OP_BLUR) || t.isKind(OP_GRAY) || t.isKind(OP_CONVOLVE) || t.isKind(OP_WIDTH) || t.isKind(OP_HEIGHT) || t.isKind(KW_SCALE)){
				if(t.isKind(KW_INTEGER) || t.isKind(KW_BOOLEAN) || t.isKind(KW_IMAGE) || t.isKind(KW_FRAME)){
					declist.add(dec());
				}
				else{
					stlist.add(statement());
				}
			}
			match(RBRACE);
		}
		else{
			throw new SyntaxException("illegal block .. text='"+t.getText()+"'.. (pos= " + t.pos+") .. "+ t.getLinePos() );
		}
		Block bl = new Block(ftoken,declist,stlist);
		return bl;
	}

	Program program() throws SyntaxException {
		//System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+" called: "+t.getText()+"...."+t.pos);
		Token ftoken = t;
		ArrayList<ParamDec> pdlist = new ArrayList<ParamDec>();
		Block bl = null;
		
		if (t.isKind(IDENT)){
			consume();
			if(t.isKind(LBRACE)){
				bl = block();
			}
			else if(t.isKind(KW_URL) || t.isKind(KW_FILE) || t.isKind(KW_INTEGER) || t.isKind(KW_BOOLEAN)){
				pdlist.add(paramDec());
				while(t.isKind(COMMA)){
					consume();
					pdlist.add(paramDec());
				}
				bl = block();
			}
			else{
				throw new SyntaxException("illegal program .. text='"+t.getText()+"'.. (pos= " + t.pos+") .. "+ t.getLinePos());
			}
		}
		else{
			throw new SyntaxException("illegal program .. text='"+t.getText()+"'.. (pos= " + t.pos+") .. "+ t.getLinePos());
		}
		Program pr = new Program(ftoken,pdlist,bl);
		return pr;
	}

	ParamDec paramDec() throws SyntaxException {
		//System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+" called: "+t.getText()+"...."+t.pos);
		Token ftoken = t;
		ParamDec paramdec = null;
		
		Kind kind = t.kind;
		if (kind.equals(KW_URL) || kind.equals(KW_FILE) || kind.equals(KW_INTEGER) || kind.equals(KW_BOOLEAN)){
			consume();
			Token identtoken = t;
			match(IDENT);
			paramdec = new ParamDec(ftoken,identtoken);
		}
		else{
			throw new SyntaxException("illegal paramDec .. text='"+t.getText()+"'.. (pos= " + t.pos+") .. "+ t.getLinePos());
		}
		return paramdec;
	}

	Dec dec() throws SyntaxException {
		//System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+" called: "+t.getText()+"...."+t.pos);
		
		Token ftoken = t;
		Dec dec = null;
		
		if(t.isKind(KW_INTEGER) || t.isKind(KW_BOOLEAN) || t.isKind(KW_IMAGE) || t.isKind(KW_FRAME)){
			consume();
			Token identtoken = t;
			match(IDENT);
			dec = new Dec(ftoken,identtoken);
		}
		else{
			throw new SyntaxException("illegal dec .. text='"+t.getText()+"'.. (pos= " + t.pos+") .. "+ t.getLinePos());
		}
		return dec;
	}

	Statement statement() throws SyntaxException {
		//System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+" called: "+t.getText()+"...."+t.pos);
		Token ftoken = t;
		Statement st = null;
				
		if(t.isKind(OP_SLEEP)){
			consume();
			Expression ep = expression();
			st = new SleepStatement(ftoken,ep);
			match(SEMI);
		}
		else if(t.isKind(KW_WHILE)){
			st = whileStatement();
		}
		else if(t.isKind(KW_IF)){
			st = ifStatement();
		}
		else if(t.isKind(IDENT)){	
			Token nextTok = scanner.peek();
			////System.out.println(t.getText()+t.kind+"***********" + nextTok.getText()+"...."+nextTok.kind);
			if(nextTok.isKind(ASSIGN)){
				st = assign();
				match(SEMI);
			}
			else{
				st = chain();
				match(SEMI);
			}
		}
		else if(t.isKind(KW_SHOW) || t.isKind(KW_HIDE) || t.isKind(KW_MOVE) || t.isKind(KW_XLOC) || t.isKind(KW_YLOC) || t.isKind(OP_BLUR) || t.isKind(OP_GRAY) || t.isKind(OP_CONVOLVE) || t.isKind(OP_WIDTH) || t.isKind(OP_HEIGHT) || t.isKind(KW_SCALE)){
			st = chain();
			match(SEMI);
		}
		else{
			throw new SyntaxException("illegal statement .. text='"+t.getText()+"'.. (pos= " + t.pos+") .. "+ t.getLinePos());
		}
		return st;
	}

	Chain chain() throws SyntaxException {
		//System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+" called: "+t.getText()+"...."+t.pos);
		Token ftoken = t;
		Chain c0 = null;
		ChainElem c1 = null;
		
		c0 = chainElem();
		Token op = t;
		arrowOp();
		c1 = chainElem();
		c0 = new BinaryChain(ftoken,c0,op,c1);
		while(t.isKind(ARROW) || t.isKind(BARARROW)){
			op = t;
			consume();
			c1 = chainElem();
			c0 = new BinaryChain(ftoken,c0,op,c1);
		}
		return c0;
	}
	
	AssignmentStatement assign() throws SyntaxException {
		//System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+" called: "+t.getText()+"...."+t.pos);	
		Token ftoken = t;
		IdentLValue i = null;
		Expression ep = null;
		
		if(t.isKind(IDENT)){
			i = new IdentLValue(ftoken);
			consume();
			match(ASSIGN);
			ep = expression();
		}
		else{
			throw new SyntaxException("illegal assign .. text='"+t.getText()+"'.. (pos= " + t.pos+") .. "+ t.getLinePos());
		}
		AssignmentStatement as = new AssignmentStatement(ftoken,i,ep);
		return as;
	}
		
	ChainElem chainElem() throws SyntaxException {
		//System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+" called: "+t.getText()+"...."+t.pos);
		Token ftoken = t;
		ChainElem ch = null;	//hw3 CHECK!!!
		
		if(t.isKind(IDENT)){
			ch = new IdentChain(ftoken);
			consume();
		}
		else if(t.isKind(OP_BLUR) || t.isKind(OP_GRAY) || t.isKind(OP_CONVOLVE)){
			//consume();	//hw2 CHECK!!!
			filterOp();
			Tuple tup = arg();
			ch = new FilterOpChain(ftoken,tup);
		}
		else if(t.isKind(KW_SHOW) || t.isKind(KW_HIDE) || t.isKind(KW_MOVE) || t.isKind(KW_XLOC) || t.isKind(KW_YLOC)){
			//consume();
			frameOp();
			Tuple tup = arg();
			ch = new FrameOpChain(ftoken,tup);
		}
		else if(t.isKind(OP_WIDTH) || t.isKind(OP_HEIGHT) || t.isKind(KW_SCALE)){
			//consume();
			imageOp();
			Tuple tup = arg();
			ch = new ImageOpChain(ftoken,tup);
		}
		else{
			throw new SyntaxException("illegal chainElem .. text='"+t.getText()+"'.. (pos= " + t.pos+") .. "+ t.getLinePos());
		}
		return ch;
	}

	Tuple arg() throws SyntaxException {
		//HOW TO MANAGE e ?????
		//System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+" called: "+t.getText()+"...."+t.pos);
		Token ftoken = t;
		List<Expression> listexp = new ArrayList<Expression>();
		
		if(t.isKind(LPAREN)){
			consume();
			listexp.add(expression());
			while(t.isKind(COMMA)){
				consume();
				listexp.add(expression());				
			}
			match(RPAREN);
		}
		else{
			
		}
		Tuple tup = new Tuple(ftoken,listexp);
		return tup;
				
	}
	
	void relOp() throws SyntaxException {
		//System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+" called: "+t.getText()+"...."+t.pos);
		Kind kind = t.kind;
		switch (kind) {
		case LT:
		case LE:
		case GT:
		case GE:
		case EQUAL:
		case NOTEQUAL: {
			consume();
		} break;
				
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal relOp .. text='"+t.getText()+"'.. (pos= " + t.pos+") .. "+ t.getLinePos());
		}
	}
	
	void weakOp() throws SyntaxException {
		//System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+" called: "+t.getText()+"...."+t.pos);
		Kind kind = t.kind;
		switch (kind) {
		case PLUS:
		case MINUS:
		case OR: {
			consume();
		} break;
				
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal weakOp .. text='"+t.getText()+"'.. (pos= " + t.pos+") .. "+ t.getLinePos());
		}
	}
	
	void strongOp() throws SyntaxException {
		//System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+" called: "+t.getText()+"...."+t.pos);
		Kind kind = t.kind;
		switch (kind) {
		case TIMES:
		case DIV:
		case AND:
		case MOD: {
			consume();
		} break;
				
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal strongOp .. text='"+t.getText()+"'.. (pos= " + t.pos+") .. "+ t.getLinePos());
		}
	}
	
	void imageOp() throws SyntaxException {
		//System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+" called: "+t.getText()+"...."+t.pos);
		Kind kind = t.kind;
		switch (kind) {
		case OP_WIDTH:
		case OP_HEIGHT:
		case KW_SCALE: {
			consume();
		} break;
				
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal imageOp .. text='"+t.getText()+"'.. (pos= " + t.pos+") .. "+ t.getLinePos());
		}
	}
	
	void frameOp() throws SyntaxException {
		//System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+" called: "+t.getText()+"...."+t.pos);
		Kind kind = t.kind;
		switch (kind) {
		case KW_SHOW:
		case KW_HIDE:
		case KW_MOVE:
		case KW_XLOC:
		case KW_YLOC: {
			consume();
		} break;
				
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal frameOp .. text='"+t.getText()+"'.. (pos= " + t.pos+") .. "+ t.getLinePos());
		}
	}
	
	void filterOp() throws SyntaxException {
		//System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+" called: "+t.getText()+"...."+t.pos);
		Kind kind = t.kind;
		switch (kind) {
		case OP_BLUR:
		case OP_GRAY:
		case OP_CONVOLVE: {
			consume();
		} break;
				
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal filterOp .. text='"+t.getText()+"'.. (pos= " + t.pos+") .. "+ t.getLinePos());
		}
	}
	
	void arrowOp() throws SyntaxException {
		//System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+" called: "+t.getText()+"...."+t.pos);
		Kind kind = t.kind;
		switch (kind) {
		case ARROW:
		case BARARROW: {
			consume();
		} break;
				
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal arrowOp .. text='"+t.getText()+"'.. (pos= " + t.pos+") .. "+ t.getLinePos());
		}
	}
	
	IfStatement ifStatement() throws SyntaxException {
		//System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+" called: "+t.getText()+"...."+t.pos);
		Token ftoken = t;
		Expression ep = null;
		Block bl = null;		
		
		if(t.isKind(KW_IF)){
			consume();
			match(LPAREN);
			ep = expression();
			match(RPAREN);
			bl = block();
		}
		else{
			throw new SyntaxException("illegal ifStatement .. text='"+t.getText()+"'.. (pos= " + t.pos+") .. "+ t.getLinePos());
		}
		
		IfStatement ist = new IfStatement(ftoken,ep,bl);
		return ist;
	}
	
	WhileStatement whileStatement() throws SyntaxException {
		//System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()+" called: "+t.getText()+"...."+t.pos);
		Token ftoken = t;
		Expression ep = null;
		Block bl = null;
		
		if(t.isKind(KW_WHILE)){
			consume();
			match(LPAREN);
			ep = expression();
			match(RPAREN);
			bl = block();
		}
		else{
			throw new SyntaxException("illegal whileStatement .. text='"+t.getText()+"'.. (pos= " + t.pos+") .. "+ t.getLinePos());
		}
		
		WhileStatement ws = new WhileStatement(ftoken,ep,bl);
		return ws;
	}
	
	

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF .. text='"+t.getText()+"'.. (pos= " + t.pos+") .. "+ t.getLinePos() );
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.isKind(kind)) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + " expected " + kind + " .. text='"+t.getText()+"'.. (pos= " + t.pos+") .. "+ t.getLinePos());
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		// TODO. Optional but handy
		return null; //replace this statement
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
