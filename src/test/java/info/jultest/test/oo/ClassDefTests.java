package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;

import org.junit.Assert;
import org.junit.Test;

import info.jultest.test.Commons;
import info.julang.execution.EngineRuntime;
import info.julang.execution.simple.SimpleEngineRuntime;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.EngineInitializationOption;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.modulesystem.ModuleInfo;
import info.julang.typesystem.jclass.JClassConstructorMember;
import info.julang.typesystem.jclass.JClassMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JParameter;

public class ClassDefTests extends ClassTestBase {

	private static final String FEATURE = "ClassDef";
	
	/*
	 	int age;
	
		string name;
		
		string getName(){
			return name;
		}
	
		int getAge(){
			return age;
		}
		
		void printInfo(){
			// ...
		}
	 */
	@Test
	public void basicClassDefTest() throws EngineInvocationError {		
		EngineRuntime rt = SimpleEngineRuntime.createDefault();
		SimpleScriptEngine engine = new SimpleScriptEngine(rt, new EngineInitializationOption());
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "person_1.jul"));
		
		JClassType ctype = assertClassTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".Person");
		
		validateTypeDefinition(
			ctype,createMembers(true, new MemberInfo[]{
				MemberInfo.createField("age"),
				MemberInfo.createMethod("getAge"),
				MemberInfo.createMethod("getName"),
				MemberInfo.createField("name"),
				MemberInfo.createMethod("printInfo")
			}));
	}
	
	@Test
	public void basicClassDefTest2() throws EngineInvocationError {		
		EngineRuntime rt = SimpleEngineRuntime.createDefault();
		SimpleScriptEngine engine = new SimpleScriptEngine(rt, new EngineInitializationOption());
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "person_5.jul"));
		
		JClassType ctype = assertClassTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".Person");
		
		validateTypeDefinition(
			ctype,createMembers(true, new MemberInfo[]{
				MemberInfo.createMethod("getName"),
				MemberInfo.createField("name")
			}));
		
		JClassMember[] ctors = ctype.getClassConstructors();
		Assert.assertEquals(1, ctors.length);
		JParameter[] params = ((JClassConstructorMember)ctors[0]).getCtorType().getParams();
		Assert.assertEquals(2, params.length);
		Assert.assertEquals("name", params[1].getName());
	}
	
	// A -> A
	@Test
	public void selfRefClassDefTest() throws EngineInvocationError {		
		EngineRuntime rt = SimpleEngineRuntime.createDefault();
		SimpleScriptEngine engine = new SimpleScriptEngine(rt, new EngineInitializationOption());
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "person_2.jul"));
		
		JClassType ctype = assertClassTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".Person");
		
		validateTypeDefinition(
			ctype,createMembers(true, new MemberInfo[]{
				MemberInfo.createField("father").is(ctype), // self reference
				MemberInfo.createMethod("getName")
			}));
	}
	
	// A -> B
	@Test
	public void basicRefDependencyTest() throws EngineInvocationError {		
		EngineRuntime rt = SimpleEngineRuntime.createDefault();
		SimpleScriptEngine engine = new SimpleScriptEngine(rt, new EngineInitializationOption());
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "person_3.jul"));
		
		JClassType ctype = assertClassTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".Family");
		JClassType ctype2 = assertClassTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".Person");
		
		validateTypeDefinition(
			ctype,createMembers(true, new MemberInfo[]{
				MemberInfo.createField("father").is(ctype2),
				MemberInfo.createField("mother").is(ctype2)
			}));
	}
	
	// A -> B -> A
	@Test
	public void circularRefDependencyTest() throws EngineInvocationError {		
		EngineRuntime rt = SimpleEngineRuntime.createDefault();
		SimpleScriptEngine engine = new SimpleScriptEngine(rt, new EngineInitializationOption());
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "person_4.jul"));
		
		JClassType ctype = assertClassTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".Family");
		JClassType ctype2 = assertClassTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".Person");
		
		validateTypeDefinition(
				ctype,createMembers(true, new MemberInfo[]{
					MemberInfo.createField("father").is(ctype2),
					MemberInfo.createField("mother").is(ctype2)
				}));
		
		validateTypeDefinition(
			ctype2,createMembers(true, new MemberInfo[]{
				MemberInfo.createField("family").is(ctype)
			}));
	}
	
	@Test
	public void initializeInstanceFieldTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "person_6.jul"));
		
		//	int age = p.age;
		validateIntValue(gvt, "age", 32);
		
		//	string account = name + "_acc";
		validateStringValue(gvt, "account", "Patrick_acc");
	}
	
	@Test
	public void arrayInstanceFieldTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "person_7.jul"));
		
		//	string h0 = p.hobbies[0];
		validateStringValue(gvt, "h0", "abc");
		validateStringValue(gvt, "h2", "xyz");
	}
	
	@Test
	public void memberSelfReferenceTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "person_8.jul"));

		validateStringValue(gvt, "s", "Nick");
	}
	
	
	@Test
	public void parentCreatesChildTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "item_7.jul"));

		validateStringValue(gvt, "s", "a");
		validateIntValue(gvt, "i", 5);
		validateIntValue(gvt, "j", 5);
	}
	
	//Uncomment this to test
	//@Test
	public void devTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "devtest.jul"));
	}
	
	@Test
	public void staticInitTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "static_init_1.jul"));

		validateIntValue(gvt, "x1", 100);
		validateIntValue(gvt, "x2", 200);
	}
	
	@Test
	public void staticInitWithDepsTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "static_init_2.jul"));

		validateStringValue(gvt, "v4", "A4");
		validateStringValue(gvt, "v3", "A4A3");
		validateStringValue(gvt, "v2", "A4A3A2");
		validateStringValue(gvt, "v1", "A4A3A2A1");
	}
	
	@Test
	public void staticAnInstMemberOfSameNameCoexistTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "sta_inst_coexist_1.jul"));

		validateIntValue(gvt, "a", 3);
		validateIntValue(gvt, "b", 5);
	}
	
	@Test
	public void staticAnInstMemberOfSameNameCoexistTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "sta_inst_coexist_2.jul"));

		validateIntValue(gvt, "a", 3);
		validateIntValue(gvt, "b", 3);
	}
	
	@Test
	public void staticConstructorBasicTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "static_ctor_1.jul"));

		validateIntValue(gvt, "v1", 100);
		validateIntValue(gvt, "v2", 200);
	}

	@Test
	public void staticConstructorAlwaysCalledAfterInitializersTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "static_ctor_2.jul"));

		validateIntValue(gvt, "v1", 100);
		validateIntValue(gvt, "v2", 200);
	}
	
	@Test
	public void staticConstructorCoexistsWithInstCtorTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "static_ctor_3.jul"));

		validateIntValue(gvt, "v1s", 100);
		validateIntValue(gvt, "v2s", 200);
		validateIntValue(gvt, "v2i", 200);
	}
	
	@Test
	public void staticMemberOnHierarchyTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "static_on_hierarchy.jul"));

		validateIntValue(gvt, "i7", 7);
		validateIntValue(gvt, "i11", 11);
	}
}
