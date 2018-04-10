package info.jultest.test.threading;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.execution.threading.FaultedThreadRecord;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.interpretation.errorhandling.JulianScriptException;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

// Test when background thread faults
public class ThreadFaultingTests extends ThreadingTestBase {

	private static final String FEATURE = "Faulting";
	
	// The background throws, but main thread should complete successfully.
	@Test
	public void backgroundThreadThrowsTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "script_ex_1.jul"));
		
		validateIntValue(gvt, "i", 5);
		
		List<FaultedThreadRecord> faulted = engine.getRuntime().getThreadManager().getFaultedThreads();
		Assert.assertEquals(1, faulted.size());
		Exception ex = faulted.get(0).getException();
		Assert.assertEquals(JulianScriptException.class, ex.getClass());
		
		JulianScriptException jse = (JulianScriptException) ex;
		Assert.assertEquals("g() threw this.", jse.getExceptionMessage());
	}
	
}
