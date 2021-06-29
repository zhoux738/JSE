package info.jultest.test.interpret.datatype;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateCharValue;
import static info.jultest.test.Commons.validateStringValue;

import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestRunner;

public class StringTypeIndexingTests {

	private static final String FEATURE = "DataType";

	@Test
	public void iterateStringTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "string_02.jul"));
		validateCharValue(gvt, "c0", 'a');
		validateCharValue(gvt, "c1", 'b');
		validateCharValue(gvt, "c2", 'c');
		validateStringValue(gvt, "s", "abc");
		validateStringValue(gvt, "s2", "abc");
	}
	
	@Test
	public void indexStringTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "string_03.jul"));
		validateCharValue(gvt, "c0", 'a');
		validateCharValue(gvt, "c1", 'b');
		validateCharValue(gvt, "c2", 'c');
		validateStringValue(gvt, "s", "abc");
	}
	
	@Test
	public void indexOutOfRangeStringTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "string_04.jul"));
		
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.IMPERATIVE, FEATURE);
		runner.executeAndExpect(
			"string_04.jul", 
			KnownJSException.ArrayOutOfRange, 
			null, 
			"Max=2");
	}
}
