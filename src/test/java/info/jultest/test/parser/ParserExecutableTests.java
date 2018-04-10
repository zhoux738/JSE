package info.jultest.test.parser;

import org.junit.Test;

public class ParserExecutableTests extends ParserRunner {
	
	@Override
	public String getFeature() {
		return "Executable";
	}
	
	// A most basic smoke test
	@Test
	public void parserBasicTest1() {
	    invokeParser("parser01.jul");
	}
	
	// Multiple statements, comments, nested blocks.
	@Test
	public void parserBasicTest2() {
	    invokeParser("parser02.jul");
	}
	
	// Compound arithmetic expressions.
	@Test
	public void parserBasicTest3() {
	    invokeParser("parser03.jul");
	}
	
	// Compound comparison expressions; chained assignment.
	@Test
	public void parserBasicTest4() {
	    invokeParser("parser04.jul");
	}
	
	// Function declaration.
	@Test
	public void parserBasicTest5() {
	    invokeParser("parser05.jul");
	}
	
	// Function declaration - additional tests.
	@Test
	public void parserBasicTest5a() {
	    invokeParser("parser05_a.jul");
	}
	
	// Object initialization.
	@Test
	public void parserBasicTest6() {
	    invokeParser("parser06.jul");
	}
	
	@Test
	public void parserLambdaBasicTest_1() {
	    invokeParser("parser_lambda_01.jul");
	}
	
	@Test
	public void parserLambdaBasicTest_2() {
	    invokeParser("parser_lambda_02.jul");
	}

	@Test
	public void parserLambdaBasicTest_3() {
	    invokeParser("parser_lambda_03.jul");
	}

	@Test
	public void parserLambdaBasicTest_4() {
	    invokeParser("parser_lambda_04.jul");
	}
	
	// Single dimension
	@Test
	public void parserArrayBasicTest_1() {
	    invokeParser("parser_array_01.jul");
	}
	
	// Multi dimension
	@Test
	public void parserArrayBasicTest_2() {
	    invokeParser("parser_array_02.jul");
	}
	
	// Single dimension with initializer
	@Test
	public void parserArrayBasicTest_3() {
	    invokeParser("parser_array_03.jul");
	}
	
	// Multi dimension with initializer
	@Test
	public void parserArrayBasicTest_4() {
	    invokeParser("parser_array_04.jul");
	}
	
	@Test
	public void parserAccessBasicTest_1() {
	    invokeParser("parser_access_01.jul");
	}
	
	@Test
	public void parserAccessBasicTest_2() {
	    invokeParser("parser_access_02.jul");
	}
	
	// The most basic form
	@Test
	public void parserForTest_1() {
	    invokeParser("parser_stmt_for_01.jul");
	}
	
	// Double initializers
	@Test
	public void parserForTest_2() {
	    invokeParser("parser_stmt_for_02.jul");
	}
	
	// Special cases
	@Test
	public void parserForTest_3() {
	    invokeParser("parser_stmt_for_03.jul");
	}
	
	// Expression instead of block
	@Test
	public void parserForTest_4() {
	    invokeParser("parser_stmt_for_04.jul");
	}
	
	// If - else if - else with blocks
	@Test
	public void parserIfTest_1() {
	    invokeParser("parser_stmt_if_01.jul");
	}
	
	// If - else if - else with expressions
	@Test
	public void parserIfTest_2() {
	    invokeParser("parser_stmt_if_02.jul");
	}
	
	@Test
	public void parserTryTest_1() {
	    invokeParser("parser_stmt_try_01.jul");
	}
	
	@Test
	public void parserTryTest_2() {
	    invokeParser("parser_stmt_try_02.jul");
	}
	
	@Test
	public void parserTryTest_3() {
	    invokeParser("parser_stmt_try_03.jul");
	}
	
	@Test
	public void parserTryTest_4() {
	    invokeParser("parser_stmt_try_04.jul");
	}
	
}
