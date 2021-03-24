package info.jultest.test.external;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import info.jultest.test.Commons;
import info.julang.external.JulianScriptEngine;
import info.julang.external.exceptions.ExternalBindingException;
import info.julang.external.exceptions.JSEException;
import info.julang.external.interfaces.ResetPolicy;

public class ReentryTestSuite {
	
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
		
		jse.reset(ResetPolicy.FULL);
		
		jse.runFile(getPath("reentry_2.jul"));
		int res2 = jse.getInt("a");
		Assert.assertEquals(1, res2);
	}
	
	@Test
	public void reloadingWithoutResettingTest2() throws ExternalBindingException {
		jse.bindInt("curr", 0);
		
		jse.runFile(getPath("reentry_3.jul"));
		int res1 = jse.getInt("curr");
		Assert.assertEquals(10, res1);
		
		jse.reset(ResetPolicy.FULL);
		
		jse.runFile(getPath("reentry_3.jul"));
		int res2 = jse.getInt("curr");
		Assert.assertEquals(10, res2);
	}

	@Test
	public void reloadingWithResettingTest2() throws ExternalBindingException {
		jse.bindInt("curr", 0);
		
		jse.runFile(getPath("reentry_3.jul"));
		int res1 = jse.getInt("curr");
		Assert.assertEquals(10, res1);
		
		jse.runFile(getPath("reentry_3.jul"));
		int res2 = jse.getInt("curr");
		Assert.assertEquals(11, res2);
	}
	
	private String getPath(String relativePath){
		return Commons.SRC_REPO_ROOT + "ExternalAPI/" + relativePath;
	}

}
