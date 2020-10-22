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

import info.julang.dev.GlobalSetting;
import info.julang.external.exceptions.JSEError;
import info.julang.hosting.mapped.IllegalTypeMappingException;
import info.julang.hosting.mapped.inspect.MappedTypeInfo;
import info.julang.hosting.mapped.inspect.PlatformTypeMapper;
import info.julang.memory.value.AttrValue;

/**
 * A special platform type mapper used to map class members of registered types provided through {@link ObjectBindingGroup}.
 * <p>
 * This mapper is different from the default mapper in a few aspects:
 * <ul>- it doesn't use the engine class loader to load the target type; instead
 * it tries to get the type directly from the given type collection.</ul>
 * <ul>- it has less restrictions than the default mapper because the mapped type would have more limited usage.</ul>
 * <ul>- it doesn't produce any constructor member.</ul>
 * 
 * @author Ming Zhou
 */
public class ImplicitPlatformTypeMapper extends PlatformTypeMapper {

	private ObjectBindingGroup grp;
	
	public ImplicitPlatformTypeMapper(ObjectBindingGroup grp) {
		this.grp = grp;
	}
	
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
		
		clazz = grp.getPlatformClassByName(className);

		if (clazz == null) {			
			throw new JSEError("No type with name '" + className + "' is registered.");
		}
		
		if (clazz.isPrimitive()) {
			// Do not provide any members for primitive type. They are just a type placeholder.
			return new MappedTypeInfo(clazz, av);
		}
		
		if (clazz.isInterface() ^ !isClass){ // XOR - true if the two operands are different
			throw new JSEError(
				className + 
				"Impilicit type mapping error: " + (
					isClass 
					? "a script class must map to a platform class."
					: "a script interface must map to a platform interface."));
		}
		
		MappedTypeInfo mti = new MappedTypeInfo(clazz, av);
		
		// all public fields, including inherited
		addFields(mti);

		// all public methods, including inherited
		addMethods(mti);
		
		return mti;
	}
}
