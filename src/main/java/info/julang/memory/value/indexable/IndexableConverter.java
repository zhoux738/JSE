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

package info.julang.memory.value.indexable;

import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.JNullReferenceException;
import info.julang.interpretation.context.Context;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.builtin.JArrayType;
import info.julang.typesystem.jclass.jufc.System.Collection.JList;
import info.julang.typesystem.jclass.jufc.System.Collection.JMap;

public final class IndexableConverter {

	/**
	 * For a given runtime value, adapt it to a {@link JIndexable}.
	 * 
	 * @param rt
	 * @param context
	 * @param val
	 * @param applyAccLock
	 * @return null if it is not adaptable.
	 */
	public static JIndexable toIndexable(ThreadRuntime rt, Context context, JValue val, boolean applyAccLock){
		val = val.deref();
		if (val.isNull()){
			throw new JNullReferenceException();
		}
		
		JType type = val.getType();
		if(JArrayType.isArrayType(type)){
			return new ArrayIndexable((ArrayValue)val);
		} else {
			String fname = type.getName();
			if(JList.FullTypeName.equals(fname)){
				return new ListIndexable((ObjectValue)val, rt, context, applyAccLock);
			} else if(JMap.FullTypeName.equals(fname)){
				return new MapIndexable((ObjectValue)val, rt, context);
			} 
		}
		return null;
	}
	
}
