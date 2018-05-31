package info.jultest.test.parser;

import org.junit.Test;

public class ParserDeclarationTests extends JulianParserRunner {
	
	@Override
	public String getFeature() {
		return "Declaration";
	}

	@Test
	public void annotationClassTest1() {
	    parseFile("annotated_01_class.jul");
	}
	
	@Test
	public void annotationFieldTest2() {
	    parseFile("annotated_02_field.jul");
	}
	
	@Test
	public void annotationMethodTest3() {
	    parseFile("annotated_03_method.jul");
	}
	
	@Test
	public void annotationCtorTest4() {
	    parseFile("annotated_04_ctor.jul");
	}
	
	@Test
	public void annotationBasicTest1() {
	    parseFile("annotated_basic_01.jul");
	}
	
	@Test
	public void annotationBasicTest2() {
	    parseFile("annotated_basic_02.jul");
	}
	
	@Test
	public void annotationBasicTest3() {
	    parseFile("annotated_basic_03.jul");
	}
	
	@Test
	public void annotationBasicTest4() {
	    parseFile("annotated_basic_04.jul");
	}
	
	@Test
	public void attrBasicTest1() {
	    parseFile("attribute_01.jul");
	}
	
	@Test
	public void attrFieldsTest9() {
	    parseFile("attribute_09_fields.jul");
	}
	
	@Test
	public void attrInitTest10() {
	    parseFile("attribute_10_initializer.jul");
	}
	
	@Test
	public void enumBasicTest1() {
	    parseFile("enum_01.jul");
	}
	
	@Test
	public void enumFieldsTest9a() {
	    parseFile("enum_09_fields_a.jul");
	}
	
	@Test
	public void enumFieldsTest9b() {
	    parseFile("enum_09_fields_b.jul");
	}
	
	@Test
	public void enumInitTest10a() {
	    parseFile("enum_10_initializer_a.jul");
	}
	
	@Test
	public void enumInitTest10b() {
	    parseFile("enum_10_initializer_b.jul");
	}
	
	@Test
	public void enumInitTest10c() {
	    parseFile("enum_10_initializer_c.jul");
	}
	
	@Test
	public void interfaceBasicTest1() {
	    parseFile("interface_01.jul");
	}
	
	@Test
	public void interfaceExtTest3() {
	    parseFile("interface_03_extension.jul");
	}
	
	@Test
	public void interfaceMethodsTest7() {
	    parseFile("interface_07_methods.jul");
	}
	
	@Test
	public void classBasicTest1() {
	    parseFile("class_01.jul");
	}
	
	@Test
	public void classModTest2() {
	    parseFile("class_02_modifiers.jul");
	}
	
	@Test
	public void classExtTest3() {
	    parseFile("class_03_extension.jul");
	}
	
	@Test
	public void classStaticCtorTest4() {
	    parseFile("class_04_static_ctor.jul");
	}
	
	@Test
	public void classCtorTest5() {
	    parseFile("class_05_ctors.jul");
	}
	
	@Test
	public void classCtorForwardTest6() {
	    parseFile("class_06_ctor_forward.jul");
	}
	
	@Test
	public void classMethodsTest7() {
	    parseFile("class_07_methods.jul");
	}
	
	@Test
	public void classAbstractTest8() {
	    parseFile("class_08_abstract.jul");
	}
	
	@Test
	public void classFieldsTest9() {
	    parseFile("class_09_fields.jul");
	}
	
	@Test
	public void classInitTest10() {
	    parseFile("class_10_initializer.jul");
	}
	
	@Test
	public void classMixTest20() {
	    parseFile("class_20_mix.jul");
	}
	
}
