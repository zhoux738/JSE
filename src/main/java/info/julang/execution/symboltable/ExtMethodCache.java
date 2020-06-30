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

package info.julang.execution.symboltable;

import java.util.HashMap;
import java.util.Map;

import info.julang.memory.value.ObjectMember;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.util.OneOrMoreList;

/**
 * A cache to store extension methods.
 * <p>
 * The cache gets automatically (but lazily) invalidated if the type is updated during runtime.
 * 
 * @author Ming Zhou
 */
public class ExtMethodCache {

	private static class ExtMethodStorage {
		private int stamp;
		private Map<String, OneOrMoreList<ObjectMember>> methodsByName;
		
		ExtMethodStorage(int stamp){
			this.stamp = stamp;
			this.methodsByName = new HashMap<String, OneOrMoreList<ObjectMember>>();
		}
	}
	
	private Map<String, ExtMethodStorage> extCache;
	
	public ExtMethodCache() {
		extCache = new HashMap<String, ExtMethodStorage>();
	}
	
	public OneOrMoreList<ObjectMember> get(ICompoundType ctyp, String methodName) {
		String typName = ctyp.getName();
		ExtMethodStorage stor = extCache.get(typName);
		if (stor == null) {
			return null;
		}
		
		if (stor.stamp != ctyp.getStamp()) {
			// Invalidate now
			extCache.remove(typName);
			return null;
		}
		
		OneOrMoreList<ObjectMember> members = stor.methodsByName.get(methodName);
		return members;
	}
	
	public void put(ICompoundType ctyp, String methodName, OneOrMoreList<ObjectMember> members) {
		String typName = ctyp.getName();
		ExtMethodStorage stor = extCache.get(typName);
		int stamp = ctyp.getStamp();
		boolean shouldAdd = false;
		if (stor == null) {
			// Not cached before
			shouldAdd = true;
		} else if (stor.stamp != stamp) {
			// Must invalidate
			extCache.remove(typName);
			shouldAdd = true;
		}
		
		if (shouldAdd) {
			stor = new ExtMethodStorage(stamp);
			extCache.put(typName, stor);
		}
		
		stor.methodsByName.put(methodName, members);
		
	}
}
