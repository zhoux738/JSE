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

public class System_Reflection_Module_ReflTestSuite extends ExceptionTestsBase {

	private static final String FEATURE = "Reflection/Module";
	
	// System module
	@Test
	public void findModuleTest1() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "find_1.jul");

		Commons.validateBoolValue(gvt, "b", true);
		Commons.validateStringValue(gvt, "s", "System");
	}
	
	// User module
	@Test
	public void findModuleTest2() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "find_2.jul");

		Commons.validateBoolValue(gvt, "b", true);
		Commons.validateStringValue(gvt, "s", "ModuleSys.FullMod");
		Commons.validateStringValue(gvt, "s2", "[MODULE|ModuleSys.FullMod]");
	}
	
	// System types
	@Test
	public void listTypesTest() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "list_1.jul");

		Commons.validateBoolValue(gvt, "b0", true);
		Commons.validateBoolValue(gvt, "b1", true);
		Commons.validateBoolValue(gvt, "b2", false);
	}
	
	// User scripts
	@Test
	public void listScriptsTest() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "scripts_1.jul");

		Commons.validateBoolValue(gvt, "b0", true);
		Commons.validateBoolValue(gvt, "b1", true);
		Commons.validateBoolValue(gvt, "b2", false);
		Commons.validateBoolValue(gvt, "b3", true);
	}
	
	// User scripts
	@Test
	public void loadFromModuleTest() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "load_1.jul");

		Commons.validateStringValue(gvt, "s", "MyClass");
	}
	
	// User scripts
	@Test
	public void getModuleFromTypeTest1() throws EngineInvocationError, IOException {
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "get_1.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		EFCommons.validateBoolValue(gvt, "b", true);
		EFCommons.validateStringValue(gvt, "s", "ModuleSys.FullMod");
		EFCommons.validateStringValue(gvt, "s2", "[MODULE|ModuleSys.FullMod]");
	}
	
	// System scripts
	@Test
	public void getModuleFromTypeTest2() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "get_2.jul");

		Commons.validateBoolValue(gvt, "b1", true);
		Commons.validateBoolValue(gvt, "b2", true);
		Commons.validateBoolValue(gvt, "b3", true);
	}
}
