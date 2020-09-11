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

import info.julang.execution.StandardIO;
import info.julang.execution.namespace.NamespacePool;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.symboltable.IVariableTable;
import info.julang.execution.symboltable.RestrictedTypeTable;
import info.julang.execution.symboltable.VariableTable;
import info.julang.execution.threading.JThread;
import info.julang.interpretation.resolving.IMemberNameResolver;
import info.julang.interpretation.resolving.InstanceMethodNameResolver;
import info.julang.interpretation.resolving.StaticMethodNameResolver;
import info.julang.memory.MemoryArea;
import info.julang.modulesystem.IModuleManager;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.loading.InternalTypeResolver;

public class MethodContext extends Context {
	
	private ICompoundType containingType;

	@SuppressWarnings("unchecked")
	public MethodContext(
		MemoryArea frame, 
		MemoryArea heap,
		IVariableTable varTable, 
		ITypeTable typTable, 
		InternalTypeResolver typResolver,
		IModuleManager mm,
		NamespacePool nsPool, 
		StandardIO io,
		JThread jthread,
		ICompoundType containingType,
		boolean isStatic,
		ExecutionContextType exeContextTyp,
		IMemberNameResolver memResolver) {
		super(
			isStatic ? 
				ContextType.SMETHOD : 
				ContextType.IMETHOD,
			frame, 
			heap, 
			varTable, 
			typTable, 
			typResolver,
			mm, 
			nsPool, 
			isStatic ? 
				new StaticMethodNameResolver(varTable, typTable, containingType, memResolver) : 
				new InstanceMethodNameResolver(varTable, typTable, containingType, memResolver),
			io,
			jthread,
			exeContextTyp
		);
		
		if (this.getResolver() instanceof IContextAware<?>){
			((IContextAware<MethodContext>)this.getResolver()).setContext(this);
		}
		
		this.containingType = containingType;
	}
	
	/**
	 * With a given context, make another one that reuses heap, type table and module manager.
	 * 
	 * @param context the context to copy from
	 * @param frame the memory frame
	 * @param vt variable table
	 * @param nsPool namespace pool
	 * @param io standard IO
	 * @param containingType
	 * @param isStatic
	 * @param exeContextType
	 * @return
	 */
	public static MethodContext duplicateContext(
		Context context, 
		MemoryArea frame, 
		VariableTable vt, 
		NamespacePool nsPool,  
		StandardIO io,
		ICompoundType containingType, 
		boolean isStatic,
		ExecutionContextType exeContextType){
		boolean restricted = exeContextType == ExecutionContextType.InAnnotation;
		MethodContext newContext = new MethodContext(
			frame,
			context.getHeap(),
			vt,
			restricted ? new RestrictedTypeTable(context.getTypTable()) : context.getTypTable(),
			context.getInternalTypeResolver(),
			context.getModManager(),
			nsPool,
			io,
			context.getJThread(),
			containingType,
			isStatic,
			exeContextType,
			null);
		return newContext;
	}
	
	/**
	 * The class type that contains the method running in the this context.
	 */
	@Override
	public ICompoundType getContainingType() {
		return containingType;
	}

}
