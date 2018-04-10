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

package info.julang.execution.threading;

import info.julang.execution.InContextTypeResolver;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.symboltable.IVariableTable;
import info.julang.interpretation.context.Context;
import info.julang.memory.MemoryArea;
import info.julang.memory.StackArea;
import info.julang.memory.simple.SimpleStackArea;
import info.julang.modulesystem.IModuleManager;
import info.julang.typesystem.loading.InternalTypeResolver;

/**
 * The thread runtime used in scenarios where the engine initiates the execution of
 * a piece of code. These include calling static initializer or static constructor, 
 * and invoking methods from a non-JSE thread, which is typical for asynchronous 
 * callbacks registered with the platform API. The motivation for using a new 
 * runtime object can differ. In the case of class initialization, execution shall
 * be performed in a system stack since it's not a user's responsibility to load 
 * types. In the case of callbacks potentially to be invoked by a different thread,
 * we must use a new runtime object, as it is not designed to be thread safe.
 * <p>
 * The created thread runtime reuses type table, module manager, heap memory and
 * global variable table from user's. It has its own stack memory though.
 * 
 * @author Ming Zhou
 */
public class SystemInitiatedThreadRuntime implements ThreadRuntime {
	
	private Context context;
	
	private ThreadStack tstack;
	
	/**
	 * Create a new type loading thread runtime mainly using an interpretation context
	 * for runtime resources.
	 * 
	 * @param context
	 */
	public SystemInitiatedThreadRuntime(Context context){
		this.context = context;
		
		tstack = new ThreadStack(
			new StackAreaFactory(){
				@Override
				public StackArea createStackArea() {
					return new SimpleStackArea();
				}
			}, 
			context.getVarTable().getGlobal());
	}

	@Override
	public MemoryArea getHeap() {
		return context.getHeap();
	}

	@Override
	public ITypeTable getTypeTable() {
		return context.getTypTable();
	}

	@Override
	public IVariableTable getGlobalVariableTable() {
		return context.getVarTable().getGlobal();
	}

	@Override
	public IModuleManager getModuleManager() {
		return context.getModManager();
	}

	@Override
	public StackArea getStackMemory() {
		return tstack.getStackArea();
	}

	@Override
	public ThreadStack getThreadStack() {
		return tstack;
	}
	
	@Override
	public JThread getJThread() {
		return context.getJThread();
	}

	@Override
	public InternalTypeResolver getTypeResolver() {
		return ((InContextTypeResolver)context.getTypeResolver()).getInternalTypeResolver();
	}

	@Override
	public JThreadManager getThreadManager() {
		return context.getThreadManager();
	}
}
