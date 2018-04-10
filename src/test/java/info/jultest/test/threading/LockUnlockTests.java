package info.jultest.test.threading;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import info.jultest.test.Commons;
import info.jultest.test.oo.ExceptionTestRunner;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import java.io.IOException;

import org.junit.Test;

// Lock.lock()/unlock()
public class LockUnlockTests extends ThreadingTestBase {

	private static final String FEATURE = "LockUnlock";
	
	@Test
	public void singleThreadLockingTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "lock_1.jul"));
		
		validateIntValue(gvt, "value", 100);
	}
	
	@Test
	public void twoThreadsSynchingTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "lock_2.jul"));
		
		validateIntValue(gvt, "value", 100);
	}
	
	@Test
	public void twoThreadsReentrantSynchingTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "lock_3.jul"));
		
		validateIntValue(gvt, "value", 400);
	}
	
	@Test
	public void unlockInFinallyBlockTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "lock_4.jul"));
		
		validateIntValue(gvt, "value", 100);
	}
	
	@Test
	public void illegalLockTest1() throws EngineInvocationError, IOException {	
 		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.THREADING, FEATURE);
		runner.executeAndValidate(
			"lock_illegal_1.jul", 
			"System.IllegalStateException", 
			null, 
			null, 
			false, 
			5);
	}
	
	@Test
	public void illegalLockTest2() throws EngineInvocationError, IOException {	
 		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.THREADING, FEATURE);
		runner.executeAndValidate(
			"lock_illegal_2.jul", 
			"System.IllegalStateException", 
			null, 
			null, 
			false, 
			7);
	}
	
	@Test
	public void syncStmtBasicTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "sync_1.jul"));
		
		validateIntValue(gvt, "value", 100);
	}
	
	@Test
	public void syncStmtThrowTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "sync_2.jul"));
		
		validateIntValue(gvt, "value", 200);
	}
	
	@Test
	public void illegalSyncTest1() throws EngineInvocationError, IOException {	
 		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.THREADING, FEATURE);
		runner.executeAndValidate(
			"sync_illegal_1.jul", 
			"System.Lang.RuntimeCheckException", 
			null, 
			null, 
			false, 
			2);
	}
	
	@Test
	public void illegalSyncTest2() throws EngineInvocationError, IOException {	
 		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.THREADING, FEATURE);
		runner.executeAndValidate(
			"sync_illegal_2.jul", 
			"System.NullReferenceException", 
			null, 
			null, 
			false, 
			2);
	}
}
