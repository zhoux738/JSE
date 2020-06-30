package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;

import org.junit.Assume;
import org.junit.Test;

import info.julang.dev.GlobalSetting;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.jultest.test.Commons;
import info.jultest.test.TestExceptionHandler;

// As of 0.1.0, all the exceptions are System.ClassLoadingException
public class ClassDefErrorTests extends ExceptionTestsBase {

	private static final String FEATURE = "ClassDefError";
	
	@Test
	public void abstractMethodInConcreteClassTest() throws EngineInvocationError {
		validateClassLoadingException("item_01.jul");
	}
	
	@Test
	public void abstractAndStaticMethodTest() throws EngineInvocationError {
		validateClassLoadingException("item_02.jul");
	}
	
	@Test
	public void duplicateIdentifiersTest() throws EngineInvocationError {
		validateClassLoadingException("item_03.jul");
	}
	
	@Test
	public void duplicateReturnTypesTest() throws EngineInvocationError {
		validateSyntaxException("item_04.jul", 4);
	}
	
	@Test
	public void duplicateReturnTypesTest2() throws EngineInvocationError {
		validateSyntaxException("item_05.jul", 4);
	}
	
	@Test
	public void illegalOrderOfModifiersTest() throws EngineInvocationError {
		validateSyntaxException("item_06.jul", 4);
	}
	
	@Test
	public void multipleAccessModifiersTest() throws EngineInvocationError {
		validateClassLoadingException("item_07.jul");
	}
	
	@Test
	public void noReturnTypeTest() throws EngineInvocationError {
		validateClassLoadingException("item_08.jul");
	}
	
	@Test
	public void badSyntaxTest() throws EngineInvocationError {
		validateSyntaxException("item_09.jul", 4);
	}
	
	@Test
	public void sameInstMemberNameTest() throws EngineInvocationError {
		validateClassLoadingException("item_10.jul");
	}
	
	@Test
	public void sameStaticMemberNameTest() throws EngineInvocationError {
		validateClassLoadingException("item_11.jul");
	}
	
	@Test
	public void badTypeNameTest() throws EngineInvocationError {
		validateClassLoadingException("item_12.jul");
	}
	
	@Test
	public void reducedVisibilityTest() throws EngineInvocationError {
		validateClassLoadingException("inherit_1.jul");
	}
	
	@Test
	public void incompatibleMemberTypesTest() throws EngineInvocationError {
		validateClassLoadingException("inherit_2.jul");
	}
	
	@Test
	public void cyclicDependentTypesTest() throws EngineInvocationError {
		validateClassLoadingException("inherit_3.jul");
	}
	
	@Test
	public void illelgalBodylessMethodDeclTest() throws EngineInvocationError {
		validateClassLoadingException("inherit_4.jul");
	}
	
	@Test
	public void illelgalParentTypeTest() throws EngineInvocationError {
		validateClassLoadingException("inherit_5.jul");
	}
	
	@Test
	public void illelgalStaticClassWithInstanceMembersTest() throws EngineInvocationError {
		validateClassLoadingException("env_1.jul");
	}
	
	@Test
	public void illelgalStaticClassWithCtorTest() throws EngineInvocationError {
		validateClassLoadingException("env_2.jul");
	}
	
	@Test
	public void illelgalStaticClassImplementsInterfaceTest() throws EngineInvocationError {
		validateClassLoadingException("env_3.jul");
	}
	
	@Test
	public void illelgalStaticClassInheritsClassTest() throws EngineInvocationError {
		validateClassLoadingException("env_4.jul");
	}
	
	@Test
	public void illelgalStaticClassOverridesObjectMethodTest() throws EngineInvocationError {
		validateClassLoadingException("env_5.jul");
	}
	
	@Test
	public void illelgalStaticClassInheritsNonStaticClassTest() throws EngineInvocationError {
		validateClassLoadingException("env_6.jul");
	}
	
	private void validateClassLoadingException(String script) throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		if (autoValidation){
			TestExceptionHandler teh = installExceptionHandler(engine);
			
			engine.run(getScriptFile(Commons.Groups.OO, FEATURE, script));
			
			assertException(teh, "System.ClassLoadingException");
		} else {
			engine.run(getScriptFile(Commons.Groups.OO, FEATURE, script));
		}
	}
	
	private void validateSyntaxException(String script, int lineNo) throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		if (autoValidation){
			TestExceptionHandler teh = installExceptionHandler(engine);
			
			engine.run(getScriptFile(Commons.Groups.OO, FEATURE, script));

			validateException(
				teh, 
				"System.Lang.BadSyntaxException",
				null,
				null,
				null,
				lineNo);		
		} else {
			engine.run(getScriptFile(Commons.Groups.OO, FEATURE, script));
		}
	}

	// For dev purpose only (should be left to true in source repo)
	private static boolean autoValidation = true; //true // false
}
