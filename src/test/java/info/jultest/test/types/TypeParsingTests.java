package info.jultest.test.types;

import org.junit.Ignore;

@Ignore
public class TypeParsingTests {
	
	/*
	@Test
	public void readFullTypeTest1() throws Exception {
		TokenStream stream = new StringSourceTokenStream("A.B.C[][] x = ...");
		ParsedTypeName tname = SyntaxHelper.parseTypeName(stream, true);
		
		String name = tname.getFQName().toString();
		assertEquals("A.B.C", name);
		
		assertEquals(2, tname.getDimensionNumber());
		
		Token tok = stream.peek();
		assertEquals("x", tok.getLiteral());
	}
	
	@Test
	public void readFullTypeTest2() throws Exception {
		TokenStream stream = new StringSourceTokenStream("A x = ...");
		ParsedTypeName tname = SyntaxHelper.parseTypeName(stream, true);
		
		String name = tname.getFQName().toString();
		assertEquals("A", name);
		
		assertEquals(0, tname.getDimensionNumber());
		
		Token tok = stream.peek();
		assertEquals("x", tok.getLiteral());
	}
	
	@Test
	public void readFullTypeTest3() throws Exception {
		TokenStream stream = new StringSourceTokenStream("A.B.C(5);");
		ParsedTypeName tname = SyntaxHelper.parseTypeName(stream, true);
		
		assertEquals(null, tname);
		
		Token tok = stream.peek();
		assertEquals("A", tok.getLiteral());
	}
	*/
	
}
