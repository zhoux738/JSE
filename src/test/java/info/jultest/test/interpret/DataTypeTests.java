package info.jultest.test.interpret;

import static info.jultest.test.Commons.*;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class DataTypeTests {

	private static final String FEATURE = "DataType";
	
	@Test
	public void floatDeclTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "float_01.jul"));
		
		validateFloatValue(gvt, "f1", 1.2f);
		validateFloatValue(gvt, "f2", 1f);
		validateFloatValue(gvt, "f3", 1f);
		validateFloatValue(gvt, "f4", 0f);
		validateFloatValue(gvt, "f5", -1.2f);
		validateFloatValue(gvt, "f6", 0.0f);
		validateFloatValue(gvt, "f7", 0.0f);
	}
	
	@Test
	public void floatExprTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "float_02.jul"));
		
		validateFloatValue(gvt, "sum1", 6.3f);
		validateFloatValue(gvt, "sum2", 12.3f);
		validateFloatValue(gvt, "diff1", -1.7f);
		validateFloatValue(gvt, "diff2", 1.7f);
		validateFloatValue(gvt, "diff3", 0f);
		validateFloatValue(gvt, "product", 9.2f);
		validateFloatValue(gvt, "quotient1", 0.575f);
		validateFloatValue(gvt, "quotient3", 1f);
		
		validateFloatValue(gvt, "f1", 3.3f);
		validateFloatValue(gvt, "f2", 3f);
	}
	
	@Test
	public void floatIntExprTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "float_03.jul"));

		validateIntValue(gvt, "qi", 0);
		validateFloatValue(gvt, "qf", 0.75f);
		
		validateIntValue(gvt, "s11", 5);
		validateIntValue(gvt, "s12", 5);
		validateFloatValue(gvt, "s21", 5.5f);
		validateFloatValue(gvt, "s22", 5.5f);
		
		validateIntValue(gvt, "d11", 0);
		validateIntValue(gvt, "d12", 0);
		validateFloatValue(gvt, "d21", 0.5f);
		validateFloatValue(gvt, "d22", -0.5f);
		
		validateIntValue(gvt, "p11", 7);
		validateIntValue(gvt, "p12", 7);
		validateFloatValue(gvt, "p21", 7.5f);
		validateFloatValue(gvt, "p22", 7.5f);
		
		validateIntValue(gvt, "q11", 1);
		validateIntValue(gvt, "q12", 0);
		validateFloatValue(gvt, "q21", 1.2f);
		validateFloatValue(gvt, "q22", 0.833f);
	}
	
	@Test
	public void floatCompareExprTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "float_04.jul"));
		
		validateBoolValue(gvt, "b11", true);
		validateBoolValue(gvt, "b12", true);
		validateBoolValue(gvt, "b13", true);
		validateBoolValue(gvt, "b14", true);
		validateBoolValue(gvt, "b21", true);
		validateBoolValue(gvt, "b22", true);
		validateBoolValue(gvt, "b23", true);
		validateBoolValue(gvt, "b24", true);
		validateBoolValue(gvt, "b31", true);
		validateBoolValue(gvt, "b32", false);
		validateBoolValue(gvt, "b33", true);
		validateBoolValue(gvt, "b34", false);
		validateBoolValue(gvt, "b41", false);
		validateBoolValue(gvt, "b42", true);
		validateBoolValue(gvt, "b43", true);
		validateBoolValue(gvt, "b44", false);
	}
	
	@Test
	public void stringCastingTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "string_01.jul"));

		validateIntValue(gvt, "i", 10);
		validateFloatValue(gvt, "f", 10.1f);
		validateStringValue(gvt, "s", "true");
		validateCharValue(gvt, "c", 't');
		validateBoolValue(gvt, "b", true);
	}
	
}
