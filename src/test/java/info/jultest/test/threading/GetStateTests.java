package info.jultest.test.threading;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateStringValue;

import java.io.IOException;

import org.junit.Test;

import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.typesystem.jclass.jufc.System.Concurrency.ScriptThread;

// Thread.getState()
public class GetStateTests extends ThreadingTestBase {

	private static final String FEATURE = "GetState";
	
	@Test
	public void basicFullThreadCycleTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "basic_full_cycle.jul"));
		
		validateStringValue(gvt, "s1", ScriptThread.ScriptThreadState.READY.toString());
		validateStringValue(gvt, "s2", ScriptThread.ScriptThreadState.RUNNING.toString());
		validateStringValue(gvt, "s3", ScriptThread.ScriptThreadState.DONE.toString());
	}
	
	@Test
	public void mainThreadStateTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "main_thread_state.jul"));
		
		validateStringValue(gvt, "s1", ScriptThread.ScriptThreadState.RUNNING.toString());
		validateStringValue(gvt, "s2", ScriptThread.ScriptThreadState.RUNNING.toString());
		validateStringValue(gvt, "s3", ScriptThread.ScriptThreadState.RUNNING.toString());
	}

}
