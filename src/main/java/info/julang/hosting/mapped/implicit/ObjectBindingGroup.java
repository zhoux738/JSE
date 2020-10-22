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

package info.julang.hosting.mapped.implicit;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import info.julang.dev.GlobalSetting;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.binding.ObjectBinding;
import info.julang.external.exceptions.ExternalBindingException;
import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.memory.value.HostedArrayValue;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;
import info.julang.modulesystem.ModuleInfo;
import info.julang.typesystem.JType;
import info.julang.typesystem.JTypeKind;
import info.julang.typesystem.jclass.builtin.JArrayType;
import info.julang.typesystem.loading.InternalTypeResolver;
import info.julang.util.Pair;

/**
 * A class holding all the object bindings. It serves as the type repository for 
 * {@link ImplicitPlatformTypeMapper}, and provides a few helper methods for class 
 * loading and value initialization.
 * 
 * @author Ming Zhou
 */
public class ObjectBindingGroup {

	private static class ObjectBindingEx {
		/** The name to bind to. Used both in-script and externally. */
		String varName;
		/** The platform-backed variable value. */
		Object value;
		/** The platform class info. Stored in {@link ObjectBindingGroup#map} and shared among ObjectBindingEx instances. */
		PlatformClassInfo info;
		/** The dimension of array. 0 if not an array. */
		int dimension;
	}
	
	private static class PlatformClassInfo {
		/** The underlying class. */
		Class<?> clazz;
		/** 
		 * The name to be used in-script mapping to this class. 
		 * Doesn't include the module part, which is always {@link ModuleInfo#IMPLICIT_MODULE_NAME}
		 */
		String inScriptClassName;
	}
	
	private List<ObjectBindingEx> bindings; // All added bindings
	private Map<String, PlatformClassInfo> map; // Keyed by class's full name, not differentiating class loader
	
	public ObjectBindingGroup() {
		bindings = new ArrayList<ObjectBindingEx>();
		map = new HashMap<String, PlatformClassInfo>();
	}
	
	public Class<?> getPlatformClassByName(String name){
		PlatformClassInfo info = map.get(name);
		if (info != null) {
			return info.clazz;
		}
		
		return null;
	}
	
	public void add(String name, ObjectBinding obind) throws ExternalBindingException {
		ObjectBindingEx binding = new ObjectBindingEx();
		binding.varName = name;
		binding.value = obind.getValue();
		
		Class<?> clazz = obind.getValue().getClass();
		int dim = 0;
		while (clazz.isArray()) {
			dim++;
			clazz = clazz.getComponentType();
		}
		
		// Filter out values of UnSupported Type.
		// (We do not throw here because it's possible the user may not have control over what kind of classes get bound.)
		if (clazz.getEnclosingClass() != null && !Modifier.isStatic(clazz.getModifiers())) {
			// UST.1) Non-static inner class
			return;
		} else if (clazz == double.class || clazz == short.class || clazz == long.class) {
			// UST.2) Primitive types that do not have counterpart in JSE
			return;
		}
		
		String key = clazz.getName();
		if (key.startsWith(GlobalSetting.PKG_PREFIX)) {
			// UST.3) Types defined in JSE
			return;
		}
		
		PlatformClassInfo info = map.get(key);
		if (info != null) {
			if (info.clazz != clazz) {
				throw ExternalBindingException.create(key, ExternalBindingException.Type.CONFLICT);
			}
		} else {
			info = new PlatformClassInfo();
			info.clazz = clazz;
			
			map.put(key, info);
		}
		
		binding.info = info;
		binding.dimension = dim;
		
		bindings.add(binding);
	}

	public String getLoadingScript(){
		Set<String> added = new HashSet<>();
		StringBuilder sb = new StringBuilder();
		for (ObjectBindingEx binding : bindings) {
			PlatformClassInfo cinfo = binding.info;
			String name = cinfo.inScriptClassName;
			if (name == null) {
				name = cinfo.inScriptClassName
					 = ImplicitTypeNameConvertor.fromClassNameToSimpleTypeName(cinfo.clazz);
			}
			
			if (!added.contains(name)) {
				addDeclaration(sb, cinfo);
				added.add(name);
			}
		}
		
		return sb.toString();
	}
	
	public List<Pair<String, JValue>> getBindingValues(ThreadRuntime rt) {
		List<Pair<String, JValue>> list = new ArrayList<Pair<String, JValue>>();
		Context context = Context.createSystemLoadingContext(rt);
		InternalTypeResolver resolver = rt.getTypeResolver();
		for (ObjectBindingEx binding : bindings) {
			PlatformClassInfo cinfo = binding.info;
			String fullName = ImplicitTypeNameConvertor.getFullTypeName(cinfo.inScriptClassName);
			
			JType type = resolver.resolveType(context, ParsedTypeName.makeFromFullName(fullName), true);
			
			if (type.getKind() != JTypeKind.CLASS) {
				throw new JSEError("The type '" + fullName + "' is not a class.", ObjectBindingGroup.class);
			}
			
			boolean isArray = binding.dimension > 0;
			if (isArray) {
				int dim = binding.dimension;
				ITypeTable tt = rt.getTypeTable();
				while (dim > 0) {
					type = JArrayType.createJArrayType(tt, type, true);
					dim--;
				}
			}
			
			HostedValue hv = isArray 
				? new HostedArrayValue(rt.getHeap(), rt.getTypeTable(), (JArrayType)type) 
				: new HostedValue(rt.getStackMemory().currentFrame(), type);
			hv.setHostedObject(binding.value);

			String varName = binding.varName;
			Pair<String, JValue> pair = new Pair<String, JValue>(varName, hv);
			
			list.add(pair);
		}
		
		return list;
	}
	
	/*
	 * Add the following snippet
	 * 
	 * [Mapped(className="a.b.MyClass")]
	 * class Mapped_a_b_MyClass {
	 * // private Mapped_a_b_MyClass(){}
	 * }
	 */
	private void addDeclaration(StringBuilder sb, PlatformClassInfo cinfo) {
		// Attribute
		sb.append("[Mapped(className=\"" + cinfo.clazz.getName() + "\")]");
		sb.append(System.lineSeparator());
		
		// Class declaration
		sb.append("internal class " + cinfo.inScriptClassName + " {");
		sb.append(System.lineSeparator());
		
		// Ctor
		//sb.append("  private " + cinfo.inScriptClassName + "(){}");
		//sb.append(System.lineSeparator());
		
		sb.append("}");
		sb.append(System.lineSeparator());
	}
}
