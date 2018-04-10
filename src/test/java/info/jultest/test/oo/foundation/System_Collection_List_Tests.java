package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
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
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "list_5.jul"));
		
		validateStringValue(gvt, "msg", "Access to array out of range. Index=0, Max=N/A.");
		validateIntValue(gvt, "i0", 5);
		validateIntValue(gvt, "i1", 6);	
		validateIntValue(gvt, "size", 1);	
	}
}
