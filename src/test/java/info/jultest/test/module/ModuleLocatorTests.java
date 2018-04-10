package info.jultest.test.module;

import static org.junit.Assert.assertEquals;
import info.jultest.test.Commons;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.modulesystem.ModuleLocationInfo;
import info.julang.modulesystem.ModuleLocator;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class ModuleLocatorTests {
	
	@Test
	public void getModuleScriptsTest() throws EngineInvocationError {
		ModuleLocator manager = new ModuleLocator();
		manager.addModulePath(Commons.SCRIPT_ROOT);
		
		ModuleLocationInfo info = manager.findModuleFiles("Imperative.While");
		
		List<String> paths = info.getScriptPaths();
		assertEquals(3, paths.size());
		
		Set<String> set = new HashSet<String>();
		set.add("while_01.jul");
		set.add("while_02.jul");
		set.add("while_03.jul");
		for(String path : paths){
			set.contains((new File(path)).getName());
		}
	}
	
}
