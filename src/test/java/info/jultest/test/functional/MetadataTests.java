package info.jultest.test.functional;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.resetTypeSystem;

import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.jultest.test.Commons;

public class MetadataTests {
	
	private static final String FEATURE = "FirstClassFunction";
	
	private static VariableTable run(String script) throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, script));
		return gvt;
	}
	
	@Test
	public void globalFuncGetStringAndReturnTypeTest() throws EngineInvocationError {
		VariableTable gvt = run("meta_global_1.jul");

		Commons.validateStringValue(gvt, "str1", "fun1(String s)");
		Commons.validateStringValue(gvt, "str2", "[TYPE|Integer]");
		Commons.validateStringValue(gvt, "str3", "fun2([Bool] b)");
		Commons.validateStringValue(gvt, "str4", "[TYPE|Void]");
		Commons.validateStringValue(gvt, "str5", "getPerson(ModuleSys.ModB.Person p)");
		Commons.validateBoolValue(gvt, "bool6", true);
		Commons.validateBoolValue(gvt, "kind", true);
	}
	
	@Test
	public void globalFuncGetParamsTest() throws EngineInvocationError {
		VariableTable gvt = run("meta_global_2.jul");
		
		Commons.validateBoolValue(gvt, "v1", true);
		Commons.validateBoolValue(gvt, "v2", true);
	}
	
	@Test
	public void instanceMethodMetadataTest() throws EngineInvocationError {
		VariableTable gvt = run("meta_imethod_1.jul");
		
		Commons.validateBoolValue(gvt, "plist", true);
		Commons.validateBoolValue(gvt, "kind", true);
	}
	
	@Test
	public void staticMethodMetadataTest() throws EngineInvocationError {
		VariableTable gvt = run("meta_smethod_1.jul");
		
		Commons.validateBoolValue(gvt, "plist", true);
		Commons.validateBoolValue(gvt, "kind", true);
	}
	
	@Test
	public void instanceOverloadedMetadataTest() throws EngineInvocationError {
		VariableTable gvt = run("meta_imethod_2.jul");
		
		Commons.validateBoolValue(gvt, "plist", true);
		Commons.validateBoolValue(gvt, "kind", true);
		Commons.validateBoolValue(gvt, "ret", true);
	}
	
	@Test
	public void extensionOverloadedMetadataTest() throws EngineInvocationError {
		VariableTable gvt = run("meta_smethod_2.jul");
		
		Commons.validateBoolValue(gvt, "plist", true);
		Commons.validateBoolValue(gvt, "kind", true);
		Commons.validateBoolValue(gvt, "ret", true);
	}
	
	
	@Test
	public void getMetadataFromMetadataMethodsTest() throws EngineInvocationError {
		VariableTable gvt = run("meta_meta.jul");
		
		Commons.validateBoolValue(gvt, "plist", true);
		Commons.validateBoolValue(gvt, "kind", true);
		Commons.validateBoolValue(gvt, "ret", true);
	}
}