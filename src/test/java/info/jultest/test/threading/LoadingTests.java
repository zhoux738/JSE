package info.jultest.test.threading;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.verifyDetectedClass;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.modulesystem.ModuleManager;

import java.io.IOException;

import org.junit.Test;

//(Uncomment @RunWith for reliability test)
//@RunWith(Parameterized.class)

// Load classes from multiple threads
public class LoadingTests extends ThreadingTestBase {

	// (Uncomment data() for reliability test)
	//@Parameterized.Parameters
	//public static List<Object[]> data() {
	//   return Arrays.asList(new Object[20][0]);
	//}
	
	private static final String FEATURE = "Loading";
	
	@Test
	public void twoThreadsLoadingTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		ModuleManager manager = new ModuleManager();
		SimpleScriptEngine engine = makeSimpleEngine(gvt, manager, false);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "2_threads.jul"));
		
		verifyDetectedClass(manager, "<default>.C1", "C1");
		verifyDetectedClass(manager, "<default>.C2", "C2");
		verifyDetectedClass(manager, "<default>.C3", "C3");
		verifyDetectedClass(manager, "<default>.C4", "C4");
		verifyDetectedClass(manager, "<default>.C5", "C5");
		verifyDetectedClass(manager, "<default>.C6", "C6");
		verifyDetectedClass(manager, "<default>.C7", "C7");
		
		validateIntValue(gvt, "v1", 1);
		validateIntValue(gvt, "v7", 7);
	}
	
}
