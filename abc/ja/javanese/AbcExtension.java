/*
 * Javanese keywords.
 * Copyright (C) 2012-2014 Tetsuo Kamina
 */

package abc.ja.javanese;

import java.util.List;
import java.util.Collection;

import abc.aspectj.parse.AbcLexer;
import abc.aspectj.parse.LexerAction_c;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.MethodCategory;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.matching.NewStmtMethodPosition;
import abc.weaving.matching.StmtMethodPosition;
import abc.weaving.matching.TrapMethodPosition;
import abc.weaving.matching.WholeMethodPosition;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.Unit;
import soot.jimple.Stmt;
import soot.util.Chain;

import abc.ja.javanese.parse.JavaParser.Terminals;

public class AbcExtension extends abc.ja.AbcExtension {
    protected void collectVersions(StringBuffer versions) {
	super.collectVersions(versions);
	versions.append(" with Javanese " +
			new abc.ja.javanese.Version().toString() +
			"\n");
    }

    public void initLexerKeywords(AbcLexer lexer) {
	lexer.addGlobalKeyword("abstract", new LexerAction_c(new Integer(Terminals.ABSTRACT)));
	lexer.addGlobalKeyword("assert", new LexerAction_c(new Integer(Terminals.ASSERT)));
	lexer.addGlobalKeyword("boolean", new LexerAction_c(new Integer(Terminals.BOOLEAN)));
	lexer.addGlobalKeyword("break", new LexerAction_c(new Integer(Terminals.BREAK)));
	lexer.addGlobalKeyword("byte", new LexerAction_c(new Integer(Terminals.BYTE)));
	lexer.addGlobalKeyword("case", new LexerAction_c(new Integer(Terminals.CASE)));
	lexer.addGlobalKeyword("catch", new LexerAction_c(new Integer(Terminals.CATCH)));
	lexer.addGlobalKeyword("char", new LexerAction_c(new Integer(Terminals.CHAR)));
	lexer.addGlobalKeyword("class", new LexerAction_c(new Integer(Terminals.CLASS)) {
		public int getToken(AbcLexer lexer) {
		    if (!lexer.getLastTokenWasDot()) {
			lexer.enterLexerState(lexer.currentState() == lexer.aspectj_state() ?
					      lexer.aspectj_state() : lexer.java_state());
		    }
		    return token.intValue();
		}
	    });
	lexer.addGlobalKeyword("const", new LexerAction_c(new Integer(Terminals.EOF)));  // disallow 'const" keyword
	lexer.addGlobalKeyword("continue", new LexerAction_c(new Integer(Terminals.CONTINUE)));
	lexer.addGlobalKeyword("default", new LexerAction_c(new Integer(Terminals.DEFAULT)));
	lexer.addGlobalKeyword("do", new LexerAction_c(new Integer(Terminals.DO)));
	lexer.addGlobalKeyword("double", new LexerAction_c(new Integer(Terminals.DOUBLE)));
	lexer.addGlobalKeyword("else", new LexerAction_c(new Integer(Terminals.ELSE)));
	lexer.addGlobalKeyword("eventDecl", new LexerAction_c(new Integer(Terminals.EVENT)));
	lexer.addGlobalKeyword("extends", new LexerAction_c(new Integer(Terminals.EXTENDS)));
	lexer.addGlobalKeyword("final", new LexerAction_c(new Integer(Terminals.FINAL)));
	lexer.addGlobalKeyword("finally", new LexerAction_c(new Integer(Terminals.FINALLY)));
	lexer.addGlobalKeyword("float", new LexerAction_c(new Integer(Terminals.FLOAT)));
	lexer.addGlobalKeyword("for", new LexerAction_c(new Integer(Terminals.FOR)));
	lexer.addGlobalKeyword("goto", new LexerAction_c(new Integer(Terminals.EOF))); // disallow 'goto' keyword
	lexer.addGlobalKeyword("implements", new LexerAction_c(new Integer(Terminals.IMPLEMENTS)));
	lexer.addGlobalKeyword("import", new LexerAction_c(new Integer(Terminals.IMPORT)));
	lexer.addGlobalKeyword("instanceof", new LexerAction_c(new Integer(Terminals.INSTANCEOF)));
	lexer.addGlobalKeyword("int", new LexerAction_c(new Integer(Terminals.INT)));
	lexer.addGlobalKeyword("interface", new LexerAction_c(new Integer(Terminals.INTERFACE)));
	lexer.addGlobalKeyword("long", new LexerAction_c(new Integer(Terminals.LONG)));
	lexer.addGlobalKeyword("native", new LexerAction_c(new Integer(Terminals.NATIVE)));
	lexer.addGlobalKeyword("new", new LexerAction_c(new Integer(Terminals.NEW)));
	lexer.addGlobalKeyword("package", new LexerAction_c(new Integer(Terminals.PACKAGE)));
	lexer.addGlobalKeyword("private", new LexerAction_c(new Integer(Terminals.PRIVATE)));
	lexer.addGlobalKeyword("protected", new LexerAction_c(new Integer(Terminals.PROTECTED)));
	lexer.addGlobalKeyword("public", new LexerAction_c(new Integer(Terminals.PUBLIC)));
	lexer.addGlobalKeyword("return", new LexerAction_c(new Integer(Terminals.RETURN)));
	lexer.addGlobalKeyword("short", new LexerAction_c(new Integer(Terminals.SHORT)));
	lexer.addGlobalKeyword("static", new LexerAction_c(new Integer(Terminals.STATIC)));
	lexer.addGlobalKeyword("strictfp", new LexerAction_c(new Integer(Terminals.STRICTFP)));
	lexer.addGlobalKeyword("super", new LexerAction_c(new Integer(Terminals.SUPER)));
	lexer.addGlobalKeyword("switch", new LexerAction_c(new Integer(Terminals.SWITCH)));
	lexer.addGlobalKeyword("synchronized", new LexerAction_c(new Integer(Terminals.SYNCHRONIZED)));
	lexer.addGlobalKeyword("throw", new LexerAction_c(new Integer(Terminals.THROW)));
	lexer.addGlobalKeyword("throws", new LexerAction_c(new Integer(Terminals.THROWS)));
	lexer.addGlobalKeyword("transient", new LexerAction_c(new Integer(Terminals.TRANSIENT)));
	lexer.addGlobalKeyword("try", new LexerAction_c(new Integer(Terminals.TRY)));
	lexer.addGlobalKeyword("void", new LexerAction_c(new Integer(Terminals.VOID)));
	lexer.addGlobalKeyword("volatile", new LexerAction_c(new Integer(Terminals.VOLATILE)));
	lexer.addGlobalKeyword("while", new LexerAction_c(new Integer(Terminals.WHILE)));
	if (abc.main.Debug.v().java15) {
	    lexer.addJavaKeyword("enum", new LexerAction_c(new Integer(Terminals.ENUM)));
	}
	lexer.addGlobalKeyword("layer", new LexerAction_c(new Integer(Terminals.LAYER)));
	lexer.addGlobalKeyword("requires", new LexerAction_c(new Integer(Terminals.REQUIRES)));
	lexer.addGlobalKeyword("proceed", new LexerAction_c(new Integer(Terminals.PROCEED)));
	lexer.addGlobalKeyword("after", new LexerAction_c(new Integer(Terminals.AFTER)));
	lexer.addGlobalKeyword("before", new LexerAction_c(new Integer(Terminals.BEFORE)));
	lexer.addGlobalKeyword("activate", new LexerAction_c(new Integer(Terminals.ACTIVATE)));
	lexer.addGlobalKeyword("deactivate", new LexerAction_c(new Integer(Terminals.DEACTIVATE)));
	lexer.addGlobalKeyword("global", new LexerAction_c(new Integer(Terminals.GLOBAL)));
	lexer.addGlobalKeyword("perthread", new LexerAction_c(new Integer(Terminals.PERTHREAD)));


	/* pointcut keywords */
	lexer.addPointcutKeyword("adviceexecution", new LexerAction_c(new Integer(Terminals.EOF))); // disallow 'adviceexecution' keyword
	lexer.addPointcutKeyword("args", new LexerAction_c(new Integer(Terminals.PC_ARGS)));
	lexer.addPointcutKeyword("call", new LexerAction_c(new Integer(Terminals.PC_CALL)));
	lexer.addPointcutKeyword("cflow", new LexerAction_c(new Integer(Terminals.PC_CFLOW)));
	lexer.addPointcutKeyword("cflowbelow", new LexerAction_c(new Integer(Terminals.PC_CFLOWBELOW)));
	lexer.addPointcutKeyword("error", new LexerAction_c(new Integer(Terminals.PC_ERROR)));
	lexer.addPointcutKeyword("execution", new LexerAction_c(new Integer(Terminals.PC_EXECUTION)));
	lexer.addPointcutKeyword("get", new LexerAction_c(new Integer(Terminals.PC_GET)));
	lexer.addPointcutKeyword("handler", new LexerAction_c(new Integer(Terminals.PC_HANDLER)));
	lexer.addPointcutKeyword("if", new LexerAction_c(new Integer(Terminals.PC_IF), new Integer(lexer.pointcutifexpr_state())));
	lexer.addPointcutKeyword("initialization", new LexerAction_c(new Integer(Terminals.PC_INITIALIZATION)));
	lexer.addPointcutKeyword("parents", new LexerAction_c(new Integer(Terminals.PC_PARENTS)));
	lexer.addPointcutKeyword("precedence", new LexerAction_c(new Integer(Terminals.PC_PRECEDENCE)));
	lexer.addPointcutKeyword("preinitialization", new LexerAction_c(new Integer(Terminals.PC_PREINITIALIZATION)));
	lexer.addPointcutKeyword("returning", new LexerAction_c(new Integer(Terminals.PC_RETURNING)));
	lexer.addPointcutKeyword("set", new LexerAction_c(new Integer(Terminals.PC_SET)));
	lexer.addPointcutKeyword("soft", new LexerAction_c(new Integer(Terminals.PC_SOFT)));
	lexer.addPointcutKeyword("staticinitialization", new LexerAction_c(new Integer(Terminals.PC_STATICINITIALIZATION)));
	lexer.addPointcutKeyword("target", new LexerAction_c(new Integer(Terminals.PC_TARGET)));
	lexer.addPointcutKeyword("this", new LexerAction_c(new Integer(Terminals.PC_THIS)));
	lexer.addPointcutKeyword("throwing", new LexerAction_c(new Integer(Terminals.PC_THROWING)));
	lexer.addPointcutKeyword("warning", new LexerAction_c(new Integer(Terminals.PC_WARNING)));
	lexer.addPointcutKeyword("within", new LexerAction_c(new Integer(Terminals.PC_WITHIN)));
	lexer.addPointcutKeyword("withincode", new LexerAction_c(new Integer(Terminals.PC_WITHINCODE)));

	/*
	 * Javanese keywords are registered as AspectJ keyword so that
         * the contextual parsing of AspectJ is reused.
	 */
	lexer.addPointcutKeyword("monitor", new LexerAction_c(new Integer(Terminals.MONITOR)));

	/*
	 * Those keywords are added as pointcut keywords
	 */
	lexer.addPointcutKeyword("after", new LexerAction_c(new Integer(Terminals.AFTER)));
	lexer.addPointcutKeyword("before", new LexerAction_c(new Integer(Terminals.BEFORE)));
	//	lexer.addPointcutKeyword("active", new LexerAction_c(new Integer(Terminals.ACTIVE)));
	//	lexer.addPointcutKeyword("until", new LexerAction_c(new Integer(Terminals.UNTIL)));
	lexer.addPointcutKeyword("from", new LexerAction_c(new Integer(Terminals.FROM)));
	lexer.addPointcutKeyword("to", new LexerAction_c(new Integer(Terminals.TO)));
	lexer.addPointcutKeyword("when", new LexerAction_c(new Integer(Terminals.WHEN)));
	lexer.addPointcutKeyword("not", new LexerAction_c(new Integer(Terminals.PC_NOTJVN)));
	lexer.addPointcutKeyword("is", new LexerAction_c(new Integer(Terminals.IS)));
	lexer.addPointcutKeyword("in", new LexerAction_c(new Integer(Terminals.IN)));
	//        lexer.addPointcutKeyword("and", new LexerAction_c(new Integer(Terminals.PC_ANDJVN)));
	//	lexer.addPointcutKeyword("or", new LexerAction_c(new Integer(Terminals.PC_ORJVN)));

	lexer.addAspectJContextKeyword("pertarget", new LexerAction_c(new Integer(Terminals.PERTARGET), new Integer(lexer.pointcut_state())));
	lexer.addAspectJContextKeyword("perthis", new LexerAction_c(new Integer(Terminals.PERTHIS), new Integer(lexer.pointcut_state())));

	// Overloaded keywords - they mean different things in pointcuts,
	// hence have to be declared separately.
	lexer.addJavaKeyword("if", new LexerAction_c(new Integer(Terminals.IF)));
	lexer.addPointcutIfExprKeyword("if", new LexerAction_c(new Integer(Terminals.IF)));
	lexer.addJavaKeyword("this", new LexerAction_c(new Integer(Terminals.THIS)));
	lexer.addPointcutIfExprKeyword("this", new LexerAction_c(new Integer(Terminals.THIS)));
	// keywords added to the Java part.
	lexer.addAspectJKeyword("monitor", new LexerAction_c(new Integer(Terminals.MONITOR), new Integer(lexer.aspectj_state())));
	lexer.addPointcutIfExprKeyword("monitor", new LexerAction_c(new Integer(Terminals.MONITOR), new Integer(lexer.aspectj_state())));
	lexer.addAspectJKeyword("context", new LexerAction_c(new Integer(Terminals.CONTEXT), new Integer(lexer.pointcut_state())));
	lexer.addPointcutIfExprKeyword("context", new LexerAction_c(new Integer(Terminals.CONTEXT), new Integer(lexer.pointcut_state())));
	lexer.addAspectJKeyword("activate", new LexerAction_c(new Integer(Terminals.ACTIVATE), new Integer(lexer.pointcut_state())));
	lexer.addPointcutIfExprKeyword("activate", new LexerAction_c(new Integer(Terminals.ACTIVATE), new Integer(lexer.pointcut_state())));
	lexer.addAspectJKeyword("subscribe", new LexerAction_c(new Integer(Terminals.SUBSCRIBE), new Integer(lexer.pointcut_state())));
	lexer.addPointcutIfExprKeyword("subscribe", new LexerAction_c(new Integer(Terminals.SUBSCRIBE), new Integer(lexer.pointcut_state())));
	lexer.addAspectJKeyword("unsubscribe", new LexerAction_c(new Integer(Terminals.UNSUBSCRIBE), new Integer(lexer.pointcut_state())));
	lexer.addPointcutIfExprKeyword("unsubscribe", new LexerAction_c(new Integer(Terminals.UNSUBSCRIBE), new Integer(lexer.pointcut_state())));
	lexer.addAspectJKeyword("subscribers", new LexerAction_c(new Integer(Terminals.SUBSCRIBERS), new Integer(lexer.pointcut_state())));
	lexer.addPointcutIfExprKeyword("subscribers", new LexerAction_c(new Integer(Terminals.SUBSCRIBERS), new Integer(lexer.pointcut_state())));
	lexer.addAspectJKeyword("subscriberTypes", new LexerAction_c(new Integer(Terminals.SUBSCRIBERTYPES), new Integer(lexer.pointcut_state())));
	lexer.addPointcutIfExprKeyword("subscriberTypes", new LexerAction_c(new Integer(Terminals.SUBSCRIBERTYPES), new Integer(lexer.pointcut_state())));


	if (!abc.main.Debug.v().pureJava) {
	    lexer.addJavaKeyword("contextgroup", new LexerAction_c(new Integer(Terminals.CONTEXTGROUP), new Integer(lexer.aspectj_state())));
	    lexer.addJavaKeyword("context", new LexerAction_c(new Integer(Terminals.CONTEXT), new Integer(lexer.pointcut_state())));
	    lexer.addJavaKeyword("activate", new LexerAction_c(new Integer(Terminals.ACTIVATE), new Integer(lexer.pointcut_state())));
	}
    }

    @Override
    public CompileSequence createCompileSequence() {
	return new CompileSequence(this);
    }

}
