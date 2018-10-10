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
import info.julang.modulesystem.ModuleInfo;
import info.julang.typesystem.jclass.jufc.System.ScriptType;
import info.jultest.test.Commons;
import info.jultest.test.EFCommons;
import info.jultest.test.TestExceptionHandler;
import info.jultest.test.oo.ExceptionTestRunner;
import info.jultest.test.oo.ExceptionTestsBase;

import java.io.IOException;

import org.junit.Test;

public class System_Reflection_Attribute_ReflTestSuite extends ExceptionTestsBase {

	private static final String FEATURE = "Reflection/Attribute";
	
	// from Type
	@Test
	public void getAttrsTest1() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "get_1.jul");

		Commons.validateStringValue(gvt, "name", "Terry");
		Commons.validateIntValue(gvt, "year", 1987);
		Commons.validateIntArrayValue(gvt, "versions", new int[]{1,2,3});
		Commons.validateBoolValue(gvt, "b", true);
	}
	
	// from Method
	@Test
	public void getAttrsTest2() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "get_2.jul");
		
		Commons.validateStringArrayValue(gvt, "results", new String[]{"test1_Allen_true", "test2_Kraun_false"});
	}
	
	// from Constructor
	@Test
	public void getAttrsTest3() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "get_3.jul");

		Commons.validateIntValue(gvt, "c0", 25);
		Commons.validateIntValue(gvt, "c1", 37);
		Commons.validateBoolValue(gvt, "failure", false);
	}
	
	// from Field
	@Test
	public void getAttrsTest4() throws EngineInvocationError, IOException {
		VariableTable gvt = runInline(FEATURE, "get_4.jul");

		Commons.validateStringArrayValue(gvt, "sources", new String[]{"ab", "cd", "xyz"});
	}
	
	@Test
	public void setAttrsTest1() throws EngineInvocationError, IOException {
 		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.OO, FEATURE);
		
 		runner.executeAndExpect(
 			"set_1.jul", 
 			"System.IllegalAssignmentException", 
 			17,
 			null,
 			-1);
	}
}
