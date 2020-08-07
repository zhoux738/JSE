package info.jultest.test.interpret;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.Commons.resetTypeSystem;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class AdvancedSwitchTests {
	
	private static final String FEATURE = "Switch";
	
	@Test
	public void forAndSwitchTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "switch_adv_01.jul"));
		
		validateIntValue(gvt, "sum", 302);
	}
	
	@Test
	public void forAndSwitchAndIfContinueTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "switch_adv_02.jul"));
		
		validateIntValue(gvt, "sum", 1401);
	}
	
	@Test
	public void forAndSwitchContinueTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "switch_adv_03.jul"));
		
		validateIntValue(gvt, "sum", 402);
	}
	
	@Test
	public void nestedSwitchTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "switch_adv_04.jul"));
		
		validateStringValue(gvt, "s", "Two Beta");
	}
	
	@Test
	public void varNameAndEnumNameOverlapTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "switch_adv_05.jul"));
		
		validateStringValue(gvt, "s", "F");
	}
	
	@Test
	public void switchOverTypeofTest() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "switch_adv_06.jul"));
		
		validateStringValue(gvt, "s", "B");
	}
}
