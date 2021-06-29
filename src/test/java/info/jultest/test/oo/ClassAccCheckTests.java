package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;

import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestsBase;
import info.jultest.test.TestExceptionHandler;
import info.julang.dev.GlobalSetting;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Assume;
import org.junit.Test;

public class ClassAccCheckTests extends ExceptionTestsBase {

	private static final String FEATURE = "ClassAccCheck";
	
	@Test
	public void accessToParentPrivateMemberByNameTest() throws EngineInvocationError {
		runAndValidate("inst_by_name.jul", "System.IllegalMemberAccessException");
	}
	
	@Test
	public void accessToParentPrivateMemberByThisTest() throws EngineInvocationError {
		runAndValidate("inst_by_this.jul", "System.IllegalMemberAccessException");
	}
	
	@Test
	public void accessToParentPrivateMemberBySuperTest() throws EngineInvocationError {
		runAndValidate("inst_by_super.jul", "System.IllegalMemberAccessException");
	}
	
	@Test
	public void accessToStaticPrivateMemberTest1() throws EngineInvocationError {
		runAndValidate("static_1.jul", "System.IllegalMemberAccessException");
	}
	
	@Test
	public void accessToStaticPrivateMemberTest2() throws EngineInvocationError {
		runAndValidate("static_2.jul", "System.IllegalMemberAccessException");
	}
	
	@Test
	public void accessFromLambdaInFuncTest() throws EngineInvocationError {
		runAndValidate("lambda_in_func.jul", "System.IllegalMemberAccessException");
	}
	
	@Test
	public void accessFromLambdaInInstMethodTest() throws EngineInvocationError {
		runAndValidate("lambda_in_imethod.jul", "System.IllegalMemberAccessException");
	}
	
	@Test
	public void accessFromLambdaInStaticMethodTest() throws EngineInvocationError {
		runAndValidate("lambda_in_smethod.jul", "System.IllegalMemberAccessException");
	}
	
	@Test
	public void accessFromLambdaInLambdaTest() throws EngineInvocationError {
		runAndValidate("lambda_in_lambda.jul", "System.IllegalMemberAccessException");
	}
	
	//------------------------- internal (module) visibility -------------------------//
	
	@Test
	public void accessToInternalMemberTest1() throws EngineInvocationError {
		runAndValidate("internal_1.jul", "System.IllegalMemberAccessException");
	}
	
	@Test
	public void accessToInternalMemberTest2() throws EngineInvocationError {
		runAndValidate("internal_2.jul", "System.IllegalMemberAccessException");
	}
	
	@Test
	public void accessToInternalMemberTest3() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "internal_3.jul"));

		validateIntValue(gvt, "val", 3);
	}
	
	@Test
	public void accessToInternalMemberTest4() throws EngineInvocationError {
		runAndValidate("internal_4.jul", "System.IllegalMemberAccessException");
	}
	

	@Test
	public void accessToInternalTypeTest1() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "internal_type_1.jul"));
		
		assertCause(teh, null, "System.IllegalTypeAccessException");
	}
	
	@Test
	public void accessToInternalTypeTest2() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "internal_type_2.jul"));

		assertCause(teh, null, "System.IllegalTypeAccessException");
	}
	
	@Test
	public void accessToInternalTypeTest3() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "internal_type_3.jul"));

		assertCause(teh, null, "System.IllegalTypeAccessException");
	}
	
	@Test
	public void accessToInternalTypeTest4() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "internal_type_4.jul"));

		assertCause(teh, null, "System.IllegalTypeAccessException");
	}
	
	@Test
	public void accessToInternalMemberTest5() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		//engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "internal_type_5.jul"));

		validateIntValue(gvt, "vc", 5);
	}
	
	// Access from global function def
	@Test
	public void accessToInternalTypeTest6() throws EngineInvocationError {
		runAndValidate("internal_type_6.jul", "System.IllegalTypeAccessException");
	}

	// Access from global function def
	@Test
	public void accessToInternalTypeTest7() throws EngineInvocationError {
		runAndValidate("internal_type_7.jul", "System.IllegalTypeAccessException");
	}
	
	// Access by local var decl
	@Test
	public void accessToInternalTypeTest8() throws EngineInvocationError {
		runAndValidate("internal_type_8.jul", "System.IllegalTypeAccessException");
	}
	
	// Access by local var decl
	@Test
	public void accessToInternalTypeTest9() throws EngineInvocationError {
		runAndValidate("internal_type_9.jul", "System.IllegalTypeAccessException");
	}
	
	private void runAndValidate(String script, String exception) throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, script));

		assertException(teh, exception);
		
	}
}
