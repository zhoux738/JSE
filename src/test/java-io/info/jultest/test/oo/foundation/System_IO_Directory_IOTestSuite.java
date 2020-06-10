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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class System_IO_Directory_IOTestSuite {

	private static final String FEATURE = "Foundation/IO";
	
	@Test
	public void newDirectoryTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, gvt, tt, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		// 1) create a temp file, write something		
		Path temp = Files.createTempDirectory("tmpdir-"); 
		File dir = temp.toFile();
		dir.deleteOnExit();
	    
		// 2) create a global var "path" and set temp directory's full path to it
	    tt.initialize(engine.getRuntime());		
	    gvt.addVariable("path", new StringValue(heap, dir.getCanonicalPath()));
		
		// 3) in dir_1.jul, new up a direcotry using "path", and run various methods
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "dir_1.jul"));
		
		// 4) validate
		validateStringValue(gvt, "fname", dir.getName());
		validateStringValue(gvt, "p", dir.getCanonicalPath());
		validateBoolValue(gvt, "e", true);
		
		// 5) delete the temp directory
		dir.delete();
	}
	
	@Test
	public void listDirectoryTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, gvt, tt, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		// 1) create a temp directory and sub-directories/files
		Path temp = Files.createTempDirectory("tmpdir-"); 
		File dir = temp.toFile();
		dir.deleteOnExit();
		
		String file1path = dir.getCanonicalPath() + File.separator + "file1";
		File file1 = new File(file1path);
		file1.createNewFile();
		file1.deleteOnExit();
		
		String file2path = dir.getCanonicalPath() + File.separator + "file2";
		File file2 = new File(file2path);
		file2.createNewFile();
		file2.deleteOnExit();
		
		String dir1path = dir.getCanonicalPath() + File.separator + "dir1";
		Path p = Files.createDirectory(Paths.get(dir1path));
		File dir1 = p.toFile();
		dir1.deleteOnExit();
		
		try {
//			File[] files = dir.listFiles();
//			for(File file : files){
//				System.out.println(file.getCanonicalPath() + ", " + (file.isDirectory() ? "D" : "F"));
//			}
			
			// 2) create a global var "path" and set temp directory's full path to it	
		    tt.initialize(engine.getRuntime());	
		    gvt.addVariable("path", new StringValue(heap, dir.getCanonicalPath()));
			
			// 3) in dir_2.jul, new up a direcotry using "path", and run various methods
			engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "dir_2.jul"));
			
//			// 4) validate
//			validateStringValue(gvt, "fname", dir.getName());
//			validateStringValue(gvt, "p", dir.getCanonicalPath());
//			validateBoolValue(gvt, "e", true);
			
			validateStringValue(gvt, "file1", "file1");
			validateStringValue(gvt, "file2", "file2");
			validateStringValue(gvt, "dir1", "dir1");
			
			validateBoolValue(gvt, "file1b", true);
			validateBoolValue(gvt, "file2b", true);
			validateBoolValue(gvt, "dir1b", false);		
		} finally {
			// 5) delete the temp files
			file1.delete();
			file2.delete();
			dir1.delete();
			dir.delete();		
		}
	}
	
}
