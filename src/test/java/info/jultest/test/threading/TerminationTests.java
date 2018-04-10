package info.jultest.test.threading;

import static info.jultest.test.EFCommons.getIntValue;
import static info.jultest.test.EFCommons.runViaFactory;
import info.jultest.test.Commons;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.interfaces.IExtEngineRuntime;
import info.julang.external.interfaces.IExtVariableTable;
import info.julang.external.interfaces.IExtValue.IIntVal;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

//(Uncomment @RunWith for reliability test)
//@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class TerminationTests extends ThreadingTestBase {

	private static final String FEATURE = "Termination";

	// (Uncomment data() for reliability test)
//	@org.junit.runners.Parameterized.Parameters
//	public static java.util.List<Object[]> data() {
//	  return java.util.Arrays.asList(new Object[20][0]);
//	}
	
	@Test
	public void terminateWhileCreatingNewThreadsTest() throws EngineInvocationError, IOException {
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.THREADING, FEATURE, "terminate_1.jul");
		IExtVariableTable gvt = rt.getGlobalVariableTable();
		IIntVal x = getIntValue(gvt, "x");
		Assert.assertTrue("x = " + x, x.getIntValue() > 1);
		
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// Ignore
		}
		
		IIntVal x2 = getIntValue(gvt, "x");
		Assert.assertEquals(x, x2);
	}
	
	@Test
	public void terminateLongRunningThreadTest() throws EngineInvocationError, IOException {
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.THREADING, FEATURE, "terminate_2.jul");
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		int x = getIntValue(gvt, "x").getIntValue();
		Assert.assertTrue(x > 0);
		
		// At this point the engine has shut down, and threads are either finished or in the process of termination.
		// Query value x repeatedly until it becomes stable.
		int stable = 0;
		int budget = 1000;
		int interval = 10;
		while(true){
			int x2 = getIntValue(gvt, "x").getIntValue();
			if (x2 == x){
				stable++;
				if (stable == 3) {
					break;
				}
			} else {
				x = x2;
				stable = 0;
			}

			// Wait for a while before we check again
			budget -= interval;
			if (budget < 0) {
				break;
			}
			
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				// Ignore
			}
		}
		
		// System.out.println("Stablized in " + (1000 - budget) + " milliseconds.");
		Assert.assertTrue("Within the given time budget (" + budget + " milliseconds), the thread didn't seem to stop.", stable == 3);
	}
	
}
