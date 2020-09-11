package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.EngineFactory.EngineParamPair;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.interfaces.IExtScriptEngine;
import info.jultest.test.Commons;
import info.jultest.test.EFCommons;

public class System_Console_Tests {

	private static final String FEATURE = "Foundation";
	
	@Test
	public void consolePrintBasicTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "console_1.jul"));
		Commons.validateBoolValue(gvt, "done", true);
	}
	
	@Test
	public void consolePrintObjectTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "console_2.jul"));
		Commons.validateBoolValue(gvt, "done", true);
	}
	
	@Test
	public void consoleRedirectTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		engine.setRedirection(new PrintStream(baos), null, null);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "console_3.jul"));
		
		baos.flush();
		String str = baos.toString();
		Assert.assertEquals("abc123falsez", str);
	}
	
	// Same script file as consoleRedirectTest(), but with an engine created from the factory. 
	@Test
	public void consoleRedirectAcrossClassLoaderTest() throws EngineInvocationError, IOException {
		EngineParamPair pair = EFCommons.prepareViaFactory(null);
		IExtScriptEngine engine = pair.getFirst();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		engine.setRedirection(new PrintStream(baos), null, null);
		
		String path = Commons.makeScriptPath(Commons.Groups.OO, FEATURE, "console_3.jul");
		engine.runFile(path);
		
		baos.flush();
		String str = baos.toString();
		Assert.assertEquals("abc123falsez", str);
	}
	
	@Test
	public void consoleReadlinesTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		String s1 = "abc";
		String s2 = "def";
		String str = s1 + System.lineSeparator() + s2 + System.lineSeparator();
		InputStream is = new ByteArrayInputStream(str.getBytes());
		engine.setRedirection(null, null, is);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "console_4.jul"));
		
		Commons.validateStringValue(gvt, "out", s1 + s2);
		Commons.validateBoolValue(gvt, "flag", false);
		Commons.validateBoolValue(gvt, "done", true);
	}
	
	@Test
	public void errorRedirectTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		engine.setRedirection(null, new PrintStream(baos), null);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "console_5.jul"));
		
		baos.flush();
		String str = baos.toString();
		Assert.assertTrue(str.contains("Failed at static ctor!"));
	}
}
