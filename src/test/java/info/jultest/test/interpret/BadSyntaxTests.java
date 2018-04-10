package info.jultest.test.interpret;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.jultest.test.Commons;
import info.jultest.test.TestExceptionHandler;
import info.jultest.test.oo.ExceptionTestsBase;

import org.junit.Test;

public class BadSyntaxTests extends ExceptionTestsBase {
	
	private static final String FEATURE = "BadSyntax";
	
	// expression
	@Test
	public void prematureTerminationTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "term_01.jul"));
		
		assertException(teh, "System.Lang.BadSyntaxException");
	}
	
	// function definition
	@Test
	public void prematureTerminationTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "term_02.jul"));
		
		assertException(teh, "System.Lang.BadSyntaxException");
	}
	
	@Test
	public void prematureTerminationTest3() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "term_03.jul"));
		
		// validateBoolArrayValue(gvt, "bs", new boolean[]{true, true, true, true, true});
		assertException(teh, "System.Lang.BadSyntaxException");
	}
	
	@Test
	public void prematureTerminationTest4() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "term_04.jul"));
		
		//validateBoolArrayValue(gvt, "bs", new boolean[]{true, true, true, true, true});
		assertException(teh, "System.Lang.BadSyntaxException");
	}
	
	@Test
	public void prematureTerminationTest5() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "term_05.jul"));
		
		//validateBoolArrayValue(gvt, "bs", new boolean[]{true, true, true, true, true, true, true, true, true, true});
		assertException(teh, "System.Lang.BadSyntaxException");
	}

}
