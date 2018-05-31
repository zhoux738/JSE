package info.jultest.test.parser;

import org.junit.Test;

public class ModuleTests extends JulianParserRunner {
	
	@Override
	public String getFeature() {
		return "Module";
	}
	
	@Test
	public void importTest1() {
	    parseFile("import_01.jul");
	}
	
	@Test
	public void importTest2() {
	    parseFile("import_02.jul");
	}
	
	@Test
	public void importTest3() {
	    parseFile("import_03.jul");
	}
	
	@Test
	public void moduleTest1() {
	    parseFile("module_01.jul");
	}
	
	@Test
	public void moduleTest2() {
	    parseFile("module_02.jul");
	}
	
	@Test
	public void moduleTest3() {
	    parseFile("module_03.jul");
	}
	
}
