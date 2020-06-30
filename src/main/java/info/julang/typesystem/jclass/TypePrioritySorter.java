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

package info.julang.typesystem.jclass;

import java.lang.reflect.Array;
import java.util.TreeSet;

/**
 * An ordered container that sorts types based the given rank and order.
 * 
 * It also deduplicates the types so that no two elements have the same type.
 * 
 * @author Ming Zhou
 */
class TypePrioritySorter {
	private int torder;
	private TreeSet<TypePriority> all;
	
	TypePrioritySorter(){
		torder = 0;
		all = new TreeSet<TypePriority>();
	}
	
	/**
	 * Create the next TypePriority instance wrapping the specified type. 
	 * It has a total order incremented by one from the previous creation.
	 * @param typ The type to be wrapped in.
	 */
	TypePriority next(JInterfaceType typ) {
		return new TypePriority(typ, torder++);
	}
	
	/**
	 * Check whether the type is already existing.
	 */
	boolean contains(TypePriority ep) {
		return all.contains(ep);
	}
	
	/**
	 * Add a new priority with the specified rank and order, both provided and tracked externally.
	 */
	void add(TypePriority ep, int rank, int order) {
		ep.setRank(rank, order);
		all.add(ep);
	}
	
	/**
	 * Convert what are stored inside the sorter into an array with the desired type, sorted in the same order as in the sorter.
	 * 
	 * @param <T> Can be JInterfaceType or JClassType 
	 * @param c Can be JInterfaceType or JClassType 
	 * @param includesFirst if false, the returned array won't contain the starting type.
	 * @return An array with the desired type, sorted in the same order as in the sorter.
	 */
	@SuppressWarnings("unchecked")
	<T extends JInterfaceType> T[] toArray(Class<T> c, boolean includesFirst) {
		int offset = includesFirst ? 0 : 1;
		Object[] rawArr = all.toArray();
		int len = rawArr.length - offset;
        final T[] arr = (T[]) Array.newInstance(c, len);
    	for (int i = offset; i < len; i++) {
    		TypePriority ep = (TypePriority)rawArr[i];
    		arr[i - offset] = (T)ep.getType();
    	}
    	
    	return arr;
	}
}