package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.Commons.validateBoolValue;

import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class System_Environment_Tests {

	private static final String FEATURE = "Foundation";
	
	@Test
	public void separatorsTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "environment_1.jul"));
		
		validateStringValue(gvt.getVariable("sep"), File.separator);
	}
	
	// This test will not work if no environment variable is defined, 
	// but that is not the case in either JUnit Eclipse plugin or Maven/surefire runtime.
	@Test
	public void getEnvVarsTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "environment_2.jul"));

		validateBoolValue(gvt.getVariable("succ"), true);
	}
	
}
