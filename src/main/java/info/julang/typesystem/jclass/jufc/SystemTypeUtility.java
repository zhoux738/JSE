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
import info.julang.typesystem.JType;
import info.julang.util.Pair;

public final class SystemTypeUtility {

	public static JType ensureTypeBeLoaded(Context context, String typeName){
		JType typ = null;
		if ((typ = context.getTypTable().getType(typeName)) == null){
			ParsedTypeName ptn = ParsedTypeName.makeFromFullName(typeName);
			typ = context.getTypeResolver().resolveType(ptn);	
		}
		
		return typ;
	}
	
	public static Pair<JType, Context> ensureTypeBeLoaded(ThreadRuntime threadRt, String typeName){
		Context context = Context.createSystemLoadingContext(threadRt);
		return new Pair<JType, Context>(ensureTypeBeLoaded(context, typeName), context);
	}
	
}
