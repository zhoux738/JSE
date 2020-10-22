package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import info.julang.execution.FileScriptProvider;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.util.OSTool;
import info.jultest.test.Commons;
import info.jultest.test.oo.ExceptionTestsBase;
import org.junit.Assert;

import org.junit.Test;

public class System_Reflection_Script_ReflTestSuite extends ExceptionTestsBase {

	private static final String FEATURE = "Reflection/Script";
	
	@Test
	public void getScriptTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		FileScriptProvider prov = getScriptFile(Commons.Groups.OO, FEATURE, "script_1.jul");
		engine.run(prov);
		
		String exp = prov.getFilePathName(true);
		String act = OSTool.canonicalizePath(Commons.getStringValue(gvt, "p"));
		Assert.assertEquals(exp, act);
	}
	
	@Test
	public void getScriptTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		FileScriptProvider prov = getScriptFile(Commons.Groups.OO, FEATURE, "script_2.jul");
		engine.run(prov);
		
		String act = OSTool.canonicalizePath(Commons.getStringValue(gvt, "s"));
		if (OSTool.isWindows()) {
	        Assert.assertTrue(act.endsWith("ModuleSys\\ReflTest3\\def.jul"));
		} else {
	        Assert.assertTrue(act.endsWith("ModuleSys/ReflTest3/def.jul"));
		}
	}
	
	@Test
	public void getScriptTest3() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "script_3.jul"));
		
		validateBoolValue(gvt, "res", true);
	}

}
