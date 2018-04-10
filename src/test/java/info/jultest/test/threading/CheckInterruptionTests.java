package info.jultest.test.threading;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import java.io.IOException;

import org.junit.Test;

// Thread.sleep()
public class CheckInterruptionTests extends ThreadingTestBase {

	private static final String FEATURE = "Interrupt";
	
	@Test
	public void basicCheckInterruptionTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "check_1.jul"));
		
		validateBoolValue(gvt, "chk0", false);
		validateBoolValue(gvt, "chk1", true);
		validateBoolValue(gvt, "chk2", false);
	}

}
