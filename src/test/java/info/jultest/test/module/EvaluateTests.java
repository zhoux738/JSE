package info.jultest.test.module;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.Commons.validateUndefinedValue;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.binding.IntegerBinding;
import info.julang.external.binding.ObjectBinding;
import info.julang.external.binding.StringBinding;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.interfaces.IExtValue.IBoolVal;
import info.julang.external.interfaces.ResetPolicy;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.modulesystem.ModuleInfo;
import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestRunner;
import info.jultest.test.ExceptionTestsBase;
import info.jultest.test.TestExceptionHandler;
import info.jultest.test.hosted.classes.Vehicle;

/**
 * Environment.evaluate()
 */
public class EvaluateTests extends ExceptionTestsBase {
	
	private static final String FEATURE = "Evaluate";

	// evaluate and return
	@Test
	public void baselineEvaluateTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test01/main.jul"));
		
		validateIntValue(gvt, "i5", 5);
	}
	
	// passing arguments
	@Test
	public void passArgumentsTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test02/main.jul"));
		
		validateStringValue(gvt, "s_abc", "abc");
		validateStringValue(gvt, "s_xyz", "xyz");
		validateStringValue(gvt, "s_empty1", "");
		validateStringValue(gvt, "s_empty2", "");
	}
	
	// resolve against default module path
	@Test
	public void defaultModulePathTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test03/main.jul"));
		
		validateStringValue(gvt, "s_a", "a");
		validateStringValue(gvt, "s_b", "b");
	}
	
	// shared globals: new and updated
	// isolated globals
	@Test
	public void globalsTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test04/main.jul"));
		
		validateBoolValue(gvt, "b1", true);
		validateIntValue(gvt, "i5", 5);
		validateIntValue(gvt, "i10", 10);
		validateIntValue(gvt, "i23", 23);
		validateIntValue(gvt, "i47", 47);
	}
	
	// throw exception
	@Test
	public void throwExceptionTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.SCRIPTING, FEATURE, true);
		
		runner.setArguments("defs.jul");
		runner.executeAndExpect("Test05/main.jul", KnownJSException.DivByZero, null, 
			"defs.jul, 1", "from", "main.jul, 2");
		
		runner.setArguments("defs2.jul");
		runner.executeAndExpect("Test05/main.jul", KnownJSException.DivByZero, null, 
			"defs2.jul, 2", "defs2.jul, 5", "from", "main.jul, 2");
		
		runner.getEngine().reset(ResetPolicy.USER_DEFINED_ONLY);
		runner.setArguments("defs3.jul");
		runner.executeAndExpect("Test05/main.jul", KnownJSException.DivByZero, null, 
			"defs2.jul, 2", "defs2.jul, 5", "defs3.jul, 3", "from", "main.jul, 2");
	}
	
	// return exception
	@Test
	public void returnExceptionTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test06/main.jul"));
		
		validateStringValue(gvt, "finde", "Versagen");
	}
	
	// never use cache
	@Test
	public void noCacheTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test07/main.jul"));
		
		validateIntValue(gvt, "v1", 1);
		validateIntValue(gvt, "v2", 2);
		validateIntValue(gvt, "v3", 3);
		validateIntValue(gvt, "v103", 103);
	}
	
	// shared types (define new types)
	@Test
	public void sharedTypesTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test08/main.jul"));
		
		validateIntValue(gvt, "i1", 1);
		validateIntValue(gvt, "i2", 2);
		validateStringValue(gvt, "mc5_updated", "mc5_updated");
	}
	
	// shared types (redefine types)
	@Test
	public void redefineTypesTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.SCRIPTING, FEATURE);
		
		runner.setArguments("defs.jul");
		runner.executeAndExpect("Test09/main.jul", KnownJSException.DuplicateSymbol, null, 
			"is defined more than once in module", ModuleInfo.DEFAULT_MODULE_NAME, "routines.jul", "main.jul, 5");
	}
	
	// namespace isolation. ns introduced by a callee script shall not make its way into the caller's context.
	@Test
	public void nsIsolationTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test10/main.jul"));
		
		validateBoolValue(gvt, "succ", true);
		validateStringValue(gvt, "mc5", "mc5");
	}
	
	// namespace isolation. ns introduced by a caller script shall not make its way into the callee's context.
	@Test
	public void nsIsolationTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test10/main2.jul"));
		
		validateBoolValue(gvt, "succ", true);
		validateStringValue(gvt, "mc5", "mc5");
		validateStringValue(gvt, "mc5b", "mc5");
	}
	
	// illegal: call from function in any form
	@Test
	public void illegalCallingContextTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test11/main.jul"));
		
		validateBoolValue(gvt, "s1", true);
		validateBoolValue(gvt, "s2", true);
		validateBoolValue(gvt, "s3", true);
		validateBoolValue(gvt, "s4", true);
	}
	
	// illegal: call from other threads
	@Test
	public void illegalCallingThreadTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test12/main.jul"));
		
		validateBoolValue(gvt, "s1", true);
		validateBoolValue(gvt, "s2", true);
		validateBoolValue(gvt, "s3", true);
	}
	
	// illegal: redefine function
	@Test
	public void redefineFunctionTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.SCRIPTING, FEATURE, true);
		
		runner.executeAndExpect("Test13/main.jul", KnownJSException.DuplicateSymbol, null, 
			"func", "main.jul, 5");
		
		runner.getEngine().reset(ResetPolicy.USER_DEFINED_ONLY);
		runner.executeAndExpect("Test13/main2.jul", KnownJSException.DuplicateSymbol, null, 
			"func", "defs1.jul, 1");
	}
	
	// vars not in the outermost scope are not shared
	@Test
	public void notSharedTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.SCRIPTING, FEATURE, true);
		
		runner.executeAndExpect("Test14/main.jul", KnownJSException.UndefinedSymbol, null, "i50");
		validateIntValue((VariableTable)runner.getEngine().getRuntime().getGlobalVariableTable(), "i40", 40);

		runner.getEngine().reset(ResetPolicy.USER_DEFINED_ONLY);
		runner.executeAndExpect("Test14/main2.jul", KnownJSException.UndefinedSymbol, null, "i50");
		validateIntValue((VariableTable)runner.getEngine().getRuntime().getGlobalVariableTable(), "i40", 40);
	}
	
	// illegal: evaluate module files
	@Test
	public void evaluateModuleFileTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.SCRIPTING, FEATURE, true);

		runner.setArguments("MyMod");
		runner.executeAndExpect("Test15/main.jul", KnownJSException.IllegalModule, null, "must not declare a module name");
		
		runner.setArguments("UrMod");
		runner.executeAndExpect("Test15/main.jul", KnownJSException.IllegalModule, null, "must not declare a module name");
	}
	
	// evaluate chain
	@Test
	public void evalChainTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt, true);

		// main -> s1 -> s2 -> s3, pass along arguments, do not share global scope
		int val = 5;
		engine.getContext().setArguments(new String[] { String.valueOf(val) } );
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test16/main.jul"));
		validateIntValue(gvt, "v", val);
		
		// main -> s1 -> s2, s1 and s2 share global scope, main won't see new defs from s1 and s2
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test16/main2.jul"));
		validateBoolValue(gvt, "succ", true);
		validateUndefinedValue(gvt, "i");

		// main -> s1 -> s2, main and s1 share global scope, s1 and s2 won't see defs from each other
		engine.reset(ResetPolicy.USER_DEFINED_ONLY);
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test16/main3.jul"));
		validateIntValue(gvt, "i", 21);
		
		// main -> s1 -> s2, s2 throws, propagates back to main.
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test16/main4.jul"));
		validateBoolValue(gvt, "succ", true);	
	}
	
	// illegal: circular evaluate
	@Test
	public void circularEvalTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.SCRIPTING, FEATURE, true);
	
		/*
		 * System.Lang.CyclicDependencyException: The following scripts form a cyclic dependency with each other: 
		 * /Users/patrick/Development/source/jse/src/test/julian/Interpret/Scripting/Evaluate/Test17/script1.jul,
		 * /Users/patrick/Development/source/jse/src/test/julian/Interpret/Scripting/Evaluate/Test17/script2.jul,
		 * /Users/patrick/Development/source/jse/src/test/julian/Interpret/Scripting/Evaluate/Test17/script1.jul
		 */
		runner.executeAndExpect(
			"Test17/main_2_without_main.jul", KnownJSException.CyclicDependency, null, 
			"script1.jul", "script2.jul", "script1.jul");

		runner.executeAndExpect(
			"Test17/main_3_with_main.jul", KnownJSException.CyclicDependency, null, 
			"in the order of inclusion", "main_3_with_main.jul", "script3a.jul", "script3b.jul", "main_3_with_main.jul");
		
		runner.executeAndExpect(
			"Test17/main_with_main.jul", KnownJSException.CyclicDependency, null, 
			"in the order of inclusion", "main_with_main.jul", "main_with_main.jul", "main_with_main.jul, 2");
	}
	
	// Thread.getCurrent()
	@Test
	public void apiTest_Thread_getCurrent() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt, true);

		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test18/main.jul"));
		validateBoolValue(gvt, "succ", true);
		
		engine.reset(ResetPolicy.FULL);
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test18/main2.jul"));
		gvt = (VariableTable) engine.getRuntime().getGlobalVariableTable();
		validateBoolValue(gvt, "succ2", true);
	}
	
	// Environment.getScript()
	@Test
	public void apiTest_Environment_getScript() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt, true);

		// get scripts from module (sub -> main)
		assertSuccess(engine, "Test19/main.jul");

		// get scripts from module (main -> sub -> main)
		assertSuccess(engine, "Test19/main2.jul");

		// types dynamically added
		assertSuccess(engine, "Test19/main3.jul");
	}
	
	// bindings
	@Test
	public void bindingTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt, false);
		engine.getContext().addBinding("i", new IntegerBinding(4));
		engine.getContext().addBinding("s", new StringBinding("abc"));
		Vehicle v = new Vehicle();
		v.setSpeed(10);
		engine.getContext().addBinding("o", new ObjectBinding(v));
		
		assertSuccess(engine, "Test20/main.jul");
		
		Assert.assertEquals(5, ((IntegerBinding)engine.getContext().getBinding("i")).getValue());
		Assert.assertEquals("def", ((StringBinding)engine.getContext().getBinding("s")).getValue());
		Assert.assertEquals(20, v.getSpeed());
	}
	
	// load script with ill-syntax
	@Test
	public void invalidScript() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt, true);

		// cannot parse
		assertSuccess(engine, "Test21/main.jul");
		
		// same as running the script directly
		TestExceptionHandler handler = new TestExceptionHandler();
		engine.setExceptionHandler(handler);
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test21/s1.jul"));
		
		String st = handler.getStandardExceptionOutput();
		Assert.assertTrue(st.contains("Encountered a syntax error during parsing"));
		Assert.assertTrue(st.contains("s1.jul, 3"));
	}
	
	// return void
	@Test
	public void returnVoidScript() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt, false);
		assertSuccess(engine, "Test22/main.jul");
	}
	
	private void assertSuccess(SimpleScriptEngine engine, String script) throws EngineInvocationError {
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, script));
		assertTrue(((IBoolVal)engine.getResult().getReturnedValue(false)).getBoolValue());
		engine.reset(ResetPolicy.USER_DEFINED_ONLY);
	}
}
