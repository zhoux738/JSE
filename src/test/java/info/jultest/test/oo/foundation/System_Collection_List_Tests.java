package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.Commons.validateIntArrayValue;
import static info.jultest.test.Commons.validateStringArrayValue;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.HeapArea;
import info.julang.memory.simple.SimpleHeapArea;
import info.jultest.test.Commons;

import java.io.IOException;

import org.junit.Test;

public class System_Collection_List_Tests {

	private static final String FEATURE = "Foundation/Collection";
	
	@Test
	public void listWithBuiltInTypesTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();
		HeapArea heap = new SimpleHeapArea();
		SimpleScriptEngine engine = makeSimpleEngine(heap, gvt, null, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "list_1.jul"));
		
		validateStringValue(gvt, "s1", "abc");
		validateStringValue(gvt, "s2", "abc");
		validateIntValue(gvt, "x1", 3);
		validateIntValue(gvt, "size1", 2);
		validateIntValue(gvt, "size2", 1);
	}

	@Test
	public void listWithCustomTypesTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();
		HeapArea heap = new SimpleHeapArea();
		SimpleScriptEngine engine = makeSimpleEngine(heap, gvt, null, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "list_2.jul"));
		
		validateStringValue(gvt, "s", "BYD");
	}
	
	@Test
	public void listAccessByIndexTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();
		HeapArea heap = new SimpleHeapArea();
		SimpleScriptEngine engine = makeSimpleEngine(heap, gvt, null, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "list_3.jul"));
		
		validateStringValue(gvt, "msg", "Access to array out of range. Index=0, Max=N/A.");
		validateIntValue(gvt, "i0", 5);
		validateIntValue(gvt, "i1", 6);	
		validateIntValue(gvt, "size", 1);	
	}
	
	//------------------ Sorting (also tests IComparable) ------------------//
	
	@Test
	public void listSortIntegersTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "list_sort_1.jul"));
		
		validateIntArrayValue(gvt, "arr0", new int[] {-1, 2, 3, 7, 11, 19} );
		validateIntArrayValue(gvt, "arr1", new int[] {19, 11, 7, 3, 2, -1} );
	}
	
	@Test
	public void listSortNumbersTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "list_sort_2.jul"));
		
		validateStringArrayValue(gvt, "arr0", new String[] {"-1.3", "2", "3", "7.1", "11", "19"} );
		validateStringArrayValue(gvt, "arr1", new String[] {"19", "11", "7.1", "3", "2", "-1.3"} );
	}
	
	@Test
	public void listSortCharsAndStringsTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "list_sort_3.jul"));
		
		validateStringArrayValue(gvt, "arr0", new String[] {"", "a", "ab", "c", "e", "ff", "fff", "z"} );
		validateStringArrayValue(gvt, "arr1", new String[] {"z", "fff", "ff", "e", "c", "ab", "a", ""} );
	}
	
	@Test
	public void listSortObjectsTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "list_sort_4.jul"));
		
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
	public void listSortObjectAndNumbersTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "list_sort_5.jul"));
		
		validateStringArrayValue(gvt, "arr0", new String[] {"-50", "-5", "1", "9.4", "12", "18", "77", "111"});
	}	
}
