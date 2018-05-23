package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.runInline;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolArrayValue;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateHostedValue;
import static info.jultest.test.Commons.validateNullValue;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.EFCommons.runViaFactory;
import static info.jultest.test.EFCommons.validateIntValue;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.interfaces.IExtEngineRuntime;
import info.julang.external.interfaces.IExtVariableTable;
import info.julang.typesystem.jclass.jufc.System.ScriptType;
import info.jultest.test.Commons;
import info.jultest.test.EFCommons;
import info.jultest.test.TestExceptionHandler;
import info.jultest.test.oo.ExceptionTestsBase;

import java.io.IOException;

import org.junit.Test;

public class System_Reflection_Method_ReflTestSuite extends ExceptionTestsBase {

	private static final String FEATURE = "Reflection/Methods";
	
	// List methods
	@Test
	public void getMethodTest1() throws EngineInvocationError, IOException {		
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "get_method_1.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		EFCommons.validateBoolValue(gvt, "res", true);
	}
	
	// List methods from a derived class
	@Test
	public void getMethodTest2() throws EngineInvocationError, IOException {		
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "get_method_2.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		EFCommons.validateIntValue(gvt, "remaining", 1);
	}
	
	// value type == method decl type
	@Test
	public void callTest1() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "call_method_1.jul");
		Commons.validateIntValue(gvt, "res", 20);
	}
	
	// value type : method decl type
	@Test
	public void callTest2() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "call_method_2.jul");

		Commons.validateIntValue(gvt, "res2", 20);
		Commons.validateIntValue(gvt, "res3", 30);
		Commons.validateIntValue(gvt, "res4", 40);
		Commons.validateIntValue(gvt, "res2a", 20);
		Commons.validateIntValue(gvt, "res3a", 30);
		Commons.validateIntValue(gvt, "res4a", 40);
	}
	
	// method decl type : value type
	@Test
	public void callTest3() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "call_method_3.jul");

		Commons.validateIntValue(gvt, "res1", 20);
		Commons.validateIntValue(gvt, "res2", 20);
		Commons.validateIntValue(gvt, "res1a", 20);
		Commons.validateIntValue(gvt, "res2a", 20);
	}
	
	// value type is invalid for method decl type
	@Test
	public void callTest4() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "call_method_4.jul");

		Commons.validateIntValue(gvt, "res1", 14);
		Commons.validateIntValue(gvt, "res2", 22);
	}
	
	// target method throws
	@Test
	public void callTest5() throws EngineInvocationError, IOException {
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "call_method_5.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		EFCommons.validateBoolValue(gvt, "b", true);
	}
	
	// call instance method without wrong arguments
	@Test
	public void callTest6() throws EngineInvocationError, IOException {
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "call_method_6.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		EFCommons.validateBoolValue(gvt, "b0", true);
		EFCommons.validateBoolValue(gvt, "b1", true);
		EFCommons.validateBoolValue(gvt, "b2", true);
		EFCommons.validateBoolValue(gvt, "b3", true);
	}
	
	// 	value type == method decl type 
	// OR 
	//  value type : method decl type
	@Test
	public void callExactTest1() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "call_exact_1.jul");

		Commons.validateIntValue(gvt, "res0a", 20);
		Commons.validateIntValue(gvt, "res3a", 20);
		Commons.validateIntValue(gvt, "res4a", 20);
	}
	
	// value type is invalid for method decl type
	@Test
	public void callExactTest2() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "call_exact_2.jul");
		
		Commons.validateBoolValue(gvt, "r0", true);
		Commons.validateBoolValue(gvt, "r1", true);
		Commons.validateBoolValue(gvt, "r2", true);
	}
	
	//  value type : method decl type
	//  method is calling other private methods on decl type
	@Test
	public void callExactTest3() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "call_exact_3.jul");

		Commons.validateIntValue(gvt, "res0", 20);
		Commons.validateIntValue(gvt, "res0a", 20);
	}
	
	@Test
	public void callStaticTest1() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "call_static_1.jul");

		Commons.validateIntValue(gvt, "res1", 20);
		Commons.validateIntValue(gvt, "res2", 20);
		Commons.validateIntValue(gvt, "res3", 20);
	}
	
	@Test
	public void callInterfaceTest1() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "call_interface_1.jul");

		Commons.validateIntValue(gvt, "res2", 20);
		Commons.validateIntValue(gvt, "res3", 30);
		Commons.validateIntValue(gvt, "res4", 40);
	}
	
	// Call interface and abstract methods exactly => must fail
	@Test
	public void callInterfaceTest2() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "call_interface_2.jul");

		Commons.validateBoolValue(gvt, "r0", true);
		Commons.validateBoolValue(gvt, "r1", true);
		Commons.validateIntValue(gvt, "val", 30);
	}	
	
	// access array via reflection
	@Test
	public void arrayAccess1() throws EngineInvocationError, IOException {
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "array_access_1.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		EFCommons.validateIntValue(gvt, "v2", 2);
		EFCommons.validateIntValue(gvt, "v20", 20);
	}
	
	// bind method
	@Test
	public void bindAccess1() throws EngineInvocationError, IOException {
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "bind_1.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		EFCommons.validateIntValue(gvt, "i", 10);
		EFCommons.validateIntValue(gvt, "j", 24);
	}
}
