package info.jultest.test.threading;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateCharArrayValue;
import static info.jultest.test.Commons.validateIntArrayValue;
import static info.jultest.test.Commons.validateStringValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.modulesystem.ModuleManager;

import org.junit.Test;

// (Uncomment @RunWith for reliability test)
//@RunWith(Parameterized.class)
// Load classes from multiple threads
public class ApplicationTests extends ThreadingTestBase {

	private static final String FEATURE = "Application";
	 
// (Uncomment data() for reliability test)
//    @Parameterized.Parameters
//    public static List<Object[]> data() {
//        return Arrays.asList(new Object[20][0]);
//    }
    
	/**
	 * A set of classes simulating banking service.
	 * Scenario 1: users transfer deposit between accounts
	 */	
	@Test
	public void multithreadedBankingTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		ModuleManager manager = new ModuleManager();
		SimpleScriptEngine engine = makeSimpleEngine(gvt, manager, false);
		
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.APPLICATION, null, "banking_4_a.jul"));
		
		validateStringValue(gvt, "res1", "Current balance: 650.0");
		validateStringValue(gvt, "res2", "Current balance: 380.0");
	}
	
	@Test
	public void producerConsumerTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "prod_cons.jul"));
		
		// The items consumed must have the same order as being produced.
		validateIntArrayValue(gvt, "results", new int[]{0,1,2,3,4,5,6,7,8,9});
		// The order of operations must strictly follow produce(+)=>consume(-) cycle since we capped the queue with 1.
		validateCharArrayValue(gvt, "logs", new char[]{
			'+','-','+','-','+','-','+','-','+','-','+','-','+','-','+','-','+','-','+','-'});
	}
}
