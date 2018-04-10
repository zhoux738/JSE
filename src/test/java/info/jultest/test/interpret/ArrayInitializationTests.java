package info.jultest.test.interpret;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateFloatValue;
import static info.jultest.test.Commons.validateStringValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class ArrayInitializationTests {

	private static final String FEATURE = "Array";
	
	@Test
	public void initialize1DIntArrayTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "array_01.jul"));
		
		validateIntValue(gvt, "sum", 300);
	}

	@Test
	public void initialize1DStringArrayTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "array_02.jul"));
		
		validateStringValue(gvt, "sum", "abcdef");
	}
	
	@Test
	public void initialize1DIntArrayTestWithInitializer() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "array_03.jul"));
		
		validateIntValue(gvt, "sum", 3000);
	}
	
	@Test
	public void initialize1DStringArrayTestWithInitializer() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "array_04.jul"));
		
		validateStringValue(gvt, "sum", "xxxyyy");
	}
	
	@Test
	public void initialize2DIntArrayTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "array_05.jul"));
		
		validateIntValue(gvt, "val_12", 5);
	}
	
	@Test
	public void initialize2DStringArrayTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "array_06.jul"));
		
		validateStringValue(gvt, "val_12", "abcd");
	}
	
	@Test
	public void initialize2DIntArrayTestWithInitializer() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "array_07.jul"));
		
		validateIntValue(gvt, "sum", 500);
	}
	
	@Test
	public void initialize2DStringArrayTestWithInitializer() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "array_08.jul"));
		
		validateStringValue(gvt, "sum", "abgh");
	}
	
	@Test
	public void initialize1DFloatArrayTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "array_09.jul"));
		
		validateFloatValue(gvt, "f1", 0f);
		validateFloatValue(gvt, "f2", 10.3f);
	}
	
}
