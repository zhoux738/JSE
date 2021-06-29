package info.jultest.test.interpret;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.EFCommons.runViaFactory;
import static info.jultest.test.Commons.validateBoolValue;
import info.jultest.test.Commons;
import info.jultest.test.EFCommons;
import info.jultest.test.ExceptionTestRunner;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.interfaces.IExtEngineRuntime;
import info.julang.external.interfaces.IExtVariableTable;
import info.julang.memory.value.IllegalAssignmentException;
import info.julang.typesystem.IllegalMemberAccessException;

import org.junit.Test;

public class AdvancedFunctionInvocationTests {
	
	private static final String FEATURE = "Function";
	
	/**
	 * Function with parameter of user-defined class type.
	 */
	@Test
	public void callFuncUserClassTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "fun_adv_01.jul"));
		
		validateStringValue(gvt, "s", "Tesla");
	}

	/**
	 * Arguments of basic type and string type are passed by value.
	 */
	@Test
	public void basicTypeArgPassedByValueTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "fun_adv_02.jul"));
		
		validateIntValue(gvt, "i", 9);
		validateStringValue(gvt, "s", "unchanged");
		
		validateIntValue(gvt, "i2", 7);
		validateStringValue(gvt, "s2", "changed");
	}
	
	/**
	 * Arguments of class type are passed by reference.
	 */
	@Test
	public void classTypeArgPassedByRefTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "fun_adv_03.jul"));
		
		validateStringValue(gvt, "s", "Tesla");
	}
	
	/**
	 * <p/>
	 * <table>
	 * <tr><td>Return type: string</td></tr>
	 * <tr><td>Parameter: Class, int</td></tr>
	 * </table>
	 * <p/>
	 * <table>
	 * <tr><td>Return type: string</td></tr>
	 * <tr><td>Parameter: Class, Class, int, int</td></tr>
	 * </table>
	 * <p/>
	 * <table>
	 * <tr><td>Return type: Class</td></tr>
	 * <tr><td>Parameter: string, int, Class</td></tr>
	 * </table>
	 */
	@Test
	public void callFuncWithClassParamReturnTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "fun_adv_04.jul"));
		
		validateStringValue(gvt, "s1", "Tesla-2013");
		validateStringValue(gvt, "s2", "Tesla-2013; T-1913");
		validateStringValue(gvt, "s3", "Tesla-2013-(T)");
	}
	
	@Test
	public void recursionTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "fun_adv_05.jul"));
		
		validateIntValue(gvt, "x", 120);
	}

	@Test
	public void nonFunctionTest() throws EngineInvocationError {
		Commons.resetTypeSystem();
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "fun_adv_06.jul"));
		
		Commons.validateBoolValue(gvt, "b0", true);
		Commons.validateBoolValue(gvt, "b1", true);
		Commons.validateBoolValue(gvt, "b2", true);
	}
	
	@Test
	public void returnFromIfTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "fun_ret_01.jul"));
		
		validateIntValue(gvt, "x1", 5);
		validateIntValue(gvt, "x2", 7);
		validateIntValue(gvt, "a", 10);
	}
	
	@Test
	public void returnFromWhileTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "fun_ret_02.jul"));
		
		validateIntValue(gvt, "x1", 4);
		validateIntValue(gvt, "x2", 1);
	}
	
	@Test
	public void returnFromForTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "fun_ret_03.jul"));
		
		validateIntValue(gvt, "x1", 4);
		validateIntValue(gvt, "x2", 1);
	}
	
	@Test
	public void returnFromBlockTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "fun_ret_04.jul"));
		
		validateIntValue(gvt, "x1", 4);
		validateIntValue(gvt, "x2", 1);
		validateIntValue(gvt, "a", 10);
	}
	
	@Test
	public void returnFromSwitchTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "fun_ret_05.jul"));
		
		validateStringValue(gvt, "s1", "One");
		validateStringValue(gvt, "s2", "Two");
		validateIntValue(gvt, "a", 10);
		validateIntValue(gvt, "b", 20);
	}
	
	@Test
	public void returnFromNestedLogicTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "fun_ret_06.jul"));
		
		validateIntValue(gvt, "x1", 40);
		validateIntValue(gvt, "x2", 100);
		validateIntValue(gvt, "x3", 600);
	}
	
	@Test
	public void returnVoidTest() throws EngineInvocationError {		
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.IMPERATIVE, FEATURE);
		runner.executeAndExpect("fun_ret_07.jul", IllegalMemberAccessException.referMemberOnNonObjectEx(""));
	}
}
