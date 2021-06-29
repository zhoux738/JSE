package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestsBase;
import info.jultest.test.TestExceptionHandler;
import info.julang.dev.GlobalSetting;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Assume;
import org.junit.Test;

// As of 0.1.0, all the exceptions are System.ClassLoadingException
public class InterfaceDefErrorTests extends ExceptionTestsBase {

	private static final String FEATURE = "InterfaceDefError";
	
	@Test
	public void basicInterfaceIllegalSemanticsTest1() throws EngineInvocationError {
		validateException("basic_01.jul");
	}
	
	@Test
	public void basicInterfaceIllegalSemanticsTest2() throws EngineInvocationError {
		validateException("basic_02.jul");
	}
	
	@Test
	public void basicInterfaceIllegalSemanticsTest3() throws EngineInvocationError {
		validateException("basic_03.jul");
	}
	
	@Test
	public void basicInterfaceIllegalSemanticsTest4() throws EngineInvocationError {
		validateException("basic_04.jul");
	}
	
	@Test
	public void basicInterfaceIllegalSemanticsTest5() throws EngineInvocationError {
		validateException("basic_05.jul");
	}
	
	@Test
	public void interfaceCannotExtendClassTest() throws EngineInvocationError {
		validateException("ext_01.jul");
	}
	
	@Test
	public void interfaceCannotExtendItselfTest() throws EngineInvocationError {
		validateException("ext_02.jul");
	}
	
	@Test
	public void cyclicExtensionTest() throws EngineInvocationError {
		validateException("ext_03.jul");
	}
	
	@Test
	public void inheritAtFirstPlaceTest() throws EngineInvocationError {
		validateException("impl_01.jul");
	}
	
	@Test
	public void inheritOnceTest() throws EngineInvocationError {
		validateException("impl_01.jul");
	}
	
	private void validateException(String script) throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, script));
		
		assertException(teh, "System.ClassLoadingException");
	}

}
