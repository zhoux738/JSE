package info.jultest.test.parser;

import org.junit.Test;

public class ModuleTests extends ParserRunner {
	
	@Override
	public String getFeature() {
		return "Module";
	}
	
	@Test
	public void importTest1() {
	    invokeParser("import_01.jul");
	}
	
	@Test
	public void importTest2() {
	    invokeParser("import_02.jul");
	}
	
	@Test
	public void importTest3() {
	    invokeParser("import_03.jul");
	}
	
	@Test
	public void moduleTest1() {
	    invokeParser("module_01.jul");
	}
	
	@Test
	public void moduleTest2() {
	    invokeParser("module_02.jul");
	}
	
	@Test
	public void moduleTest3() {
	    invokeParser("module_03.jul");
	}
	
}
