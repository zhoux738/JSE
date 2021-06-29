package info.jultest.test.interpret;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestRunner;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class BasicForEachLoopTests {

	private static final String FEATURE = "ForEach";
	
	@Test
	public void basicForEachLoopOnArrayTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "foreach_01.jul"));
		
		validateIntValue(gvt, "sum", 1000);
	}
	
	@Test
	public void basicForEachLoopAltSyntaxTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "foreach_01_alt_1.jul"));
		
		validateIntValue(gvt, "sum", 1000);
	}
	
	@Test
	public void basicForEachLoopAltSyntaxTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "foreach_01_alt_2.jul"));
		
		validateIntValue(gvt, "sum", 1000);
	}
	
	@Test
	public void basicForEachLoopOnArrayTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "foreach_02.jul"));
		
		validateIntValue(gvt, "sum", 1000);
	}
	
	@Test
	public void basicForEachLoopOnEmptyArrayTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "foreach_03.jul"));
		
		validateIntValue(gvt, "sum", 1000);
	}
	
	@Test
	public void basicForEachLoopOnArrayTest3() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "foreach_04.jul"));
		
		validateIntValue(gvt, "sum", 1000);
	}
	
	@Test
	public void basicForEachLoopBreakTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "foreach_05.jul"));
		
		validateIntValue(gvt, "sum", 300);
	}
	
	@Test
	public void basicForEachLoopContinueTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "foreach_06.jul"));
		
		validateIntValue(gvt, "sum", 700);
	}
	
	@Test
	public void basicForEachLoopReturnTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "foreach_07.jul"));
		
		validateIntValue(gvt, "result", 200);
	}
	
	@Test
	public void basicForEachTwiceTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "foreach_08.jul"));
		
		validateIntValue(gvt, "sum", 2000);
	}
	
	@Test
	public void basicForEachLoopOnListTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "foreach_list_01.jul"));
		
		validateIntValue(gvt, "sum", 600);
	}
	
	@Test
	public void basicForEachLoopOnEmptyListTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "foreach_list_02.jul"));
		
		validateIntValue(gvt, "sum", 600);
	}
	
	@Test
	public void forEachOnNullTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.IMPERATIVE, FEATURE);
		runner.executeAndValidate(
			"foreach_list_03.jul", 
			"System.NullReferenceException", 
			"Cannot dereference a null value.", 
			new String[]{
				"fun()  (/.../foreach_list_03.jul, 6)"
			},
			false, 
			11);
	}
	
	@Test
	public void forEachOnNonIterableTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.IMPERATIVE, FEATURE);
		runner.executeAndValidate(
			"foreach_fault.jul", 
			"System.Lang.RuntimeCheckException", 
			"Cannot perform iteration over a non-iterable object.", 
			null, 
			false, 
			2);
	}	
}
