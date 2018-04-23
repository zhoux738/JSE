package info.jultest.test.interpret;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolArrayValue;
import static info.jultest.test.Commons.validateIntArrayValue;
import static info.jultest.test.Commons.validateStringArrayValue;

import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.jultest.test.Commons;
import info.jultest.test.oo.ExceptionTestRunner;

public class ArraySortTests {

	private static final String FEATURE = "ArraySort";
	
	@Test
	public void sort1DIntArrayBasicTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "as_01.jul"));

		validateIntArrayValue(gvt, "a0", new int[] {-3, 0, 1, 2, 5, 5, 12} );
		validateIntArrayValue(gvt, "a1", new int[] {12, 5, 5, 2, 1, 0, -3} );
	}
	
	@Test
	public void sort1DBoolArrayBasicTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "as_02.jul"));

		validateBoolArrayValue(gvt, "a0", new boolean[] {false, false, false, false, true, true, true} );
	}
	
	@Test
	public void sort1DStringArrayBasicTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "as_03.jul"));
		
		validateStringArrayValue(gvt, "arr0", new String[] {
			"", 
			"aaa",
			"abc",
			"b",
			"bc",
			"bc",
			"zab"
		});
	}
	
	@Test
	public void sort1DObjectArrayBasicTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "as_04.jul"));
		
		validateStringArrayValue(gvt, "arr0", new String[] {
			"901_cathimult", 
			"1737_devurwen",
			"1776_anvallon",
			"1817_aekain",
			"1899_sevaris",
			"1911_kostuvald",
			"1911_phoretics",
			"1911_tierollory",
			"2000_liebodin"
		});
	}
	
	@Test
	public void sort1DObjectStringArrayBasicTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "as_05.jul"));
		
		validateStringArrayValue(gvt, "arr0", new String[] {
			"Alex",
			"Demore",
			"Filipe",
			"Filipe",
			"Kubart",
			"Lansing",
			"Maximilian",
			"Nemoah",
			"Nemoah",
			"Unton",
			"Unton",
			"Wilson",
			"Yollex"
		});
	}

	@Test
	public void throwsFromArraySortingTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.IMPERATIVE, FEATURE);
		runner.executeAndExpect(
			"as_06.jul", 
			KnownJSException.HostingPlatform, 
			KnownJSException.UndefinedSymbol, 
			"selbst", 
			"invoked through an engine callback.");
	}
	
}
