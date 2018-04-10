package info.jultest.test.functional;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateUntypedValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.value.JValue;

import org.junit.Test;

public class LambdaEnvTests {
	
	private static final String FEATURE = "LambdaEnv";
	
	/*
	 * Reference a variable from lexical scope
	 */
	@Test
	public void refVarFromLexcialScopeTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_env_01.jul"));
		
		validateIntValue(gvt, "a", 8);
	}
	
	/*
	 * Reference a variable from the function where lambda is defined
	 */
	@Test
	public void refVarFromDefiningFunctionTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_env_02.jul"));
		
		validateIntValue(gvt, "a", 111);
	}
	
	/*
	 * Modify a variable from the function where lambda is defined
	 */
	@Test
	public void setVarFromDefiningFunctionTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_env_03.jul"));
		
		validateIntValue(gvt, "a1", 111);
		validateIntValue(gvt, "a2", 211);
	}
	
	/*
	 * Modify an untyped variable from the scope where lambda is defined
	 */
	@Test
	public void reassignUntypedDisplayValueTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_env_04.jul"));
		
		JValue c = validateUntypedValue(gvt, "c");
		validateIntValue(c, 15);
	}
	
	/*
	 * Reference the loop variable of for loop:
	 * 
	 * for(int i = 0; i<2; i++){
	 *   funs[i] = (int x) => { return x + i; };
	 * }
	 * 
	 * This behavior is also expected in other programming languages.
	 */
	@Test
	public void refLoopVarOfForLoopTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_for_01.jul"));
		
		validateIntValue(gvt, "a1", 7);
		validateIntValue(gvt, "a2", 7);
	}
	
	/*
	 * Reference the scope variable from for loop:
	 * 
	 * for(int i = 0; i<2; i++){
	 *   int j = i;
	 *   funs[i] = (int x) => { return x + j; };
	 * }
	 */
	@Test
	public void refScopeVarFromForLoopTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_for_02.jul"));
		
		validateIntValue(gvt, "a1", 5);
		validateIntValue(gvt, "a2", 6);
	}
	
	/*
	 * Reference the untyped loop variable of for loop:
	 * 
	 * for(var i = 0; i<2; i++){
	 *   funs[i] = (int x) => { return x + i; };
	 * }
	 * 
	 * This behavior is also expected in other programming languages.
	 */
	@Test
	public void refUntypedLoopVarOfForLoopTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_for_03.jul"));
		
		validateIntValue(gvt, "a1", 7);
		validateIntValue(gvt, "a2", 7);
	}
	
	
}