package info.jultest.test.functional;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.Commons.validateUntypedValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.value.JValue;

import org.junit.Test;

public class LambdaBlockTests {
	
	private static final String FEATURE = "LambdaBlock";
	
	/**
	 * Function f = (int x) => { return x + 5; };
	 */
	@Test
	public void syntaxIntBlockTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_01.jul"));
		
		validateIntValue(gvt, "a", 8);
	}

	/**
	 * Function f = (int x, string s) => { return x + s.length; };
	 */
	@Test
	public void syntaxIntStringBlockTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_02.jul"));
		
		validateIntValue(gvt, "a", 8);
	}
	
	/**
	 * Function f = (x) => { return x + 5; };
	 */
	@Test
	public void syntaxSingleUntypedBlockTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_03.jul"));
		
		validateIntValue(gvt, "a", 8);
	}
	
	/**
	 * Function f = x => { return x + 5; };
	 */
	@Test
	public void syntaxSingleUntypedBlockTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_04.jul"));
		
		validateIntValue(gvt, "a", 8);
	}
	
	/**
	 * Function f = (int x, string s) => { return x + s.length; };
	 */
	@Test
	public void syntaxIntUntypedBlockTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_05.jul"));
		
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
	 * Pass lambda as argument
	 * x => ...
	 */
	@Test
	public void passLambdaAsArgTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_07.jul"));
		
		validateIntValue(gvt, "a", 8);
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

	/**
	 * Function f = () => { return 8; };
	 */
	@Test
	public void syntaxParameterlessBlockTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_11.jul"));
		
		validateIntValue(gvt, "a", 8);
	}
	
	@Test
	public void reassignUntypedArgTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_12.jul"));
		
		JValue v1 = validateUntypedValue(gvt, "v1");
		JValue v2 = validateUntypedValue(gvt, "v2");
		validateStringValue(v1, "a");
		validateIntValue(v2, 10);
	}
	
	@Test
	public void emptyLambdaTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_13.jul"));
		
		validateIntValue(gvt, "i", 10);
	}
	
}