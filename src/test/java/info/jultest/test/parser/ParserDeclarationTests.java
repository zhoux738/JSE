package info.jultest.test.parser;

import org.junit.Test;

public class ParserDeclarationTests extends ParserRunner {
	
	@Override
	public String getFeature() {
		return "Declaration";
	}

	@Test
	public void annotationClassTest1() {
	    invokeParser("annotated_01_class.jul");
	}
	
	@Test
	public void annotationFieldTest2() {
	    invokeParser("annotated_02_field.jul");
	}
	
	@Test
	public void annotationMethodTest3() {
	    invokeParser("annotated_03_method.jul");
	}
	
	@Test
	public void annotationCtorTest4() {
	    invokeParser("annotated_04_ctor.jul");
	}
	
	@Test
	public void annotationBasicTest1() {
	    invokeParser("annotated_basic_01.jul");
	}
	
	@Test
	public void annotationBasicTest2() {
	    invokeParser("annotated_basic_02.jul");
	}
	
	@Test
	public void annotationBasicTest3() {
	    invokeParser("annotated_basic_03.jul");
	}
	
	@Test
	public void annotationBasicTest4() {
	    invokeParser("annotated_basic_04.jul");
	}
	
	@Test
	public void attrBasicTest1() {
	    invokeParser("attribute_01.jul");
	}
	
	@Test
	public void attrFieldsTest9() {
	    invokeParser("attribute_09_fields.jul");
	}
	
	@Test
	public void attrInitTest10() {
	    invokeParser("attribute_10_initializer.jul");
	}
	
	@Test
	public void enumBasicTest1() {
	    invokeParser("enum_01.jul");
	}
	
	@Test
	public void enumFieldsTest9a() {
	    invokeParser("enum_09_fields_a.jul");
	}
	
	@Test
	public void enumFieldsTest9b() {
	    invokeParser("enum_09_fields_b.jul");
	}
	
	@Test
	public void enumInitTest10a() {
	    invokeParser("enum_10_initializer_a.jul");
	}
	
	@Test
	public void enumInitTest10b() {
	    invokeParser("enum_10_initializer_b.jul");
	}
	
	@Test
	public void enumInitTest10c() {
	    invokeParser("enum_10_initializer_c.jul");
	}
	
	@Test
	public void interfaceBasicTest1() {
	    invokeParser("interface_01.jul");
	}
	
	@Test
	public void interfaceExtTest3() {
	    invokeParser("interface_03_extension.jul");
	}
	
	@Test
	public void interfaceMethodsTest7() {
	    invokeParser("interface_07_methods.jul");
	}
	
	@Test
	public void classBasicTest1() {
	    invokeParser("class_01.jul");
	}
	
	@Test
	public void classModTest2() {
	    invokeParser("class_02_modifiers.jul");
	}
	
	@Test
	public void classExtTest3() {
	    invokeParser("class_03_extension.jul");
	}
	
	@Test
	public void classStaticCtorTest4() {
	    invokeParser("class_04_static_ctor.jul");
	}
	
	@Test
	public void classCtorTest5() {
	    invokeParser("class_05_ctors.jul");
	}
	
	@Test
	public void classCtorForwardTest6() {
	    invokeParser("class_06_ctor_forward.jul");
	}
	
	@Test
	public void classMethodsTest7() {
	    invokeParser("class_07_methods.jul");
	}
	
	@Test
	public void classAbstractTest8() {
	    invokeParser("class_08_abstract.jul");
	}
	
	@Test
	public void classFieldsTest9() {
	    invokeParser("class_09_fields.jul");
	}
	
	@Test
	public void classInitTest10() {
	    invokeParser("class_10_initializer.jul");
	}
	
	@Test
	public void classMixTest20() {
	    invokeParser("class_20_mix.jul");
	}
	
}
