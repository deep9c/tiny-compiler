package cop5556sp17;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import cop5556sp17.Scanner.Kind;

public class Scanner {

	public static final HashMap<String,Kind> kindtextmap = new HashMap<String,Kind>();	//to store words,Kind as key-value pairs
	public ArrayList<Integer> newlineposarray = new ArrayList<Integer>();
	
	/**
	* Kind enum
	*/

	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), KW_IMAGE("image"), KW_URL("url"), KW_FILE(
		"file"), KW_FRAME("frame"), KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), SEMI(
		";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), RBRACE("}"), ARROW("->"), BARARROW(
		"|->"), OR("|"), AND("&"), EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(
		">="), PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), ASSIGN(
		"<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE(
		"convolve"), KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH(
		"screenwidth"), OP_WIDTH("width"), OP_HEIGHT(
		"height"), KW_XLOC("xloc"), KW_YLOC(
		"yloc"), KW_HIDE("hide"), KW_SHOW(
		"show"), KW_MOVE(
		"move"), OP_SLEEP(
		"sleep"), KW_SCALE(
		"scale"), EOF(
		"eof");

		Kind(String text) {
			this.text = text;
			kindtextmap.put(text,this);
		}

		final String text;

		String getText() {
			return text;
		}
		
		public static Kind getKindFromText(String text) {
			return kindtextmap.get(text);
		}
	}
	
	public static enum State {
		START, IN_DIGIT, AFTER_EQ, IN_IDENT, ERROR, IN_COMMENT, AFTER_DIV, IN_COMMENT_STAR,
		AFTER_NOT, AFTER_LT, AFTER_GT, AFTER_MINUS, AFTER_OR, AFTER_OR_MINUS;
	}
	

	/**
	* Thrown by Scanner when an illegal character is encountered
	*/
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}

	/**
	* Thrown by Scanner when an int literal is not a value that can be
	* represented by an int.
	*/
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
		public IllegalNumberException(String message) {
			super(message);
		}
	}

	/**
	* Holds the line and position in the line of a token.
	*/
	static class LinePos {
		public final int line;
		public final int posInLine;

		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}

	public class Token {
		public final Kind kind;
		public final int pos; // position in input array
		public final int length;

		// returns the text of this Token
		public String getText() {
			// TODO IMPLEMENT THIS	(DONE!)
			//System.out.println("Token.getText()= "+chars.substring(pos, (pos+length)));
			return chars.substring(pos, (pos+length));
			//return null;
		}

		// returns a LinePos object representing the line and column of this
		// Token
		LinePos getLinePos() {
			// TODO IMPLEMENT THIS						
			/*for(int i = 0; i < newlineposarray.size(); i++) {
				System.out.println("newlineposarray.get("+i+")"+newlineposarray.get(i));
			}*/
			//System.out.println("getLinePos pos= "+pos);
			int newlineindex=Collections.binarySearch(newlineposarray, pos);
			//System.out.println("newlineindex= "+newlineindex);
			int lineno = Math.abs(newlineindex+2);
			//System.out.println("lineno= "+lineno);
			int posInLine = pos-newlineposarray.get(lineno)-1;
			
			//System.out.println("Token.getLinePos:: lineno= "+lineno+", pos= "+posInLine);
			return new LinePos(lineno,posInLine);
			//return null;
		}

		Token(Kind kind, int pos, int length) {
			//System.out.println("New token created:: Kind="+kind+", pos= "+pos+", length= "+length);
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		/**
		* Precondition: kind = Kind.INT_LIT, the text can be represented with a
		* Java int. Note that the validity of the input should have been
		* checked when the Token was created. So the exception should never be
		* thrown.
		* 
		* @return int value of this token, which should represent an INT_LIT
		* @throws NumberFormatException
		*/
		public int intVal() throws NumberFormatException {
			// TODO IMPLEMENT THIS 	(DONE!)
			//System.out.println("Token.intVal()= "+chars.substring(pos, (pos+length)));
			return Integer.parseInt(chars.substring(pos, (pos+length)));
			//return 0;
		}

		public boolean isKind(Kind k) {
			// TODO Auto-generated method stub
			if(k.equals(kind)){
				return true;
			}
			else{
				return false;
			}
			//return false;
		}
		
		//For HW 3, hashcode, equals, getOuterType:-
		@Override
		  public int hashCode() {
		   final int prime = 31;
		   int result = 1;
		   result = prime * result + getOuterType().hashCode();
		   result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		   result = prime * result + length;
		   result = prime * result + pos;
		   return result;
		  }

		  @Override
		  public boolean equals(Object obj) {
		   if (this == obj) {
		    return true;
		   }
		   if (obj == null) {
		    return false;
		   }
		   if (!(obj instanceof Token)) {
		    return false;
		   }
		   Token other = (Token) obj;
		   if (!getOuterType().equals(other.getOuterType())) {
		    return false;
		   }
		   if (kind != other.kind) {
		    return false;
		   }
		   if (length != other.length) {
		    return false;
		   }
		   if (pos != other.pos) {
		    return false;
		   }
		   return true;
		  }

		 

		  private Scanner getOuterType() {
		   return Scanner.this;
		  }
		

	}

	Scanner(String chars) {
		//System.out.println("Scanner constructer called");
		this.chars = chars;
		tokens = new ArrayList<Token>();

	}

	/**
	* Initializes Scanner object by traversing chars and adding tokens to tokens list.
	* 
	* @return this scanner
	* @throws IllegalCharException
	* @throws IllegalNumberException
	*/
	public Scanner scan() throws IllegalCharException, IllegalNumberException {

		//TODO IMPLEMENT THIS!!!!
		
		int pos = 0;
		int length = chars.length();
		State state = State.START;
		int startPos = 0;
		int ch;
		newlineposarray.add(-1);
		//System.out.println("String length= "+length);
		while (pos <= length) {
			//System.out.println("---------------");
			//System.out.println("pos= " + pos);
			ch = pos < length ? chars.charAt(pos) : -1;
			//System.out.println("ch= "+ (char)ch);
			//System.out.println("state= "+ state);
			switch (state) {
			case START: {
					pos = pos < length ? skipWhiteSpace(pos) : pos;
					//System.out.println("pos after skipWhiteSpace= " + pos);
					//pos = skipWhiteSpace(pos);
					ch = pos < length ? chars.charAt(pos) : -1;
					startPos = pos;
					switch (ch) {
					case -1: {
							//System.out.println("got '-1'");
							tokens.add(new Token(Kind.EOF, pos, 0)); 
							pos++;
						}  break;
					case '/': {
							//System.out.println("got '/'");
							
							state = State.AFTER_DIV;
							pos++;

						} break;
					case '+': {
							//System.out.println("got '+'");
							tokens.add(new Token(Kind.PLUS, startPos, 1));
							pos++;
						} break;
					case '*': {
							//System.out.println("got '*'");
							tokens.add(new Token(Kind.TIMES, startPos, 1));
							pos++;
						} break;
					case '-': {
							//System.out.println("got '-'");
							//tokens.add(new Token(Kind.MINUS, startPos, 1));
							state = State.AFTER_MINUS;
							pos++;
						} break;
					case ';': {
							//System.out.println("got ';'");
							tokens.add(new Token(Kind.SEMI, startPos, 1));
							pos++;
						} break;
					case ',': {
							//System.out.println("got ','");
							tokens.add(new Token(Kind.COMMA, startPos, 1));
							pos++;
						} break;
					case '(': {
							//System.out.println("got '('");
							tokens.add(new Token(Kind.LPAREN, startPos, 1));
							pos++;
						} break;
					case ')': {
							//System.out.println("got ')'");
							tokens.add(new Token(Kind.RPAREN, startPos, 1));
							pos++;
						} break;
					case '{': {
							//System.out.println("got '{'");
							tokens.add(new Token(Kind.LBRACE, startPos, 1));
							pos++;
						} break;
					case '}': {
							//System.out.println("got '}'");
							tokens.add(new Token(Kind.RBRACE, startPos, 1));
							pos++;
						} break;
					case '|': {
							//System.out.println("got '|'");
							//tokens.add(new Token(Kind.OR, startPos, 1));
							state = State.AFTER_OR;
							pos++;
						} break;
					case '&': {
							//System.out.println("got '&'");
							tokens.add(new Token(Kind.AND, startPos, 1));
							pos++;
						} break;
					case '<': {
							//System.out.println("got '<'");
							//tokens.add(new Token(Kind.LT, startPos, 1));
							state = State.AFTER_LT;
							pos++;
						} break;
					case '>': {
							//System.out.println("got '>'");
							//tokens.add(new Token(Kind.GT, startPos, 1));
							state = State.AFTER_GT;
							pos++;
						} break;
					case '%': {
							//System.out.println("got '%'");
							tokens.add(new Token(Kind.MOD, startPos, 1));
							pos++;
						} break;
					case '=': {
							//System.out.println("got '='");
							state = State.AFTER_EQ;
							pos++;
						}break;
					case '!': {
							//System.out.println("got '!'");
							state = State.AFTER_NOT;
							pos++;
						}break;
					case '0': {
							//System.out.println("got '0'");
							tokens.add(new Token(Kind.INT_LIT,startPos, 1));
							pos++;
						}break;
					default: {
							if (Character.isDigit(ch)) {
								state = State.IN_DIGIT;
								pos++;
							} 
							else if (Character.isJavaIdentifierStart(ch)) {
								state = State.IN_IDENT;
								pos++;
							} 
							else {throw new IllegalCharException("illegal char " +(char)ch+" at pos "+pos);
							}
						}
					} // switch (ch)
				}  break;
				
			case IN_DIGIT: {
					if (Character.isDigit(ch)) {
						pos++;
					} else {
						Token tok = new Token(Kind.INT_LIT, startPos, pos - startPos);
						tokens.add(tok);

						try{
							Integer.parseInt(tok.getText());
						}
						catch(NumberFormatException Ex){
							throw new IllegalNumberException("illegal number " +tok.getText()+" at pos "+startPos);
						}
						state = State.START;
					}
				}  break;
			case IN_IDENT: {
					if (Character.isJavaIdentifierPart(ch)) {
						pos++;
					} else {
						Kind kd = Kind.getKindFromText(chars.substring(startPos, pos));
						//System.out.println("identifier= "+chars.substring(startPos, pos));
						if(kd != null){
							tokens.add(new Token(kd, startPos, pos - startPos));
						}
						else{
							//System.out.println("getKindFromText returned null ");
							tokens.add(new Token(Kind.IDENT, startPos, pos - startPos));
						}
						state = State.START;
					}
				}  break;
			case AFTER_EQ: {
					if(ch == '='){
						tokens.add(new Token(Kind.EQUAL, startPos, 2));
						state = State.START;
						pos++;
					}
					else{
						throw new IllegalCharException("illegal char " +(char)ch+" at pos "+pos);
					}
				}  break;
			case AFTER_NOT: {
					if(ch == '='){
						tokens.add(new Token(Kind.NOTEQUAL, startPos, 2));
						state = State.START;
						pos++;
					}
					else{
						tokens.add(new Token(Kind.NOT, startPos, 1));
						state = State.START;
					}
				}  break;
			case AFTER_LT: {
					if(ch == '='){
						tokens.add(new Token(Kind.LE, startPos, 2));
						state = State.START;
						pos++;
					}
					else if(ch == '-'){
						tokens.add(new Token(Kind.ASSIGN, startPos, 2));
						state = State.START;
						pos++;
					}
					else{
						tokens.add(new Token(Kind.LT, startPos, 1));
						state = State.START;
					}
				}  break;
			case AFTER_GT: {
					if(ch == '='){
						tokens.add(new Token(Kind.GE, startPos, 2));
						state = State.START;
						pos++;
					}
					else{
						tokens.add(new Token(Kind.GT, startPos, 1));
						state = State.START;
					}
				}  break;
			case AFTER_MINUS: {
					if(ch == '>'){
						tokens.add(new Token(Kind.ARROW, startPos, 2));
						state = State.START;
						pos++;
					}
					else{
						tokens.add(new Token(Kind.MINUS, startPos, 1));
						state = State.START;
					}
				}  break;
			case AFTER_OR: {
					if(ch == '-'){
						//tokens.add(new Token(Kind.ARROW, startPos, 2));
						state = State.AFTER_OR_MINUS;
						pos++;
					}
					else{
						tokens.add(new Token(Kind.OR, startPos, 1));
						state = State.START;
					}
				}  break;
			case AFTER_OR_MINUS: {
					if(ch == '>'){
						tokens.add(new Token(Kind.BARARROW, startPos, 3));
						state = State.START;
						pos++;
					}
					else{
						tokens.add(new Token(Kind.OR, startPos, 1));
						tokens.add(new Token(Kind.MINUS, startPos+1, 1));
						state = State.START;
					}
				}  break;
			case AFTER_DIV: {
					if(ch == '*'){
						state = State.IN_COMMENT;
						pos++;
					}
					else{
						tokens.add(new Token(Kind.DIV, startPos, 1));
						state = State.START;
					}
				}  break;
			case IN_COMMENT: {
					if(ch=='*'){
						state = State.IN_COMMENT_STAR;
						pos++;
					}
					else if(ch==-1){
						tokens.add(new Token(Kind.EOF, pos, 0));	//might not be necessary
						//throw new IllegalCharException("Unclosed comment at end of file");
						state = State.START;
						pos++;
					}
					else{
						//pos = skipWhiteSpace(pos);	//****check
						if(ch == '\n'){
							newlineposarray.add(pos);
						}
						pos++;
					}
					
				} break;
			case IN_COMMENT_STAR: {
					if(ch=='/'){
						state = State.START;						
						pos++;
					}
					else if(ch==-1){
						tokens.add(new Token(Kind.EOF, pos, 0));	//might not be necessary
						//throw new IllegalCharException("Unclosed comment at end of file");
						state = State.START;
						pos++;
					}
					else if(ch=='*'){
						state = State.IN_COMMENT_STAR;
						pos++;
					}
					else{
						state = State.IN_COMMENT;
						pos++;
					}
					
				} break;
			default:  assert false;
			}// switch(state)
		} // while

		
		//tokens.add(new Token(Kind.EOF,pos,0));
		return this;  
	}

	private int skipWhiteSpace(int pos) {
		while( Character.isWhitespace(chars.charAt(pos))){
			//System.out.println("got whitespace");			
			if(chars.charAt(pos) == '\n'){
				newlineposarray.add(pos);
			}
			pos++;
			if(pos==chars.length())	break;
		}		
		return pos;
	}

	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum;

	/*
	* Return the next token in the token list and update the state so that the
	* next call will return the Token..
	*/
	public Token nextToken() {
		if (tokenNum >= tokens.size())
		return null;
		return tokens.get(tokenNum++);
	}

	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */
	public Token peek() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);
	}

	/**
	* Returns a LinePos object containing the line and position in line of the
	* given token.
	* 
	* Line numbers start counting at 0
	* 
	* @param t
	* @return
	*/
	public LinePos getLinePos(Token t) {
		// TODO IMPLEMENT THIS
		return t.getLinePos();
		//return null;
	}

}
