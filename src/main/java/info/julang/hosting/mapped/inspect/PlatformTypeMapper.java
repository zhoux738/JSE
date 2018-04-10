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

import info.julang.dev.GlobalSetting;
import info.julang.hosting.PlatformExceptionInfo;
import info.julang.hosting.mapped.IllegalTypeMappingException;
import info.julang.hosting.mapped.exec.PlatformClassLoadingException;
import info.julang.memory.value.AttrValue;
import info.julang.typesystem.VoidType;
import info.julang.typesystem.basic.BoolType;
import info.julang.typesystem.basic.ByteType;
import info.julang.typesystem.basic.CharType;
import info.julang.typesystem.basic.FloatType;
import info.julang.typesystem.basic.IntType;
import info.julang.typesystem.jclass.builtin.JStringType;
import info.julang.typesystem.loading.ClassLoadingException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

/**
 * The platform type mapper maps certain members of a platform (Java) class to a set of JSE members.
 * <p>
 * A public constructor/method/field will have a mapped member if all the types, including the return type
 * for method, are compliant with a certain subset of Julian types. 
 * <p>
 * As of 0.1.7, the allowed types are:
 * <p>
 * <table>
 * <tr><th>Java Type</th><th>Julian Type</th></tr>
 * <tr><td>int</td><td>int</td></tr>
 * <tr><td>byte</td><td>byte</td></tr>
 * <tr><td>char</td><td>char</td></tr>
 * <tr><td>boolean</td><td>bool</td></tr>
 * <tr><td>float</td><td>float</td></tr>
 * <tr><td>java.lang.String</td><td>string</td></tr>
 * </table>
 * <p>
 * In the future, array types, certain collection types and the mapped type itself, and any other mapped types will be supported.
 * 
 * @author Ming Zhou
 */
public class PlatformTypeMapper {

	private Class<?> clazz = null;
	
	/**
	 * Map a given platform type to {@link MappedTypeInfo} object, which contains 
	 * all the mappable members, static of instance-scoped, that can be potentially
	 * added to the eventual script type.
	 * 
	 * @param loader
	 * @param className
	 * @return
	 */
	public synchronized MappedTypeInfo mapType(ClassLoader loader, String className, AttrValue av, boolean isClass) 
		throws IllegalTypeMappingException {
		if (className.startsWith(GlobalSetting.PKG_PREFIX)){
			throw new IllegalTypeMappingException(className, "cannot map to a Julian engine's internal class.");
		}
		
		clazz = null;
		
		try {
			clazz = Class.forName(className, true, loader);
		} catch (ClassNotFoundException | LinkageError e) {
			throw new PlatformClassLoadingException(new PlatformExceptionInfo(className, 0, e)); // no stack depth deduction
		}
		
		if (clazz.isInterface() ^ !isClass){ // XOR - true if the two operands are different
			throw new IllegalTypeMappingException(className, 
				isClass ? "a script class must map to a platform class." : "a script interface must map to a platform interface.");
		}
		
		if (!Modifier.isPublic(clazz.getModifiers())) {
			throw new IllegalTypeMappingException(className, "the platform type is not public.");
		}

		Class<?> enclosingClass = clazz.getEnclosingClass();
		if (enclosingClass != null) {
			if (!Modifier.isStatic(clazz.getModifiers())) {
				throw new IllegalTypeMappingException(className, "the platform type is neither at top level nor statically nested.");
			}
		}
		
		MappedTypeInfo mti = new MappedTypeInfo(clazz, av);
		
		// all public fields, including inherited
		Field[] fields = clazz.getFields();
		for (Field fld : fields) {
			Class<?> cls = fld.getType();
			IMappedType imtype = translateType("<field>", cls);
			mti.addField(fld, imtype);
		}

		// all public methods, including inherited
		Method[] methods = clazz.getMethods();
		for (Method mtd : methods) {
			Class<?> cls = mtd.getReturnType();
			IMappedType rtype = translateType("<return>", cls);
			IMappedType[] ptyps = getParamTypes(mtd.getParameters(), mtd.getParameterTypes());
			mti.addMethod(mtd, rtype, ptyps);
		}
		
		// all public constructors
		Constructor<?>[] ctors = clazz.getConstructors();
		for (Constructor<?> ctor : ctors) {
			IMappedType[] ptyps = getParamTypes(ctor.getParameters(), ctor.getParameterTypes());
			mti.addConstructor(ctor, ptyps);
		}
		
		return mti;
	}

	private IMappedType[] getParamTypes(Parameter[] parameters, Class<?>[] clss) {
		Class<?> cls;
		IMappedType ptyps[] = null;
		ptyps = new IMappedType[clss.length];
		for (int i = 0; i < clss.length; i++) {
			cls = clss[i];
			String name = parameters[i].getName();
			IMappedType imtype = translateType(name, cls);
			ptyps[i] = imtype;
		}
		return ptyps;
	}
	
	private IMappedType translateType(String name, Class<?> cls) {
		return translateType0(name, cls, cls, 0);
	}

	private IMappedType translateType0(String name, Class<?> ocls, Class<?> cls, int dim) {
		if (cls.isArray()){
			// If an array type, recursively call this method to drill 
			// down to the element type, with dimension added by one.
			return translateType0(name, ocls, cls.getComponentType(), dim + 1);
		}
		
		switch (cls.getName()){
		case "int": return new KnownMappedType(IntType.getInstance(), dim, ocls, name);
		case "boolean": return new KnownMappedType(BoolType.getInstance(), dim, ocls, name);
		case "byte": return new KnownMappedType(ByteType.getInstance(), dim, ocls, name);
		case "float": return new KnownMappedType(FloatType.getInstance(), dim, ocls, name);
		case "char": return new KnownMappedType(CharType.getInstance(), dim, ocls, name);
		case "java.lang.String": return new KnownMappedType(JStringType.getInstance(), dim, ocls, name);
		case "void": 
		case "java.lang.Void": return new KnownMappedType(VoidType.getInstance(), 0, ocls, name); // dim should be 0, otherwise a JVM error.
		default: 
			DeferredMappedType dmt = new DeferredMappedType(cls.getName(), dim, ocls, name);
			if (clazz == cls){ // Using == to ensure equality of class loader
				dmt.setSameToEnclosingType(true);
			}
			return dmt;
		}
	}

}
