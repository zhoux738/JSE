package info.jultest.test.interpret;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static org.junit.Assert.*;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.memory.value.JValue;
import info.julang.typesystem.JType;
import info.julang.typesystem.basic.IntType;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.builtin.JFunctionType;
import info.julang.typesystem.jclass.builtin.JStringType;

import org.junit.Test;

public class BasicFunctionInvocationTests {
	
	private static final String FEATURE = "Function";
	
	/**
	 * In this test, we read a script where functions are defined. Consider function definition as below:
	 * <pre><code>
	 * void foo(int a, string s){
	 *   int result = bar(a, s);
	 *   print("done: " + result);
	 * }
	 * </code></pre>
	 * We are going to check:
	 * <ul>
	 * <li>a function with name foo is added into type table</li>
	 * <li>foo's parameters and return type are correct</li>
	 * <li>foo's function body is as expected</li>
	 * </ul>
	 */
	@Test
	public void functionDefinitionTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "fun_def_01.jul"));
		
		JValue jv = gvt.getVariable("foo");
		JType type = jv.getType();
		
		// Check 1: type gets defined
		assertNotNull(type);
		
		// Check 2: it is function
		assertTrue(type instanceof JFunctionType);
		
		// Check 3: return type
		JFunctionType funType = (JFunctionType) type;
		assertEquals(IntType.getInstance(), funType.getReturn().getReturnType());
		
		// Check 4: parameter list
		JParameter[] params = funType.getParams();
		assertEquals(2, params.length);
		assertEquals(IntType.getInstance(), params[0].getType());
		assertEquals(JStringType.getInstance(), params[1].getType());
	}
	
	/**
	 * Function should have side-effect on global variable.
	 * <p/>
	 * <table>
	 * <tr><td>Return type: void</td></tr>
	 * <tr><td>Parameter: int</td></tr>
	 * </table>
	 */
	@Test
	public void callFuncVoidIntTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "fun_01.jul"));
		
		validateIntValue(gvt, "a", 8);
	}
	
	/**
	 * Function should have side-effect on global variable.
	 * <p/>
	 * <table>
	 * <tr><td>Return type: void</td></tr>
	 * <tr><td>Parameter: int, int</td></tr>
	 * </table>
	 */
	@Test
	public void callFuncVoidIntIntTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "fun_02.jul"));
		
		validateIntValue(gvt, "a", 8);
	}
	
	/**
	 * <table>
	 * <tr><td>Return type: int</td></tr>
	 * <tr><td>Parameter: int, int</td></tr>
	 * </table>
	 */
	@Test
	public void callFuncIntIntIntTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "fun_03.jul"));
		
		validateIntValue(gvt, "a", 8);
	}
	
	@Test
	public void callFuncVoidIntTwiceTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "fun_04.jul"));
		
		validateIntValue(gvt, "a", 8);
	}
	
	@Test
	public void callFuncWithExcessiveArgsTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "fun_05.jul"));
		
		JulianScriptException jse = engine.getContext().getException();
		assertNotNull(jse);
		assertEquals("Wrong number of arguments when calling fun.", jse.getExceptionMessage()); // scope exits properly
	}

}
