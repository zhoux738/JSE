package info.jultest.test.clapp;

import org.junit.Before;
import org.junit.Test;

import info.julang.external.JulianScriptEngine;
import info.julang.external.exceptions.JSEException;
import org.junit.Assert;

public class JulianScriptEngineTests {

	private JulianScriptEngine jse;
	
	@Before
	public void setup() {
		jse = new JulianScriptEngine(false, false);
	}
	
	@Test
	public void passArgsTest() throws JSEException {
		String val = (String)jse.runScript("return arguments[1];", new String[] { "hello", "world" });
		
		Assert.assertEquals("world", val);
	}
	
	@Test
	public void passArgsTest2() throws JSEException {
		int val = (int)jse.runScript("return arguments.length;", new String[] { "hello", "world" });
		
		Assert.assertEquals(2, val);
	}
	
	@Test
	public void passArgsTest3() throws JSEException {
		int val = (int)jse.runScript("return arguments.length;", null);
		
		Assert.assertEquals(0, val);
	}
}
