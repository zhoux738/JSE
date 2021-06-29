package info.jultest.test.interpret;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateNullValue;
import static info.jultest.test.Commons.validateStringValue;
import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestRunner;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.value.IllegalAssignmentException;

import org.junit.Test;

public class AdvancedArrayInitializationTests {

	private static final String FEATURE = "Array";
	
	@Test
	public void initializeStringArrayWithEndingCommaTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "array_adv_01.jul"));
		
		validateStringValue(gvt, "s", "a");
	}
	
	@Test
	public void initializeStringArrayWithNullTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "array_adv_02.jul"));
		
		validateStringValue(gvt, "s", "a");
		validateNullValue(gvt, "s2");
	}
	
	@Test
	public void initializeStringArrayWithNonStringValueTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.IMPERATIVE, FEATURE);
		runner.executeAndExpect("array_adv_03.jul", new IllegalAssignmentException(""));
	}
	
}
