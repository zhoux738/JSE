package info.jultest.test.external;

import info.jultest.test.TestCaseEngineFactory;
import info.julang.external.EngineComponentClassLoader;
import info.julang.external.EngineFactory;
import info.julang.external.interfaces.IExtMemoryArea;
import info.julang.external.interfaces.IExtModuleManager;
import info.julang.external.interfaces.IExtScriptEngine;
import info.julang.external.interfaces.IExtTypeTable;
import info.julang.external.interfaces.IExtVariableTable;

import org.junit.Assert;
import org.junit.Test;

public class EngineFactoryTestSuite {
	
	@Test
	public void createFactoryTest() {
		EngineFactory factory = new TestCaseEngineFactory();
		IExtScriptEngine engine = factory.createEngine();
		
		Assert.assertNotNull(engine);
	}
	
	@Test
	public void createFactoryComponentTest() {
		EngineFactory factory = new TestCaseEngineFactory();
		IExtMemoryArea mem = factory.createHeapMemory();
		IExtVariableTable gvt = factory.createGlobalVariableTable();
		IExtTypeTable tt = factory.createTypeTable(mem);
		IExtModuleManager modm = factory.createModuleManager();
		
		Assert.assertNotNull(mem);
		Assert.assertNotNull(gvt);
		Assert.assertNotNull(tt);
		Assert.assertNotNull(modm);

		// All of these components should use customized class loader
		ClassLoader loader = mem.getClass().getClassLoader();
		Assert.assertEquals(loader.getClass().getName(), EngineComponentClassLoader.class.getName());
		Assert.assertEquals(loader, gvt.getClass().getClassLoader());
		Assert.assertEquals(loader, tt.getClass().getClassLoader());
		Assert.assertEquals(loader, modm.getClass().getClassLoader());
		
		// But it is different from the one that is exposed to the caller
		Assert.assertNotSame(EngineFactory.class.getClassLoader(), loader);
	}
}
