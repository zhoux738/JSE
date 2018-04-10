package info.jultest.test.threading;

import static info.jultest.test.EFCommons.runViaFactory;
import static info.jultest.test.EFCommons.validateIntValue;
import info.jultest.test.Commons;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.interfaces.IExtEngineRuntime;
import info.julang.external.interfaces.IExtVariableTable;

import java.io.IOException;

import org.junit.Test;

//(Uncomment @RunWith for reliability test)
//@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
//@RunWith(Parameterized.class)
public class InterruptReliabilityTests extends ThreadingTestBase {

	// (Uncomment data() for reliability test)
//	@org.junit.runners.Parameterized.Parameters
//	public static java.util.List<Object[]> data() {
//	  return java.util.Arrays.asList(new Object[20][0]);
//	}
    
	private static final String FEATURE = "Interrupt";
	
	@Test
	public void interruptSleepingAfterStartTest() throws EngineInvocationError, IOException {
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.THREADING, FEATURE, "interrupt_1.jul");
		IExtVariableTable gvt = rt.getGlobalVariableTable();
		
		validateIntValue(gvt, "icnt", 1);
		validateIntValue(gvt, "rcnt", 9);
	}
	
	@Test
	public void interruptSelfSleepingTest() throws EngineInvocationError, IOException {
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.THREADING, FEATURE, "interrupt_2.jul");
		IExtVariableTable gvt = rt.getGlobalVariableTable();
		
		validateIntValue(gvt, "icnt", 10);
		validateIntValue(gvt, "rcnt", 0);
	}
	
	@Test
	public void interruptBeforeAndAfterRunningTest() throws EngineInvocationError, IOException {
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.THREADING, FEATURE, "interrupt_3.jul");
		IExtVariableTable gvt = rt.getGlobalVariableTable();
		
		validateIntValue(gvt, "icnt", 0);
		validateIntValue(gvt, "rcnt", 10);
	}
	
}
