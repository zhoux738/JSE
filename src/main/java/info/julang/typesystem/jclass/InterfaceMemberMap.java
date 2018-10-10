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

import info.julang.util.OneOrMoreList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A class used to store the members defined by an interface and all its ancestors, which are
 * also interfaces. 
 * 
 * @author Ming Zhou
 */
public class InterfaceMemberMap {

	private Map<String, OneOrMoreList<InstanceMemberLoaded>> typeMemsMap;
	
	public InterfaceMemberMap(JInterfaceType jct) {
		typeMemsMap = new HashMap<String, OneOrMoreList<InstanceMemberLoaded>>();
		initialize(jct);
	}
	
	OneOrMoreList<InstanceMemberLoaded> getMembersByName(String name){
		return typeMemsMap.get(name);
	}
	
	JClassMethodMember[] getAllMembers(){
		List<JClassMethodMember> list = new ArrayList<JClassMethodMember>();
		Collection<OneOrMoreList<InstanceMemberLoaded>> values = typeMemsMap.values();
		for(OneOrMoreList<InstanceMemberLoaded> val: values){
			for(InstanceMemberLoaded iml : val){
				list.add(iml.getMember());
			}
		}
		
		JClassMethodMember[] members = new JClassMethodMember[list.size()];
		list.toArray(members);
		
		return members;
	}
	
	private void initialize(JInterfaceType jct) {
		// 1) Add members from this interface. 
		Map<String, OneOrMoreList<JClassMember>> members = jct.instanceMemberMap;
		if(members != null && !members.isEmpty()){
			Set<Entry<String, OneOrMoreList<JClassMember>>> entries = members.entrySet();
			for(Entry<String, OneOrMoreList<JClassMember>> entry: entries){
				String name = entry.getKey();
				OneOrMoreList<JClassMember> entryList = entry.getValue();
				List<InstanceMemberLoaded> toAdd = new ArrayList<InstanceMemberLoaded>();
				for(JClassMember jcm : entryList){
					JClassMethodMember jcmm = JClassTypeUtil.isInterfaceMember(jcm);
					if (jcmm != null) {
						toAdd.add(new InstanceMemberLoaded(jcmm, jct));
					}
				}
				
				OneOrMoreList<InstanceMemberLoaded> list = typeMemsMap.get(name);
				if (list == null){
					list = new OneOrMoreList<InstanceMemberLoaded>(toAdd);
					typeMemsMap.put(name, list);
				} else {
					// Add new members
					
					// If a member we are to add is already defined (contributed by other interface
					// which contains a method that bears the very same signature), we do not add it
					// again. Instead, just update the contributing list. This information is mostly
					// used in diagnosis.
					List<InstanceMemberLoaded> filtered = new ArrayList<InstanceMemberLoaded>();
					for(InstanceMemberLoaded iml : toAdd){
						boolean overloadedWithOtherInterfaces = false;
						MemberKey key = iml.getMember().getKey();
						for(InstanceMemberLoaded existing : list){ 
							if (existing.getMember().getKey().equals(key)){
								existing.addContributingType(jct);
								overloadedWithOtherInterfaces = true;
								break;
							}
						}
						
						if(!overloadedWithOtherInterfaces){
							filtered.add(iml);
						}
					}
					
					for(InstanceMemberLoaded iml : filtered){
						list.add(iml);
					}
				}
			}
		}
		
		// 2) Add members from extended interfaces.
		JInterfaceType[] interfaces = jct.getInterfaces();
		for(JInterfaceType jit: interfaces){
			initialize(jit);
		}
	}
}
