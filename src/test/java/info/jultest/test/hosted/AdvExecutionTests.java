package info.jultest.test.hosted;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;

import org.junit.Assert;
import org.junit.Test;

import info.jultest.test.Commons;
import info.jultest.test.TestExceptionHandler;
import info.jultest.test.oo.ExceptionTestRunner;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

public class AdvExecutionTests {
	
	private static final String FEATURE = "Execution";
	
	// Map info.jultest.test.hosted.AdvClass1
	@Test
	public void concurrentCallTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "exec_adv_1_concurrency.jul"));

		validateIntValue(gvt, "v1", 100);
		validateIntValue(gvt, "v2", 200);
	}
	
	// Map info.jultest.test.hosted.Faulty1
	@Test
	public void throwExceptionFromPlatformTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.HOSTING, FEATURE);
		runner.executeAndExpect(
			"exec_adv_2_exception.jul", 
			"System.HostingPlatformException", 
			8, 
			"System.PlatformOriginalException", 
			-1); // The platform exception doesn't have a line number in the expecetd format
	}
	
	// Map info.jultest.test.hosted.Faulty2
	@Test
	public void throwChainedExceptionFromPlatformTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		TestExceptionHandler teh = new TestExceptionHandler();
		engine.setExceptionHandler(teh);
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "exec_adv_3_exception_chain.jul"));
		
		// Make sure we hit "Caused By:" exactly twice
		String str = teh.getStandardExceptionOutput();
		String pat = "Caused by:" + System.lineSeparator();
		int index = str.indexOf(pat);
		Assert.assertTrue(index >= 0);
		index = str.indexOf(pat, index + pat.length());
		Assert.assertTrue(index >= 0);
		index = str.indexOf(pat, index + pat.length());
		Assert.assertTrue(index < 0);
	}
}