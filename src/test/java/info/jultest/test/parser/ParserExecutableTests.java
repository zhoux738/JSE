package info.jultest.test.parser;

import org.junit.Test;

public class ParserExecutableTests extends JulianParserRunner {
	
	@Override
	public String getFeature() {
		return "Executable";
	}
	
	// A most basic smoke test
	@Test
	public void parserBasicTest1() {
	    parseFile("parser01.jul");
	}
	
	// Multiple statements, comments, nested blocks.
	@Test
	public void parserBasicTest2() {
	    parseFile("parser02.jul");
	}
	
	// Compound arithmetic expressions.
	@Test
	public void parserBasicTest3() {
	    parseFile("parser03.jul");
	}
	
	// Compound comparison expressions; chained assignment.
	@Test
	public void parserBasicTest4() {
	    parseFile("parser04.jul");
	}
	
	// Function declaration.
	@Test
	public void parserBasicTest5() {
	    parseFile("parser05.jul");
	}
	
	// Function declaration - additional tests.
	@Test
	public void parserBasicTest5a() {
	    parseFile("parser05_a.jul");
	}
	
	// Object initialization.
	@Test
	public void parserCtorTest1() {
	    parseFile("parser_ctor_01.jul");
	}
	
	// Object initialization with initializer.
	@Test
	public void parserCtorTest2() {
	    parseFile("parser_ctor_02.jul");
	}
	
	@Test
	public void parserLambdaBasicTest_1() {
	    parseFile("parser_lambda_01.jul");
	}
	
	@Test
	public void parserLambdaBasicTest_2() {
	    parseFile("parser_lambda_02.jul");
	}

	@Test
	public void parserLambdaBasicTest_3() {
	    parseFile("parser_lambda_03.jul");
	}

	@Test
	public void parserLambdaBasicTest_4() {
	    parseFile("parser_lambda_04.jul");
	}
	
	// Single dimension
	@Test
	public void parserArrayBasicTest_1() {
	    parseFile("parser_array_01.jul");
	}
	
	// Multi dimension
	@Test
	public void parserArrayBasicTest_2() {
	    parseFile("parser_array_02.jul");
	}
	
	// Single dimension with initializer
	@Test
	public void parserArrayBasicTest_3() {
	    parseFile("parser_array_03.jul");
	}
	
	// Multi dimension with initializer
	@Test
	public void parserArrayBasicTest_4() {
	    parseFile("parser_array_04.jul");
	}
	
	@Test
	public void parserAccessBasicTest_1() {
	    parseFile("parser_access_01.jul");
	}
	
	@Test
	public void parserAccessBasicTest_2() {
	    parseFile("parser_access_02.jul");
	}
	
	// The most basic form
	@Test
	public void parserForTest_1() {
	    parseFile("parser_stmt_for_01.jul");
	}
	
	// Double initializers
	@Test
	public void parserForTest_2() {
	    parseFile("parser_stmt_for_02.jul");
	}
	
	// Special cases
	@Test
	public void parserForTest_3() {
	    parseFile("parser_stmt_for_03.jul");
	}
	
	// Expression instead of block
	@Test
	public void parserForTest_4() {
	    parseFile("parser_stmt_for_04.jul");
	}
	
	// If - else if - else with blocks
	@Test
	public void parserIfTest_1() {
	    parseFile("parser_stmt_if_01.jul");
	}
	
	// If - else if - else with expressions
	@Test
	public void parserIfTest_2() {
	    parseFile("parser_stmt_if_02.jul");
	}
	
	@Test
	public void parserTryTest_1() {
	    parseFile("parser_stmt_try_01.jul");
	}
	
	@Test
	public void parserTryTest_2() {
	    parseFile("parser_stmt_try_02.jul");
	}
	
	@Test
	public void parserTryTest_3() {
	    parseFile("parser_stmt_try_03.jul");
	}
	
	@Test
	public void parserTryTest_4() {
	    parseFile("parser_stmt_try_04.jul");
	}
	
}
