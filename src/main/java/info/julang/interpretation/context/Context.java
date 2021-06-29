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

import info.julang.execution.InContextTypeResolver;
import info.julang.execution.StandardIO;
import info.julang.execution.namespace.NamespacePool;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.symboltable.IVariableTable;
import info.julang.execution.symboltable.VariableTable;
import info.julang.execution.threading.JThread;
import info.julang.execution.threading.JThreadManager;
import info.julang.execution.threading.ThreadFrame;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.resolving.INameResolver;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.FuncValue;
import info.julang.modulesystem.IModuleManager;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.loading.ITypeResolver;
import info.julang.typesystem.loading.InternalTypeResolver;

/**
 * The runtime context in which a statement is interpreted. 
 * <p>
 * From the context one can get access to engine-wide (heap memory, module manager, type table) and 
 * thread-specific resources (variable table).
 * <p>
 * The context has its type, which can be {@link ContextType#FUNCTION Function}, {@link ContextType#IMETHOD Instance Method}, etc.
 * This type defines certain behaviors during the execution, such as how to resolve a name.
 *  
 * @author Ming Zhou
 */
public abstract class Context {

	private ContextType cntxTyp;
	
	private MemoryArea frame;

	private IVariableTable varTable;

	private ITypeTable typTable;

	private MemoryArea heap;
	
	private IModuleManager mm;
	
	private NamespacePool nsPool;
	
	private INameResolver nr;
	
	private InContextTypeResolver tr;
	
	private StandardIO io;
	
	private JThread jthread;

	private ExecutionContextType exeContextTyp;
	
	protected Context(
		ContextType cntxTyp,
		MemoryArea frame, 
		MemoryArea heap,
		IVariableTable varTable,
		ITypeTable typTable,
		InternalTypeResolver typResolver,
		IModuleManager mm,
		NamespacePool nsPool,
		INameResolver nameResolver,
		StandardIO io,
		JThread jthread,
		ExecutionContextType exeContextTyp) {
		this.cntxTyp = cntxTyp;
		this.frame = frame;
		this.varTable = varTable;
		this.typTable = typTable;
		this.tr = new InContextTypeResolver(this, typResolver);
		this.heap = heap;
		this.mm = mm;
		this.nsPool = nsPool;
		this.nr = nameResolver;
		this.io = io;
		this.jthread = jthread;
		this.exeContextTyp = exeContextTyp;
	}
	
	/**
	 * The type of this context.
	 */
	public ContextType getContextType(){
		return cntxTyp;
	}
	
	/**
	 * The memory for current frame.
	 */
	public MemoryArea getFrame() {
		return frame;
	}

	/**
	 * The variable table for current frame.
	 */
	public IVariableTable getVarTable() {
		return varTable;
	}
	
	/**
	 * The namespace pool for current execution context.
	 */
	public NamespacePool getNamespacePool() {
		return nsPool;
	}

	/**
	 * The global type table
	 */
	public ITypeTable getTypTable() {
		return typTable;
	}
	
	/**
	 * The current type resolver
	 */
	public ITypeResolver getTypeResolver() {
		return tr;
	}

	/**
	 * The global heap memory
	 */
	public MemoryArea getHeap() {
		return heap;
	}
	
	/**
	 * The global module manager
	 */
	public IModuleManager getModManager() {
		return mm;
	}
	
	/**
	 * The name resolver used to resolve an identifier.
	 */
	public INameResolver getResolver(){
		return nr;
	}
	
	InternalTypeResolver getInternalTypeResolver(){
		return tr.getInternalTypeResolver();
	}

	public JThreadManager getThreadManager() {
		return jthread.getThreadRuntime().getThreadManager();
	}

	public JThread getJThread() {
		return jthread;
	}
	
	public StandardIO getStandardIO() {
		return io;
	}
	
	/**
	 * Whether containing type associated with this context. This meaning of the returned value is subject to the interpretation
	 * of concrete class. This method can return null.
	 */
	public ICompoundType getContainingType(){
		return null;
	}
	
	/**
	 * The current type of execution context.
	 */
	public ExecutionContextType getExecutionContextType(){
		return exeContextTyp;
	}
	
	private static class SystemLoadingContext extends FunctionContext {

		SystemLoadingContext(ThreadRuntime rt, MemoryArea mem, IVariableTable vt) {
			super(
				/* Function value */         FuncValue.DUMMY,
				/* MemoryArea frame */       mem, 
				/* MemoryArea heap */        rt.getHeap(), 
				/* VariableTable varTable */ vt, 
				/* TypeTable typTable */     rt.getTypeTable(),
				/* InternalTypeResolver typResolver */ 
				                             rt.getTypeResolver(), 
				/* ModuleManager mm */       rt.getModuleManager(), 
				/* NamespacePool nsPool */   rt.getThreadStack().getNamespacePool(),
				/* StandardIO io */          rt.getStandardIO(),
				/* JThread jthread */        rt.getJThread());
		}
		
	}
	
	/**
	 * Create a new context using the given thread runtime. This context must be only 
	 * used for system loading purpose and not for calling user-defined classes.
	 * 
	 * @param rt thread runtime.
	 * @return
	 */
	public static Context createSystemLoadingContext(ThreadRuntime rt){
	    ThreadFrame frame = rt.getThreadStack().currentFrame();
	    if (frame == null) {
	        VariableTable vt = new VariableTable(rt.getGlobalVariableTable());
	        return new SystemLoadingContext(rt, rt.getStackMemory(), vt);
	    } else {
            return new SystemLoadingContext(rt, frame.getMemory(), frame.getVariableTable());
	    }
	}
}
