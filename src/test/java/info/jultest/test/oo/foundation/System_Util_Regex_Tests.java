package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateStringArrayValue;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.Commons.validateIntArrayValue;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestsBase;

import org.junit.Test;

public class System_Util_Regex_Tests extends ExceptionTestsBase {

	private static final String FEATURE = "Regex";
	
	//-------------------- Basic API --------------------//
	
	// Match all
	@Test
	public void regexFromCtorTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "regex_1.jul"));

		validateStringValue(gvt, "s0", "true:0:8(0)");
		validateStringValue(gvt, "s1", "true:0:3(0)");
		validateStringValue(gvt, "s2", "false:-1:-1(0)");		
	}
	
	// Match next
	@Test
	public void regexFromCtorTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "regex_2.jul"));
		
		validateStringArrayValue(gvt, "strs", new String[] {
			"true:0:8(3)",
			"true:8:11(3)"
		});
	}
	
	//-------------------- Grouping --------------------//
	
	@Test
	public void regexFromCtorTest3() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "regex_3.jul"));

		validateStringValue(gvt, "acc0", "_abc_:1:abc");
		validateStringValue(gvt, "acc1", "abxxyyzzmnp:3:bxxyyzz/b/mnp");
	}
	
	//-------------------- Special matching chars --------------------//
	
	@Test
	public void regexFromCtorTest4() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "regex_4.jul"));

		validateStringValue(gvt, "acc0", "2:128/1");
		validateStringValue(gvt, "acc1", "Not matched");
		validateStringValue(gvt, "acc2", "2:{}/A");
		validateStringValue(gvt, "acc3", "Not matched");
		validateStringValue(gvt, "acc4", "Not matched");
	}
	
	@Test
	public void regexFromCtorTest6() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "regex_6.jul"));
		
		validateBoolValue(gvt, "b0", true);
		validateBoolValue(gvt, "b1", false);
		validateBoolValue(gvt, "b2", true);
	}
	
	//-------------------- Illegal patterns --------------------//
	
	@Test
	public void regexFromCtorTest5() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "regex_5.jul"));

		validateBoolValue(gvt, "b0", true);
		validateBoolValue(gvt, "b1", true);
		validateBoolValue(gvt, "b2", true);
		validateBoolValue(gvt, "b3", true);
		validateBoolValue(gvt, "b4", true);
	}
	
	//-------------------- Creating regex from literals --------------------//
	
	@Test
	public void regexFromLiteralTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "regex_literal_1.jul"));
		
		validateBoolValue(gvt, "b0", true);
		validateBoolValue(gvt, "b1", true);
	}
	
	@Test
	public void regexFromLiteralTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "regex_literal_2.jul"));
		
		validateBoolValue(gvt, "b0", true);
		validateBoolValue(gvt, "b1", true);
		validateBoolValue(gvt, "b2", true);
	}
	
	@Test
	public void regexFromLiteralTest3() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "regex_literal_3.jul"));
		
		validateBoolValue(gvt, "b0", true);
		validateBoolValue(gvt, "b1", false);
		validateBoolValue(gvt, "b2", true);
	}
	
	@Test
	public void regexFromLiteralTest4() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "regex_literal_4.jul"));

		validateBoolValue(gvt, "b0", true);
		validateBoolValue(gvt, "b1", true);
		validateBoolValue(gvt, "b2", true);
		validateBoolValue(gvt, "b3", true);
		validateBoolValue(gvt, "b4", true);
	}
	
	@Test
	public void regexFromLiteralTest5() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "regex_literal_5.jul"));

		validateStringValue(gvt, "s0", "def");
	}
	
	@Test
	public void regexFromLiteralTest6() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "regex_literal_6.jul"));

		validateBoolValue(gvt, "matched", true);
		validateIntArrayValue(gvt, "results", new int[] {2, 3, 6, 1});
	}
}
