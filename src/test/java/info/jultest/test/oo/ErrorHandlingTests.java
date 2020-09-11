package info.jultest.test.oo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import info.julang.execution.StandardIO;
import info.julang.execution.namespace.NamespacePool;
import info.julang.execution.simple.SimpleEngineRuntime;
import info.julang.execution.symboltable.TypeTable;
import info.julang.execution.symboltable.VariableTable;
import info.julang.execution.threading.JThread;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.context.FunctionContext;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.memory.HeapArea;
import info.julang.memory.MemoryArea;
import info.julang.memory.simple.SimpleHeapArea;
import info.julang.modulesystem.ModuleManager;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.loading.InternalTypeResolver;
import info.jultest.test.Commons;
import info.jultest.test.FakeJThread;

public class ErrorHandlingTests {

	class TestableJulianScriptException extends JulianScriptException {

		private static final long serialVersionUID = 1L;

		TestableJulianScriptException(JClassType type, MemoryArea memory) {
			super(type, memory, Commons.DummyTypeTable);
		}
		
		void addStackTrace0(String funcName, String[] parameters){
			this.addStackTrace(Commons.DummyTypeTable, funcName, parameters, null, -1);
		}
		
	}
	
	private HeapArea memory;
	private TypeTable tt;
	private ModuleManager manager;
	private Context context;
	private InternalTypeResolver resolver;
	private JThread mt;

	@Before
	public void setup(){
		mt = new FakeJThread();
		memory = new SimpleHeapArea();
		tt = new TypeTable(memory);
		
		ModuleManager mm = new ModuleManager();
		VariableTable gvt = new VariableTable(null);
		SimpleEngineRuntime sert = new SimpleEngineRuntime(memory, gvt, tt, mm);
		
		tt.initialize(sert);
		
		manager = new ModuleManager();
		manager.loadModule(mt, "System");
		
		resolver = new InternalTypeResolver();
		
		context = new FunctionContext(
			null, //(MemoryArea) 
			memory,
			new VariableTable(null), //(VariableTable)
			tt, 
			resolver,
			manager,
			new NamespacePool(),
			new StandardIO(),
			null); //(JThread)
	}
	
	@Test
	public void basicFuncTest() throws EngineInvocationError {	
		JType typ = context.getTypeResolver().resolveType(
			ParsedTypeName.makeFromFullName("System.Exception"));
		
		TestableJulianScriptException jse = new TestableJulianScriptException((JClassType)typ, memory);
		
		Assert.assertEquals("Script exception. Type: System.Exception", jse.getMessage());
		
		// We add 11 records to make the array scale up.
		jse.addStackTrace0("fun1", new String[]{});
		jse.addStackTrace0("fun2", new String[]{});
		jse.addStackTrace0("fun3", new String[]{});
		jse.addStackTrace0("fun4", new String[]{});
		jse.addStackTrace0("fun5", new String[]{});
		jse.addStackTrace0("fun6", new String[]{});
		jse.addStackTrace0("fun7", new String[]{});
		jse.addStackTrace0("fun8", new String[]{});
		jse.addStackTrace0("fun9", new String[]{});
		jse.addStackTrace0("fun10", new String[]{});
		jse.addStackTrace0("fun11", new String[]{});
		
		String[] strs = jse.getStackTraceAsArray();
		Assert.assertEquals(11, strs.length);
		Assert.assertEquals("fun1()", strs[0]);
		Assert.assertEquals("fun11()", strs[10]);
	}

	@Test
	public void inheritedExceptionTest() throws EngineInvocationError {	
		JType typ = context.getTypeResolver().resolveType(
			ParsedTypeName.makeFromFullName("System.DivByZeroException"));
		
		TestableJulianScriptException jse = new TestableJulianScriptException((JClassType)typ, memory);
		
		Assert.assertEquals("Script exception. Type: System.DivByZeroException", jse.getMessage());
		
		jse.addStackTrace0("funA", new String[]{"String"});
		jse.addStackTrace0("funB", new String[]{});
		jse.addStackTrace0("funC", new String[]{"int", "int"});
		
		String[] strs = jse.getStackTraceAsArray();
		Assert.assertEquals(3, strs.length);
		Assert.assertEquals("funA(String)", strs[0]);
		Assert.assertEquals("funB()", strs[1]);		
		Assert.assertEquals("funC(int,int)", strs[2]);
	}
}
