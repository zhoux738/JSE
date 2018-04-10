package info.jultest.test.hosted;

import static info.jultest.test.Commons.getScriptFile;
import info.jultest.test.Commons;
import info.jultest.test.hosted.classes.GBlob;
import info.jultest.test.hosted.classes.Settings;
import info.julang.execution.FileScriptProvider;
import info.julang.jsr223.JulianScriptingEngine;

import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.junit.Assert;
import org.junit.Test;

public class PlatformInteropTests {
	
	private static final String FEATURE = "Execution";
	
	@Test
	public void returnMappedValueTest() throws ScriptException, FileNotFoundException {
		ScriptEngine jse = new JulianScriptingEngine();
		FileScriptProvider provider = getScriptFile(Commons.Groups.HOSTING, FEATURE, "interop_1.jul");
		FileReader reader = new FileReader(provider.getFilePathName(false));
		Object o = jse.eval(reader);
		Assert.assertEquals(GBlob.class, o.getClass());
		GBlob gb = (GBlob)o;
		Assert.assertEquals(10, gb.hash());
	}
	
	@Test
	public void modifyStaticFieldTest() throws ScriptException, FileNotFoundException {
		ScriptEngine jse = new JulianScriptingEngine();
		
		Settings.set(100);
		
		FileScriptProvider provider = getScriptFile(Commons.Groups.HOSTING, FEATURE, "interop_2.jul");
		FileReader reader = new FileReader(provider.getFilePathName(false));
		jse.eval(reader);
		
		int v = Settings.get();
		Assert.assertEquals(200, v);
	}
}
