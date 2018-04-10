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

package info.julang.hosting.mapped.inspect;

import info.julang.memory.value.AttrValue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class MappedTypeInfo {

	private Class<?> clazz;
	private List<MappedFieldInfo> fields;
	private List<MappedMethodInfo> methods;
	private List<MappedConstructorInfo> ctors;
	private AttrValue av;
	
	public MappedTypeInfo(Class<?> clazz, AttrValue av){
		this.clazz = clazz;
		this.av = av;
	}
	
	public Class<?> getPlatformClass(){
		return clazz;
	}
	
	public AttrValue getAttributeValue(){
		return av;
	}
	
	public List<MappedFieldInfo> getFields(){
		return fields == null ? new ArrayList<MappedFieldInfo>(0) : fields;
	}
	
	public List<MappedMethodInfo> getMethods(){
		return methods == null ? new ArrayList<MappedMethodInfo>(0) : methods;
	}
	
	public List<MappedConstructorInfo> getConstructors(){
		return ctors == null ? new ArrayList<MappedConstructorInfo>(0) : ctors;
	}
	
	//---------- Builder ----------//
	
	void addField(Field f, IMappedType typ){ //Member
		if (fields == null) {
			fields = new ArrayList<MappedFieldInfo>();
		}
		
		MappedFieldInfo info = new MappedFieldInfo(f.getName(), Modifier.isStatic(f.getModifiers()), Modifier.isFinal(f.getModifiers()), typ, f);
		fields.add(info);
	}
	
	void addMethod(Method m, IMappedType retTyp, IMappedType[] paramTyps){
		if (methods == null) {
			methods = new ArrayList<MappedMethodInfo>();
		}
		
		MappedMethodInfo info = new MappedMethodInfo(
			m.getName(), Modifier.isStatic(m.getModifiers()), retTyp, paramTyps, m);
		methods.add(info);
	}

	public void addConstructor(Constructor<?> ctor, IMappedType[] ptyps) {
		if (ctors == null) {
			ctors = new ArrayList<MappedConstructorInfo>();
		}
		
		MappedConstructorInfo info = new MappedConstructorInfo(ptyps, ctor);
		ctors.add(info);
	}
}
