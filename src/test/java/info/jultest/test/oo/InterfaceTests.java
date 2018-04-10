package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class InterfaceTests {

	private static final String FEATURE = "Interface";
	
	@Test
	public void basicImplementTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "basic_01.jul"));
		
		validateIntValue(gvt, "life", 80);
	}
	
	@Test
	public void basicImplementTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "basic_02.jul"));
		
		validateIntValue(gvt, "life", 80);
	}
	
	@Test
	public void basicImplementTest3() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "basic_03.jul"));
		
		validateIntValue(gvt, "life1", 1);
		validateIntValue(gvt, "life2", 2);
		validateIntValue(gvt, "life3", 3);
	}
	
	@Test
	public void basicImplementTest4() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "basic_04.jul"));
		
		validateBoolValue(gvt, "hungry1", true);
		validateBoolValue(gvt, "hungry2", false);
		validateBoolValue(gvt, "equal", true);
		validateBoolValue(gvt, "empty", true);		
		validateBoolValue(gvt, "hungry3", true);
		validateBoolValue(gvt, "hungry4", false);
	}
	
	@Test
	public void basicCastingTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "cast_01.jul"));
		
		validateIntValue(gvt, "life", 80);
	}
	
	@Test
	public void basicCastingTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "cast_02.jul"));
		
		validateIntValue(gvt, "life1", 10);
		validateIntValue(gvt, "life2", 10);
	}
	
	@Test
	public void basicTypingTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "type_01.jul"));
		
		validateIntValue(gvt, "life", 80);
	}
	
	@Test
	public void basicTypingTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "type_02.jul"));
		
		validateBoolValue(gvt, "isAnimal", true);
		validateBoolValue(gvt, "isPerson", true);
	}
	
	@Test
	public void basicTypingTest3() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "type_03.jul"));
		
		validateBoolValue(gvt, "isAnimal", true);
		validateBoolValue(gvt, "isPerson", true);
	}
	
	@Test
	public void basicTypingTest4() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "type_04.jul"));
		
		validateBoolValue(gvt, "i1", true);
		validateBoolValue(gvt, "i2", true);
		validateBoolValue(gvt, "i3", true);
	}
	
	@Test
	public void advancedImplementTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "adv_01.jul"));

		validateBoolValue(gvt, "caught", true);
		validateBoolValue(gvt, "caught2", true);
	}
	
	@Test
	public void advancedImplementTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "adv_02.jul"));

		validateIntValue(gvt, "rate", 12);
	}
	
	@Test
	public void advancedImplementTest3() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "adv_03.jul"));

		validateIntValue(gvt, "r1", 88);
		validateIntValue(gvt, "r2", 300);
		validateIntValue(gvt, "r3", 88);
		validateBoolValue(gvt, "caught", true);
	}
	
	@Test
	public void advancedImplementTest4() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "adv_04.jul"));

		validateIntValue(gvt, "i", 5);
	}
	
	@Test
	public void advancedImplementTest5() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "adv_05.jul"));

		validateIntValue(gvt, "i", 5);
	}
}
