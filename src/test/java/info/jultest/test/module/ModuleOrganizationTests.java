package info.jultest.test.module;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeScriptPath;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;

import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.jultest.test.AssertHelper;
import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestsBase;
import info.jultest.test.TestExceptionHandler;

/**
 * module and import usage related to the default module path.
 */
public class ModuleOrganizationTests extends ExceptionTestsBase {
	
	private static final String FEATURE = "ModuleOrganization";

	@Test
	public void defaultModuleTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test1/default_mod.jul"));
		
		validateIntValue(gvt, "i329", 329);
	}
	
	@Test
	public void defaultModuleTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test2/default_mod.jul"));
		
		validateIntValue(gvt, "i329", 329);
	}
	
	@Test
	public void defaultModuleTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(makeScriptPath(Commons.Groups.SCRIPTING, FEATURE, "Test2/another_module_dir"));
		TestExceptionHandler teh = super.installExceptionHandler(engine);
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "Test2/default_mod.jul"));
		super.validateException(
			teh, "System.IllegalModuleException", null, null, null, -1);
		
		AssertHelper.validateStringOccurences(
			teh.getMessage(),
			"illegal module file", 
			"Default module declaration",
			"found under the default module path");
		
		// As of 0.1.34 the error message is something like:
		// System.IllegalModuleException: Encountered an illegal module file: 
		// The module name is not defined. Implicit module declaration (module;) 
		// can only be used for module files found under the default module path, 
		// i.e. 'jse_modules/' co-located with the invoked script.
	}
}
