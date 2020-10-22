package info.jultest.test.parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.antlr.v4.runtime.Token;
import org.junit.Test;

import info.julang.langspec.ast.JulianLexer;
import info.julang.parser.ANTLRParser;
import info.julang.parser.Directives;
import info.jultest.test.Commons;
import org.junit.Assert;

public class JulianDirectiveTests {
	
	private static final String FEATURE = "Directive";

	// base case: single import at the beginning
	@Test
	public void directiveTest1() throws FileNotFoundException {
		ANTLRParser parser = getParser("pragma_1.jul");
		Directives dirs = parser.getDirectives();
		Assert.assertTrue(dirs.contains("info.julang.test.directive1"));
		Assert.assertEquals(1, dirs.getAll().length);
	}
	
	// empty file
	@Test
	public void directiveTest2() throws FileNotFoundException {
		ANTLRParser parser = getParser("pragma_2.jul");
		Directives dirs = parser.getDirectives();
		Assert.assertTrue(dirs.contains("info.julang.test.directive1"));
		Assert.assertEquals(1, dirs.getAll().length);
	}
	
	// 2 directives
	@Test
	public void directiveTest3() throws FileNotFoundException {
		ANTLRParser parser = getParser("pragma_3.jul");
		Directives dirs = parser.getDirectives();
		Assert.assertTrue(dirs.contains("info.julang.test.directive1"));
		Assert.assertTrue(dirs.contains("info.julang.test.directive2"));
		Assert.assertEquals(2, dirs.getAll().length);
	}
	
	// 2 directives
	@Test
	public void directiveTest4() throws FileNotFoundException {
		ANTLRParser parser = getParser("pragma_4.jul");
		Directives dirs = parser.getDirectives();
		Assert.assertTrue(dirs.contains("info.julang.test.directive1"));
		Assert.assertTrue(dirs.contains("info.julang.test.directive2"));
		Assert.assertEquals(2, dirs.getAll().length);
	}
	
	// do not recognize pragmas appearing elsewhere
	@Test
	public void directiveTest5() throws FileNotFoundException {
		ANTLRParser parser = getParser("pragma_5.jul");
		Directives dirs = parser.getDirectives();
		Assert.assertTrue(dirs.contains("info.julang.test.directive1"));
		Assert.assertEquals(1, dirs.getAll().length);
	}
	
	// do not recognize pragmas appearing elsewhere
	@Test
	public void directiveTest6() throws FileNotFoundException {
		ANTLRParser parser = getParser("pragma_6.jul");
	}
	
	// do not recognize pragmas spanning over lines
	@Test
	public void directiveTest7() throws FileNotFoundException {
		ANTLRParser parser = getParser("pragma_7.jul");
		Directives dirs = parser.getDirectives();
		Assert.assertTrue(dirs.contains("info.julang.test.directive1"));
		Assert.assertEquals(1, dirs.getAll().length);
	}
	
	// If processing is turned off, no directive should be given no matter what
	@Test
	public void noDirectiveTest() throws FileNotFoundException {
		for (int i = 1; i <= 7; i++) {
	    	String fileName = Commons.PARSING_ROOT + FEATURE + "/pragma_" + i + ".jul";
	    	FileInputStream fis = new FileInputStream(fileName);
			ANTLRParser parser = new ANTLRParser(fileName, fis, false);
			
			parser.parse(false, true);
			Directives dirs = parser.getDirectives();
			Assert.assertEquals(0, dirs.getAll().length);
		}
	}
	
	// do not recognize pragmas spanning over lines
	@Test
	public void directiveIsNotDocTest() throws FileNotFoundException {
		ANTLRParser parser = getParser("pragma_8.jul");
		Token tok1 = null, tok2 = null;
		for (Token tok : parser.getAllTokens()) {
			if (tok.getType() == JulianLexer.CLASS) {
				if (tok1 == null) {
					tok1 = tok;
				} else {
					tok2 = tok;
					break;
				}
			}
		}
		
		String doc1 = parser.getDoc(tok1);
		Assert.assertNull("First class declaration must not have doc.", doc1);
		String doc2 = parser.getDoc(tok2);
		Assert.assertNotNull("Second class declaration must have doc.", doc2);
	}
	
	public ANTLRParser getParser(String fileName) throws FileNotFoundException {
    	fileName = Commons.PARSING_ROOT + FEATURE + "/" + fileName;
    	FileInputStream fis = new FileInputStream(fileName);
		ANTLRParser parser = new ANTLRParser(fileName, fis, false);
		parser.setProcessDirectives(true);
		return parser;
	}
}
