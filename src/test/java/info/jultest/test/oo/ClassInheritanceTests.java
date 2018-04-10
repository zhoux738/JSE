package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import info.jultest.test.Commons;
import info.jultest.test.module.TestLoadingState;
import info.julang.dev.GlobalSetting;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.value.IllegalAssignmentException;
import info.julang.typesystem.loading.ClassLoadingException;

import org.junit.Assume;
import org.junit.Test;

public class ClassInheritanceTests {

	private static final String FEATURE = "Inheritance";
	
	@Test
	public void inheritFieldTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "person_1.jul"));
		
		validateIntValue(gvt, "i", 32);
	}
	
	@Test
	public void inheritMethodTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "person_2.jul"));
		
		validateIntValue(gvt, "i", 32);
	}
	
	@Test
	public void assignToParentTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "person_3.jul"));
		
		validateIntValue(gvt, "i", 32);
	}
	
	@Test
	public void assignToNonParentTest() throws EngineInvocationError {		
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.OO, FEATURE);
		runner.executeAndExpect("person_4.jul", new IllegalAssignmentException(null));
	}
	
	@Test
	public void overrideParentMethodTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "person_5.jul"));
		
		validateStringValue(gvt, "s", "Employee");
	}
	
	@Test
	public void cannotExposeChildMethodTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "person_6.jul"));
		
		validateBoolValue(gvt, "caught", true);
	}

	@Test
	public void inheritFromFinalClassTest() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.OO, FEATURE);
		runner.executeAndExpect("item_1.jul", new ClassLoadingException(new TestLoadingState("", null, false)));
	}
	
	// C can have a private method with same name in P.
	@Test
	public void visibilityTest1() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "visibility_1.jul"));
		
		validateBoolValue(gvt, "b", true);
		validateStringValue(gvt, "s", "Employee");
	}
	
	// C can have a private method M1 with same name in P. Calling P's public method M2
	// that calls M1 will call M1 in P not C.
	@Test
	public void visibilityTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "visibility_2.jul"));
		
		validateStringValue(gvt, "s", "Person");
	}
}
