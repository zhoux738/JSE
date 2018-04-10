package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import info.jultest.test.Commons;
import info.julang.execution.EngineRuntime;
import info.julang.execution.simple.SimpleEngineRuntime;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.external.EngineInitializationOption;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.modulesystem.ModuleInfo;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JInterfaceType;

import org.junit.Test;

public class InterfaceImplTests extends ClassTestBase {

	private static final String FEATURE = "InterfaceDef";
	
	// Declare a class which implements interface(s)
	@Test
	public void basicInterfaceImplTest1() throws EngineInvocationError {		
		EngineRuntime rt = SimpleEngineRuntime.createDefault();
		SimpleScriptEngine engine = new SimpleScriptEngine(rt, new EngineInitializationOption());
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "impl_01.jul"));
		
		JInterfaceType itype = assertInterfaceTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".IPerson1");
		
		itype = assertInterfaceTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".IPerson2");
		validateTypeDefinition(
		itype,createMembers(false, new MemberInfo[]{
			MemberInfo.createMethod("speak").isAbstract(true).isAccessible(Accessibility.PUBLIC),
		}));
		
		JClassType ctype = assertClassTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".Person1");
		validateTypeDefinition(
		ctype,createMembers(true, new MemberInfo[]{
			MemberInfo.createMethod("speak").isAbstract(false).isAccessible(Accessibility.PUBLIC),
		}));
		
		ctype = assertClassTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".Person2");
		validateTypeDefinition(
		ctype,createMembers(true, new MemberInfo[]{
			MemberInfo.createMethod("speak").isAbstract(false).isAccessible(Accessibility.PUBLIC),
		}));
		
		ctype = assertClassTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".Person3");
		validateTypeDefinition(
		ctype,createMembers(true, new MemberInfo[]{
			MemberInfo.createMethod("speak1").isAbstract(false).isAccessible(Accessibility.PUBLIC),
			MemberInfo.createMethod("speak2").isAbstract(false).isAccessible(Accessibility.PUBLIC),
			MemberInfo.createMethod("speak3").isAbstract(false).isAccessible(Accessibility.PUBLIC),
		}));
	}
	
	@Test
	public void basicInterfaceImplTest2() throws EngineInvocationError {		
		EngineRuntime rt = SimpleEngineRuntime.createDefault();
		SimpleScriptEngine engine = new SimpleScriptEngine(rt, new EngineInitializationOption());
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "impl_02.jul"));
		
		JClassType ctype = assertClassTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".Employee");
		validateTypeDefinition(
		ctype,createMembers(true, new MemberInfo[]{
			MemberInfo.createMethod("speak").isAbstract(false).isAccessible(Accessibility.PUBLIC),
			MemberInfo.createMethod("work").isAbstract(false).isAccessible(Accessibility.PUBLIC),
		}));
	}
	
	@Test
	public void basicInterfaceImplTest3() throws EngineInvocationError {		
		EngineRuntime rt = SimpleEngineRuntime.createDefault();
		SimpleScriptEngine engine = new SimpleScriptEngine(rt, new EngineInitializationOption());
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "impl_03.jul"));
		
		JClassType ctype = assertClassTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".Employee");
		validateTypeDefinition(
		ctype,createMembers(true, new MemberInfo[]{
			MemberInfo.createMethod("speak").isAbstract(false).isAccessible(Accessibility.PUBLIC),
		}));
	}
	
	@Test
	public void basicInterfaceImplTest4() throws EngineInvocationError {		
		EngineRuntime rt = SimpleEngineRuntime.createDefault();
		SimpleScriptEngine engine = new SimpleScriptEngine(rt, new EngineInitializationOption());
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "impl_04.jul"));
		
		JClassType ctype = assertClassTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".Employee");
		validateTypeDefinition(
		ctype,createMembers(true, new MemberInfo[]{
			MemberInfo.createMethod("speak").isAbstract(false).isAccessible(Accessibility.PUBLIC),
			MemberInfo.createMethod("work").isAbstract(false).isAccessible(Accessibility.PUBLIC),
		}));
	}
	
	/*
	@Test
	public void basicInterfaceImplTest5() throws EngineInvocationError {		
		EngineRuntime rt = SimpleEngineRuntime.createDefault();
		SimpleScriptEngine engine = new SimpleScriptEngine(rt, false, true);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "impl_05.jul"));
		
		JClassType ctype = assertClassTypeDefined(rt, "Test.Person");
		validateTypeDefinition(
		ctype,createMembers(true, new MemberInfo[]{
			MemberInfo.createMethod("speak").isAbstract(false).isAccessible(Accessibility.PUBLIC),
		}));
	}
	*/
}
