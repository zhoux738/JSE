package info.jultest.test.threading;

import static info.jultest.test.Commons.getIntValue;
import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

//(Uncomment @RunWith for reliability test)
//@RunWith(Parameterized.class)
// Thread.sleep()
public class SleepTests extends ThreadingTestBase {

	private static final String FEATURE = "Sleep";
	
	// (Uncomment data() for reliability test)
	//@Parameterized.Parameters
	//public static List<Object[]> data() {
	//  return Arrays.asList(new Object[20][0]);
	//}
	
	@Test
	public void sleepInMainTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "sleep_in_main_1.jul"));
		
		int diffInMillis = getIntValue(gvt.getVariable("diffInMillis"));
		Assert.assertTrue("Slept only " + diffInMillis, diffInMillis >= 100);
		
		validateBoolValue(gvt, "interrupted", false);
	}

	@Test
	public void sleepInBgTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "sleep_in_bg_1.jul"));
		
		int diffInMillis = getIntValue(gvt.getVariable("diffInMillis"));
		Assert.assertTrue("Slept only " + diffInMillis, diffInMillis >= 100);
	}
}
