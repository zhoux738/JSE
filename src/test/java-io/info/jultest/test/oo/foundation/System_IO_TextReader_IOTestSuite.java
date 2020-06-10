package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateObjectArrayValue;

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
import info.julang.memory.value.IntValue;
import info.julang.memory.value.StringValue;
import info.jultest.test.Commons;
import info.jultest.test.FileSysHelper;
import info.jultest.test.Commons.StringValueValidator;

public class System_IO_TextReader_IOTestSuite {

	private static final String FEATURE = "Foundation/IO";
	
	@Test
	public void readlnTest_readAllInOneAttempt() throws EngineInvocationError, IOException {
		runTextReaderTest(
			1024,
			new String[] {
				"abc",
				"def"
			});
	}
	
	@Test
	public void readlnTest_bufferGrows() throws EngineInvocationError, IOException {
		runTextReaderTest(
			4,
			new String[] {
				"abcde",
				"fghij",
				"k"
			});
	}
	
	@Test
	public void readlnTest_bufferGrowsMultipleTimes() throws EngineInvocationError, IOException {
		runTextReaderTest(
			4,
			new String[] {
				"00",
				"a1234567890123456789012345678901234567890123456789012345678z",
				"k"
			});
	}
	
	@Test
	public void readlnTest_emptyLines() throws EngineInvocationError, IOException {
		runTextReaderTest(
			4,
			new String[] {
				"abc",
				"",
				"",
				"",
				"",
				"",
				"def",
				"",
				""
			});
	}
	
	@Test
	public void readlnTest_readAllEmptyLinesInOneAttempt() throws EngineInvocationError, IOException {
		runTextReaderTest(
			64,
			new String[] {
				"abc",
				"",
				"",
				"",
				"def",
				"",
				""
			});
	}
	
	@Test
	public void readlnTest_startsWithEmptyLine() throws EngineInvocationError, IOException {
		runTextReaderTest(
			4,
			new String[] {
				"",
				"",
				"",
				"",
				"",
				"abc"
			});
	}
	
	private void runTextReaderTest(int bufferSize, String[] lines) throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, gvt, tt, null);
		
		File temp = null;
		try {
			// 1) create an empty temp file		
			temp = FileSysHelper.createTempFile();
		    try (FileWriter writer = new FileWriter(temp)) {
		    	for (String line : lines) {
			    	writer.append(line);
			    	writer.append(System.lineSeparator());
		    	}
			}
		    
			// 2) create a global var "path" and set temp file's full path to it	
			tt.initialize(engine.getRuntime());
			gvt.addVariable("path", new StringValue(heap, temp.getAbsolutePath()));
			gvt.addVariable("bufferSize", new IntValue(heap, bufferSize));
			
			// 3) in the script, new up a file stream and text reader using "path"
			engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "textreader.jul"));
			
			// 4) validation - each line is correctly read into a corresponding element of a string array.
			validateObjectArrayValue(gvt, "strarr", lines, StringValueValidator.INSTANCE);
		} finally {
			temp.delete();
		}
	}
}
