package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolArrayValue;
import static info.jultest.test.Commons.validateByteArrayValue;
import static info.jultest.test.Commons.validateCharValue;
import static info.jultest.test.Commons.validateIntValue;
import info.jultest.test.Commons;
import info.jultest.test.FileSysHelper;
import info.jultest.test.oo.ExceptionTestRunner;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.TypeTable;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.HeapArea;
import info.julang.memory.simple.SimpleHeapArea;
import info.julang.memory.value.StringValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Test;

public class System_IO_FileStream_IOTestSuite {

	private static final String FEATURE = "Foundation/IO";
	
	@Test
	public void writeToFileTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, gvt, tt, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		File temp = null;
		InputStreamReader reader = null;
		try {
			// 1) create an empty temp file		
			temp = FileSysHelper.createTempFile();
			
			// 2) create a global var "path" and set temp file's full path to it	
			tt.initialize(engine.getRuntime());
			gvt.addVariable("path", new StringValue(heap, temp.getAbsolutePath()));
			
			// 3) in fs_1.jul, new up a file stream using "path", and call various methods
			engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "fs_1.jul"));
			
			// 4) validation
			reader = new InputStreamReader(new FileInputStream(temp));
			char[] buf = new char[4];
			int read = reader.read(buf, 0, 4);
			Assert.assertEquals(4, read);
			read = reader.read(buf, 0, 1);
			Assert.assertArrayEquals(new char[]{'z', 'a', 'b', 'c'}, buf);
			Assert.assertEquals(-1, read);
		} finally {
			reader.close();
			temp.delete();
		}
	}
	
	@Test
	public void appendToFileWithFlushTest() throws EngineInvocationError, IOException {
		appendToFileTestInternal("fs_3a.jul");
	}
	
	@Test
	public void appendToFileWithoutFlushTest() throws EngineInvocationError, IOException {
		appendToFileTestInternal("fs_3b.jul");
	}
	
	private void appendToFileTestInternal(String fileName) throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, gvt, tt, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		File temp = null;
		InputStreamReader reader = null;
		try {
			// 1) create a temp file, write something		
			temp = FileSysHelper.createTempFile();
			String contents = "xa";
		    try (FileWriter writer = new FileWriter(temp)) {
		    	writer.append(contents);
			}
			
			// 2) create a global var "path" and set temp file's full path to it	
			tt.initialize(engine.getRuntime());
			gvt.addVariable("path", new StringValue(heap, temp.getAbsolutePath()));
			
			// 3) in fs_3.jul, new up a file stream using "path", and call various methods
			engine.run(getScriptFile(Commons.Groups.OO, FEATURE, fileName));
			
			// 4) validation
			reader = new InputStreamReader(new FileInputStream(temp));
			char[] buf = new char[4];
			int read = reader.read(buf, 0, 4);
			Assert.assertEquals(4, read);
			read = reader.read(buf, 0, 1);
			Assert.assertArrayEquals(new char[]{'x', 'a', 'b', 'c'}, buf);
			Assert.assertEquals(-1, read);
		} finally {
			reader.close();
			temp.delete();
		}
	}
	
	@Test
	public void readFromFileTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, gvt, tt, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		File temp = null;
		InputStreamReader reader = null;
		try {
			// 1) create a temp file, write something		
			temp = FileSysHelper.createTempFile();
			String contents = "xabc";
		    try (FileWriter writer = new FileWriter(temp)) {
		    	writer.append(contents);
			}
			
			// 2) create a global var "path" and set temp file's full path to it	
			tt.initialize(engine.getRuntime());
			gvt.addVariable("path", new StringValue(heap, temp.getAbsolutePath()));
			
			// 3) in fs_2.jul, new up a file stream using "path", and call various methods
			engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "fs_2.jul"));
			
			// 4) validation
			validateCharValue(gvt, "c0", 'x');
			validateByteArrayValue(gvt, "bs", new int[]{97, 98, 99});
			validateIntValue(gvt, "end", -1);
			
			// (the following is to confirm that the script didn't change the contents of file)
			reader = new InputStreamReader(new FileInputStream(temp));
			char[] buf = new char[4];
			int read = reader.read(buf, 0, 4);
			Assert.assertEquals(4, read);
			read = reader.read(buf, 0, 1);
			Assert.assertArrayEquals(new char[]{'x', 'a', 'b', 'c'}, buf);
			Assert.assertEquals(-1, read);
		} finally {
			reader.close();
			temp.delete();
		}
	}

	@Test
	public void illegalOperationsTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, gvt, tt, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		File temp = null;
		InputStreamReader reader = null;
		try {
			// 1) create a temp file, write something		
			temp = FileSysHelper.createTempFile();
			String contents = "xabc";
		    try (FileWriter writer = new FileWriter(temp)) {
		    	writer.append(contents);
			}
			
			// 2) create a global var "path" and set temp file's full path to it	
			tt.initialize(engine.getRuntime());
			gvt.addVariable("path", new StringValue(heap, temp.getAbsolutePath()));
			
			// 3) in fs_2.jul, new up a file stream using "path", and call various methods
			engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "fs_err_1.jul"));
			
			// 4) validation
			validateBoolArrayValue(gvt, "res", new boolean[]{true, true, true, false, true});
			validateBoolArrayValue(gvt, "ast", new boolean[]{true, true, true});
			
			// (the following is to confirm that the script didn't change the contents of file)
			reader = new InputStreamReader(new FileInputStream(temp));
			char[] buf = new char[4];
			int read = reader.read(buf, 0, 4);
			Assert.assertEquals(4, read);
			read = reader.read(buf, 0, 1);
			Assert.assertArrayEquals(new char[]{'x', 'a', 'b', 'c'}, buf);
			Assert.assertEquals(-1, read);
		} finally {
			if (reader != null) { reader.close(); }
			if (temp != null) { temp.delete(); }
		}
	}
	
	@Test
	public void nonexsitingFileTest() throws EngineInvocationError {
 		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.OO, FEATURE);
		runner.executeAndValidate(
			"fs_err_2.jul", 
			"System.IO.IOException", 
			null, 
			new String[]{
				"<ctor-System.IO.FileStream>(System.IO.FileStream,String,System.IO.FileMode)  (/.../FileStream.jul, 61)",
			}, 
			false, 
			4);
	}
}
