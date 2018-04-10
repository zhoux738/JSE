package info.jultest.test.external;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import info.jultest.test.Commons;
import info.julang.external.JulianScriptEngine;
import info.julang.external.exceptions.ExternalBindingException;
import info.julang.external.exceptions.JSEException;

public class ReentryTests {
	
	private JulianScriptEngine jse = null;
	
	@Before
	public void setup() {
		jse = new JulianScriptEngine(false, false);
	}
	
	@Test
	public void basicBindingTest() throws JSEException {
		jse.bindInt("a", 5);
		
		jse.runFile(getPath("reentry_1.jul"), null);
		int res1 = jse.getInt("a");
		Assert.assertEquals(6, res1);
		
		jse.runFile(getPath("reentry_1.jul"));
		int res2 = jse.getInt("a");
		Assert.assertEquals(7, res2);
	}
	
	@Test
	public void reloadingWithoutResettingTest() throws ExternalBindingException {
		jse.bindInt("a", 0);
		
		jse.runFile(getPath("reentry_2.jul"));
		int res1 = jse.getInt("a");
		Assert.assertEquals(1, res1);
		
		jse.runFile(getPath("reentry_2.jul"));
		int res2 = jse.getInt("a");
		Assert.assertEquals(2, res2);
	}
	
	@Test
	public void reloadingWithResettingTest() throws ExternalBindingException {
		jse.bindInt("a", 0);
		
		jse.runFile(getPath("reentry_2.jul"));
		int res1 = jse.getInt("a");
		Assert.assertEquals(1, res1);
		
		jse.reset();
		
		jse.runFile(getPath("reentry_2.jul"));
		int res2 = jse.getInt("a");
		Assert.assertEquals(1, res2);
	}
	
	private String getPath(String relativePath){
		return Commons.SRC_REPO_ROOT + "ExternalAPI/" + relativePath;
	}

}
