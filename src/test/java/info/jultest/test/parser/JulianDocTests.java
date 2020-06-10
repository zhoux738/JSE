package info.jultest.test.parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.junit.Test;

import info.julang.langspec.ast.JulianLexer;
import info.julang.langspec.ast.JulianParser.Class_definitionContext;
import info.julang.langspec.ast.JulianParser.DeclarationsContext;
import info.julang.langspec.ast.JulianParser.Interface_definitionContext;
import info.julang.langspec.ast.JulianParser.Type_declarationContext;
import info.julang.parser.ANTLRParser;
import info.jultest.test.Commons;
import junit.framework.Assert;

public class JulianDocTests {
	
	private static final String FEATURE = "Documentation";

	@Test
	public void documentationTest1() throws FileNotFoundException {
		ANTLRParser parser = invokeParser("doc_1.jul");
		DeclarationsContext decls = parser.getAstInfo().getAST().declarations();
		
		Type_declarationContext decl = decls.type_declaration(0);
		assertDoc(parser, decl, "This is documentation for C1");
		Class_definitionContext cdecl = decl.class_definition();
		assertDoc(parser, cdecl.class_body().class_member_declaration(0), "This is the ctor");
		assertDoc(parser, cdecl.class_body().class_member_declaration(1), "This is a method");
		assertDoc(parser, cdecl.class_body().class_member_declaration(2), "[DOC] C1: a field");
	}
	
	@Test
	public void documentationTest2() throws FileNotFoundException {
		ANTLRParser parser = invokeParser("doc_2.jul");
		DeclarationsContext decls = parser.getAstInfo().getAST().declarations();
		
		Type_declarationContext decl = decls.type_declaration(0);
		assertDoc(parser, decl, null);
		Class_definitionContext cdecl = decl.class_definition();
		assertDoc(parser, cdecl.class_body().class_member_declaration(0), "comments not physically aside");
		assertDoc(parser, cdecl.class_body().class_member_declaration(1), "comments separated by //");
		assertDoc(parser, cdecl.class_body().class_member_declaration(2), "only care the immediately preceding block");
		assertDoc(parser, cdecl.class_body().class_member_declaration(3), null);
		
		decl = decls.type_declaration(1);
		assertDoc(parser, decl, "interface type doc");
		Interface_definitionContext idecl = decl.interface_definition();
		assertDoc(parser, idecl.interface_body().interface_member_declaration(0), "interface method doc");
	}
	
	@Test
	public void allTokensTest() {
		String text = "int /*type*/ a = 5;";
		ANTLRParser parser = ANTLRParser.createMemoryParser(text);
		List<Token> tokens = parser.getAllTokens();

		Assert.assertEquals(11, tokens.size()); // Including EOF
		Assert.assertEquals(JulianLexer.SKIPPED, tokens.get(1).getChannel());
		Assert.assertEquals(JulianLexer.JULDOC, tokens.get(2).getChannel());
	}
	
	public ANTLRParser invokeParser(String fileName) throws FileNotFoundException {
    	fileName = Commons.PARSING_ROOT + FEATURE + "/" + fileName;
    	FileInputStream fis = new FileInputStream(fileName);
		ANTLRParser parser = new ANTLRParser(fileName, fis, false);
		parser.parse(true, true);
		
		return parser;
	}
	
	private void assertDoc(ANTLRParser parser, ParserRuleContext node, String contained){
		String doc = parser.getDoc(node.start);
		if (contained != null) {
			Assert.assertNotNull(doc);
			assertDocContents(contained, doc);
		} else {
			Assert.assertNull(doc);
		}
	}

	private void assertDocContents(String expected, String doc) {
		Assert.assertTrue(doc.startsWith("/*"));
		Assert.assertTrue(doc.contains(expected));
		Assert.assertTrue(doc.endsWith("*/"));
	}
	
}
