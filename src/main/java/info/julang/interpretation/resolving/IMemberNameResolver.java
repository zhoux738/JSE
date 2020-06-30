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

package info.julang.interpretation.resolving;

import info.julang.memory.value.JValue;

/**
 * The member name resolver is responsible for resolving an id as a member of the containing type. The member may be defined 
 * in the type, inherited from an ancestor type, implemented for an interface or installed by an extension type.
 * <p>
 * Since the types loaded into Julian's runtime are immutable for their life cycle, it's pointless to resolve the same id
 * more than once within the same executable context. This resolver is in fact a cache of what have been resolved - its implementation
 * of {@link #resolve(String)} simply returns what is set through {@link #save(String, JValue)}. It it returns null, no value
 * has been set for the given id and the caller must continue with the regular resolution routine; if it returns {@link info.julang.memory.value.VoidValue#DEFAULT},
 * the id has been resolved without a match; otherwise the value that represents the resolved is returned.
 * 
 * @author Ming Zhou
 */
public interface IMemberNameResolver extends INameResolver {

	/**
	 * Cache the resolved value for the id.
	 * 
	 * @param id The id to resolve
	 * @param val The value resolved. A null value will be interpreted as no-match, so that calling {@link #resolve(String)} with this id would
	 * return {@link info.julang.memory.value.VoidValue#DEFAULT the void value}.
	 */
	void save(String id, JValue val);

}
