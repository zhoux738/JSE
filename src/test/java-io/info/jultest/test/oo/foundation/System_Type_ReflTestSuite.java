package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.resetTypeSystem;
import static info.jultest.test.Commons.validateBoolArrayValue;
import static info.jultest.test.Commons.validateNullValue;
import static info.jultest.test.Commons.validateStringValue;

import java.io.IOException;

import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.typesystem.AnyType;
import info.jultest.test.Commons;
import info.jultest.test.EFCommons;
import info.jultest.test.ExceptionTestsBase;
import info.jultest.test.TestExceptionHandler;

public class System_Type_ReflTestSuite extends ExceptionTestsBase {

	private static final String FEATURE = "Reflection";
	
	//-------- Reflection/GetType --------//
	
	@Test
	public void getFromObjectTest1() throws EngineInvocationError, IOException {		
		VariableTable gvt = resetAndRun("GetType/get_from_object_1.jul");

		Commons.validateBoolValue(gvt, "bt0", true);
		Commons.validateBoolValue(gvt, "bt1", true);
		Commons.validateBoolValue(gvt, "bt2", true);
		Commons.validateBoolValue(gvt, "bt3", true);
	}	
	
	@Test
	public void getFromTypeTest1() throws EngineInvocationError, IOException {
		VariableTable gvt = resetAndRun("GetType/get_from_type_1.jul");

		Commons.validateBoolValue(gvt, "bt0", true);
		Commons.validateBoolValue(gvt, "bt1", true);
		Commons.validateBoolValue(gvt, "bt2", true);
		Commons.validateBoolValue(gvt, "bt3", true);
		Commons.validateBoolValue(gvt, "bt4", true);
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
		VariableTable gvt = resetAndRun("GetType/get_from_builtin.jul");

		Commons.validateBoolValue(gvt, "b0", true);
		Commons.validateBoolValue(gvt, "b1", true);
	}
	
	@Test
	public void getFromFunctionTest1() throws EngineInvocationError, IOException {
		VariableTable gvt = resetAndRun("GetType/get_from_function_1.jul");

		Commons.validateBoolValue(gvt, "hasType", true);
		Commons.validateBoolValue(gvt, "hasBind", true);
		Commons.validateIntValue(gvt, "methodCnt", 3);
	}
	
	@Test
	public void getFromFunctionTest2() throws EngineInvocationError, IOException {
		VariableTable gvt = resetAndRun("GetType/get_from_function_2.jul");

		Commons.validateBoolValue(gvt, "hasType", true);
		Commons.validateBoolValue(gvt, "hasBind", true);
		Commons.validateIntValue(gvt, "methodCnt", 3);
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
		VariableTable gvt = resetAndRun("BasicInfo/get_parent.jul");

		Commons.validateStringValue(gvt, "sc", "[TYPE|<default>.C]");
		Commons.validateStringValue(gvt, "so", "[TYPE|Object]");
		Commons.validateBoolValue(gvt, "b1", true);
		Commons.validateBoolValue(gvt, "b2", true);
	}
	
	@Test
	public void getInterfacesTest1() throws EngineInvocationError, IOException {		
		VariableTable gvt = resetAndRun("BasicInfo/get_interfaces_1.jul");

		Commons.validateBoolValue(gvt, "res1", true);
		Commons.validateBoolValue(gvt, "res2", true);
		Commons.validateBoolValue(gvt, "res3", true);
		Commons.validateBoolValue(gvt, "res4", true);
	}
	
	@Test
	public void getInterfacesTest2() throws EngineInvocationError, IOException {		
		VariableTable gvt = resetAndRun("BasicInfo/get_interfaces_2.jul");

		Commons.validateIntValue(gvt, "res0", 0);
		Commons.validateIntValue(gvt, "res2", 2);
	}
	
	@Test
	public void getExtensionsTest1() throws EngineInvocationError, IOException {		
		VariableTable gvt = resetAndRun("BasicInfo/get_extensions_1.jul");
		
		Commons.validateBoolValue(gvt, "res1", true);
		Commons.validateBoolValue(gvt, "res2", true);
	}
	
	// Type loading
	
	@Test
	public void loadTypeTest1() throws EngineInvocationError, IOException {
		VariableTable gvt = resetAndRun("Loading/load_1.jul");

		Commons.validateIntValue(gvt, "i0", 10);
		Commons.validateIntValue(gvt, "i1", 0);
		Commons.validateIntValue(gvt, "i2", 5);
	}
	
	// Load twice
	@Test
	public void loadTypeTest2() throws EngineInvocationError, IOException {		
		VariableTable gvt = resetAndRun("Loading/load_2.jul");

		Commons.validateIntValue(gvt, "v", 10);
	}
	
	// Built-int and array types
	@Test
	public void loadTypeTest3() throws EngineInvocationError, IOException {		
		VariableTable gvt = resetAndRun("Loading/load_3.jul");

		Commons.validateIntValue(gvt, "i0", 1);
		Commons.validateIntValue(gvt, "i1", 0);
		Commons.validateIntValue(gvt, "i2", 0);
		Commons.validateIntValue(gvt, "i3", 0);
		Commons.validateBoolValue(gvt, "b0", true);
		Commons.validateBoolValue(gvt, "b1", true);
		Commons.validateStringValue(gvt, "s0", AnyType.getInstance().getName());
	}
	
	// Wrong syntax
	@Test
	public void loadTypeTest4() throws EngineInvocationError, IOException {		
		VariableTable gvt = resetAndRun("Loading/load_4.jul");

		Commons.validateBoolValue(gvt, "b0", true);
		Commons.validateBoolValue(gvt, "b1", true);
		Commons.validateBoolValue(gvt, "b2", true);
	}
	
	// Type not exists
	@Test
	public void loadTypeTest5() throws EngineInvocationError, IOException {		
		VariableTable gvt = resetAndRun("Loading/load_5.jul");
		
		EFCommons.validateBoolValue(gvt, "b", true);
	}
	
	private VariableTable resetAndRun(String script) throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, script));
		
		return gvt;
	}
}
