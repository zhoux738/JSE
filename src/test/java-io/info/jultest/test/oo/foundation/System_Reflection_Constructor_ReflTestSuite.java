package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.runInline;
import static info.jultest.test.EFCommons.runViaFactory;

import java.io.IOException;

import org.junit.Test;

import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.interfaces.IExtEngineRuntime;
import info.julang.external.interfaces.IExtVariableTable;
import info.jultest.test.Commons;
import info.jultest.test.EFCommons;
import info.jultest.test.oo.ExceptionTestsBase;

public class System_Reflection_Constructor_ReflTestSuite extends ExceptionTestsBase {

	private static final String FEATURE = "Reflection/Constructors";
	
	// Success at 1-arg ctor
	@Test
	public void getCtorTest1() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "get_ctor_1.jul");

		Commons.validateIntValue(gvt, "val", 10);
	}
	
	// Failure caused by ctor's inner logic
	@Test
	public void getCtorTest2() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "get_ctor_2.jul");
		
		Commons.validateStringValue(gvt, "res", "Argument 'value' is invalid.");
	}
	
	// Failure caused by wrong args to ctor
	@Test
	public void callCtorTest1() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "call_ctor_1.jul");
		
		Commons.validateBoolValue(gvt, "res", true);
	}
	
	// Failure caused by invoking abstract ctor
	@Test
	public void callCtorTest2() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "call_ctor_2.jul");
		
		Commons.validateBoolValue(gvt, "res", true);
	}
	
	// Call super ctor
	@Test
	public void callCtorTest3() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "call_ctor_3.jul");
		
		Commons.validateStringValue(gvt, "s", "abc");
		Commons.validateIntValue(gvt, "i", 3);
	}
	
	// Ctor throws
	@Test
	public void callCtorTest4() throws EngineInvocationError, IOException {
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "call_ctor_4.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();
		
		EFCommons.validateBoolValue(gvt, "b", true);
	}
	
	// Get multiple ctors from single class
	@Test
	public void getCtorTest3() throws EngineInvocationError, IOException {		
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "get_ctor_3.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		EFCommons.validateStringValue(gvt, "s0", "(none)-10");
		EFCommons.validateStringValue(gvt, "s1", "ABC-20");
	}
	
	@Test
	public void getJuFCCtorTest1() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "get_ctor_jufc_1.jul");
		
		Commons.validateIntValue(gvt, "totalList", 1);
		Commons.validateIntValue(gvt, "totalType", 0);
	}
	
	@Test
	public void getJuFCCtorTest2() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "get_ctor_jufc_2.jul");
		
		Commons.validateIntValue(gvt, "totalCtors1a", 1);
		Commons.validateIntValue(gvt, "totalCtors1b", 1);
		Commons.validateIntValue(gvt, "totalCtors0a", 0);
		Commons.validateIntValue(gvt, "totalCtors0b", 0);
	}
	
	@Test
	public void getParamsTest1() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "get_ctor_params_1.jul");

		Commons.validateBoolValue(gvt, "res1", true);
		Commons.validateBoolValue(gvt, "res2", true);
		Commons.validateBoolValue(gvt, "res3", true);
	}
}
