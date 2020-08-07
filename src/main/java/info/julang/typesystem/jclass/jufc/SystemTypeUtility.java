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

package info.julang.typesystem.jclass.jufc;

import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.TypeValue;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.jufc.System.ScriptType;
import info.julang.util.Pair;

/**
 * Provides a few static routines to interact with System types.
 * 
 * @author Ming Zhou
 *
 */
public final class SystemTypeUtility {
	
	public static JClassType ensureTypeBeLoaded(ThreadRuntime threadRt, String typeName) {
		JClassType systemType = (JClassType) threadRt.getTypeTable().getType(typeName);
		if (systemType == null) {
			Context context = Context.createSystemLoadingContext(threadRt);
			threadRt.getTypeResolver().resolveType(context, ParsedTypeName.makeFromFullName(typeName), true);
			systemType = (JClassType) threadRt.getTypeTable().getType(typeName);
		}
		
		return systemType;
	}
	
	public static RefValue createEnumValue(String fullTypeName, ThreadRuntime rt, String enumName){
		ensureTypeBeLoaded(rt, fullTypeName);
		TypeValue tv = (TypeValue) rt.getTypeTable().getValue(fullTypeName);
		RefValue rv = (RefValue) tv.getMemberValue(enumName);
		
		rv = new RefValue(rt.getStackMemory().currentFrame(), rv.getReferredValue());
		return rv;
	}
}
