package info.jultest.test.functional;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateCharValue;
import static info.jultest.test.Commons.validateByteValue;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateNullValue;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.EFCommons.runViaFactory;

import info.jultest.test.Commons;
import info.jultest.test.EFCommons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.interfaces.IExtEngineRuntime;
import info.julang.external.interfaces.IExtVariableTable;

import org.junit.Test;

public class MetadataTests {
	
	private static final String FEATURE = "FirstClassFunction";
	
	@Test
	public void globalFuncGetStringAndReturnTypeTest() throws EngineInvocationError {
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.FUNCTIONAL, FEATURE, "meta_global_1.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();
		
		EFCommons.validateStringValue(gvt, "str1", "fun1(String s)");
		EFCommons.validateStringValue(gvt, "str2", "[TYPE|Integer]");
		EFCommons.validateStringValue(gvt, "str3", "fun2([Bool] b)");
		EFCommons.validateStringValue(gvt, "str4", "[TYPE|Void]");
		EFCommons.validateStringValue(gvt, "str5", "getPerson(ModuleSys.ModB.Person p)");
		EFCommons.validateBoolValue(gvt, "bool6", true);
		EFCommons.validateBoolValue(gvt, "kind", true);
	}
	
	@Test
	public void globalFuncGetParamsTest() throws EngineInvocationError {
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.FUNCTIONAL, FEATURE, "meta_global_2.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();
		
		EFCommons.validateBoolValue(gvt, "v1", true);
		EFCommons.validateBoolValue(gvt, "v2", true);
	}
	
	@Test
	public void instanceMethodMetadataTest() throws EngineInvocationError {
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.FUNCTIONAL, FEATURE, "meta_imethod_1.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();
		
		EFCommons.validateBoolValue(gvt, "plist", true);
		EFCommons.validateBoolValue(gvt, "kind", true);
	}
	
	@Test
	public void staticMethodMetadataTest() throws EngineInvocationError {
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.FUNCTIONAL, FEATURE, "meta_smethod_1.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();
		
		EFCommons.validateBoolValue(gvt, "plist", true);
		EFCommons.validateBoolValue(gvt, "kind", true);
	}
	
	@Test
	public void instanceOverloadedMetadataTest() throws EngineInvocationError {
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.FUNCTIONAL, FEATURE, "meta_imethod_2.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();
		
		EFCommons.validateBoolValue(gvt, "plist", true);
		EFCommons.validateBoolValue(gvt, "kind", true);
		EFCommons.validateBoolValue(gvt, "ret", true);
	}
	
	@Test
	public void extensionOverloadedMetadataTest() throws EngineInvocationError {
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.FUNCTIONAL, FEATURE, "meta_smethod_2.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();
		
		EFCommons.validateBoolValue(gvt, "plist", true);
		EFCommons.validateBoolValue(gvt, "kind", true);
		EFCommons.validateBoolValue(gvt, "ret", true);
	}
	
	
	@Test
	public void getMetadataFromMetadataMethodsTest() throws EngineInvocationError {
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.FUNCTIONAL, FEATURE, "meta_meta.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();
		
		EFCommons.validateBoolValue(gvt, "plist", true);
		EFCommons.validateBoolValue(gvt, "kind", true);
		EFCommons.validateBoolValue(gvt, "ret", true);
	}
}