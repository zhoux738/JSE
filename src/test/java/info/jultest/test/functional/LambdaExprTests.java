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

// Tests in this class are analogous to what are in LambdaBlockTests
public class LambdaExprTests {
	
	private static final String FEATURE = "LambdaExpr";
	
	/**
	 * Function f = (int x) => return x + 5;
	 */
	@Test
	public void syntaxIntExprTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_01.jul"));
		
		validateIntValue(gvt, "a", 8);
	}
	
	/**
	 * Function f = (int x) => return x + 5;
	 */
	@Test
	public void syntaxIntExprWithReturnTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_01_return.jul"));
		
		validateIntValue(gvt, "a", 8);
	}
	
	/**
	 * Pass lambda as argument.
	 * (x) => ...
	 */
	@Test
	public void passLambdaAsArgTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_06.jul"));
		
		validateIntValue(gvt, "a", 8);
	}
	
	/**
	 * Return lambda from lambda.
	 */
	@Test
	public void returnLambdaFromLambdaTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_07.jul"));
		
		validateIntValue(gvt, "v", 8);
	}
	
	/**
	 * Return lambda from function.
	 */
	@Test
	public void returnLambdaTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_08.jul"));
		
		validateIntValue(gvt, "a", 8);
	}
	
	/**
	 * Use lambda in expression.
	 */
	@Test
	public void useLambdaInExprTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_09.jul"));
		
		validateIntValue(gvt, "a", 8);
	}
	
	/**
	 * Use lambda in array initializer.
	 */
	@Test
	public void useLambdaInArrayInitializerTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_10.jul"));
		
		validateIntValue(gvt, "a1", 8);
		validateIntValue(gvt, "a2", 9);
	}
	
	@Test
	public void useLambdaInExprTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_in_expr_1.jul"));
		
		validateIntValue(gvt, "a", 20);
	}
	
	@Test
	public void useLambdaInExprTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_in_expr_2.jul"));
		
		validateIntValue(gvt, "a", 20);
	}
	
	@Test
	public void useLambdaInExprTest3() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_in_expr_3.jul"));
		
		JValue result = validateUntypedValue(gvt, "result");
		validateIntValue(result, 6);
	}
	
	@Test
	public void useLambdaInExprTest4() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_in_expr_4.jul"));
		
		JValue result = validateUntypedValue(gvt, "result");
		validateIntValue(result, 6);
	}
	
	@Test
	public void useLambdaInExprTest5() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_in_expr_5.jul"));
		
		JValue result = validateUntypedValue(gvt, "result");
		validateIntValue(result, 6);
	}
	
}