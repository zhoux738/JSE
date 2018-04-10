package info.jultest.test.threading;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateStringValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import java.io.IOException;

import org.junit.Test;

// Thread.getCurrent()
public class GetCurrentTests extends ThreadingTestBase {

	private static final String FEATURE = "GetCurrent";
	
	@Test
	public void backgroundThreadBasicTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "bg_thread_basic.jul"));
		
		validateStringValue(gvt, "name", "t1");
	}
	
	@Test
	public void backgroundThreadCallTwiceTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "bg_thread_call_twice.jul"));
		
		validateBoolValue(gvt, "same", true);
		validateBoolValue(gvt, "nonnull", true);
	}

	@Test
	public void mainThreadBasicTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "main_thread_basic.jul"));
		
		validateStringValue(gvt, "name", "<Julian-Main>");
	}
	
	@Test
	public void mainThreadCallTwiceTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "main_thread_call_twice.jul"));
		
		validateBoolValue(gvt, "same", true);
		validateBoolValue(gvt, "nonnull", true);
	}
	
}
