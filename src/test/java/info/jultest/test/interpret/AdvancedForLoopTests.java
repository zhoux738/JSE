package info.jultest.test.interpret;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class AdvancedForLoopTests {

	private static final String FEATURE = "For";
	
	@Test
	public void nestedForLoopWithSingleExpressionTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "for_adv_01.jul"));
		
		validateIntValue(gvt, "sum", 30);
	}
	
	@Test
	public void nestedForLoopWithBreakTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "for_adv_02.jul"));
		
		validateIntValue(gvt, "sum", 21);
	}
	
	@Test
	public void nestedForLoopWithDoubleBreaksTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "for_adv_03.jul"));
		
		validateIntValue(gvt, "sum", 11);
	}
	
	@Test
	public void nestedForLoopWithContinueTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "for_adv_04.jul"));
		
		validateIntValue(gvt, "sum", 27);
	}
	
	@Test
	public void forLoopWithoutBodyTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "for_adv_05.jul"));
		
		validateIntValue(gvt, "i", 3);
	}
}
