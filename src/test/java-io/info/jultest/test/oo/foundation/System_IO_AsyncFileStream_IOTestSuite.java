package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateByteArrayValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.TypeTable;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.HeapArea;
import info.julang.memory.simple.SimpleHeapArea;
import info.julang.memory.value.StringValue;
import info.jultest.test.Commons;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.CharBuffer;

import org.junit.Assert;

import org.junit.Test;

//(Uncomment @RunWith for reliability test)
//@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class System_IO_AsyncFileStream_IOTestSuite {
	
	// (Uncomment data() for reliability test)
//	@org.junit.runners.Parameterized.Parameters
//	public static java.util.List<Object[]> data() {
//	  return java.util.Arrays.asList(new Object[20][0]);
//	}
	
	private static final String FEATURE = "Foundation/IO";
	
	@Test
	public void writeAsyncTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, gvt, tt, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
	
		File temp = null;
		try {
			// 1) create a temp file, write something		
			temp = File.createTempFile("__jse_test_", ".tmp"); 
			temp.deleteOnExit();
			
			// 2) create a global var "path" and set temp file's full path to it	
			tt.initialize(engine.getRuntime());
			gvt.addVariable("path", new StringValue(heap, temp.getAbsolutePath()));
			
			// 3) in script, new up a file stream using "path", and call various methods
			engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "fs_writeasync_1.jul"));
			
			// 4) validation
			validateIntValue(gvt, "finalWrite", 16);
			try (FileReader fr = new FileReader(temp)){
				CharBuffer cb = CharBuffer.allocate(30);
				fr.read(cb);
				cb.flip();
				String str = cb.toString();
				Assert.assertEquals("Hello world! This is Julian.", str);			
			}			
		} finally {
			temp.delete();
		}
	}
	
	@Test
	public void readAllAsyncTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, gvt, tt, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		File temp = null;
		try {
			// 1) create a temp file, write something		
			temp = File.createTempFile("__jse_test_", ".tmp"); 
			temp.deleteOnExit();
			String contents = "abcd";
		    try (FileWriter writer = new FileWriter(temp)) {
		    	writer.append(contents);
			}
			
			// 2) create a global var "path" and set temp file's full path to it	
			tt.initialize(engine.getRuntime());
			gvt.addVariable("path", new StringValue(heap, temp.getAbsolutePath()));
			
			// 3) in script, new up a file stream using "path", and call various methods
			engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "fs_readallasync_1.jul"));
			
			// 4) validation
			validateByteArrayValue(gvt, "buffer", new int[]{100, 98, 99}); // d, b, c
			validateIntValue(gvt, "tc", 2);
			validateIntValue(gvt, "tr", 4);
		} finally {
			temp.delete();
		}
	}
	
	@Test
	public void readAsyncTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, gvt, tt, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		File temp = null;
		try {
			// 1) create a temp file, write something		
			temp = File.createTempFile("__jse_test_", ".tmp"); 
			temp.deleteOnExit();
			String contents = "abcd";
		    try (FileWriter writer = new FileWriter(temp)) {
		    	writer.append(contents);
			}
			
			// 2) create a global var "path" and set temp file's full path to it	
			tt.initialize(engine.getRuntime());
			gvt.addVariable("path", new StringValue(heap, temp.getAbsolutePath()));
			
			// 3) in script, new up a file stream using "path", and call various methods
			engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "fs_readasync_1.jul"));
			
			// 4) validation
			validateByteArrayValue(gvt, "buffer", new int[]{97, 98, 99, 100}, 128);
			validateIntValue(gvt, "i", 4);
			validateIntValue(gvt, "j", 4);
			validateIntValue(gvt, "k", 100);
		} finally {
			temp.delete();
		}
	}
	
	// Fail implicitly (throwing exception)
	@Test
	public void readAsyncFailedTest1() throws EngineInvocationError, IOException {
		readAsyncFailedInternal("fs_readasync_fail_1.jul", "failed!");
	}
	
	// Fail explicitly (using handle)
	@Test
	public void readAsyncFailedTest2() throws EngineInvocationError, IOException {
		readAsyncFailedInternal("fs_readasync_fail_2.jul", "The promise was rejected: failed!");
	}
	
	// Fail unexpectedly (underlying stream closed)
	@Test
	public void readAsyncFailedTest3() throws EngineInvocationError, IOException {
		readAsyncFailedInternal("fs_readasync_fail_3.jul", "The promise was rejected: IO error: File stream is already closed.");
	}
	
	private void readAsyncFailedInternal(String fileName, String msg) throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, gvt, tt, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		File temp = null;
		try {
			// 1) create a temp file, write something		
			temp = File.createTempFile("__jse_test_", ".tmp"); 
			temp.deleteOnExit();
			String contents = "abcd";
		    try (FileWriter writer = new FileWriter(temp)) {
		    	writer.append(contents);
			}
			
			// 2) create a global var "path" and set temp file's full path to it	
			tt.initialize(engine.getRuntime());
			gvt.addVariable("path", new StringValue(heap, temp.getAbsolutePath()));
			
			// 3) in script, new up a file stream using "path", and call various methods
			engine.run(getScriptFile(Commons.Groups.OO, FEATURE, fileName));
			
			// 4) validation
			validateStringValue(gvt, "s", msg);
		} finally {
			temp.delete();
		}
	}
}
