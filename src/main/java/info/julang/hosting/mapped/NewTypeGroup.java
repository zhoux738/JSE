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

package info.julang.hosting.mapped;

import info.julang.hosting.mapped.inspect.MappedTypeInfo;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.util.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A collection to host all the new types being added in the current type loading session.
 * <p>
 * This is necessary because of the niche we are at when adding mapped types, which have 
 * already left type incubator but yet to be added to type table.
 * 
 * @author Ming Zhou
 */
public class NewTypeGroup {

	private Map<String, Pair<ICompoundType, MappedTypeInfo>> map;
	
	public NewTypeGroup(){
		map = new HashMap<String, Pair<ICompoundType, MappedTypeInfo>>();
	}
	
	public void add(ICompoundType typ, MappedTypeInfo mti) {
		map.put(typ.getName(), new Pair<>(typ, mti));
	}
	
	public Collection<Pair<ICompoundType, MappedTypeInfo>> listAll(){
		return map.values();
	}

	public ICompoundType getType(String strName) {
		Pair<ICompoundType, MappedTypeInfo> pair = map.get(strName);
		return pair != null ? pair.getFirst() : null;
	}
	
}
