package info.jultest.test.hosted;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.resetTypeSystem;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;

import org.junit.Assert;
import org.junit.Test;

import info.julang.execution.security.PACON;
import info.julang.execution.security.UnderprivilegeException;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.binding.ObjectBinding;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.hosting.mapped.implicit.ImplicitTypeNameConvertor;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.modulesystem.ModuleInfo;
import info.jultest.test.Commons;
import info.jultest.test.hosted.classes.Car;
import info.jultest.test.oo.ExceptionTestRunner;

public class ImplicitBindingTests {
	
	private static final String FEATURE = "Implicit";

	public static class IBTClass1 {
		
		private int a;
		
		public IBTClass1() {}
		
		public IBTClass1(int a) { set(a); }
		
		public void set(int a) {
			this.a = a;
		}
		
		public int get() {
			return a;
		}
	}
	
	@Test
	public void baselineImplicitBindingTest() throws EngineInvocationError {
		// resetTypeSystem();
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		IBTClass1 a = new IBTClass1();
		engine.getContext().addBinding("a", new ObjectBinding(a));
		engine.runSnippet(
			// "Console.println(a.get());" + 
			System.lineSeparator() + 
			"a.set(3);");
		
		Assert.assertEquals(3, a.get());
	}
	
	@Test
	public void implicitBindingPropertiesTest() throws EngineInvocationError {
		resetTypeSystem();
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		IBTClass1 a = new IBTClass1();
		engine.getContext().addBinding("a", new ObjectBinding(a));
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "forbid_1.jul"));
		
		validateIntValue(gvt, "len", 0);
		validateStringValue(gvt, "str", ModuleInfo.IMPLICIT_MODULE_NAME);
	}
	
	public static class IBTClass2 {
		
		private int a;
		
		public IBTClass2(int value) {
			this.a = value;
		}
		
		public void multiply(IBTClass3 arg) {
			this.a *= arg.get();
		}
		
		public int get() {
			return a;
		}
	}
	
	// info.jultest.test.hosted.ImplicitBindingTests.IBTClass3
	public static class IBTClass3 {

		private int a;
		
		public IBTClass3(int value) {
			this.a = value;
		}
		
		public int get() {
			return a;
		}
		
		public void set(int v) {
			this.a = v;
		}
	}
	
	@Test
	public void interdepImplicitBindingTest1() throws EngineInvocationError {
		//resetTypeSystem();
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		IBTClass2 a = new IBTClass2(3);
		IBTClass3 b = new IBTClass3(5);
		engine.getContext().addBinding("a", new ObjectBinding(a));
		engine.getContext().addBinding("b", new ObjectBinding(b));
		engine.runSnippet("a.multiply(b);");
		
		Assert.assertEquals(15, a.get());
	}
	
	// Only reverse the order of adding bindings in the above test
	@Test
	public void interdepImplicitBindingTest2() throws EngineInvocationError {
		//resetTypeSystem();
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		IBTClass3 b = new IBTClass3(5);
		IBTClass2 a = new IBTClass2(3);
		engine.getContext().addBinding("b", new ObjectBinding(b));
		engine.getContext().addBinding("a", new ObjectBinding(a));
		engine.runSnippet("a.multiply(b);");
		
		Assert.assertEquals(15, a.get());
	}
	
	public static class IBTClass4 {
		public static final int ConstVal = 4;
	}
	
	// For public-static-final values of primitive or string type, a static field would be created reflecting its value.
	// However, since the type has unreferenceable name, one can only use reflection API to obtain the value.
	@Test
	public void implicitBindingConstTest() throws EngineInvocationError {
		resetTypeSystem();
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		IBTClass4 a = new IBTClass4();
		engine.getContext().addBinding("a", new ObjectBinding(a));
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "special_1_const.jul"));
		
		validateIntValue(gvt, "cv", 4);
	}
	
	public class IBTClass5 {
		public int get() {
			return 10;
		}
	}
	
	// Disallow nested class binding. This is not supported by the Mapping API, 
	// so out of consistency we won't allow it here either, although it makes little sense to 
	// not support such scenario. We will revisit this in the future. It would require some
	// substantial refactoring in the execution framework to support calling non-static methods
	// with the entire more than one 'this' object.
	@Test
	public void implicitBindingNestedTest() throws EngineInvocationError {
		//resetTypeSystem();
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		IBTClass5 a = new IBTClass5();
		IBTClass5[] arr = new IBTClass5[] { };
		engine.getContext().addBinding("a", new ObjectBinding(a));
		engine.getContext().addBinding("arr", new ObjectBinding(arr));
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "forbid_2.jul"));
		
		validateBoolValue(gvt, "b1", true);
		validateBoolValue(gvt, "b2", true);
	}
	
	public static class IBTClass6 {
		public int get() {
			throw new RuntimeException("Unexpectedly failed during runtime!");
		}
	}
	
	// throw exception
	@Test
	public void implicitBindingThrowsTest() throws EngineInvocationError {
		//resetTypeSystem();
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		IBTClass6 a = new IBTClass6();
		engine.getContext().addBinding("a", new ObjectBinding(a));
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "special_2_throw.jul"));
		
		validateBoolValue(gvt, "b1", true);
	}
	
	// incompatible with explicit mapping
	@Test
	public void implicitBindingIncompatTest() throws EngineInvocationError {
		//resetTypeSystem();
		
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.HOSTING, FEATURE);
		Car a = new Car();
		runner.getEngine().getContext().addBinding("a", new ObjectBinding(a));
		runner.executeAndExpect(
			"special_3_incompat.jul",
			KnownJSException.IllegalAssignment,
			null, 
			// The following are strings that should appear in the exception's message.
			ModuleInfo.IMPLICIT_MODULE_NAME, 
			ImplicitTypeNameConvertor.fromClassNameToSimpleTypeName(Car.class), 
			ModuleInfo.DEFAULT_MODULE_NAME,
			"Car");
	}
	
	@Test
	public void implicitDoubleBindingTest() throws EngineInvocationError {
		//resetTypeSystem();
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		IBTClass3 b = new IBTClass3(5);
		engine.getContext().addBinding("a", new ObjectBinding(b));
		engine.getContext().addBinding("b", new ObjectBinding(b));
		engine.runSnippet("int v1 = a.get() + b.get(); a.set(3); b.set(6);");
		
		Assert.assertEquals(6, b.get());
		validateIntValue(gvt, "v1", 10);
	}
	
	// Implicit dependent, explicit dependency
	@Test
	public void interdepHybridImplicitBindingTest1() throws EngineInvocationError {
		//resetTypeSystem();
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		IBTClass2 a = new IBTClass2(3);
		engine.getContext().addBinding("a", new ObjectBinding(a));
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "hybrid_1.jul"));
		
		Assert.assertEquals(15, a.get());
	}
	
	// Explicit dependent, implicit dependency
	@Test
	public void interdepHybridImplicitBindingTest2() throws EngineInvocationError {
		//resetTypeSystem();
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		IBTClass3 a = new IBTClass3(5);
		engine.getContext().addBinding("a", new ObjectBinding(a));
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "hybrid_2.jul"));

		validateIntValue(gvt, "res", 15);
	}
	
	@Test
	public void disableBindingTest() {
		//resetTypeSystem();
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addPolicy(false, PACON.Interop.Name, new String[] { PACON.Interop.Op_map });
		IBTClass3 a = new IBTClass3(5);
		engine.getContext().addBinding("a", new ObjectBinding(a));
		boolean caught = false;
		
		try {
			engine.runSnippet("int v1 = a.get();");
		} catch (EngineInvocationError err) {
			Throwable cause = err.getCause();
			Assert.assertTrue(cause instanceof UnderprivilegeException);
			Assert.assertTrue(cause.getMessage().contains(PACON.Interop.Name));
			caught = true;
		}
		
		Assert.assertTrue("The UnderprivilegeException was expected but not caught.", caught);
	}
}
