package info.jultest.test.interpret;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.value.TempValueFactory;

import org.junit.Test;

public class IfElseTests {

	private static final String FEATURE = "If";
	
	@Test
	public void basicIfTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		gvt.enterScope();
		gvt.addVariable("a", TempValueFactory.createTempIntValue(1));
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "if_01.jul"));
		
		validateIntValue(gvt, "b", 100);
	}
	
	@Test
	public void basicElseIfTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		gvt.enterScope();
		gvt.addVariable("a", TempValueFactory.createTempIntValue(2));
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "if_01.jul"));
		
		validateIntValue(gvt, "b", 200);
	}
	
	@Test
	public void basicElseTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		gvt.enterScope();
		gvt.addVariable("a", TempValueFactory.createTempIntValue(3));
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "if_01.jul"));
		
		validateIntValue(gvt, "b", 0);
	}
	
	@Test
	public void basicOneLinerIfElseTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "if_02.jul"));
		
		validateIntValue(gvt, "b", 100);
		validateIntValue(gvt, "c", 200);
	}
	
}
