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

import info.julang.typesystem.JType;
import info.julang.util.OneOrMoreList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A class used to store all the loaded members for a class type.
 * <p/>
 * The members are stored in a hierarchical structure such that a field in subclass
 * may override the one of same name in any of its ancestors.
 * 
 * @author Ming Zhou
 */
public class ClassMemberMap {

	// From a given type name, get the index in typeMemsArray where the members for this type are stored.
	private Map<String, Integer> rankMap;
	
	// All the types which contributes to this map, in the same order as and has same length to typeMemsArray.
	private JClassType[] typesArray;
	
	// Stores all the type members in the hierarchical order, with the last one being the root type (Object).
	private Map<String, OneOrMoreList<ClassMemberLoaded>>[] typeMemsArray;
	
	public ClassMemberMap(JClassType jct, boolean isStatic) {
		rankMap = new HashMap<String, Integer>();
		initialize(jct, 0, isStatic);
	}
	
	/**
	 * All the types which contributes to this map, in the same order as and has same length to {@link #getDefinedMembers()}.
	 * @return
	 */
	public JClassType[] getContributingTypes(){
		return typesArray;
	}
	
	/**
	 * All the type members in the hierarchical order, with the last one being the root type (Object).
	 * @return
	 */
	public Map<String, OneOrMoreList<ClassMemberLoaded>>[] getDefinedMembers(){
		return typeMemsArray;
	}
	
	/**
	 * Get loaded class member by name.
	 * 
	 * @param name
	 * @return null if not found.
	 */
	public OneOrMoreList<ClassMemberLoaded> getLoadedMemberByName(String name){
		return getLoadedMemberByName(0, name, true);
	}
	
	/**
	 * Get loaded class member by name; starts searching from the specified type in type hierarchy.
	 * 
	 * @param type
	 * @param name
	 * @param includeNonvisible if true, include private members too
	 * @return
	 */
	public OneOrMoreList<ClassMemberLoaded> getLoadedMemberByName(JType type, String name, boolean includeNonvisible){
		Integer rank = rankMap.get(type.getName());
		if(rank == null){
			return null;
		}
		
		return getLoadedMemberByName(rank, name, includeNonvisible);
	}
	
	/**
	 * Get an array of all the members for this class.
	 * <p/>
	 * Member can shadow the one with same name in ancestor classes. For example, if we have class A : B,
	 * where both A and B defines a member x, then calling this method against A would return an array 
	 * where we only have member x as defined in A.
	 * 
	 * @return
	 */
	JClassMember[] getClassMembers(){
		Map<MemberKey, JClassMember> map = new HashMap<MemberKey, JClassMember>();
		
		// Iterate from end (ancestor) to start (this type).
		// This is because if a member defined by ancestor class is overridden by subclass,
		// we can conveniently replace it using .put() API later
		for(int i = typeMemsArray.length - 1; i>=0; i--){
			Collection<OneOrMoreList<ClassMemberLoaded>> cmlss = typeMemsArray[i].values();
			for(OneOrMoreList<ClassMemberLoaded> cmls : cmlss){
				for(ClassMemberLoaded cml : cmls){
					JClassMember jcm = cml.getClassMember();
					
					// A private member should no be added.
					if (i!=0){
						switch(jcm.getAccessibility()){
						case PRIVATE:
						case HIDDEN:
							continue;
						default:
							break;
						}
					}
					
					// Note this could substitute an existing member defined by ancestors.
					map.put(jcm.getKey(), jcm);
				}
			}
		}
		
		JClassMember[] array = new JClassMember[map.size()];
		map.values().toArray(array);
		
		return array;
	}

	private OneOrMoreList<ClassMemberLoaded> getLoadedMemberByName(int rank, String name, boolean includeNonvisible){
		// Starting from the root type, place all the members of the given name to a map,
		// with those from subclasses overriding methods of same signature defined in any 
		// ancestor.
		// For example, let's say we have class C => P => G, each of them having the following
		// methods with name "fun" defined:
		//
		//   C [0]       P [1]       G [2]
		//   fun(int)    fun(int)    fun(int)
		//   fun()                   fun()
		//               fun(string) fun(string)
		//
		// The calling getLoadedMemberByName(0, "fun") returns
		//   C.fun(int), C.fun() and P.fun(string)
		// and calling getLoadedMemberByName(1, "fun") returns
		//   P.fun(int), G.fun() and P.fun(string)
		
		// Starting from the specified type, try to find a member of the given name.
		if(typeMemsArray != null){
			// The members are added in top-down order but will be returned in bottom-up order.
			Map<MemberKey, ClassMemberLoaded> loaded = new LinkedHashMap<>();
			for(int i = typeMemsArray.length - 1; i >= rank ; i--){
				Map<String, OneOrMoreList<ClassMemberLoaded>> map = typeMemsArray[i];
				OneOrMoreList<ClassMemberLoaded> cmls = map.get(name);
				if(cmls!=null){
					for(ClassMemberLoaded cml : cmls){
						JClassMember jcm = cml.getClassMember();
						if (i == rank || includeNonvisible || jcm.getAccessibility().isSubclassVisible()) {
							loaded.put(jcm.getKey(), cml);
						}
					}
				}
			}
			
			Collection<ClassMemberLoaded> coll = loaded.values();
			int len = coll.size();
			ClassMemberLoaded[] arr = new ClassMemberLoaded[len];
			int i = len - 1;
			for(ClassMemberLoaded cml : coll){
				arr[i] = cml;
				i--;
			}
			OneOrMoreList<ClassMemberLoaded> result = new OneOrMoreList<>();
			for (int j = 0; j < len; j++) {
				result.add(arr[j]);
			}
			
			return result;
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private void initialize(JClassType jct, int rank, boolean isStatic){
		int total = rank + 1;
		// Recursively add members from its parent first
		JClassType parent = jct.getParent();
		if(parent != null) {
			initialize(parent, total, isStatic);
		}
		
		if(typeMemsArray == null){ // this will be called at the root type
			typeMemsArray = new Map[total];	
			typesArray = new JClassType[total];
		}
		
		Map<String, OneOrMoreList<JClassMember>> map = isStatic ? jct.staticMemberMap : jct.instanceMemberMap;
		Collection<JClassMember> values = new ArrayList<JClassMember>();
		if (map != null) {
			for(OneOrMoreList<JClassMember> overloads : map.values()){
				for(JClassMember mem : overloads){
					values.add(mem);
				}
			}
		}
		
		Map<String, OneOrMoreList<ClassMemberLoaded>> descMap = new HashMap<>();
		for(JClassMember member : values){
			ClassMemberLoaded mem = new ClassMemberLoaded(member, rank);
			String name = member.getName();
			OneOrMoreList<ClassMemberLoaded> list = descMap.get(name);
			if(list == null){
				// Initialize the overloading list
				list = new OneOrMoreList<ClassMemberLoaded>(mem);
				descMap.put(name, list);
			} else {
				// Add an overloaded member
				list.add(mem);
			}
		}
		typeMemsArray[rank] = descMap;
		
		rankMap.put(jct.getName(), rank);
		typesArray[rank] = jct;
	}
	
}
