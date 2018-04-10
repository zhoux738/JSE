package info.jultest.test.interpret.datatype;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateStringValue;

import org.junit.Assume;
import org.junit.Test;

import info.julang.dev.GlobalSetting;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.jultest.test.Commons;
import info.jultest.test.TestExceptionHandler;
import info.jultest.test.oo.ExceptionTestRunner;
import info.jultest.test.oo.ExceptionTestsBase;

public class BuiltInIrregularUseTests extends ExceptionTestsBase {

	private static final String FEATURE = "DataType";

	// Cannot get access to a static member of built-in types
	@Test
	public void staticAccTest() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "builtin_misuse_01.jul"));
		
		validateBoolValue(gvt, "b0", true);
		validateBoolValue(gvt, "b1", true);	
		validateBoolValue(gvt, "b2", true);
		validateBoolValue(gvt, "b3", true);	
	}

	// Cannot new up built-in types
	@Test
	public void instantiationTest() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "builtin_misuse_02.jul"));
		
		validateBoolValue(gvt, "b0", true);
		validateBoolValue(gvt, "b1", true);	
		validateBoolValue(gvt, "b2", true);
		validateBoolValue(gvt, "b3", true);	
	}
	
	@Test
	public void fullNameTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "builtin_fullname.jul"));

		validateStringValue(gvt, "s", "a5");	
	}
	
	// void as local variable type, function or lambda's param type
	@Test
	public void illegalVoidTest1() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "illegal_void_01.jul"));
		validateBoolValue(gvt, "b0", true);
		validateBoolValue(gvt, "b1", true);	
		validateBoolValue(gvt, "b2", true);
		validateBoolValue(gvt, "b3", true);	
		
		assertException(teh, "System.Lang.RuntimeCheckException");
	}
	
	// void as field type
	@Test
	public void illegalVoidTest2() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.IMPERATIVE, FEATURE);
		runner.executeAndExpect(
			"illegal_void_02.jul", 
			KnownJSException.ClassLoading, 
			KnownJSException.BadSyntax, 
			"Cannot use void as parameter type");
	}

	// void as method type
	@Test
	public void illegalVoidTest3() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.IMPERATIVE, FEATURE);
		runner.executeAndExpect(
			"illegal_void_03.jul", 
			KnownJSException.ClassLoading, 
			KnownJSException.BadSyntax, 
			"Illegal member type for class field declaration: void");
	}

	// void as ctor type
	@Test
	public void illegalVoidTest4() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.IMPERATIVE, FEATURE);
		runner.executeAndExpect(
			"illegal_void_04.jul", 
			KnownJSException.ClassLoading, 
			KnownJSException.BadSyntax, 
			"Cannot use void as parameter type");
	}
}
