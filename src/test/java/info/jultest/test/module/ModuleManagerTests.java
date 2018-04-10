package info.jultest.test.module;

import static info.jultest.test.Commons.verifyDetectedClass;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import info.jultest.test.Commons;
import info.jultest.test.FakeJThread;
import info.julang.execution.threading.JThread;
import info.julang.modulesystem.ModuleManager;

//(Uncomment @RunWith for reliability test)
//@RunWith(Parameterized.class)
public class ModuleManagerTests {

	// (Uncomment data() for reliability test)
	//@Parameterized.Parameters
	//public static List<Object[]> data() {
	//  return Arrays.asList(new Object[200][0]);
	//}
	
	private JThread mt;
	
	@Before
	public void setup(){
		mt = new FakeJThread();
	}

	@Test
	public void basicDepTests() {
		ModuleManager manager = new ModuleManager();
		manager.addModulePath(Commons.SRC_REPO_ROOT);
		
		manager.loadModule(mt, "ModuleSys.BasicDep.ModX");
		
		assertTrue(manager.isLoaded("ModuleSys.BasicDep.ModX"));
		assertTrue(manager.isLoaded("ModuleSys.BasicDep.ModY"));
		
		verifyDetectedClass(manager, "ModuleSys.BasicDep.ModX.Xa", "Xa");
		verifyDetectedClass(manager, "ModuleSys.BasicDep.ModY.Ya", "Ya");
	}
	
	@Test
	public void circularDepTests() {
		ModuleManager manager = new ModuleManager();
		manager.addModulePath(Commons.SRC_REPO_ROOT);
		
		manager.loadModule(mt, "ModuleSys.CircularDep.ModX");
		
		assertTrue(manager.isLoaded("ModuleSys.CircularDep.ModX"));
		assertTrue(manager.isLoaded("ModuleSys.CircularDep.ModY"));
		assertTrue(manager.isLoaded("ModuleSys.CircularDep.ModZ"));
	}
	
	// Dependency graph:
	// U -> V -> X -> Y
	@Test
	public void incrementalDepTests() {
		ModuleManager manager = new ModuleManager();
		manager.addModulePath(Commons.SRC_REPO_ROOT);
		
		// Loading X causes loading X and Y
		manager.loadModule(mt, "ModuleSys.IncrementalDep.ModX");
		
		assertTrue(manager.isLoaded("ModuleSys.IncrementalDep.ModX"));
		assertTrue(manager.isLoaded("ModuleSys.IncrementalDep.ModY"));
		assertFalse(manager.isLoaded("ModuleSys.IncrementalDep.ModU"));
		
		verifyDetectedClass(manager, "ModuleSys.IncrementalDep.ModX.Xa", "Xa");
		verifyDetectedClass(manager, "ModuleSys.IncrementalDep.ModY.Ya", "Ya");
		
		// Loading U causes loading U and V
		manager.loadModule(mt, "ModuleSys.IncrementalDep.ModU");
		assertTrue(manager.isLoaded("ModuleSys.IncrementalDep.ModU"));
		assertTrue(manager.isLoaded("ModuleSys.IncrementalDep.ModV"));
		
		verifyDetectedClass(manager, "ModuleSys.IncrementalDep.ModU.Ua", "Ua");
		verifyDetectedClass(manager, "ModuleSys.IncrementalDep.ModV.Va", "Va");
		
		// This test cannot verify whether X and Y are loaded twice.
	}
	
	@Test
	public void foundationModuleTests() {
		ModuleManager manager = new ModuleManager();
		manager.addModulePath(Commons.SRC_REPO_ROOT);
		
		manager.loadModule(mt, "System");
		
		assertTrue(manager.isLoaded("System"));
		
		verifyDetectedClass(manager, "System.Exception", "Exception");
	}
	
	@Test
	public void concurrentLoadingTests() {
		final ModuleManager manager = new ModuleManager();
		manager.addModulePath(Commons.SRC_REPO_ROOT);
		
		Thread t1 = new Thread(new Runnable(){
			@Override
			public void run() {
				manager.loadModule(mt, "ModuleSys.CircularDep.ModX");
			}
		});
		Thread t2 = new Thread(new Runnable(){
			@Override
			public void run() {
				manager.loadModule(mt, "ModuleSys.CircularDep.ModY");
			}
		});
		Thread t3 = new Thread(new Runnable(){
			@Override
			public void run() {
				manager.loadModule(mt, "ModuleSys.CircularDep.ModZ");
			}
		});
		
		t1.start();
		t2.start();
		t3.start();
		
		try {
			t1.join();
			t2.join();
			t3.join();
		} catch (InterruptedException e) {
			fail("Loading threads are interrupted.");
		}
		
		assertTrue(manager.isLoaded("ModuleSys.CircularDep.ModX"));
		assertTrue(manager.isLoaded("ModuleSys.CircularDep.ModY"));
		assertTrue(manager.isLoaded("ModuleSys.CircularDep.ModZ"));
	}
}
