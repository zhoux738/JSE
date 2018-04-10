package info.jultest.test.parser;

import static info.jultest.test.Commons.makeSimpleEngine;
import info.jultest.test.Commons;
import info.jultest.test.TestExceptionHandler;
import info.jultest.test.oo.ExceptionTestsBase;
import info.julang.dev.GlobalSetting;
import info.julang.execution.FileScriptProvider;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Assume;
import org.junit.Test;

public class SyntaxErrorTests extends ExceptionTestsBase {

	private FileScriptProvider getScriptFile(String fileName){
		String path = Commons.PARSING_ROOT + "SyntaxError/" + fileName;
		return FileScriptProvider.create(path);
	}
	
	@Test
	public void typeDeclMisplacementTest() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);

		FileScriptProvider fsp = getScriptFile("prog_6.jul");
		engine.run(fsp);

		validateException(
			teh, 
			"System.Lang.BadSyntaxException",
			null,
			new String[0], // No stacktrace for a syntax error.
			fsp.getFilePathName(true),
			3);
	}
	
	@Test
	public void globalScriptSyntaxErrorTest1() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);

		FileScriptProvider fsp = getScriptFile("prog_1.jul");
		engine.run(fsp);

		validateException(
			teh, 
			"System.Lang.BadSyntaxException",
			null,
			new String[0], // No stacktrace for a syntax error.
			fsp.getFilePathName(true),
			2);
	}
	

	@Test
	public void globalScriptSyntaxErrorTest2() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);

		FileScriptProvider fsp = getScriptFile("prog_2.jul");
		engine.run(fsp);

		validateException(
			teh, 
			"System.Lang.BadSyntaxException",
			null,
			new String[0], // No stacktrace for a syntax error.
			fsp.getFilePathName(true),
			4);
	}
	
	// The syntax error is found in another module, but it doesn't prevent the module from being detected.
	@Test
	public void moduleSyntaxErrorTest1() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);

		FileScriptProvider fsp = getScriptFile("prog_3.jul");
		engine.run(fsp);

		validateException(
			teh, 
			"System.ClassLoadingException",
			null,
			new String[0],
			null,
			3);
			
		validateCause(
			teh, 
			"System.Lang.BadSyntaxException",
			null,
			new String[0],
			null,
			4);
	}
	
	// Multiple syntax errors are found in another module, but they don't prevent the modules from being detected.
	// The results from the aggregated exception is not ideal yet - we are missing location info for some nested exceptions.
	@Test
	public void moduleSyntaxErrorTest2() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);

		FileScriptProvider fsp = getScriptFile("prog_4.jul");
		engine.run(fsp);
	}
	
	@Test
	public void fatalModuleSyntaxErrorTest1() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);

		FileScriptProvider fsp = getScriptFile("prog_5.jul");
		engine.run(fsp);
		
		validateException(
			teh, 
			"System.ClassLoadingException",
			null,
			new String[0],
			null,
			3);
			
		validateCause(
			teh, 
			"System.Lang.BadSyntaxException",
			null,
			new String[0],
			null,
			7);
	}
	
}
