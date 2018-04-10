package info.jultest.test.threading;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.getStringValue;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateBoolValueEx;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;

import org.junit.Assert;
import org.junit.Test;

import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

// Promise API
public class PromiseBasicTests extends ThreadingTestBase {

	private static final String FEATURE = "Promise";
	
	@Test
	public void basicContinuityTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "prom_1.jul"));
		
		validateIntValue(gvt, "result", 12);
	}
	
	@Test
	public void multiPromisesTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "prom_2.jul"));
		
		validateIntValue(gvt, "result1", 6);
		validateIntValue(gvt, "result2a", 7);
		validateIntValue(gvt, "result2b", 12);
	}
	
	@Test
	public void onErrorTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "prom_3.jul"));
		
		validateStringValue(gvt, "msg", "failed!");
	}
	
	@Test
	public void skipCallbacksTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "prom_4.jul"));
		
		validateStringValue(gvt, "msg", "failed!");
	}
	
	@Test
	public void voidResultTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "prom_5.jul"));

		validateBoolValueEx(gvt, "result", true);
	}
	
	@Test
	public void handleTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "prom_6.jul"));

		validateStringValue(gvt, "msg1", "The promise was rejected."); // This is the default message of PromiseRejectedException
		String s = getStringValue(gvt, "msg2"); //, "The promise was rejected."
		Assert.assertTrue(s.contains("failed!"));
	}
	
	@Test
	public void handleFromOnErrorTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "prom_7.jul"));

		String s = getStringValue(gvt, "msg1"); //, "The promise was rejected."
		Assert.assertTrue(s.contains("epically failed!"));
	}
	
	@Test
	public void throwTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "prom_8.jul"));

		validateBoolValue(gvt, "caught", true);
	}
}
