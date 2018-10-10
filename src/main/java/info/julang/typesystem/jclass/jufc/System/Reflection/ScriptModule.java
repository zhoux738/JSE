/*
MIT License

Copyright (c) 2017 Ming Zhou

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package info.julang.typesystem.jclass.jufc.System.Reflection;

import info.julang.execution.Argument;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.execution.threading.ThreadRuntimeHelper;
import info.julang.execution.threading.ThreadRuntimeHelper.IObjectPopulater;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.CtorNativeExecutor;
import info.julang.hosting.execution.InstanceNativeExecutor;
import info.julang.hosting.execution.StaticNativeExecutor;
import info.julang.interpretation.syntax.ClassDeclInfo;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.modulesystem.ClassInfo;
import info.julang.modulesystem.IModuleManager;
import info.julang.modulesystem.ModuleInfo;
import info.julang.modulesystem.ScriptInfo;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.JClassConstructorMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.jufc.FoundationModulesInfo;

import java.util.ArrayList;
import java.util.List;

public class ScriptModule {

    public static final String FQCLASSNAME = "System.Reflection.Module";
    
    //----------------- IRegisteredMethodProvider -----------------//
    
    public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FQCLASSNAME){

        @Override
        protected void implementProvider(SimpleHostedMethodProvider provider) {
            provider
                .add("ctor", new InitExecutor())
                .add("find", new FindExecutor())
                .add("getName", new GetNameExecutor())
                .add("getTypes", new GetTypesExecutor())
            	.add("getScripts", new GetScriptsExecutor());
        }
        
    };
    
    private ModuleInfo module;

    public void setModule(ModuleInfo moduleInfo) {
        this.module = moduleInfo;
    }
    
    private static class InitExecutor extends CtorNativeExecutor<ScriptModule> {

        @Override
        protected void initialize(ThreadRuntime rt, HostedValue hvalue, ScriptModule sm, Argument[] args) throws Exception {
            // NO-OP
        }
        
    }
    
    private static class FindExecutor extends StaticNativeExecutor<ScriptModule> {
        
        @Override
        protected JValue apply(ThreadRuntime rt, Argument[] args) throws Exception {
            String name = getString(args, 0);
            JValue res = find(rt, name);
            return res;
        }
        
    }
    
    private static class GetNameExecutor extends InstanceNativeExecutor<ScriptModule> {
        
        @Override
        protected JValue apply(ThreadRuntime rt, ScriptModule sm, Argument[] args) throws Exception {
            String name = sm.getName(rt);
            return TempValueFactory.createTempStringValue(name);
        }
        
    }
    
    private static class GetTypesExecutor extends InstanceNativeExecutor<ScriptModule> {
        
        @Override
        protected JValue apply(ThreadRuntime rt, ScriptModule sm, Argument[] args) throws Exception {
            JValue typs = sm.getTypes(rt);
            return typs;
        }
        
    }
    
    private static class GetScriptsExecutor extends InstanceNativeExecutor<ScriptModule> {
        
        @Override
        protected JValue apply(ThreadRuntime rt, ScriptModule sm, Argument[] args) throws Exception {
            JValue typs = sm.getScripts(rt);
            return typs;
        }
        
    }
    
    //----------------- native executors -----------------//

    private static JValue find(ThreadRuntime rt, String name) {
        IModuleManager mm = rt.getModuleManager();
        ModuleInfo mi = mm.loadModule(rt.getJThread(), name);
        JValue val = mi.getOrCreateScriptObject(rt);       
        return val;
    }
    
    public JValue getScripts(ThreadRuntime rt) {
    	List<ScriptInfo> scripts = this.module.getScripts();
		JValue[] vals = new JValue[scripts.size()];
		for(int i = 0; i < vals.length; i++){
			vals[i] = scripts.get(i).getScriptScriptObject(rt);
		}
		for(ScriptInfo info : scripts){
			info.getScriptScriptObject(rt);
		}

		JClassType typ = (JClassType)ThreadRuntimeHelper.loadSystemType(rt, ScriptScript.FQCLASSNAME);
		ArrayValue array = ThreadRuntimeHelper.createAndPopulateArrayValue(rt, typ, vals);
    	
		return array;
	}

	public ArrayValue getTypes(ThreadRuntime rt) {
    	// 1) Filter types to return. 
    	//    If this is system module, only public types are returned
    	//    If this is user module, all types are returned
    	// (Note the filtering doesn't honor Reflected attribute, which at this time cannot be accessed yet since the types are not loaded.)
    	boolean isSys = FoundationModulesInfo.isFoundationModule(this.module.getName());
		List<ClassInfo> infos = this.module.getClasses();
		List<ClassInfo> filtered = new ArrayList<ClassInfo>(infos.size());
		for(ClassInfo info : infos){
			ClassDeclInfo cdl = info.getClassDeclInfo();
			Accessibility acc = cdl.getAccessibility();
			if (acc == Accessibility.PUBLIC || !isSys) {
				filtered.add(info);
			}
		}
		
		final ClassInfo[] infoArray = new ClassInfo[filtered.size()];
		filtered.toArray(infoArray);
		
		// 2) Load System.Reflection.TypeInfo
		JClassType typ = (JClassType)ThreadRuntimeHelper.loadSystemType(rt, TypeInfo.FQCLASSNAME);
		JClassConstructorMember ctor = typ.getClassConstructors()[0];
		
		// 3) Create and populate an array of System.Reflection.TypeInfo
		ArrayValue array = ThreadRuntimeHelper.createAndPopulateObjectArrayValue(
			rt, infoArray.length, typ, ctor, 
			new IObjectPopulater(){

				@Override
				public Argument[] getArguments(int index) {
					return new Argument[0];
				}
	
				@Override
				public void postCreation(int index, ObjectValue ov) {
					HostedValue hv = (HostedValue) ov;
					hv.setHostedObject(new TypeInfo());
					TypeInfo inst = (TypeInfo)hv.getHostedObject();
					inst.setClassInfo(infoArray[index]);
				}
			});
		
		return array;
	}

	private String getName(ThreadRuntime rt) {       
        return module.getName();
    }
}
