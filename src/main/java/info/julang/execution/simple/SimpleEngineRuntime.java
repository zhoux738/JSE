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

package info.julang.execution.simple;

import info.julang.execution.EngineRuntime;
import info.julang.execution.StandardIO;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.symboltable.IVariableTable;
import info.julang.execution.symboltable.TypeTable;
import info.julang.execution.symboltable.VariableTable;
import info.julang.execution.threading.JThreadManager;
import info.julang.external.EngineFactory;
import info.julang.external.interfaces.IExtMemoryArea;
import info.julang.external.interfaces.IExtModuleManager;
import info.julang.external.interfaces.IExtTypeTable;
import info.julang.external.interfaces.IExtVariableTable;
import info.julang.memory.HeapArea;
import info.julang.memory.MemoryArea;
import info.julang.memory.simple.SimpleHeapArea;
import info.julang.modulesystem.IModuleManager;
import info.julang.modulesystem.ModuleManager;
import info.julang.typesystem.loading.InternalTypeResolver;

public class SimpleEngineRuntime implements EngineRuntime {

	private MemoryArea heap;
	private ITypeTable tt;
	private InternalTypeResolver tr;
	private IModuleManager mm;
	protected IVariableTable gvt;
	private JThreadManager tm;
	private StandardIO io;
	
	private SimpleEngineRuntime(
		MemoryArea heap, IVariableTable gvt, ITypeTable tt, IModuleManager mm, InternalTypeResolver tr, JThreadManager tm) {
		this.heap = heap;
		this.gvt = gvt;
		this.tt = tt;
		this.mm = mm;
		this.tr = tr;
		this.tm = tm;
		this.io = new StandardIO();
	}
	
	/**
	 * [CFOW]
	 * 
	 * A special constructor to be only used by {@link EngineFactory}. The parameters nominally take IExt* types,
	 * but in fact they must be instances of certain subclasses.
	 * 
	 * @param heap Must be {@link MemoryArea}
	 * @param gvt  Must be {@link IVariableTable}
	 * @param tt   Must be {@link ITypeTable}
	 * @param mm   Must be {@link IModuleManager}
	 */
	public SimpleEngineRuntime(IExtMemoryArea heap, IExtVariableTable gvt, IExtTypeTable tt, IExtModuleManager mm) {
		this((MemoryArea)heap, (IVariableTable)gvt, (ITypeTable)tt, (IModuleManager)mm);
	}
	
	public SimpleEngineRuntime(MemoryArea heap, IVariableTable gvt, ITypeTable tt, IModuleManager mm) {
		this(heap, gvt, tt, mm, new InternalTypeResolver(), new JThreadManager());
	}
	
	public SimpleEngineRuntime(MemoryArea heap, IVariableTable gvt, IModuleManager mm) {
		this(heap, gvt, new TypeTable(heap), mm);
	}
	
	public static SimpleEngineRuntime createDefault(){
		HeapArea heap = new SimpleHeapArea();
		return new SimpleEngineRuntime(
			heap, new VariableTable(null), new TypeTable(heap), new ModuleManager());
	}

	@Override
	public MemoryArea getHeap() {
		return heap;
	}

	@Override
	public ITypeTable getTypeTable() {
		return tt;
	}
	
	@Override
	public IVariableTable getGlobalVariableTable() {
		return gvt;
	}

	@Override
	public IModuleManager getModuleManager() {
		return mm;
	}

	@Override
	public InternalTypeResolver getTypeResolver() {
		return tr;
	}

	@Override
	public JThreadManager getThreadManager() {
		return tm;
	}

	@Override
	public StandardIO getStandardIO() {
		return io;
	}
	
	public void setStandardIO(StandardIO io) {
		this.io = io;
	}

}
