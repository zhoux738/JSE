package info.jultest.test.threading;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateIntValueWithinRange;
import info.jultest.test.Commons;
import info.jultest.test.oo.ExceptionTestRunner;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import java.io.IOException;

import org.junit.Test;

// Lock.wait()/notify()
public class WaitNotifyTests extends ThreadingTestBase {

	private static final String FEATURE = "WaitNotify";
	
	@Test
	public void basicWaitNotifyTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "wait_1.jul"));
		
		validateIntValue(gvt, "value", 4);
	}
	
	@Test
	public void interruptWaitTest1() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "wait_2a.jul"));
		
		validateBoolValue(gvt, "flag", true);
	}
	
	@Test
	public void interruptWaitTest2() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "wait_2b.jul"));
		
		validateBoolValue(gvt, "flag", true);
	}
	
	@Test
	public void interruptWaitForDurationTest1() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "wait_for_1.jul"));
		
		validateIntValue(gvt, "waited", 0);
	}
	
	@Test
	public void interruptWaitForDurationTest2() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "wait_for_2.jul"));
		
		validateIntValueWithinRange(gvt, "waited", "[3,)");
	}
	
	@Test
	public void illegalWaitTest() throws EngineInvocationError, IOException {	
 		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.THREADING, FEATURE);
		runner.executeAndValidate(
			"wait_illegal_1.jul", 
			"System.IllegalStateException", 
			null, 
			null, 
			false, 
			5);
	}
	
	@Test
	public void illegalNotifyTest() throws EngineInvocationError, IOException {	
 		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.THREADING, FEATURE);
		runner.executeAndValidate(
			"wait_illegal_2.jul", 
			"System.IllegalStateException", 
			null, 
			null, 
			false, 
			5);
	}
	
}
