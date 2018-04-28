package info.jultest.test.functional;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class LambdaInClassTests {
	
	private static final String FEATURE = "LambdaEnv";
	
	/*
	 * Reference an instance member.
	 */
	@Test
	public void refInstanceMemberTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_imethod_01.jul"));
		
		validateStringValue(gvt, "a", "To: Luke");
	}
	
	/*
	 * Local/argument hides instance member of same name.
	 */
	@Test
	public void localHidesInstanceMemberTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_imethod_02.jul"));
		
		validateStringValue(gvt, "a", "a");
		validateStringValue(gvt, "b", "b");
		validateStringValue(gvt, "mem", "Luke");
	}
	
	/*
	 * Modify instance member.
	 */
	@Test
	public void modifyInstanceMemberTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_imethod_03.jul"));
		
		validateStringValue(gvt, "a", "Luke2");
	}
	
	@Test
	public void refInstanceMemberByThisTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_imethod_04.jul"));
		
		validateStringValue(gvt, "x1", "ab");
		validateStringValue(gvt, "x2", "a");
	}
	
	@Test
	public void modInstanceMemberByThisTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_imethod_05.jul"));
		
		validateStringValue(gvt, "x", "b");
	}
	
	/*
	 * Reference an instance method
	 */
	@Test
	public void callInstanceMethodTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_imethod_06.jul"));
		
		validateIntValue(gvt, "x", 8);
	}
	
	/*
	 * Reference an instance method from a higher-order lambda
	 */
	@Test
	public void returnLambdaFromLambdaTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_imethod_07.jul"));
		
		validateIntValue(gvt, "x", 16);
	}
	
	/*
	 * Reference a static member.
	 */
	@Test
	public void refStaticMemberTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_smethod_01.jul"));
		
		validateStringValue(gvt, "a", "To: Luke");
	}
	
	/*
	 * Local/argument hides static member of same name.
	 */
	@Test
	public void localHidesStaticMemberTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_smethod_02.jul"));
		
		validateStringValue(gvt, "a", "a");
		validateStringValue(gvt, "b", "b");
		validateStringValue(gvt, "mem", "Luke");
	}
	
	/*
	 * Modify static member.
	 */
	@Test
	public void modifyStaticMemberTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_smethod_03.jul"));
		
		validateStringValue(gvt, "a", "Luke2");
	}
	
	/*
	 * Modify a static member.
	 */
	@Test
	public void modStaticMemberTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_smethod_04.jul"));
		
		validateIntValue(gvt, "v", 111);
	}
	
	/*
	 * Modify a static member.
	 */
	@Test
	public void modStaticMemberTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_smethod_05.jul"));
		
		validateIntValue(gvt, "v", 111);
	}
	
	/*
	 * Reference a static method.
	 */
	@Test
	public void refStaticMethodTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "lambda_smethod_06.jul"));
		
		validateIntValue(gvt, "v", 111);
	}
	
}