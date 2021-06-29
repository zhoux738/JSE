package info.jultest.test.interpret;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntValue;
import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestRunner;
import info.jultest.test.ExceptionTestsBase;
import info.jultest.test.TestExceptionHandler;
import info.julang.dev.GlobalSetting;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.interpretation.errorhandling.KnownJSException;

import org.junit.Assume;
import org.junit.Test;

public class DeclarationTests extends ExceptionTestsBase {

	private static final String FEATURE = "Declaration";
	
	@Test
	public void declareVarTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "var_01.jul"));
		
		validateIntValue(gvt, "a", 5);
		validateIntValue(gvt, "b", 7);
	}
	
	@Test
	public void useUndeclaredVarTest1() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "undef_var_01.jul"));
		
		assertException(teh, "System.Lang.UndefinedSymbolException");
	}
	
	@Test
	public void useUndeclaredVarTest2() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "undef_var_02.jul"));
		
		assertException(teh, "System.Lang.UndefinedSymbolException");
	}
	
	// use type name as variable
	@Test
	public void badDeclareTest1() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "var_bad_1.jul"));
		
		validateBoolValue(gvt, "b0", true);	
		validateBoolValue(gvt, "b1", true);
	}
	
	// use type name as function param
	@Test
	public void badDeclareTest2() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.IMPERATIVE, FEATURE);
		runner.executeAndExpect(
			"var_bad_2.jul", 
			KnownJSException.NamespaceConflict, 
			null,
			"Integer");
	}
	
	// use type name as method param
	@Test
	public void badDeclareTest3() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.IMPERATIVE, FEATURE);
		runner.executeAndExpect(
			"var_bad_3.jul", 
			KnownJSException.ClassLoading, 
			KnownJSException.NamespaceConflict, 
			"Namespace conflict: C (variable) and C (type)");
	}
	
	// use type name as lambda param
	@Test
	public void badDeclareTest4() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.IMPERATIVE, FEATURE);
		runner.executeAndExpect(
			"var_bad_4.jul", 
			KnownJSException.NamespaceConflict, 
			null,
			"Integer");
	}
}
