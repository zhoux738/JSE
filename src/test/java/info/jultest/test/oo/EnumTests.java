package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import info.jultest.test.Commons;
import info.julang.dev.GlobalSetting;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.value.AttemptToChangeConstException;

import org.junit.Assume;
import org.junit.Test;

public class EnumTests {

	private static final String FEATURE = "Enum";
	
	@Test
	public void basicEnumDeclarationTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "enum_1.jul"));
		
		// string literal = p.literal;
		// int ordinal = p.ordinal;
		validateStringValue(gvt, "literal", "Venus");
		validateIntValue(gvt, "ordinal", 0);
	}
	
	@Test
	public void enumVarDeclWithInitTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "enum_2.jul"));
		
		validateStringValue(gvt, "literal", "Mars");
		validateIntValue(gvt, "ordinal", 2);
	}
	
	@Test
	public void enumAssignmentTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "enum_3.jul"));
		
		validateStringValue(gvt, "l1", "Mars");
		validateStringValue(gvt, "l2", "Earth");
		validateStringValue(gvt, "l3", "Mars");
	}
	
	@Test
	public void enumComparisonTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "enum_4.jul"));
		
		validateBoolValue(gvt, "b1", false);
		validateBoolValue(gvt, "b2", true);
	}
	
	//	enum Planet {
	//		Venus = 10,
	//		Earth,
	//		Mars = 30
	//	}
	@Test
	public void enumCustomOrdinalTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "enum_5.jul"));
		
		validateIntValue(gvt, "o1", 10);
		validateIntValue(gvt, "o2", 11);
		validateIntValue(gvt, "o3", 30);
	}
	
	// Object.toString()
	@Test
	public void enumToStringTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "enum_6.jul"));
		
		validateStringValue(gvt, "s", "Mars");
	}
	
	// ordinal/literal is not changeable
	@Test
	public void enumConstFields1Test() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.OO, FEATURE);
		runner.executeAndExpect("enum_illegal_1.jul", new AttemptToChangeConstException());
	}
	
	// Enum values are not changeable
	@Test
	public void enumConstFields2Test() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.OO, FEATURE);
		runner.executeAndExpect("enum_illegal_2.jul", new AttemptToChangeConstException());
	}
	
	@Test
	public void enumSwitchTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "enum_switch_1.jul"));
		
		validateStringValue(gvt, "s", "DQ");
	}
}
