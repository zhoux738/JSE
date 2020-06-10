package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeAssertableEngine;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateStringValue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.TypeTable;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.HeapArea;
import info.julang.memory.simple.SimpleHeapArea;
import info.julang.memory.value.StringValue;
import info.jultest.test.Commons;
import info.jultest.test.FileSysHelper;
import junit.framework.Assert;

public class System_IO_File_IOTestSuite {

	private static final String FEATURE = "Foundation/IO";
	
	@Test
	public void newFileTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, gvt, tt, null);
		
		// 1) create a temp file, write something
		File temp = FileSysHelper.createTempFile();
		String contents = "This is a file.";
		writeToFile(temp, contents);
	    
		// 2) create a global var "path" and set temp file's full path to it	
	    tt.initialize(engine.getRuntime());
	    gvt.addVariable("path", new StringValue(heap, temp.getAbsolutePath()));
		
		// 3) in file_1.jul, new up a file using "path", and run various methods
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "file_1.jul"));
		
		// 4) validate
		validateStringValue(gvt, "fname", temp.getName());
		validateStringValue(gvt, "contents", contents);
		validateStringValue(gvt, "p", temp.getCanonicalPath());
		validateBoolValue(gvt, "e", true);
		
		// 5) delete the temp file
		temp.delete();
	}
	
	@Test
	public void renameAndMoveFileTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, gvt, tt, null);
		
		// 1) Create two dirs		
		File dira = FileSysHelper.createTempDir();
		File dirb = FileSysHelper.createTempDir();
		
	    try {		
			// 2) Create a file under dir-a and write something
			File file1 = new File(dira, "file1.txt");
			file1.deleteOnExit();
			
			file1.createNewFile();
			writeToFile(file1, "This is a file.");
			
			// 3) Pass along the variables
		    tt.initialize(engine.getRuntime());
		    gvt.addVariable("pathDirA", new StringValue(heap, dira.getAbsolutePath()));
		    gvt.addVariable("pathDirB", new StringValue(heap, dirb.getAbsolutePath()));
		    gvt.addVariable("pathFile1", new StringValue(heap, file1.getAbsolutePath()));
		
			// 4) Test
			//    - rename to file2
			//    - relocated to dir-b	    
			engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "file_2.jul"));
			
			// 5) Validate
			//    - there are no more file1 and file2 inside dir-a
			//    - there is a file2 under dir-b
			//	  - contents of file2 remain unchanged
			Assert.assertEquals(0, dira.listFiles().length);
			File[] files = dirb.listFiles();
			Assert.assertEquals(1, files.length);
			File file2 = files[0];
			Assert.assertEquals("file2.txt", file2.getName());
			validateStringValue(gvt, "contents", FileSysHelper.readAllString(file2));
	    } finally {
	    	// 6) Clean up
	    	FileSysHelper.deleteAll(dira);
	    	FileSysHelper.deleteAll(dirb);
	    }
	}
	
	@Test
	public void comprehensiveFileTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeAssertableEngine(heap, gvt, tt);
		
		// 1) Create two dirs		
		File dira = FileSysHelper.createTempDir();
		File dirb = FileSysHelper.createTempDir();
		
	    try {
			// 2) Pass along the variables
		    tt.initialize(engine.getRuntime());
		    gvt.addVariable("pathDirA", new StringValue(heap, dira.getAbsolutePath()));
		    gvt.addVariable("pathDirB", new StringValue(heap, dirb.getAbsolutePath()));
		
			// 3) Test
			engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "file_3.jul"));
	    } finally {
	    	// 4) Clean up
	    	FileSysHelper.deleteAll(dira);
	    	FileSysHelper.deleteAll(dirb);
	    }
	}
	
	private void writeToFile(File file, String contents) throws IOException {
	    try (FileWriter writer = new FileWriter(file)) {
	    	writer.append(contents);
		}
	}
}
