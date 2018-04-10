package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import info.jultest.test.Commons;
import info.julang.execution.EngineRuntime;
import info.julang.execution.simple.SimpleEngineRuntime;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.external.EngineInitializationOption;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.modulesystem.ModuleInfo;
import info.julang.typesystem.VoidType;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.JInterfaceType;

import org.junit.Test;

public class InterfaceDefTests extends ClassTestBase {

	private static final String FEATURE = "InterfaceDef";
	
	// Declare an interface
	@Test
	public void basicInterfaceDefTest1() throws EngineInvocationError {		
		EngineRuntime rt = SimpleEngineRuntime.createDefault();
		SimpleScriptEngine engine = new SimpleScriptEngine(rt, new EngineInitializationOption());
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "decl_01.jul"));
		
		JInterfaceType ctype = assertInterfaceTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".IPerson1");
		
		ctype = assertInterfaceTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".IPerson2");
		validateTypeDefinition(
		ctype,createMembers(false, new MemberInfo[]{
			MemberInfo.createMethod("speak").isAbstract(true).isAccessible(Accessibility.PUBLIC),
		}));	
		
		ctype = assertInterfaceTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".IPerson3");
		validateTypeDefinition(
		ctype,createMembers(false, new MemberInfo[]{
			MemberInfo.createMethod("speak1").isAbstract(true).isAccessible(Accessibility.PUBLIC).returns(VoidType.getInstance()),
			MemberInfo.createMethod("speak2").isAbstract(true).isAccessible(Accessibility.PUBLIC).returns(VoidType.getInstance()),
			MemberInfo.createMethod("speak3").isAbstract(true).isAccessible(Accessibility.PUBLIC).returns(VoidType.getInstance()),
		}));
	}
	
	// Declare an interface extending another interface
	@Test
	public void basicInterfaceDefTest2() throws EngineInvocationError {		
		EngineRuntime rt = SimpleEngineRuntime.createDefault();
		SimpleScriptEngine engine = new SimpleScriptEngine(rt, new EngineInitializationOption());
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "decl_02.jul"));
		
		JInterfaceType ctype = assertInterfaceTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".IPerson");
		validateTypeDefinition(
		ctype,createMembers(false, new MemberInfo[]{
			MemberInfo.createMethod("speak").isAbstract(true).isAccessible(Accessibility.PUBLIC),
		}));
		ctype = assertInterfaceTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".IEmployee");		
		validateTypeDefinition(
		ctype,createMembers(false, new MemberInfo[]{
			MemberInfo.createMethod("speak").isAbstract(true).isAccessible(Accessibility.PUBLIC).returns(VoidType.getInstance()),
			MemberInfo.createMethod("work").isAbstract(true).isAccessible(Accessibility.PUBLIC).returns(VoidType.getInstance()),
		}));
	}
	
	// Declare an interface extending other interfaces
	@Test
	public void basicInterfaceDefTest3() throws EngineInvocationError {		
		EngineRuntime rt = SimpleEngineRuntime.createDefault();
		SimpleScriptEngine engine = new SimpleScriptEngine(rt, new EngineInitializationOption());
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "decl_03.jul"));
		
		JInterfaceType ctype = assertInterfaceTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".IAmphibious");
		validateTypeDefinition(
		ctype,createMembers(false, new MemberInfo[]{
			MemberInfo.createMethod("dive").isAbstract(true).isAccessible(Accessibility.PUBLIC).returns(VoidType.getInstance()),
			MemberInfo.createMethod("swim").isAbstract(true).isAccessible(Accessibility.PUBLIC).returns(VoidType.getInstance()),
			MemberInfo.createMethod("walk").isAbstract(true).isAccessible(Accessibility.PUBLIC).returns(VoidType.getInstance()),
		}));
	}
	
	// Declare an interface extending another which itself from a third one  
	@Test
	public void basicInterfaceDefTest4() throws EngineInvocationError {		
		EngineRuntime rt = SimpleEngineRuntime.createDefault();
		SimpleScriptEngine engine = new SimpleScriptEngine(rt, new EngineInitializationOption());
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "decl_04.jul"));
		
		JInterfaceType ctype = assertInterfaceTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".I");
		validateTypeDefinition(
		ctype,createMembers(false, new MemberInfo[]{
			MemberInfo.createMethod("g").isAbstract(true).isAccessible(Accessibility.PUBLIC).returns(VoidType.getInstance()),
			MemberInfo.createMethod("i").isAbstract(true).isAccessible(Accessibility.PUBLIC).returns(VoidType.getInstance()),
			MemberInfo.createMethod("p").isAbstract(true).isAccessible(Accessibility.PUBLIC).returns(VoidType.getInstance()),
		}));
	}
	
	// A combination of basicInterfaceDefTest #3 and #4
	//
	// IA1     IB1     IC1
	//  |       |       |
	// IA2     IB2      |
	//  |       |       |
	//  |       +---+---+
	//  |           |
	//  |          IBC1        ID1
	//  |           |           |
	//  +-----------+-----------+
	//              |
	//            IABCD1
	@Test
	public void basicInterfaceDefTest5() throws EngineInvocationError {		
		EngineRuntime rt = SimpleEngineRuntime.createDefault();
		SimpleScriptEngine engine = new SimpleScriptEngine(rt, new EngineInitializationOption());
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "decl_05.jul"));
		
		JInterfaceType ctype = assertInterfaceTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".IABCD1");
		validateTypeDefinition(
		ctype,createMembers(false, new MemberInfo[]{
			MemberInfo.createMethod("a1"),
			MemberInfo.createMethod("a2"),
			MemberInfo.createMethod("abcd1"),
			MemberInfo.createMethod("b1"),
			MemberInfo.createMethod("b2"),
			MemberInfo.createMethod("bc1"),
			MemberInfo.createMethod("c1"),
			MemberInfo.createMethod("d1"),
		}));
	}
	
	// Extending from same interface via different chains
	@Test
	public void advInterfaceDefTest1() throws EngineInvocationError {		
		EngineRuntime rt = SimpleEngineRuntime.createDefault();
		SimpleScriptEngine engine = new SimpleScriptEngine(rt, new EngineInitializationOption());
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "decl_adv_01.jul"));
		
		JInterfaceType ctype = assertInterfaceTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".I");
		validateTypeDefinition(
		ctype,createMembers(false, new MemberInfo[]{
			MemberInfo.createMethod("g").isAbstract(true).isAccessible(Accessibility.PUBLIC).returns(VoidType.getInstance()),
			MemberInfo.createMethod("i").isAbstract(true).isAccessible(Accessibility.PUBLIC).returns(VoidType.getInstance()),
			MemberInfo.createMethod("p").isAbstract(true).isAccessible(Accessibility.PUBLIC).returns(VoidType.getInstance()),
		}));
	}
	
	// Implementing two interfaces sharing the same method.
	@Test
	public void advInterfaceDefTest2() throws EngineInvocationError {		
		EngineRuntime rt = SimpleEngineRuntime.createDefault();
		SimpleScriptEngine engine = new SimpleScriptEngine(rt, new EngineInitializationOption());
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "decl_adv_02.jul"));
		
		JInterfaceType ctype = assertInterfaceTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".Warden");
		validateTypeDefinition(
		ctype,createMembers(true, new MemberInfo[]{
			MemberInfo.createMethod("execute").isAbstract(false).isAccessible(Accessibility.PUBLIC).returns(VoidType.getInstance()),
		}));
	}
}
