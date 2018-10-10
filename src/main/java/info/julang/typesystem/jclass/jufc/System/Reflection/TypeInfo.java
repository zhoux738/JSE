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
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.CtorNativeExecutor;
import info.julang.hosting.execution.InstanceNativeExecutor;
import info.julang.interpretation.syntax.ClassSubtype;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.modulesystem.ClassInfo;

public class TypeInfo {

    public static final String FQCLASSNAME = "System.Reflection.TypeInfo";
    
    //----------------- IRegisteredMethodProvider -----------------//
    
    public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FQCLASSNAME){

        @Override
        protected void implementProvider(SimpleHostedMethodProvider provider) {
            provider
                .add("ctor", new InitExecutor())
                .add("getFullName", new GetNameExecutor())
                .add("isInterface", new IsInterfaceExecutor())
                .add("isFinal", new IsFinalExecutor());
        }
        
    };
    
    private static class InitExecutor extends CtorNativeExecutor<TypeInfo> {

        @Override
        protected void initialize(ThreadRuntime rt, HostedValue hvalue, TypeInfo sm, Argument[] args) throws Exception {
            // NO-OP
        }
        
    }
    
    private static class GetNameExecutor extends InstanceNativeExecutor<TypeInfo> {

        @Override
        protected JValue apply(ThreadRuntime rt, TypeInfo sm, Argument[] args) throws Exception {
            String fn = sm.getFullName();
            return TempValueFactory.createTempStringValue(fn);
        }
        
    }
    
    private static class IsInterfaceExecutor extends InstanceNativeExecutor<TypeInfo> {

        @Override
        protected JValue apply(ThreadRuntime rt, TypeInfo sm, Argument[] args) throws Exception {
            boolean fn = sm.isInterface();
            return TempValueFactory.createTempBoolValue(fn);
        }
        
    }
    
    private static class IsFinalExecutor extends InstanceNativeExecutor<TypeInfo> {

        @Override
        protected JValue apply(ThreadRuntime rt, TypeInfo sm, Argument[] args) throws Exception {
            boolean fn = sm.isFinal();
            return TempValueFactory.createTempBoolValue(fn);
        }
        
    }
    
    private ClassInfo cinfo;
    
    public void setClassInfo(ClassInfo cinfo) {
        this.cinfo = cinfo;
    }
    
    //----------------- native executors -----------------//
    
    private String getFullName(){
        return cinfo.getFQName();
    }
    
    private boolean isInterface(){
        return cinfo.getClassDeclInfo().getSubtype() == ClassSubtype.INTERFACE;
    }

    private boolean isFinal(){
        return cinfo.getClassDeclInfo().isFinal();
    }
   
//    private JValue load(ThreadRuntime rt){
//        String fqname = cinfo.getFQName();
//        JType type = rt.getTypeTable().getType(fqname);
//        
//        if (type == null) {
//            // Try to load the type
//            if (type == null) {
//                Context context = Context.createSystemLoadingContext(rt);
//                rt.getTypeResolver().resolveType(context, ParsedTypeName.makeFromFullName(fqname), true);
//                type = (JClassType) rt.getTypeTable().getType(fqname);
//            }
//        }
//        
//        ObjectValue ov = ThreadRuntimeHelper.getScriptTypeObject(rt, type);
//        return ov;
//    }
}
