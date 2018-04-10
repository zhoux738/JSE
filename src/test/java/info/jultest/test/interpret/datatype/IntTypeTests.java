package info.jultest.test.interpret.datatype;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateByteValue;
import static info.jultest.test.Commons.validateCharValue;
import static info.jultest.test.Commons.validateFloatValue;
import static info.jultest.test.Commons.validateIntValue;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.jultest.test.Commons;

import org.junit.Test;

public class IntTypeTests {

	private static final String FEATURE = "DataType";

	// literal
	@Test
	public void intDeclTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "int_01.jul"));
		
		validateIntValue(gvt, "i1", 2147483647);
		validateIntValue(gvt, "i2", -2147483648);
		validateBoolValue(gvt, "res1", true);
		validateBoolValue(gvt, "res2", true);	
	}
	
	// overflow
	@Test
	public void intOverflowTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "int_02.jul"));
		
		validateIntValue(gvt, "i2", -2147483648);
		validateIntValue(gvt, "i3", 2147483647);
	}

	// casting
	@Test
	public void intCastingTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "int_03.jul"));
		
		validateBoolValue(gvt, "z", true);
		validateBoolValue(gvt, "z0", false);
		validateByteValue(gvt, "b", 10);
		validateFloatValue(gvt, "f", 10f);
		validateCharValue(gvt, "c", (char)10);
	}
}
