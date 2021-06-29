package info.jultest.test.module;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.binding.IntegerBinding;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.interfaces.IExtValue.IBoolVal;
import info.julang.external.interfaces.ResetPolicy;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.modulesystem.ModuleInfo;
import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestRunner;
import info.jultest.test.ExceptionTestsBase;
import info.jultest.test.TestExceptionHandler;
import info.jultest.test.TestIO;

/**
 * include "path/to/script";
 * 
 * include module "path/to/script";
 * include system "stdio";
 * include script "path/to/another";
 */
public class IncludeTests extends ExceptionTestsBase {
	
	private static final String FEATURE = "Include";

	// include a function
	@Test
	public void baselineIncludeTest() throws EngineInvocationError {
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
		engine.getContext().setArguments(new String[] {"abc"});
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test02/main.jul"));

		validateStringValue(gvt, "s_abc", "abc");
	}
	
	// throw exception
	@Test
	public void throwExceptionTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.SCRIPTING, FEATURE, true);
		
		String script = "main.jul";
		runner.executeAndExpect("Test05/main.jul", KnownJSException.DivByZero, null, 
			"defs.jul, 3", "from", script + ", 2");
		
		script = "main2.jul";
		runner.executeAndExpect("Test05/main2.jul", KnownJSException.DivByZero, null, 
			"defs2.jul, 2", "defs2.jul, 5", "from", script + ", 2");

		script = "main3.jul";
		runner.getEngine().reset(ResetPolicy.USER_DEFINED_ONLY);
		runner.executeAndExpect("Test05/main3.jul", KnownJSException.DivByZero, null, 
			"defs2.jul, 2", "defs2.jul, 5", "defs3.jul, 3", "from", script + ", 2");
	}

	// always use cache
	@Test
	public void alwaysCacheTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test07/main.jul"));
		
		validateIntValue(gvt, "i5", 5);
		validateIntValue(gvt, "i7", 7);
	}

	// shared types (define new types)
	@Test
	public void sharedTypesTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test08/main.jul"));
		
		validateIntValue(gvt, "i2", 2);
		validateStringValue(gvt, "mc5_updated", "mc5_updated");
	}

	// shared types (redefine types)
	@Test
	public void redefineTypesTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.SCRIPTING, FEATURE);
		
		runner.executeAndExpect("Test09/main.jul", KnownJSException.DuplicateSymbol, null, 
			"is defined more than once in module", ModuleInfo.DEFAULT_MODULE_NAME, "routines2.jul", "main.jul, 3");
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
		
		validateStringValue(gvt, "mc5b", "mc5");
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
	
	// illegal: include module files
	@Test
	public void includeModuleFileTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.SCRIPTING, FEATURE, true);
		
		runner.executeAndExpect("Test15/main.jul", KnownJSException.IllegalModule, null, "must not declare a module name");
		
		runner.executeAndExpect("Test15/main2.jul", KnownJSException.IllegalModule, null, "must not declare a module name");
	}
	
	// include chain
	@Test
	public void includeChainTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt, true);

		// main -> s1 -> s2 -> s3, which defines a function
		int val = 5;
		engine.getContext().setArguments(new String[] { String.valueOf(val) } );
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test16/main.jul"));
		validateIntValue(gvt, "v", val);
	
		// main -> s1 -> s2, s2 throws, propagates back to main.
		TestExceptionHandler handler = new TestExceptionHandler();
		engine.setExceptionHandler(handler);
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test16/main4.jul"));
		String st = handler.getStandardExceptionOutput();
		Assert.assertTrue(st.contains("FAILED!"));
		Assert.assertTrue(st.contains("helper4.jul, 2"));
		Assert.assertTrue(st.contains("main4.jul, 2"));
	}
	
	// circular include is always allowed
	@Test
	public void circularIncludeTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt, true);

		assertSuccess(engine, "Test17/main_2_without_main.jul");
		assertSuccess(engine, "Test17/main_3_with_main.jul");
		assertSuccess(engine, "Test17/main_with_main.jul");
	}
	
	// Thread.getCurrent()
	@Test
	public void apiTest_Thread_getCurrent() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		TestIO io = new TestIO(engine);
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test18/main.jul"));
		String[] entries = io.locateEntriesInOutput();
		
		Assert.assertEquals(2, entries.length);
		Assert.assertEquals(entries[0], entries[1]);
	}
	
	// Environment.getScript()
	@Test
	public void apiTest_Environment_getScript() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt, true);

		// get scripts from module (sub -> main)
		assertSuccess(engine, "Test19/main.jul");

		// types dynamically added
		assertSuccess(engine, "Test19/main3.jul");

		// types dynamically added
		assertSuccess(engine, "Test19/main4.jul");
	}
	
	// bindings
	@Test
	public void bindingTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.SCRIPTING, FEATURE, false);
		
		SimpleScriptEngine engine = runner.getEngine();
		engine.getContext().addBinding("i", new IntegerBinding(4));
		runner.executeAndExpect("Test20/main.jul", KnownJSException.UndefinedSymbol, null, "i is not defined");
	}
	
	// load script with ill-syntax
	@Test
	public void invalidScript() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt, true);

		// cannot parse
		TestExceptionHandler handler = new TestExceptionHandler();
		engine.setExceptionHandler(handler);
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test21/main.jul"));
		
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
