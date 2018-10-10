package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.runInline;
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

public class System_Reflection_Field_ReflTestSuite extends ExceptionTestsBase {

	private static final String FEATURE = "Reflection/Fields";
	
	// List fields
	@Test
	public void getFieldTest1() throws EngineInvocationError, IOException {		
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "get_field_1.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		EFCommons.validateBoolValue(gvt, "res", true);
	}
	
	// List fields in hierarchical classes
	@Test
	public void getFieldTest2() throws EngineInvocationError, IOException {		
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "get_field_2.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		EFCommons.validateBoolValue(gvt, "b1", true);
		EFCommons.validateBoolValue(gvt, "b2", true);
		EFCommons.validateBoolValue(gvt, "b3", true);
	}
	
	@Test
	public void getTest1() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "get_1.jul");

		Commons.validateIntValue(gvt, "i", 5);
		Commons.validateStringValue(gvt, "str", "abc");
		Commons.validateStringValue(gvt, "arr0", "uvw");
		Commons.validateStringValue(gvt, "arr1", "def");
	}
	
	@Test
	public void getTest2() throws EngineInvocationError, IOException {	
		VariableTable gvt = runInline(FEATURE, "get_2.jul");

		Commons.validateIntValue(gvt, "i", 5);
		Commons.validateStringValue(gvt, "str", "abc");
		Commons.validateStringValue(gvt, "arr0", "uvw");
		Commons.validateStringValue(gvt, "arr1", "def");
	}
	
	@Test
	public void getTest3() throws EngineInvocationError, IOException {	
		VariableTable gvt = runInline(FEATURE, "get_3.jul");

		Commons.validateBoolValue(gvt, "checked", true);
	}
	
	@Test
	public void getTest2Fail() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "get_2_fail.jul");

		Commons.validateIntValue(gvt, "i", 10);
		Commons.validateBoolValue(gvt, "b0", true);
		Commons.validateBoolValue(gvt, "b1", true);
	}
	
	@Test
	public void setFieldTest1() throws EngineInvocationError, IOException {
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "set_1.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		EFCommons.validateIntValue(gvt, "i", 7);
		EFCommons.validateStringValue(gvt, "s0", "abc");
		EFCommons.validateStringValue(gvt, "s1", "def");
		EFCommons.validateStringValue(gvt, "s2", "rty");
	}
	
	@Test
	public void setFieldTest2() throws EngineInvocationError, IOException {	
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "set_2.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		EFCommons.validateStringValue(gvt, "s", "7_def");
	}
	
	@Test
	public void setFieldTest3() throws EngineInvocationError, IOException {	
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "set_3.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();
		
		EFCommons.validateIntValue(gvt, "ip", 0);
		EFCommons.validateIntValue(gvt, "ic", 5);
		EFCommons.validateBoolValue(gvt, "b", true);
	}
	
	@Test
	public void setFieldTest4() throws EngineInvocationError, IOException {	
		VariableTable gvt = runInline(FEATURE, "set_4.jul");

		Commons.validateIntValue(gvt, "ic", 5);
	}
	
	// const field
	@Test
	public void setFieldTest5() throws EngineInvocationError, IOException {	
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "set_5.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		EFCommons.validateBoolValue(gvt, "b0", true);
		EFCommons.validateBoolValue(gvt, "b1", true);
	}
	
	@Test
	public void setFieldTest6() throws EngineInvocationError, IOException {	
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "set_6.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		EFCommons.validateBoolValue(gvt, "b1", true);
	}
	
}
