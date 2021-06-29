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

package info.julang.hosting.mapped.exec;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import info.julang.execution.Result;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.mapped.inspect.KnownMappedType;
import info.julang.interpretation.context.Context;
import info.julang.memory.value.JValue;
import info.julang.typesystem.jclass.ICompoundType;

/**
 * An executable for initializing mapped type. This is effectively a hosted method.
 * 
 * @author Ming Zhou
 */
public class MappedInitializerExecutable extends MappedExecutableBase {

	private KnownMappedType kmt;
	private Field field;
	
	public MappedInitializerExecutable(ICompoundType ofType, KnownMappedType kmt, Field field, boolean isStatic) {
		super("/Init>-"+field.getName(), ofType, isStatic);
		this.field = field;
		this.kmt = kmt;
	}
	
	// The static value has been pre-computed when loading the platform class. The initializer merely sets it to engine value object.
	@Override
	protected Result executeInternal(ThreadRuntime runtime, Context ctxt) throws InvocationTargetException, IllegalAccessException {
		JValue val = kmt.getStaticValue(ctxt.getHeap(), field, ctxt);
		Result res = new Result(val);
		return res;
	}
	
	@Override
	protected String getCalleeType() {
		return "initializer";
	}
	
	@Override
	protected String getCalleeName() {
		return field.getName();
		
	}
	
	@Override
	protected Class<?> getCalleeContainingType() {
		return field.getDeclaringClass();
	}
}
