package info.jultest.test.interpret;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class DoWhileTests {
	
	private static final String FEATURE = "DoWhile";
	
	@Test
	public void basicWhileLoopTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "do_while_01.jul"));
		
		validateIntValue(gvt, "i", 5);
	}
	
	@Test
	public void basicWhileLoopBreakTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "do_while_02.jul"));
		
		validateIntValue(gvt, "i", 4);
	}
	
	@Test
	public void basicWhileLoopContinueTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "do_while_03.jul"));
		
		validateIntValue(gvt, "sum", 800);
	}
	
	@Test
	public void basicWhileLoopJustBreakTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "do_while_04.jul"));
		
		validateIntValue(gvt, "i", 3);
	}
	
	@Test
	public void basicWhileLoopJustContinueTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "do_while_05.jul"));
		
		validateIntValue(gvt, "i", 3);
	}
	
}
