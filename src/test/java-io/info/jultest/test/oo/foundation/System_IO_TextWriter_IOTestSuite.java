package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.TypeTable;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.HeapArea;
import info.julang.memory.simple.SimpleHeapArea;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.ArrayValueFactory;
import info.julang.memory.value.BoolValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.StringValue;
import info.julang.typesystem.jclass.builtin.JStringType;
import info.jultest.test.Commons;
import info.jultest.test.FileSysHelper;
import junit.framework.Assert;

public class System_IO_TextWriter_IOTestSuite {

	private static final String FEATURE = "Foundation/IO";
	
	@Test
	public void writelnTest_flushAtClosing() throws EngineInvocationError, IOException {
		runTextWriterTest(1024, false,
			"abc",
			"def"
		);
	}
	
	@Test
	public void writelnTest_autoFlush() throws EngineInvocationError, IOException {
		runTextWriterTest(1024, true,
			"abc",
			"def"
		);
	}
	
	@Test
	public void writelnTest_withBuffer() throws EngineInvocationError, IOException {
		runTextWriterTest(4, false,
			"a2345",
			"b23",
			"c23456",
			"d2",
			"e2",
			"f2345678",
			"g",
			"h",
			"i2345"
		);
	}
	
	@Test
	public void writelnTest_bigBuffer() throws EngineInvocationError, IOException {
		runTextWriterTest(4, false,
			"a23456789b23456789c23456789d23456789e23456789f23456789_63_66_69*_73_76_79*",
			"",
			"a23456789b23456789c23456789d23456789e23456789f23456789_63_66_69*_73_76_79*",
			""
		);
	}
	
	private void runTextWriterTest(int bufferSize, boolean autoFlush, String... input) throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, gvt, tt, null);
		
		File temp = null;
		BufferedReader reader = null;
		try {
			// 1) create an empty temp file		
			temp = FileSysHelper.createTempFile();
		    
			// 2) create a few variables:
			//    - a global var "path" and set temp file's full path to it
			//    - buffer size
			//    - whether or not to use auto flush
			//    - an array of string holding what to be written to the target file in each line
			tt.initialize(engine.getRuntime());
			gvt.addVariable("path", new StringValue(heap, temp.getAbsolutePath()));
			gvt.addVariable("bufferSize", new IntValue(heap, bufferSize));
			gvt.addVariable("autoFlush", new BoolValue(heap, autoFlush));
			int len = input.length;
			ArrayValue av = ArrayValueFactory.createArrayValue(heap, tt, JStringType.getInstance(), len);
			for (int i = 0; i < len; i++) {
				JValue ele = av.getValueAt(i);
				StringValue sv = new StringValue(heap, input[i]);
				sv.assignTo(ele);
			}
			gvt.addVariable("strarr", av);
			
			// 3) in the script, new up a file stream and text writer using "path"
			engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "textwriter.jul"));
			
			// 4) validation - each line is correctly written from a corresponding element of the string array.
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(temp)));
			String line = null;
			int i = 0;
			while ((line = reader.readLine()) != null) {
				Assert.assertEquals(input[i], line);
				i++;
			}
			
			Assert.assertEquals(len, i);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
					// Best efforts
				}
			}
			
			if (temp != null) {
				temp.delete();
			}
		}
	}
}
