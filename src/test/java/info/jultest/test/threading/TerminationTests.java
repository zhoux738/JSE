package info.jultest.test.threading;

import static info.jultest.test.EFCommons.getIntValue;
import static info.jultest.test.EFCommons.prepareViaFactory;
import static info.jultest.test.EFCommons.runViaFactory;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import info.julang.external.EngineFactory.EngineParamPair;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.interfaces.IExtEngineRuntime;
import info.julang.external.interfaces.IExtResult;
import info.julang.external.interfaces.IExtScriptEngine;
import info.julang.external.interfaces.IExtValue.IIntVal;
import info.julang.external.interfaces.IExtVariableTable;
import info.julang.util.Box;
import info.jultest.test.Commons;

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
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.THREADING, FEATURE, "terminate_1.jul", null);
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
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.THREADING, FEATURE, "terminate_2.jul", null);
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
	
	@Test
	public void terminateMainThreadTest() throws EngineInvocationError, IOException, InterruptedException {
		EngineParamPair pair = prepareViaFactory(null);
		String path = Commons.makeScriptPath(Commons.Groups.THREADING, FEATURE, "terminate_3.jul");
		
		IExtScriptEngine eng = pair.getFirst();
		
		// Run the script, i.e. the main JSE thread, in a separate thread
		Box<EngineInvocationError> mex = new Box<EngineInvocationError>(null);
		Thread m = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					eng.runFile(path);
				} catch (EngineInvocationError e) {
					mex.set(e);
				}
			}
		
		});
		m.start();
		
		IExtEngineRuntime rt = pair.getSecond();
		IExtVariableTable gvt = rt.getGlobalVariableTable();
		
		// From the main test thread, abort JSE
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					// The following is to ensure that the engine is indeed running when we are aborting it.
					int x = 0;
					
					try {
						x = getIntValue(gvt, "x").getIntValue();
					} catch (java.lang.AssertionError e) {
						// Ignore
					}
					
					if (x == 0) {
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
						}
					} else {
						break;
					}
				}

				// System.out.println("[CONTROL] ABORT NOW");
				eng.abort();
			}
		
		}).start();
		
		// Wait until JSE is finished running
		m.join();

		IExtResult result = eng.getResult();
		
		Assert.assertNull(result);
	}
	
	@Test
	public void terminateMainSleepingThreadTest() throws EngineInvocationError, IOException, InterruptedException {
		EngineParamPair pair = prepareViaFactory(null);
		String path = Commons.makeScriptPath(Commons.Groups.THREADING, FEATURE, "terminate_4.jul");
		
		IExtScriptEngine eng = pair.getFirst();
		
		// Run the script, i.e. the main JSE thread, in a separate thread
		Box<EngineInvocationError> mex = new Box<EngineInvocationError>(null);
		Thread m = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					eng.runFile(path);
				} catch (EngineInvocationError e) {
					mex.set(e);
				}
			}
		
		});
		m.start();
		
		IExtEngineRuntime rt = pair.getSecond();
		IExtVariableTable gvt = rt.getGlobalVariableTable();
		
		// From the main test thread, abort JSE
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					// The following is to ensure that the engine is indeed running when we are aborting it.
					int x = 0;
					
					try {
						x = getIntValue(gvt, "x").getIntValue();
					} catch (java.lang.AssertionError e) {
						// Ignore
					}
					
					if (x != 10) {
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
						}
					} else {
						break;
					}
				}

				// System.out.println("[CONTROL] ABORT NOW");
				eng.abort();
			}
		
		}).start();
		
		// Wait until JSE is finished running
		m.join();

		IExtResult result = eng.getResult();
		
		Assert.assertNull(result);
	}
}
