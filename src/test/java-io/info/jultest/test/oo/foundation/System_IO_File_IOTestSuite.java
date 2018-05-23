package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateStringValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.TypeTable;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.HeapArea;
import info.julang.memory.simple.SimpleHeapArea;
import info.julang.memory.value.StringValue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;

public class System_IO_File_IOTestSuite {

	private static final String FEATURE = "Foundation/IO";
	
	@Test
	public void newFileTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, gvt, tt, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		// 1) create a temp file, write something		
		File temp = File.createTempFile("__jse_test_", ".tmp"); 
		temp.deleteOnExit();
		String contents = "This is a file";
	    try (FileWriter writer = new FileWriter(temp)) {
	    	writer.append(contents);
		}
	    
		// 2) create a global var "path" and set temp file's full path to it	
	    tt.initialize(engine.getRuntime());
	    gvt.addVariable("path", new StringValue(heap, temp.getAbsolutePath()));
		
		// 3) in file_1.jul, new up a file using "path", and run various methods
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "file_1.jul"));
		
		// 4) validate
		validateStringValue(gvt, "fname", temp.getName());
		validateStringValue(gvt, "contents", contents);
		validateStringValue(gvt, "p", temp.getAbsolutePath());
		validateBoolValue(gvt, "e", true);
		
		// 5) delete the temp file
		temp.delete();
	}
	
}
