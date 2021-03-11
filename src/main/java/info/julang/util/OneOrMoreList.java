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

package info.julang.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A list optimized for the case that there is only one element.
 * <p/>
 * This class doesn't support removing elements from list.
 * 
 * @author Ming Zhou
 */
public class OneOrMoreList<T> implements Iterable<T> {

	private T one; // this element can be null.
	
	private List<T> list; // this list won't be initialized until we have more than one element.
	
	private short preListSize; // 0, 1, LIST_INITIALIZED
	
	/**
	 * Create a list containing no element.
	 */
	public OneOrMoreList(){
		preListSize = 0;
	}
	
	/**
	 * Create a list containing exactly one element.
	 * 
	 * @param first
	 */
	public OneOrMoreList(T first){
		one = first;
		preListSize = 1;
	}
	
	/**
	 * Create from a collection.
	 * 
	 * @param all
	 */
	public OneOrMoreList(Collection<T> all){
		int total = all != null ? all.size() : 0;
		if(total == 0){
			preListSize = 0;
		} else if(total == 1){
			one = all.iterator().next();
			preListSize = 1;
		} else {
			list = new ArrayList<T>();
			list.addAll(all);
			one = list.get(0);
			preListSize = LIST_INITIALIZED;
		}
	}

	/**
	 * Determine if there is only one element.
	 * @return
	 */
	public boolean hasOnlyOne(){
		return preListSize == 1;
	}
	
	/**
	 * Return the very first element (the one added through constructor).
	 * @return
	 */
	public T getFirst(){
		return one;
	}
	
	@Override
	public Iterator<T> iterator() {
		if(preListSize == LIST_INITIALIZED){
			return list.iterator();
		} else {
			return new Iterator<T>(){
				int returned = preListSize;
				
				@Override
				public boolean hasNext() {
					return returned > 0;
				}

				@Override
				public T next() {
					returned--;
					return one;
				}
			};
		}
	}
	
	public int size(){
		if(preListSize == LIST_INITIALIZED){
			return list.size();
		} else {
			return preListSize;
		}
	}
	
	public boolean add(T element) {
		if(preListSize == 0){
			one = element;
			preListSize = 1;
			return true;
		} else {
			init();
			return list.add(element);
		}
	}
	
	public boolean addAll(Collection<? extends T> c)  {
		init();
		return list.addAll(c);
	}
	
	public List<T> getList(){
		if(preListSize != LIST_INITIALIZED){
			List<T> result = new ArrayList<T>();
			if(preListSize == 1){
				result.add(one);
			}
			return result;
		}
		
		// Replicate references in a new list.
		return new ArrayList<T>(this.list);
	}
	
	@Override
	public String toString() {
		return "[" + (preListSize != LIST_INITIALIZED ? preListSize : (list != null ? list.size() : "")) + "]";
	}

	private void init() {
		if(preListSize != LIST_INITIALIZED){
			list = new ArrayList<T>();
			if(preListSize == 1){
				list.add(one);
			}
			preListSize = LIST_INITIALIZED;
		}
	}
	
	private final static short LIST_INITIALIZED = -1;
}
