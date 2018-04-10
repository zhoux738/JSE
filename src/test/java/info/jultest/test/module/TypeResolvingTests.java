package info.jultest.test.module;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.Commons.verifyDetectedClass;
import static org.junit.Assert.assertTrue;
import info.jultest.test.Commons;
import info.jultest.test.TestExceptionHandler;
import info.jultest.test.oo.ExceptionTestsBase;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.modulesystem.ModuleManager;

import org.junit.Test;

public class TypeResolvingTests extends ExceptionTestsBase {

	private static final String FEATURE = "Import";
	
	@Test
	public void loadScriptAsModuleTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		ModuleManager manager = new ModuleManager();
		SimpleScriptEngine engine = makeSimpleEngine(gvt, manager, false);
		
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "import_1.jul"));
		
		assertTrue(manager.isLoaded("ModuleSys.ModB"));
		
		verifyDetectedClass(manager, "ModuleSys.ModB.Person", "Person");
		
		validateIntValue(gvt, "x", 5);
	}
	
	@Test
	public void namespaceConflictTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "import_2.jul"));
		
		assertException(teh, "System.Lang.NamespaceConflictException");
	}
	
	@Test
	public void resolveFullNameTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		ModuleManager manager = new ModuleManager();
		SimpleScriptEngine engine = makeSimpleEngine(gvt, manager, false);
		
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "import_3.jul"));
		
		validateStringValue(gvt, "s", "HUMAN");
	}

}
