package info.jultest.test.functional;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class FirstClassFunctionClassMethodTests {
	
	private static final String FEATURE = "FirstClassFunction";
	
	/**
	 * Assign an instance method to a Function variable
	 */
	@Test
	public void assignInstanceMethodToVarTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "imethod_01.jul"));
		
		validateIntValue(gvt, "a", 8);
	}
	
	/**
	 * Resolve "this" in a Function variable assigned from an instance method.
	 */
	@Test
	public void resolveThisInInstanceMethodTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "imethod_02.jul"));
		
		validateIntValue(gvt, "a", 8);
	}
	
	/**
	 * Assign a static method to a Function variable
	 */
	@Test
	public void assignStaticMethodToVarTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "smethod_01.jul"));
		
		validateIntValue(gvt, "a", 8);
	}

}