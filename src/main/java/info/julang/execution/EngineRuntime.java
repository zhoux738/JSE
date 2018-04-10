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

package info.julang.execution;

import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.symboltable.IVariableTable;
import info.julang.execution.threading.JThreadManager;
import info.julang.external.interfaces.IExtEngineRuntime;
import info.julang.memory.MemoryArea;
import info.julang.modulesystem.IModuleManager;
import info.julang.typesystem.loading.InternalTypeResolver;

/**
 * EngineRuntime represents the global runtime context of an script engine. The context provides 
 * interface, among others, for getting access to engine's various global memory areas.
 * <p/>
 * Note thread-wise storage, such as stack, is accessible from {@link info.julang.execution.threading.ThreadRuntime ThreadRuntime}.
 *
 * @author Ming Zhou
 */
public interface EngineRuntime extends IExtEngineRuntime {
	
	/**
	 * Get heap area of the script engine.
	 * 
	 * @return
	 */
	MemoryArea getHeap();
	
	/**
	 * Get global type table.
	 * 
	 * @return
	 */
	ITypeTable getTypeTable();
	
	/**
	 * Get default type resolver.
	 * @return
	 */
	InternalTypeResolver getTypeResolver();
	
	/**
	 * Get global variable table.
	 * 
	 * @return
	 */
	IVariableTable getGlobalVariableTable();
	
	/**
	 * Get module manager.
	 * 
	 * @return
	 */
	IModuleManager getModuleManager();
	
	/**
	 * Get thread manager.
	 * 
	 * @return
	 */
	JThreadManager getThreadManager();
}
