package info.jultest.test.external;

import javax.script.ScriptException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import info.julang.jsr223.JulianScriptingEngine;

public class JSR223BasicTests {
	
	private JulianScriptingEngine jse = null;
	
	@Before
	public void setup() {
		jse = new JulianScriptingEngine();
	}
	
	@Test
	public void basicEvalTest1() throws ScriptException {
		Object o = jse.eval("return 3;");
		Assert.assertEquals(o, 3);
	}
	
	@Test
	public void basicEvalTest2() throws ScriptException {
		Object o = jse.eval("");
		Assert.assertNull(o);
	}

}
