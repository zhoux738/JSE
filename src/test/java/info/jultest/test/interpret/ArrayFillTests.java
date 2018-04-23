package info.jultest.test.interpret;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntArrayValue;
import static info.jultest.test.Commons.validateStringArrayValue;

import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.jultest.test.Commons;

public class ArrayFillTests {

	private static final String FEATURE = "ArrayFill";
	
	@Test
	public void fill1DIntArrayBasicTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "af_01.jul"));
		
		validateIntArrayValue(gvt, "src", new int[]{10,10,10});
	}
	
	@Test
	public void fill1DStringArrayBasicTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "af_02.jul"));
		
		validateStringArrayValue(gvt, "src", new String[]{"abc", "xyz", "abc"});
	}
	
	@Test
	public void fill1DObjectArrayBasicTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "af_03.jul"));

		validateBoolValue(gvt, "b", true);
	}
}
