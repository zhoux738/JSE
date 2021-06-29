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

package info.julang.typesystem.loading;

import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.modulesystem.ModuleManager;
import info.julang.typesystem.JType;

/**
 * A type resolver used to resolve a name to a Julian type, loading it if necessary.
 * <p>
 * The main job of resolver is to correctly interpret the type name within the current
 * context. Based on the name pool, it will choose the first deduced fully qualified
 * name which corresponds to a type that has been loaded or is potentially loadable
 * (note {@link ModuleManager} has the complete class name list when loading modules.). 
 * If the type has not been loaded before, the resolver uses a {@link TypeLoader} to 
 * load it.
 * 
 * @author Ming Zhou
 */
public interface ITypeResolver {

	/**
	 * Resolve a parsed type name.
	 * 
	 * @param typeName the type's parsed name.
	 * @return the type object for the specified name, if successfully resolved.
	 * @throws info.julang.typesystem.UnknownTypeException if the type is not found.
	 */
	public JType resolveType(ParsedTypeName typeName);
	
	/**
	 * Resolve a parsed type name.
	 * 
	 * @param typeName the type's parsed name.
	 * @param throwIfNotFound if specified, throw {@link info.julang.typesystem.UnknownTypeException} if not found.
	 * @return the type object for the specified name, if successfully resolved. Otherwise null.
	 * @throws info.julang.typesystem.UnknownTypeException if <code>throwIfNotFound</code> is true.
	 */
	public JType resolveType(ParsedTypeName typeName, boolean throwIfNotFound);
	
}
