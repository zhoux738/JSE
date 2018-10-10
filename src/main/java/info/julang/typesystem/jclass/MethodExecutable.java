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

package info.julang.typesystem.jclass;

import info.julang.execution.Argument;
import info.julang.execution.namespace.NamespacePool;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.symboltable.IVariableTable;
import info.julang.execution.threading.JThread;
import info.julang.execution.threading.JThreadManager;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.InterpretedExecutable;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.context.ExecutionContextType;
import info.julang.interpretation.context.MethodContext;
import info.julang.interpretation.statement.StatementOption;
import info.julang.memory.MemoryArea;
import info.julang.modulesystem.IModuleManager;
import info.julang.parser.AstInfo;
import info.julang.typesystem.JType;
import info.julang.typesystem.loading.InternalTypeResolver;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 * The executable for method invocation.
 * <p/>
 * The method executable takes care of a couple of extra things than function executable:
 * <li>The namespace pool is fixed and shared among all the methods of the same class.</li>
 * <li>The treatment for <b>this</b> keyword</li> (TODO - maybe not done here, but rather passed along as 1st arg with name = "this")
 * <li>The treatment for <b>super</b> keyword</li>
 * <br/><br/>
 * @author Ming Zhou
 */
public class MethodExecutable extends InterpretedExecutable implements Cloneable {

	private NamespacePool nsPool;
	
	private ICompoundType containingType;
	
	private boolean isStatic;
	
	private void copyFrom(MethodExecutable ie){
		super.copyFrom(ie);
		this.nsPool = ie.nsPool;
		this.containingType = ie.containingType;
		this.isStatic = ie.isStatic;
		this.ast = ie.ast;
	}
	
	@Override
	public MethodExecutable clone(){
		MethodExecutable me = new MethodExecutable();
		me.copyFrom(this);
		return me;
	}
	
	private MethodExecutable(){
		super(null, false, false);
	}
	
	public MethodExecutable(AstInfo<? extends ParserRuleContext> ast, ICompoundType ofType, boolean isStatic) {
		super(ast, false, true);
		containingType = ofType;
		// Load shared namespace
		nsPool = containingType.getNamespacePool();
		this.isStatic = isStatic;
	}
	
	public boolean isStatic(){
		return isStatic;
	}
	
	@Override
	protected void preExecute(ThreadRuntime runtime, StatementOption option, Argument[] args){
		super.preExecute(runtime, option, args);
		runtime.getThreadStack().setNamespacePool(nsPool);
	}
	
	@Override
	protected Context prepareContext(
		MemoryArea frame, 
		MemoryArea heap,
		IVariableTable varTable, 
		ITypeTable typTable, 
		InternalTypeResolver typResolver,
		IModuleManager mm,
		NamespacePool namespaces,
		JThreadManager tm,
		JThread jthread){
		return new MethodContext(
			frame, heap, varTable, typTable, typResolver, mm, namespaces, tm, jthread, 
			containingType, isStatic, ExecutionContextType.InMethodBody); 
			// Technically setting exe-context type to InMethodBody is not correct. We should
			// deduce this value from the context/runtime. But it's fine so far because 
			// calling a method on any un-vetted types within an annotation expression (the 
			// other exe-context type) would be forbidden in the first place. 
	}
	
	//---------------------------- IStackFrameInfo ----------------------------//

	@Override
	public JType getContainingType() {
		return this.containingType;
	}
}
