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

package info.julang.typesystem.loading.depresolving;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * HardDependencyResolver sorts a collection of {@link IOrderResolvable} based on their dependency relationship. 
 * For example, assume A =&gt; (reads: "depends on") B, C; B =&gt; D; E; C =&gt; D; D =&gt; (NONE), and the given unsorted 
 * collection is [{A: B, C}, {B: D, E}, {C: D}, {D: }]. The sorted collection will be [D, E, C, B, A], with items 
 * at the lower index having higher priority to load.
 * <p>
 * If two types have same loading dependency, the can appear in an arbitrary order. Types which do not depend on 
 * each other in any way can also appear in the resultant list with arbitrary relative order. For example, if we 
 * have [{A: B}, {C: D}] as input, the output only ensures that A is placed before B, and C before D. But the order
 * between A and C, etc., is not enforced.
 * <p> 
 * If a cyclic dependency is detected, this method will throw {@link CyclicDependencyException an exception}. Note 
 * the dependency is based on parent class type and interfaces, and doesn't concern with the field, parameter and 
 * return types. This means a loading sequences produced by this resolver can cause a static field of class
 * A to be left in its default value if that field is to be initialized by B's field, when the loading order is 
 * [A, B] due to B having a hard parent/interface dependency on A. This can also happen when A and B are unrelated
 * but have fields to be initialized by the other. In such case only one of the types will get properly initialized. 
 * Such behaviors are totally legitimate and implemented by design.  
 * 
 * @author Julia Wei
 */
/* 
 * IMPLEMENTATION NOTES:
 * 
 * The logic goes through the given unsorted collection, recursively finding each IOrderResolvable's dependencies 
 * until the root, which depends on nothing, is found. There could be multiple roots. The root is given priority 
 * zero which should be loaded first. After finding root, the program pops up to the outer call and assigns to 
 * IResolvables being processed a priority equal to its parent's priority plus 1.
 */  
public class HardDependencyResolver implements IDependencyResolver {

	private static final int MINIMUM_ORDER = 0;
	
	private class MultiHashMap<K, V> {
		
		private HashMap<K, List<V>> map = null;
		
		public MultiHashMap() {
			map = new HashMap<K,List<V>>();
		}
		
		public void put(K key, V v) {
			List<V> values = map.get(key);
			if( values == null) {
				values = new ArrayList<V>();
				map.put(key, values);
			}
			if(!values.contains(v)) {
				map.get(key).add(v);
			}
		}
		
		public List<V> get(K key) {
			return (List<V>)map.get(key);
		}
		
		public int size() {
			return map.size();
		}
	}

	@Override
	public List<IOrderResolvable> resolve(final Collection<? extends IOrderResolvable> states) {
		HashMap<String, IOrderResolvable> nameMap = new HashMap<String, IOrderResolvable>();
		MultiHashMap<Integer, IOrderResolvable> typeMap = new MultiHashMap<Integer, IOrderResolvable>();
		MultiHashMap<Integer, IOrderResolvable> classMap = new MultiHashMap<Integer, IOrderResolvable>();
		for(IOrderResolvable state:states) {
			nameMap.put(state.getTypeName(), state);
		}
		List<String> children = new ArrayList<String>();
		
		for(IOrderResolvable state:states) {
			children.clear();
			if(state.isAttributeType()){
				sort(states, state, typeMap, nameMap, children);
			} else {
				sort(states, state, classMap, nameMap, children);
			}
		}
		
		Set<String> merged = new HashSet<String>();
		List<IOrderResolvable> sortedStates = new ArrayList<IOrderResolvable>();
		mergeSorted(typeMap, merged, sortedStates);
		mergeSorted(classMap, merged, sortedStates);
		
		return sortedStates;
	}

	private void mergeSorted(
		MultiHashMap<Integer, IOrderResolvable> typeMap, Set<String> merged, List<IOrderResolvable> sortedStates) {
		int typeMapSize = MINIMUM_ORDER + typeMap.size();
		for(int order = MINIMUM_ORDER; order < typeMapSize; order++) {
			List<IOrderResolvable> ilss = typeMap.get(order);
			for(IOrderResolvable state : ilss) {
				String fqn = state.getTypeName();
				if (!merged.contains(fqn)){
					sortedStates.add(state);
					merged.add(fqn);
				}
			}
		}
	}
	
	private int sort(
		final Collection<? extends IOrderResolvable> states, 
		final IOrderResolvable state, 
		final MultiHashMap<Integer, IOrderResolvable> map, 
		final HashMap<String, IOrderResolvable> nameMap,
		List<String> children) {
		
		String name = state.getTypeName();
		children.add(name);
		
		List<String> depsName = state.getDependentTypeNames();
		int order = MINIMUM_ORDER;
		if(depsName != null && !depsName.isEmpty()) {
			for(String depName:depsName) {
				IOrderResolvable dep = nameMap.get(depName);
				if(dep != null) {
					if(children.contains(depName)) {
						// Found cyclic dependency
						children.add(depName);
						throw new CyclicDependencyException(children.toArray(new String[0]), true);
					}
					
					int depOrder = sort(states, dep, map, nameMap, children);
					order = order <= depOrder? (depOrder +1): order;
				}
			}
			map.put(order, state);
		} else {
			map.put(MINIMUM_ORDER, state);	
		}
		
		children.remove(name);
		return order;
	}
	
}
