package info.jultest.test.threading;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntArrayValue;
import static info.jultest.test.Commons.validateIntValue;
import info.jultest.test.Commons;
import info.jultest.test.oo.ExceptionTestRunner;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import java.io.IOException;

import org.junit.Test;

// Thread.start()/join()
public class JoinTests extends ThreadingTestBase {

	private static final String FEATURE = "Join";
	
	@Test
	public void oneThreadOnFuncTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "1_thread_on_func.jul"));
		
		validateIntValue(gvt, "value", 100);
	}
	
	@Test
	public void oneThreadOnInstMethodTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "1_thread_on_imethod.jul"));
		
		validateIntValue(gvt, "value", 100);
	}
	
	@Test
	public void oneThreadOnStaticMethodTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "1_thread_on_smethod.jul"));
		
		validateIntValue(gvt, "value", 5050);
	}
	
	@Test
	public void twoThreadsOnFuncTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "2_threads_on_func.jul"));
		
		validateIntArrayValue(gvt, "values", new int[]{100, 50});
	}
	
	@Test
	public void twoThreadsOnBlockedLambdaTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "2_threads_on_lambda_blocked.jul"));
		
		validateIntArrayValue(gvt, "values", new int[]{100, 50});
	}
	
	@Test
	public void twoThreadsOnOnlinerLambdaTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "2_threads_on_lambda_oneliner.jul"));
		
		validateIntArrayValue(gvt, "values", new int[]{100, 50});
	}
	
	@Test
	public void twoThreadsOnInstMethodTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "2_threads_on_imethod.jul"));
		
		validateIntValue(gvt, "v1", 100);
		validateIntValue(gvt, "v2", 50);
	}

	@Test
	public void twoThreadsOnCtorTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "2_threads_on_ctor.jul"));
		
		validateIntArrayValue(gvt, "values", new int[]{5050, 1275});
	}
	
	@Test
	public void twoThreadsOnStaticMethodTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "2_threads_on_smethod.jul"));
		
		validateIntArrayValue(gvt, "values", new int[]{5050, 1275});
	}

	@Test
	public void illegalDoubleStartTest1() throws EngineInvocationError, IOException {	
 		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.THREADING, FEATURE);
		runner.executeAndValidate(
			"illegal_1_double_start.jul", 
			"System.IllegalStateException", 
			null, 
			null, 
			false, 
			-1);
	}
	
	@Test
	public void illegalDoubleStartTest2() throws EngineInvocationError, IOException {	
 		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.THREADING, FEATURE);
		runner.executeAndValidate(
			"illegal_2_double_start.jul", 
			"System.IllegalStateException", 
			null, 
			null, 
			false, 
			-1);
	}
	
	@Test
	public void illegalStartFromCurrentTest() throws EngineInvocationError, IOException {	
 		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.THREADING, FEATURE);
		runner.executeAndValidate(
			"illegal_3_start_from_current.jul", 
			"System.IllegalStateException", 
			null, 
			null, 
			false, 
			-1);
	}
	
	@Test
	public void illegalJoinToCurrentTest() throws EngineInvocationError, IOException {	
 		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.THREADING, FEATURE);
		runner.executeAndValidate(
			"illegal_4_join_to_current.jul", 
			"System.IllegalStateException", 
			null, 
			null, 
			false, 
			-1);
	}
	
	@Test
	public void legalDoubleJoinTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "legal_1_double_join.jul"));
		
		validateIntValue(gvt, "value", 5);
	}
	
	// should not deadlock.
	@Test
	public void legalJoinMainTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "legal_2_join_main.jul"));
	}
}
