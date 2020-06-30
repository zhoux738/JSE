package info.jultest.test.external;

import static info.jultest.test.Commons.getScriptFile;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.junit.Assert;
import org.junit.Test;

import info.julang.execution.FileScriptProvider;
import info.julang.jsr223.JulianScriptingEngine;
import info.jultest.test.Commons;
import info.jultest.test.EFCommons;

public class JSR223AdvancedTestSuite {
	
	@Test
	public void bindingTest1() throws ScriptException {
		ScriptEngine jse = new JulianScriptingEngine();
		Bindings bindings = jse.createBindings();
		bindings.put("a", 3);
		Object o = jse.eval("return a;", bindings);
		Assert.assertEquals(o, 3);
		assertIntBinding(bindings, "a", 3);
	}
	
	@Test
	public void bindingTest2() throws ScriptException {
		ScriptEngine jse = new JulianScriptingEngine();
		jse.put("a", 3);
		Object o = jse.eval("a++; return a;");
		Assert.assertEquals(o, 4);
		assertIntBinding(jse, "a", 4);
	}
	
	@Test
	public void bindingTest3() throws ScriptException {
		ScriptEngine jse = new JulianScriptingEngine();
		jse.put("s", "string");
		jse.put("b", true);
		jse.put("f", 1.2f);
		jse.put("c", 'c');
		Object o = jse.eval("return s + b + f + c;");
		Assert.assertEquals(o, "stringtrue1.2c");
	}
	
	@Test
	public void advTest1() throws ScriptException, FileNotFoundException {
		ScriptEngine jse = new JulianScriptingEngine();
		jse.put("val", "abc");
		FileScriptProvider provider = FileScriptProvider.create(getPath("jsr223_1.jul"));
		FileReader reader = new FileReader(provider.getFilePathName(false));
		Object o = jse.eval(reader);
		Assert.assertEquals("def", jse.get("val"));
		Assert.assertEquals("xyz", o);
	}
	
	@Test
	public void replTest1() throws ScriptException {
		ScriptEngine jse = new JulianScriptingEngine();
		jse.put("a", 3);
		jse.eval("a++;");
		assertIntBinding(jse, "a", 4);
		jse.eval("a++;");
		assertIntBinding(jse, "a", 5);
	}
	
	@Test
	public void replTest2() throws ScriptException {
		ScriptEngine jse = new JulianScriptingEngine();
		jse.put("a", 4);
		jse.eval("int b = a;");
		jse.eval("a = b + 1;");
		assertIntBinding(jse, "a", 5);
	}
	
	@Test
	public void replTest3() throws ScriptException {
		ScriptEngine jse = new JulianScriptingEngine();
		jse.eval("int getVal(){return 4;}");
		Object o = jse.eval("return getVal();");
		Assert.assertEquals(o, 4);
	}
	
	/**
	 * A set of classes simulating banking service.
	 * Scenario 3: event listening using lambda
	 */	
	@Test
	public void moduleTest() throws ScriptException, FileNotFoundException {
		ScriptEngine jse = new JulianScriptingEngine();
		List<String> modules = new ArrayList<String>();
		modules.add(Commons.SRC_REPO_ROOT);
		jse.getContext().setAttribute(JulianScriptingEngine.MODULE_PATHS, modules, ScriptContext.ENGINE_SCOPE);	
		
		FileScriptProvider provider = getScriptFile(Commons.Groups.APPLICATION, null, "banking_3.jul");
		FileReader reader = new FileReader(provider.getFilePathName(false));
		Object o = jse.eval(reader);
		
		Assert.assertEquals("10001 [DEPOSIT] VALUE=1000.0, 10001 [WITHDRAW] VALUE=300.0, ", o);	
	}
	
	/*
	 * // [static.jul]
	 * 
	 * import System.Concurrency;
	 * 
	 * class MyClass {
	 *   static int Value = 0;
	 * }
	 * 
	 * for(int i = 0; i < times; i++){
	 * 	Thread.sleep(sleepInterval);
	 * 	MyClass.Value += base;
	 * }
	 * 
	 * return MyClass.Value;
	 */
	/**
	 * Starts multiple engines, running in parallel. These engines run against the same script, which defines a class
	 * and keeps changing a static field on it. At the end we verify that those changes are independent of each other.
	 * (i.e. each engine instance has a unique type instance of the defined class).
	 * 
	 * @throws ScriptException
	 * @throws FileNotFoundException
	 */
	@Test
	public void multiInstancesTest() throws ScriptException, FileNotFoundException {
		int total = 10;
		final int ASSERTION_FAILURE = 1;
		final int RUNNING_FAILURE = 2;
		final int SUCCESS = 9;
		
		Thread[] ts = new Thread[total];
		final int[] statuses = new int[total];
		for(int i = 0; i < total; i++) {
			final int k = i;
			Thread t = new Thread(new Runnable(){
				@Override
				public void run() {
					try {
						runSingle(k + 1, k + 1, k % 2 + 1);
						statuses[k] = SUCCESS;
					} catch (AssertionError e) {
						statuses[k] = ASSERTION_FAILURE;
					} catch (Throwable e) {
						statuses[k] = RUNNING_FAILURE;
					}
				}
			});
			t.start();
			ts[i] = t;
		}
		

		for(int i = 0; i < total; i++) {
			try {
				ts[i].join();
			} catch (InterruptedException e) {
				// Ignore
			}
			
			if (statuses[i] != SUCCESS) {
				Assert.fail("Thread " + i + " failed with status = " + statuses[i]);
			}
		}
	}
	
	private void runSingle(int times, int base, int sleepInterval) throws FileNotFoundException, ScriptException {
		JulianScriptingEngine engine = new JulianScriptingEngine();
		String path = EFCommons.EXTERNAL_ROOT + "static.jul";
		engine.put("times", times);
		engine.put("base", base);
		engine.put("sleepInterval", sleepInterval);
		Object o = engine.eval(new FileReader(path));
		Assert.assertEquals(o, times * base);
	}
	
	private void assertIntBinding(Bindings bindings, String name, int value){
		Object obj = bindings.get(name);
		Assert.assertTrue(obj instanceof Integer);
		int i = (int) obj;
		Assert.assertEquals(value, i);
	}
	
	private void assertIntBinding(ScriptEngine jse, String name, int value){
		assertIntBinding(jse.getBindings(ScriptContext.ENGINE_SCOPE), name, value);
	}

	
	private String getPath(String relativePath){
		return Commons.SRC_REPO_ROOT + "ExternalAPI/" + relativePath;
	}
}
