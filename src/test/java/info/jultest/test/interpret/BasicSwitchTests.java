package info.jultest.test.interpret;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateStringValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.value.TempValueFactory;

import org.junit.Test;

public class BasicSwitchTests {

	private static final String FEATURE = "Switch";
	
	@Test
	public void basicSwitchCase1Test() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		gvt.enterScope();
		gvt.addVariable("a", TempValueFactory.createTempIntValue(1));
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "switch_01.jul"));
		
		validateStringValue(gvt, "s", "One");
	}
	
	@Test
	public void basicSwitchCase2Test() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		gvt.enterScope();
		gvt.addVariable("a", TempValueFactory.createTempIntValue(2));
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "switch_01.jul"));
		
		validateStringValue(gvt, "s", "Two");
	}
	
	@Test
	public void basicSwitchDefaultTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		gvt.enterScope();
		gvt.addVariable("a", TempValueFactory.createTempIntValue(4));
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "switch_01.jul"));
		
		validateStringValue(gvt, "s", "Unknown");
	}
	
	@Test
	public void basicSwitchMatchWithoutDefaultTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		gvt.enterScope();
		gvt.addVariable("a", TempValueFactory.createTempIntValue(2));
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "switch_02.jul"));
		
		validateStringValue(gvt, "s", "Two");
	}
	
	@Test
	public void basicSwitchNoMatchWithoutDefaultTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		gvt.enterScope();
		gvt.addVariable("a", TempValueFactory.createTempIntValue(4));
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "switch_02.jul"));
		
		validateStringValue(gvt, "s", "None");
	}
	
	@Test
	public void emptySwitchTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "switch_03.jul"));
		
		validateStringValue(gvt, "s", "Nothing");
	}
	
	@Test
	public void basicSwitchDefaultOnlyTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "switch_05.jul"));
		
		validateStringValue(gvt, "s", "Unknown");
	}
	
	@Test
	public void fallThroughTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "switch_04.jul"));
		
		validateStringValue(gvt, "s", "Two");
	}
	
	@Test
	public void stringCaseTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "switch_06.jul"));
		
		validateStringValue(gvt, "s", "Two");
	}
	
	@Test
	public void varNameCaseTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "switch_07.jul"));
		
		validateStringValue(gvt, "s", "Two");
	}
}
