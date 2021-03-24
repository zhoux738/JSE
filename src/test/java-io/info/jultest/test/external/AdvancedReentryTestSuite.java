package info.jultest.test.external;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.script.ScriptException;

import org.junit.Assert;
import org.junit.Test;

import info.julang.execution.security.EngineLimit;
import info.julang.execution.security.PACON;
import info.julang.external.JulianScriptEngine;
import info.julang.external.exceptions.JSEException;
import info.julang.external.interfaces.ResetPolicy;
import info.julang.util.OSTool;
import info.jultest.test.Commons;

/**
 * This suite tests various scenario surrounding engine instance re-entrance. It also tests
 * the builder API for JulianScriptEngine (non-JSR223).
 */
public class AdvancedReentryTestSuite {

	@Test
	public void clearUserDefinedTypesTest() throws JSEException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JulianScriptEngine jse = JulianScriptEngine.Builder.create()
			.addModulePath(Commons.SRC_REPO_ROOT)
			.allow(PACON.IO.Name, PACON.IO.Op_list)
			.deny(PACON.Process.Name, PACON.Process.Op_control)
			.setLimit(EngineLimit.MAX_THREADS.getPublicName(), 2)
			.setIn(null)
			.setOut(null)
			.setError(baos)
			.setAllowReentry(true)
			.setClearUserDefinedTypesOnReentry(true)
			.setInteractiveMode(false)
			.setUseExceptionDefaultHandler(true)
			.setClearUserBindingsOnExit(true)
			.build();
			
		jse.bindInt("curr", 0);
		
		jse.runScript(
			   "class MyClass { static int value = 10; } " + System.lineSeparator()
			 + "curr = MyClass.value;",
			null);
		int res1 = jse.getInt("curr");
		Assert.assertEquals(10, res1);

		// Since we set setClearUserDefinedTypesOnReentry = true,
		// Upon re-entrance, we don't have this class anymore. It therefore fails at name resolution.
		runToFail(
			jse, 
			  "MyClass.value *= 2; " + System.lineSeparator()
			+ "curr = MyClass.value;",
			"UndefinedSymbolException");
		
		baos.flush();
		String errStr = new String(baos.toByteArray());
		Assert.assertTrue(errStr.contains("UndefinedSymbolException"));
		
		// Re-run the engine, this time with a different class definition for MyClass
		jse.bindInt("curr", 2);
		jse.runScript(
			   "class MyClass { String value() { return \"hello\"; } }" + System.lineSeparator()
			 + "curr += new MyClass().value().length;",
			null);
		res1 = jse.getInt("curr");
		Assert.assertEquals(7, res1);
	}
	
	@Test
	public void partialResetTest() throws JSEException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JulianScriptEngine jse = JulianScriptEngine.Builder.create()
			.addModulePath(Commons.SRC_REPO_ROOT)
			.setOut(baos)
			.setError(baos)
			.setAllowReentry(true)
			.setClearUserDefinedTypesOnReentry(false)
			.setUseExceptionDefaultHandler(true)
			.setClearUserBindingsOnExit(false)
			.build();
			
		jse.bindInt("curr", 0);
		
		int value = 31;
		jse.runScript(
			   "class MyClass { static int value = " + value + "; } " + System.lineSeparator()
			 + "curr = MyClass.value;" + System.lineSeparator()
			 + "Console.println(\"value=\" + curr);" + System.lineSeparator(),
			null);
		int res1 = jse.getInt("curr");
		Assert.assertEquals(value, res1);
		
		jse.reset(ResetPolicy.USER_DEFINED_ONLY);

		// Since we have reset user-defined types,
		// Upon re-entrance, we don't have this class anymore. It therefore fails at name resolution.
		runToFail(
			jse, 
			  "MyClass.value *= 2; " + System.lineSeparator()
			+ "curr = MyClass.value;",
			"UndefinedSymbolException");
		
		baos.flush();
		String errStr = new String(baos.toByteArray());
		Assert.assertTrue(errStr.contains("value=31")); // first run
		Assert.assertTrue(errStr.contains("UndefinedSymbolException")); // second run
	}
	
	@Test
	public void resetLimitTest() throws JSEException, IOException {
		final int max = 512;
		JulianScriptEngine jse = JulianScriptEngine.Builder.create()
			.addModulePath(Commons.SRC_REPO_ROOT)
			.allow(PACON.IO.Name, PACON.IO.Op_list)
			.deny(PACON.Process.Name, PACON.Process.Op_control)
			.setLimit(EngineLimit.MAX_USED_MEMORY_IN_BYTE.getPublicName(), max)
			.setAllowReentry(true)
			.setClearUserDefinedTypesOnReentry(false)
			.setInteractiveMode(false)
			.setUseExceptionDefaultHandler(true)
			.setClearUserBindingsOnExit(false)
			.build();
		
		int size = OSTool.WordSize;
		int count = (int)((max * 0.9) / size);
		jse.bindInt("count", count);
		runToFail(jse,
			   "var arr = new int[count];" + System.lineSeparator() // SUCC
			+  "arr = new int[count];" + System.lineSeparator(),    // FAIL
			"About to exceed max memory");

		jse.bindInt("curr", 0);
		jse.runScript(
			"var arr = new int[count];" + System.lineSeparator() +  // SUCC (memory usage reset)
			"curr = arr.length;",
			null);
		
		int res1 = jse.getInt("curr");
		Assert.assertEquals(count, res1);
	}
	
	private void runToFail(JulianScriptEngine jse, String script, String errorMsg){
		try {
			jse.runScript(script, null);
			Assert.fail("Didn't fail.");
		} catch (JSEException ex) {
			Throwable cause = ex.getCause();
			if (cause != null && cause instanceof ScriptException) {
				String message = cause.getMessage();
				Assert.assertTrue(message.contains(errorMsg));
			} else {
				Assert.fail("Not a ScriptException thrown.");
			}
		}
	}
}
