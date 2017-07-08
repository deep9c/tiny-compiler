package cop5556sp17;

//import java.io.PrintWriter;
//import java.lang.reflect.Constructor;
//import java.lang.reflect.InvocationTargetException;
//import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

//import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
//import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
//import org.objectweb.asm.util.TraceClassVisitor;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
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
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	private int slot=1;			//added for HW5
	private int iterator=0;		//added for HW5
	
	MethodVisitor mv; 

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	int bararrowcheck = 0 ;		//HW7
	Label startLabel;		//HW6
	Label endLabel;			//HW6

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		ArrayList<ParamDec> params = program.getParams();
		for (ParamDec dec : params){
			dec.visit(this, mv);
			iterator++;
		}
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
		
//TODO  visit the local variables
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, startRun, endRun, 1);
		//extra line
		
		
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method
		
		
		cw.visitEnd();//end of class
		
		//generate classfile and return it
		return cw.toByteArray();
	}



	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		if(assignStatement.getVar().getDec() instanceof ParamDec){
			mv.visitVarInsn(ALOAD, 0);
		}
		
		assignStatement.getE().visit(this, arg);
		
		/*if(assignStatement.getE().getType().equals(TypeName.IMAGE))
		{
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage", PLPRuntimeImageOps.copyImageSig, false);
		}*/	//hw7
		
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getType());
		assignStatement.getVar().visit(this, arg);
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
				
		if(binaryChain.getFirstToken().isKind(OP_GRAY)){
			if(binaryChain.getArrow().isKind(BARARROW)){
				bararrowcheck=1;
			}
			else{
				bararrowcheck=0;
			}
		}
		
		
		binaryChain.getE0().visit(this, 1);
		binaryChain.getE1().visit(this, 2);
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		
		//CodeGenUtils.genPrint(DEVEL, mv, "\nentering binary exp");

		Label l0 = new Label();
		Label l1 = new Label();

		TypeName t0 = binaryExpression.getE0().getType();
		TypeName t1 = binaryExpression.getE1().getType();
		
		if (t1.equals(TypeName.IMAGE) && t0.equals(TypeName.INTEGER)) {
			binaryExpression.getE1().visit(this, arg);
			binaryExpression.getE0().visit(this, arg);
		} else {
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
		}
		
		
		if(binaryExpression.getOp().isKind(PLUS)){
			if((t0.equals(TypeName.INTEGER) && t1.equals(TypeName.INTEGER)) ){
				mv.visitInsn(IADD);
			}
			else if(t0.equals(TypeName.IMAGE) && t1.equals(TypeName.IMAGE)){
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "add", PLPRuntimeImageOps.addSig, false);
			}
		}
		else if(binaryExpression.getOp().isKind(MINUS)){
			if((t0.equals(TypeName.INTEGER) && t1.equals(TypeName.INTEGER)) ){
				mv.visitInsn(ISUB);
			}
			else if(t0.equals(TypeName.IMAGE) && t1.equals(TypeName.IMAGE)){
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "sub", PLPRuntimeImageOps.subSig, false);
			}
		}
		else if(binaryExpression.getOp().isKind(EQUAL)){
			
			if((t0.equals(TypeName.INTEGER) || t0.equals(TypeName.BOOLEAN)) ){
				mv.visitJumpInsn(IF_ICMPEQ, l0);
				mv.visitLdcInsn(false);
			}
			else{
				mv.visitJumpInsn(IF_ACMPEQ, l0);
				mv.visitLdcInsn(false);
			}
			
		}
		else if(binaryExpression.getOp().isKind(NOTEQUAL)){
			if((t0.equals(TypeName.INTEGER) || t0.equals(TypeName.BOOLEAN)) ){
				mv.visitJumpInsn(IF_ICMPNE, l0);
				mv.visitLdcInsn(false);
			}
			else{
				mv.visitJumpInsn(IF_ACMPNE, l0);
				mv.visitLdcInsn(false);
			}
		}
		else if(binaryExpression.getOp().isKind(LT)){
			mv.visitJumpInsn(IF_ICMPLT, l0);
			mv.visitLdcInsn(false);
		}
		else if(binaryExpression.getOp().isKind(GT)){
			mv.visitJumpInsn(IF_ICMPGT, l0);
			mv.visitLdcInsn(false);
		}
		else if(binaryExpression.getOp().isKind(GE)){
			mv.visitJumpInsn(IF_ICMPGE, l0);
			mv.visitLdcInsn(false);
		}
		else if(binaryExpression.getOp().isKind(LE)){
			mv.visitJumpInsn(IF_ICMPLE, l0);
			mv.visitLdcInsn(false);
		}
		else if(binaryExpression.getOp().isKind(AND)){
			mv.visitInsn(IAND);
		}
		else if(binaryExpression.getOp().isKind(OR)){
			mv.visitInsn(IOR);
		}
		else if(binaryExpression.getOp().isKind(TIMES)){
			if (t0.equals(TypeName.IMAGE)||t1.equals(TypeName.IMAGE)) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul",
						PLPRuntimeImageOps.scaleSig,
						false);
			} else {
				mv.visitInsn(IMUL);
			}
		}
		else if(binaryExpression.getOp().isKind(DIV)){
			if((t0.equals(TypeName.INTEGER) && t1.equals(TypeName.INTEGER)) ){
				mv.visitInsn(IDIV);
			}
			else if( (t0.equals(TypeName.IMAGE) && t1.equals(TypeName.INTEGER))){
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "div", PLPRuntimeImageOps.divSig, false);
			}
		}
		else if(binaryExpression.getOp().isKind(MOD)){
			if((t0.equals(TypeName.INTEGER) && t1.equals(TypeName.INTEGER)) ){
				mv.visitInsn(IREM);
			}
			else if( (t0.equals(TypeName.IMAGE) && t1.equals(TypeName.INTEGER))){
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mod", PLPRuntimeImageOps.modSig, false);
			}
		}
		
		
		mv.visitJumpInsn(GOTO, l1);

		mv.visitLabel(l0);
		mv.visitLdcInsn(true);

		mv.visitLabel(l1);
				
		return null;
	}

	
	
	
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		Label l0 = new Label();

		for(Dec dec: block.getDecs()){
			
			if(dec.getFirstToken().isKind(KW_FRAME) || dec.getFirstToken().isKind(KW_FRAME)){
				mv.visitInsn(ACONST_NULL);
				mv.visitVarInsn(ASTORE, slot);
			}
			
			dec.setSlot(slot);
			slot++;
		}
		startLabel = l0;
		mv.visitLabel(l0);
		for(Statement stmt:block.getStatements()){
			stmt.visit(this, arg);
			if(stmt instanceof BinaryChain){
				mv.visitInsn(POP);
			}
		}
		Label l1 = new Label();
		endLabel = l1;
		mv.visitLabel(l1);

		ArrayList<Dec> listDec = block.getDecs();
		int i=0, size=listDec.size();
		
		while(i<size){
			Dec dec = listDec.get(i);
			dec.visit(this, mv);
			i++;
		}
		
		
		return null;		
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		mv.visitLdcInsn(booleanLitExpression.getValue());
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		//HW6
		if(constantExpression.getFirstToken().isKind(KW_SCREENWIDTH)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenWidth", PLPRuntimeFrame.getScreenWidthSig,false);
		} else if(constantExpression.getFirstToken().isKind(KW_SCREENHEIGHT)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenHeight", PLPRuntimeFrame.getScreenHeightSig,false);
		}
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {		
		mv.visitLocalVariable(declaration.getIdent().getText(),
				 Type.getTypeName(declaration.getType()).getJVMTypeDesc(),
				 	null, startLabel, endLabel, declaration.getSlot());
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		//mv.visitInsn(DUP);		//HW7
		
		if(filterOpChain.getFirstToken().isKind(OP_BLUR)){
			mv.visitInsn(ACONST_NULL);		//HW7
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "blurOp", PLPRuntimeFilterOps.opSig, false);
		}
		else if(filterOpChain.getFirstToken().isKind(OP_GRAY)){
			if(bararrowcheck==1){	//HW7
				mv.visitInsn(DUP);
			}
			else{
				mv.visitInsn(ACONST_NULL);		//HW7
			}
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "grayOp", PLPRuntimeFilterOps.opSig, false);
		}
		else if(filterOpChain.getFirstToken().isKind(OP_CONVOLVE)){
			mv.visitInsn(ACONST_NULL);		//HW7
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "convolveOp", PLPRuntimeFilterOps.opSig,false);
		}

		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {

		frameOpChain.getArg().visit(this, arg);
				
		if(frameOpChain.getFirstToken().isKind(KW_XLOC)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getXVal", PLPRuntimeFrame.getXValDesc, false);
		}
		else if(frameOpChain.getFirstToken().isKind(KW_HIDE)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "hideImage", PLPRuntimeFrame.hideImageDesc,false);
		}
		else if(frameOpChain.getFirstToken().isKind(KW_YLOC)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getYVal", PLPRuntimeFrame.getYValDesc,false);
		}
		else if(frameOpChain.getFirstToken().isKind(KW_SHOW)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "showImage", PLPRuntimeFrame.showImageDesc,false);
		}
		else if(frameOpChain.getFirstToken().isKind(KW_MOVE)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "moveFrame", PLPRuntimeFrame.moveFrameDesc,false);
		}
		
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {		
		
		Integer part = (Integer) arg;
		TypeName type = identChain.getTypeName();

		if(part==1){
			if(identChain.getDec() instanceof ParamDec){
				mv.visitVarInsn(ALOAD, 0);

				mv.visitFieldInsn(GETFIELD, className, identChain.getFirstToken().getText(), identChain.getTypeName().getJVMTypeDesc());

				if(type.equals(TypeName.URL)){
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL", PLPRuntimeImageIO.readFromURLSig, false);
				}
				else if(type.equals(TypeName.FILE)){
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile", PLPRuntimeImageIO.readFromFileDesc, false);
				}

			} else {
				if (identChain.getTypeName().equals(TypeName.INTEGER) || identChain.getTypeName().equals(TypeName.BOOLEAN)) {
					mv.visitVarInsn(ILOAD, identChain.getDec().getSlot());
				} else {
					if(type.equals(TypeName.URL)){
						mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
						mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL", PLPRuntimeImageIO.readFromURLSig, false);
					}
					else if(type.equals(TypeName.FILE)){
						mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
						mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile", PLPRuntimeImageIO.readFromFileDesc, false);
					}
					else if(type.equals(TypeName.IMAGE) || type.equals(TypeName.FRAME)){
						mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
					}					
				}
			}
		}
		else{
			
			 if(identChain.getTypeName().equals(TypeName.FILE)){
				
				if(identChain.getDec() instanceof ParamDec){
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(), type.getJVMTypeDesc());
				}
				else {
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
				}

				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write",
						PLPRuntimeImageIO.writeImageDesc, false);
			}
			else if(identChain.getTypeName().equals(TypeName.FRAME)){
				mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame", PLPRuntimeFrame.createOrSetFrameSig, false);
				mv.visitInsn(DUP);
				mv.visitVarInsn(ASTORE, identChain.getDec().getSlot());
			}
			if(!identChain.getTypeName().equals(TypeName.FRAME)){	//HW7
				mv.visitInsn(DUP);
			}
			
			if (identChain.getTypeName().equals(TypeName.IMAGE) || identChain.getTypeName().equals(TypeName.INTEGER) || identChain.getTypeName().equals(TypeName.BOOLEAN)) {
				if (identChain.getDec() instanceof ParamDec) {
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD, className, identChain.getFirstToken().getText(),
							identChain.getTypeName().getJVMTypeDesc());
				} else {
					if (identChain.getTypeName().equals(TypeName.IMAGE)) {
						mv.visitVarInsn(ASTORE, identChain.getDec().getSlot());
					} else if (identChain.getTypeName().equals(TypeName.INTEGER) || identChain.getTypeName().equals(TypeName.BOOLEAN)) {
						mv.visitVarInsn(ISTORE, identChain.getDec().getSlot());
					}
				}
			}
		}
		return null;
		
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		if (identExpression.getDec() instanceof ParamDec) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, identExpression.getFirstToken().getText(), identExpression.getType().getJVMTypeDesc());
			
		} else{
			if (identExpression.getType().equals(TypeName.INTEGER)
					|| identExpression.getType().equals(TypeName.BOOLEAN)) {
						mv.visitVarInsn(ILOAD, identExpression.getDec().getSlot());
					}else if(identExpression.getType().equals(TypeName.IMAGE)
					||identExpression.getType().equals(TypeName.FRAME)
					||identExpression.getType().equals(TypeName.FILE)
					||identExpression.getType().equals(TypeName.URL)){
						mv.visitVarInsn(ALOAD, identExpression.getDec().getSlot());
					}
			
		}	
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		if(identX.getDec() instanceof ParamDec){

			mv.visitFieldInsn(PUTFIELD, className,
			 identX.getFirstToken().getText(),
			 identX.getDec().getTypeName().getJVMTypeDesc());
		} else {

			if(identX.getDec().getTypeName().equals(TypeName.IMAGE)){
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage", PLPRuntimeImageOps.copyImageSig, false);
				mv.visitVarInsn(ASTORE, identX.getDec().getSlot());
			}
			else if (identX.getDec().getTypeName().equals(TypeName.INTEGER)
					|| identX.getDec().getTypeName().equals(TypeName.BOOLEAN)) {
				mv.visitVarInsn(ISTORE, identX.getDec().getSlot());
			}
		}
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		Label lab = new Label();
		ifStatement.getE().visit(this, arg);
		
		mv.visitJumpInsn(IFEQ, lab);
		ifStatement.getB().visit(this, arg);
		mv.visitLabel(lab);		
		return null;
		
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {

		imageOpChain.getArg().visit(this, arg);
					
		if(imageOpChain.getFirstToken().isKind(OP_WIDTH)){	
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeImageIO.BufferedImageClassName, "getWidth", PLPRuntimeImageOps.getWidthSig, false);
		}
		else if(imageOpChain.getFirstToken().isKind(OP_HEIGHT)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeImageIO.BufferedImageClassName, "getHeight", PLPRuntimeImageOps.getWidthSig, false);
		}
		else if(imageOpChain.getFirstToken().isKind(KW_SCALE)){			
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale", PLPRuntimeImageOps.scaleSig, false);
		}
		
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		mv.visitLdcInsn(new Integer(intLitExpression.value));
		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {		
		cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), Type.getTypeName(paramDec.getType()).getJVMTypeDesc(), null, null);

		TypeName type  = Type.getTypeName(paramDec.getType());

		if(type.equals(TypeName.INTEGER)){
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(iterator);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
		}
		else if(type.equals(TypeName.BOOLEAN)){
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(iterator);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");
		}
		else if(type.equals(TypeName.FILE)){
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(iterator);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), type.getJVMTypeDesc());
		}
		else if(type.equals(TypeName.URL)){
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(iterator);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL", PLPRuntimeImageIO.getURLSig, false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), type.getJVMTypeDesc());
		}
		return null;				
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.getE().visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {	//HW6 
		
		List<Expression> exprList = tuple.getExprList();
		int i=0, size=exprList.size();
		
		while(i<size){
			Expression ex = exprList.get(i);
			ex.visit(this, arg);
			i++;
		}
		
		
		
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		Label l0 = new Label();
		Label l1 = new Label();
		Label l2 = new Label();
		mv.visitJumpInsn(GOTO, l2);
		mv.visitLabel(l0);
		whileStatement.getB().visit(this, arg);
		mv.visitLabel(l2);
		mv.visitLabel(l1);
		whileStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFNE, l0);
		return null;
	}

}
