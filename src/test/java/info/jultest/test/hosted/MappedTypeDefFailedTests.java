package info.jultest.test.hosted;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;

import org.junit.Assert;
import org.junit.Test;

import info.julang.execution.EngineRuntime;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.modulesystem.ModuleInfo;
import info.julang.typesystem.JType;
import info.jultest.test.AssertHelper;
import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestRunner;
import info.jultest.test.TestExceptionHandler;

public class MappedTypeDefFailedTests {
	
	private static final String FEATURE = "ClassDefError";
	
	// Map info.jultest.test.hosted.Error1
	// Failed inside static block
	@Test
	public void initializationFailureTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		TestExceptionHandler teh = new TestExceptionHandler();
		engine.setExceptionHandler(teh);
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "def_err_1.jul"));
		
		/*
		    System.PlatformClassLoadingException: Failed to load platform type "info.jultest.test.hosted.classes.Error1".
			  from  (..omitted../source/jse/src/test/julian/Interpret/Hosting/ClassDefError/def_err_1.jul, 6)
			Caused by:
			System.PlatformOriginalException: (java.lang.ExceptionInInitializerError) 
			  at ... (25 more)
			Caused by:
			System.PlatformOriginalException: (java.lang.ArithmeticException) / by zero
			  at info.jultest.test.hosted.classes.Error1.<clinit>(Error1.java:10)
			  at ... (more)
		 */
		String str = teh.getStandardExceptionOutput();
		AssertHelper.validateStringOccurences(str, 
			"System.PlatformClassLoadingException", 
			"Caused by:", "System.PlatformOriginalException", "java.lang.ExceptionInInitializerError", 
			"Caused by:", "System.PlatformOriginalException", "java.lang.ArithmeticException");
		
		validateNoTypesAdded(engine.getRuntime(), "Error1");
	}
	
	// Map info.jultest.test.hosted.Error2
	// Failed inside static field initializer
	@Test
	public void initializationFailureTest2() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.HOSTING, FEATURE);
		runner.executeAndExpect(
			"def_err_2.jul", 
			"System.PlatformClassLoadingException", 
			-1, 
			"System.PlatformOriginalException", 
			-1);
	}
	
	@Test
	public void classNotExistsTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.HOSTING, FEATURE);
		runner.executeAndExpect(
			"def_err_3.jul", 
			"System.PlatformClassLoadingException", 
			-1, 
			"System.PlatformOriginalException", 
			-1);
	}
	
	/*
	VariableTable gvt = new VariableTable(null);		
	SimpleScriptEngine engine = makeSimpleEngine(gvt);
	
	engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "def_err_4.jul"));
	*/
	
	@Test
	public void illegalInheritanceTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		TestExceptionHandler teh = new TestExceptionHandler();
		engine.setExceptionHandler(teh);
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "def_err_4.jul"));
		
		/*
			System.ClassLoadingException: Encountered an error when loading <default>.Obj1
			  from  (E:\Personal\Development\repo\jse\src\test\julian\Interpret\Hosting\ClassDefError\def_err_4.jul, 10)
			Caused by:
			System.Lang.BadSyntaxException: The definition of class "<default>.Obj1" is incorrect: A mapped type must not inherit from a non-mapped type.
			  from  (E:\Personal\Development\repo\jse\src\test\julian\Interpret\Hosting\ClassDefError\def_err_4.jul, 1)
		 */
		String str = teh.getStandardExceptionOutput();
		AssertHelper.validateStringOccurences(str, 
			"System.ClassLoadingException", 
			"Obj1",
			"Caused by:", 
			"A mapped type must not inherit from a non-mapped type");
		
		validateNoTypesAdded(engine.getRuntime(), "Error1");
	}
	
	private void validateNoTypesAdded(EngineRuntime rt, String... simpleNames){
		for(String sn : simpleNames) {
			JType t = rt.getTypeTable().getType(ModuleInfo.DEFAULT_MODULE_NAME + "." + sn, false);
			Assert.assertNull(t);
			
			Class<?> clazz = rt.getModuleManager().getHostedMethodManager().getMappedPlatformClass(
				"info.jultest.test.hosted.classes." + sn);
			Assert.assertNull(clazz);
		}
	}
	
	//-------------- Illegal definition --------------//
	
	@Test
	public void typeUnmatchedTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.HOSTING, FEATURE);
		runner.executeAndExpect("illegal_1.jul", KnownJSException.ClassLoading, null, "a script interface must map to a platform interface");
	}
	
	@Test
	public void customizedFieldTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.HOSTING, FEATURE);
		runner.executeAndExpect("illegal_2.jul", KnownJSException.ClassLoading, null, "Mapped type cannot have explicitly defined fields");
	}
	
	@Test
	public void customizedCtorTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.HOSTING, FEATURE);
		runner.executeAndExpect("illegal_3.jul", KnownJSException.ClassLoading, null, "A mapped class must not contain user-defined constructors.");
	}
	
	@Test
	public void typeDisallowedTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.HOSTING, FEATURE);
		runner.executeAndExpect("illegal_4.jul", KnownJSException.ClassLoading, null, "Can only map to a class or interface.");
	}
	
	@Test
	public void nonpublicDisallowedTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.HOSTING, FEATURE);
		runner.executeAndExpect("illegal_5.jul", KnownJSException.ClassLoading, null, "the platform type is not public");
	}
	
	@Test
	public void innerClassDisallowedTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.HOSTING, FEATURE);
		runner.executeAndExpect("illegal_6.jul", KnownJSException.ClassLoading, null, "the platform type is neither at top level nor statically nested.");
	}
}