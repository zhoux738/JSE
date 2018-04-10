package info.jultest.test.functional;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class FirstClassFunctionGlobalFunctionTests {
	
	private static final String FEATURE = "FirstClassFunction";
	
	/**
	 * Assign a function to a Function variable
	 */
	@Test
	public void assignGlobalFunctionToVarTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "fun_01.jul"));
		
		validateIntValue(gvt, "a", 8);
	}
	
	/**
	 * Pass a function as an argument
	 */
	@Test
	public void passGlobalFunctionAsArgTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "fun_02.jul"));
		
		validateIntValue(gvt, "a", 8);
	}
	
	/**
	 * Return a function
	 */
	@Test
	public void returnGlobalFunctionTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "fun_03.jul"));
		
		validateIntValue(gvt, "a", 8);
	}
	
	/**
	 * Get a function from expression
	 */
	@Test
	public void getGlobalFunctionFromExprTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "fun_04.jul"));
		
		validateIntValue(gvt, "a", 8);
	}
	
	/**
	 * Invoke function from an expression result
	 * 
	 * fun(x)(y); // fun(x) returns another Function which is invoked with argument y.
	 */
	@Test
	public void invokeGlobalFunctionFromExprTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "fun_05.jul"));
		
		validateIntValue(gvt, "a", 8);
	}

}