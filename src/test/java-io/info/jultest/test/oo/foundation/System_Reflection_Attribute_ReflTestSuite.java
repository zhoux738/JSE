package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.runInline;

import java.io.IOException;

import org.junit.Test;

import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestRunner;
import info.jultest.test.ExceptionTestsBase;

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
