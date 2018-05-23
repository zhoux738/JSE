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

// These tests verify the consistent between using void and Void
public class VoidTests {

	private static final String FEATURE = "Void";

	@Test
	public void declWithVoidTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "void_1.jul"));
		
		validateIntValue(gvt, "i", 1);
		validateBoolValue(gvt, "b0", true);
		validateBoolValue(gvt, "b1", true);	
	}
	
	@Test
	public void typeDeclWithVoidTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "void_2.jul"));

		validateBoolValue(gvt, "b0", true);
		validateBoolValue(gvt, "b1", true);	
	}
	
	@Test
	public void voidAsArrayElementTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "void_3.jul"));
	
		validateBoolValue(gvt, "b0", true);
		validateBoolValue(gvt, "b1", true);	
	}
}
