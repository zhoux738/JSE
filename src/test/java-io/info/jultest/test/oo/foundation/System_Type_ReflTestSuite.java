package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolArrayValue;
import static info.jultest.test.Commons.validateNullValue;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.EFCommons.runViaFactory;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.interfaces.IExtEngineRuntime;
import info.julang.external.interfaces.IExtVariableTable;
import info.julang.typesystem.AnyType;
import info.jultest.test.Commons;
import info.jultest.test.EFCommons;
import info.jultest.test.TestExceptionHandler;
import info.jultest.test.oo.ExceptionTestsBase;

import java.io.IOException;

import org.junit.Test;

public class System_Type_ReflTestSuite extends ExceptionTestsBase {

	private static final String FEATURE = "Reflection";
	
	//-------- Reflection/GetType --------//
	
	@Test
	public void getFromObjectTest1() throws EngineInvocationError, IOException {		
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "GetType/get_from_object_1.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

//		EFCommons.validateHostedValue(gvt, "t1", ScriptType.class);	
		EFCommons.validateBoolValue(gvt, "bt0", true);
		EFCommons.validateBoolValue(gvt, "bt1", true);
		EFCommons.validateBoolValue(gvt, "bt2", true);
		EFCommons.validateBoolValue(gvt, "bt3", true);
	}	
	
	@Test
	public void getFromTypeTest1() throws EngineInvocationError, IOException {
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "GetType/get_from_type_1.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

//		EFCommons.validateHostedValue(gvt, "t1", ScriptType.class);	
		EFCommons.validateBoolValue(gvt, "bt0", true);
		EFCommons.validateBoolValue(gvt, "bt1", true);
		EFCommons.validateBoolValue(gvt, "bt2", true);
		EFCommons.validateBoolValue(gvt, "bt3", true);
		EFCommons.validateBoolValue(gvt, "bt4", true);
	}
	
	@Test
	public void getFromTypeTest2() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "GetType/get_from_type_2.jul"));
		
		validateException(
			teh, 
			"System.UnknownTypeException",
			null,
			null,
			null,
			9);
	}
	
	@Test
	public void getFromBuiltInTest() throws EngineInvocationError, IOException {
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "GetType/get_from_builtin.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		EFCommons.validateBoolValue(gvt, "b0", true);
		EFCommons.validateBoolValue(gvt, "b1", true);
	}
	
	//-------- Reflection/BasicInfo --------//
	
	@Test
	public void getNameTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "BasicInfo/get_name.jul"));
		
		validateStringValue(gvt, "fn", "ModuleSys.FullMod.MyClass");
		validateStringValue(gvt, "mn", "ModuleSys.FullMod");
		validateStringValue(gvt, "sn", "MyClass");
		
		validateStringValue(gvt, "fn2", "Integer");
		validateNullValue(gvt, "mn2");
		validateStringValue(gvt, "sn2", "Integer");
		
		validateStringValue(gvt, "fn3", "String");
		validateNullValue(gvt, "mn3");
		validateStringValue(gvt, "sn3", "String");
		
		validateStringValue(gvt, "fn4", "[Integer]");
		validateNullValue(gvt, "mn4");
		validateStringValue(gvt, "sn4", "[Integer]");
	}
	
	@Test
	public void getBasicInfoTest1() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "BasicInfo/get_basic_1.jul"));
		
		/*
		ar[0] = t.isPrimitive();
		ar[1] = t.isClass();
		ar[2] = t.isInterface();
		ar[3] = t.isArray();
		ar[4] = t.isFinal();
		*/
		
		validateBoolArrayValue(gvt, "c_arr", new boolean[]{false, true, false, false, false});
		validateBoolArrayValue(gvt, "i_arr", new boolean[]{false, false, true, false, false});
		validateBoolArrayValue(gvt, "fc_arr", new boolean[]{false, true, false, false, true});
		validateBoolArrayValue(gvt, "ca_arr", new boolean[]{false, true, false, true, true});
		validateBoolArrayValue(gvt, "iaa_arr", new boolean[]{false, true, false, true, true});
		validateBoolArrayValue(gvt, "int_arr", new boolean[]{true, false, false, false, true});
		validateBoolArrayValue(gvt, "inta_arr", new boolean[]{false, true, false, true, true});
		validateBoolArrayValue(gvt, "var_arr", new boolean[]{false, false, false, false, true});
		validateBoolArrayValue(gvt, "vara_arr", new boolean[]{false, true, false, true, true});
	}
	
	@Test
	public void getParentTest() throws EngineInvocationError, IOException {		
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "BasicInfo/get_parent.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		EFCommons.validateStringValue(gvt, "sc", "[TYPE|<default>.C]");
		EFCommons.validateStringValue(gvt, "so", "[TYPE|Object]");
		EFCommons.validateBoolValue(gvt, "b1", true);
		EFCommons.validateBoolValue(gvt, "b2", true);
	}
	
	@Test
	public void getInterfacesTest1() throws EngineInvocationError, IOException {		
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "BasicInfo/get_interfaces_1.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		EFCommons.validateBoolValue(gvt, "res1", true);
		EFCommons.validateBoolValue(gvt, "res2", true);
		EFCommons.validateBoolValue(gvt, "res3", true);
		EFCommons.validateBoolValue(gvt, "res4", true);
	}
	
	@Test
	public void getInterfacesTest2() throws EngineInvocationError, IOException {		
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "BasicInfo/get_interfaces_2.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		EFCommons.validateIntValue(gvt, "res0", 0);
		EFCommons.validateIntValue(gvt, "res2", 2);
	}
	
	// Type loading
	
	@Test
	public void loadTypeTest1() throws EngineInvocationError, IOException {		
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "Loading/load_1.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		EFCommons.validateIntValue(gvt, "i0", 10);
		EFCommons.validateIntValue(gvt, "i1", 0);
		EFCommons.validateIntValue(gvt, "i2", 5);
	}
	
	// Load twice
	@Test
	public void loadTypeTest2() throws EngineInvocationError, IOException {		
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "Loading/load_2.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		EFCommons.validateIntValue(gvt, "v", 10);
	}
	
	// Built-int and array types
	@Test
	public void loadTypeTest3() throws EngineInvocationError, IOException {		
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "Loading/load_3.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		EFCommons.validateIntValue(gvt, "i0", 1);
		EFCommons.validateIntValue(gvt, "i1", 0);
		EFCommons.validateIntValue(gvt, "i2", 0);
		EFCommons.validateIntValue(gvt, "i3", 0);
		EFCommons.validateBoolValue(gvt, "b0", true);
		EFCommons.validateBoolValue(gvt, "b1", true);
		EFCommons.validateStringValue(gvt, "s0", AnyType.getInstance().getName());
	}
	
	// Wrong syntax
	@Test
	public void loadTypeTest4() throws EngineInvocationError, IOException {		
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "Loading/load_4.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		EFCommons.validateBoolValue(gvt, "b0", true);
		EFCommons.validateBoolValue(gvt, "b1", true);
		EFCommons.validateBoolValue(gvt, "b2", true);
	}
	
	// Type not exists
	@Test
	public void loadTypeTest5() throws EngineInvocationError, IOException {		
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "Loading/load_5.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();
		
		EFCommons.validateBoolValue(gvt, "b", true);
	}
}
