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

package info.julang.interpretation.context;

import info.julang.execution.namespace.NamespacePool;
import info.julang.execution.symboltable.Display;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.symboltable.IVariableTable;
import info.julang.execution.threading.JThread;
import info.julang.execution.threading.JThreadManager;
import info.julang.interpretation.resolving.LambdaNameResolver;
import info.julang.memory.MemoryArea;
import info.julang.modulesystem.IModuleManager;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.loading.InternalTypeResolver;

/**
 * The lambda context differs from other contexts in that it has a defining context, which
 * is the context in which this lambda is defined. For example, if a lambda is defined in
 * an instance method then it will have an {@link MethodContext instance method context};
 * If a lambda is defined in another lambda, then it will inherit the defining context from
 * the latter lambda. This means the defining context cannot be a LambdaContext itself.
 * 
 * @author Ming Zhou
 */
public class LambdaContext extends Context {

	private ICompoundType containingType;
	
	private ContextType definingContextType;
	
	public LambdaContext(
		MemoryArea frame, 
		MemoryArea heap,
		IVariableTable varTable, 
		ITypeTable typTable, 
		InternalTypeResolver typResolver,
		IModuleManager mm,
		NamespacePool nsPool, 
		JThreadManager tm,
		JThread jthread,
		Display display,
		ContextType definingContextType,
		ICompoundType containingType) {
		super(ContextType.LAMBDA,
			frame, 
			heap, 
			varTable, 
			typTable, 
			typResolver,
			mm, 
			nsPool,
			new LambdaNameResolver(varTable, typTable, display, definingContextType, containingType),
			tm,
			jthread
		);
		
		this.containingType = containingType;
		this.definingContextType = definingContextType;
	}
	
	/**
	 * The class type that contains the original method in which this lambda is defined.
	 * (even if the lambda is defined in another lambda)
	 */
	@Override
	public ICompoundType getContainingType() {
		return containingType;
	}

	public ContextType getDefiningContextType() {
		return definingContextType;
	}

}
