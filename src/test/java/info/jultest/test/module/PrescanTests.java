package info.jultest.test.module;

import info.julang.interpretation.syntax.ClassDeclInfo;
import info.julang.interpretation.syntax.ClassSubtype;
import info.julang.modulesystem.RequirementInfo;
import info.julang.modulesystem.prescanning.CollectScriptInfoStatement;
import info.julang.modulesystem.prescanning.RawClassInfo;
import info.julang.modulesystem.prescanning.RawScriptInfo;
import info.jultest.test.Commons;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PrescanTests {

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(
			new Object[][]{
				{ true }, // fully loading
				{ false } // lazily loading
			});
	}
	
	private boolean fullyLoadNow;
	
	public PrescanTests(boolean fullyLoadNow){
		this.fullyLoadNow = fullyLoadNow;
	}
	
	/**
	 * module a.b.c;
	 * 
	 * class C { .. }
	 */
	@Test
	public void moduleClassDefTest() throws IOException {
		RawScriptInfo info = runPrescan("mod_a.jul", fullyLoadNow);
		
		List<RawClassInfo> classes = info.getClasses();
		
		org.junit.Assert.assertEquals(1, classes.size());
		org.junit.Assert.assertEquals("MyClass", classes.get(0).getName());
	}
	
	/**
	 * module a.b.c;
	 * 
	 * attribute A { .. }
	 */
	@Test
	public void moduleAttrDefTest() throws IOException {
		RawScriptInfo info = runPrescan("mod_attr_1.jul", fullyLoadNow);
		
		List<RawClassInfo> classes = info.getClasses();
		
		org.junit.Assert.assertEquals(1, classes.size());
		RawClassInfo raw = classes.get(0);
		ClassDeclInfo decl = raw.getDeclInfo();
		org.junit.Assert.assertTrue(raw.getName().equals(decl.getName()));
		org.junit.Assert.assertEquals("MyAttr", raw.getName());
		org.junit.Assert.assertEquals(ClassSubtype.ATTRIBUTE, decl.getSubtype());
	}
	
	/**
	 * module a.b.c;
	 * 
	 * attribute A1 { .. }
	 * class C { .. }
	 * attribute A2 { .. }
	 */
	@Test
	public void moduleAttrClassAttrDefTest() throws IOException {
		RawScriptInfo info = runPrescan("mod_attr_2.jul", fullyLoadNow);
		
		List<RawClassInfo> classes = info.getClasses();
		
		org.junit.Assert.assertEquals(3, classes.size());
		boolean[] checks = new boolean[]{false, false, false};
		for(RawClassInfo cinfo : classes){
			ClassSubtype stype = cinfo.getDeclInfo().getSubtype();
			if(cinfo.getName().equals("MyAttr1") && stype == ClassSubtype.ATTRIBUTE){
				checks[0] = true;
			} else if(cinfo.getName().equals("MyClass1") && stype == ClassSubtype.CLASS){
				checks[1] = true;
			} else if(cinfo.getName().equals("MyAttr2") && stype == ClassSubtype.ATTRIBUTE){
				checks[2] = true;
			}
		}
		org.junit.Assert.assertTrue(checks[0] && checks[1] && checks[2]);
	}
	
	/*
	 * module a.b.c;
	 * 
	 * [A1()]
	 * [A2()]
	 * class C { .. }
	 * @throws TokenStreamRelocationException 
	 *
	public void moduleAnnotatedClassDefTest() throws TokenStreamRelocationException {
		RawScriptInfo info = runPrescan("mod_aa.jul");
		
		List<RawClassInfo> classes = info.getClasses();
		
		org.junit.Assert.assertEquals(1, classes.size());
		RawClassInfo raw = classes.get(0);
		ClassDeclInfo decl = raw.getDeclInfo();
		org.junit.Assert.assertTrue(raw.getName().equals(decl.getName()));
		org.junit.Assert.assertEquals("MyClass", raw.getName());
		org.junit.Assert.assertEquals(ClassSubtype.CLASS, decl.getSubtype());
		
		List<AttributeDeclInfo> list = decl.getAttributes();
		org.junit.Assert.assertEquals(2, list.size());
		
		TokenStream ts = info.getStream();

		String a1 = validateAttrDecl(list.get(0), ts);
		String a2 = validateAttrDecl(list.get(1), ts);
		
		String[] names = new String[]{"MyAttr1", "MyAttr2"};
		boolean valid = names[0].equals(a1) && names[1].equals(a2) || 
						names[1].equals(a1) && names[0].equals(a2);
		org.junit.Assert.assertTrue(valid);
	}
	
	private String validateAttrDecl(AttributeDeclInfo adinfo, TokenStream ts) throws TokenStreamRelocationException {
		ts.set(adinfo.getStart());
		org.junit.Assert.assertTrue(KnownTokens.LEFT_SQUARE_T == ts.next());
		Token tok = ts.next();
		org.junit.Assert.assertEquals(TokenKind.IDENTIFIER, tok.getKind());
		ts.set(adinfo.getEnd());
		ts.backoff();
		org.junit.Assert.assertTrue(KnownTokens.RIGHT_SQUARE_T == ts.next());
		return tok.getLiteral();
	}
	*/
	
	/**
	 * enum E { .. }
	 */
	@Test
	public void moduleEnumDefTest() throws IOException {
		RawScriptInfo info = runPrescan("mod_e.jul", fullyLoadNow);
		
		List<RawClassInfo> classes = info.getClasses();
		
		org.junit.Assert.assertEquals(1, classes.size());
		org.junit.Assert.assertEquals("MyEnum", classes.get(0).getName());
	}
	
	/**
	 * enum E { .. }
	 * class C { .. }
	 */
	@Test
	public void moduleEnumClassDefTest() throws IOException {
		RawScriptInfo info = runPrescan("mod_ec.jul", fullyLoadNow);
		
		List<RawClassInfo> classes = info.getClasses();
		
		org.junit.Assert.assertEquals(2, classes.size());
		
		boolean[] checks = new boolean[]{false, false};
		for(RawClassInfo cinfo : classes){
			ClassSubtype stype = cinfo.getDeclInfo().getSubtype();
			if(cinfo.getName().equals("MyEnum") && stype == ClassSubtype.ENUM){
				checks[0] = true;
			} else if(cinfo.getName().equals("MyClass") && stype == ClassSubtype.CLASS){
				checks[1] = true;
			}
		}
		org.junit.Assert.assertTrue(checks[0] && checks[1]);
	}
	
	/**
	 * module a.b.c;
	 * 
	 * import ...;
	 * 
	 * class { ... }
	 */
	@Test
	public void moduleImportClassDefTest() throws IOException {
		RawScriptInfo info = runPrescan("mod_b.jul", fullyLoadNow);
		
		List<RequirementInfo> reqs = info.getRequirements();
		
		org.junit.Assert.assertEquals(4, reqs.size());
		org.junit.Assert.assertEquals("System", reqs.get(0).getName());
		org.junit.Assert.assertEquals("first.dep", reqs.get(1).getName());
		org.junit.Assert.assertEquals("second.dep", reqs.get(2).getName());
		org.junit.Assert.assertEquals("third.dep", reqs.get(3).getName());
		
		List<RawClassInfo> classes = info.getClasses();
		
		org.junit.Assert.assertEquals(2, classes.size());
		org.junit.Assert.assertEquals("MyClass1", classes.get(0).getName());
		org.junit.Assert.assertEquals("MyClass2", classes.get(1).getName());
	}
	
	/**
	 * module a.b.c;
	 * 
	 * import ...;
	 * import ... as ...;
	 * 
	 */
	@Test
	public void moduleImportAsTest() throws IOException {
		RawScriptInfo info = runPrescan("mod_c.jul", fullyLoadNow);
		
		List<RequirementInfo> reqs = info.getRequirements();
		
		org.junit.Assert.assertEquals(4, reqs.size());
		org.junit.Assert.assertEquals("System", reqs.get(0).getName());
		org.junit.Assert.assertEquals("first.dep", reqs.get(1).getName());
		org.junit.Assert.assertEquals("second.dep", reqs.get(2).getName());
		org.junit.Assert.assertEquals("sd", reqs.get(2).getAlias());
		org.junit.Assert.assertEquals("third.dep", reqs.get(3).getName());
	}
	
	private static RawScriptInfo runPrescan(String fileName, boolean fullyLoadNow) throws IOException {
		RawScriptInfo info = new RawScriptInfo("ModuleSys.ModA", false);
		info.initialize(Commons.SRC_REPO_ROOT + "ModuleSys/ModA/" + fileName);
		
		CollectScriptInfoStatement csis = new CollectScriptInfoStatement(fullyLoadNow);
		csis.prescan(info);
		
		return info;
	}

}
