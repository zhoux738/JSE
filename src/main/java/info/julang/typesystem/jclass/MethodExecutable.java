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

import java.util.concurrent.ConcurrentHashMap;

import org.antlr.v4.runtime.ParserRuleContext;

import info.julang.execution.Argument;
import info.julang.execution.StandardIO;
import info.julang.execution.namespace.NamespacePool;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.symboltable.IVariableTable;
import info.julang.execution.threading.JThread;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.InterpretedExecutable;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.context.ExecutionContextType;
import info.julang.interpretation.context.MethodContext;
import info.julang.interpretation.resolving.IMemberNameResolver;
import info.julang.interpretation.statement.StatementOption;
import info.julang.langspec.Keywords;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.IFuncValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.JValueBase;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.VoidValue;
import info.julang.modulesystem.IModuleManager;
import info.julang.parser.AstInfo;
import info.julang.typesystem.JType;
import info.julang.typesystem.loading.InternalTypeResolver;

/**
 * The executable for method invocation.
 * <p>
 * The method executable takes care of a couple of extra things than function executable:
 * <ul>
 * <li>The namespace pool is fixed and shared among all the methods of the same class.</li>
 * <li>The treatment for <b>this</b> keyword</li>
 * <li>The treatment for <b>super</b> keyword</li>
 * </ul>
 * 
 * @author Ming Zhou
 */
// Implementation Notes:
// The treatment for this keyword may be not done here, but rather passed along as 1st arg with name = "this"
public class MethodExecutable extends InterpretedExecutable implements Cloneable {
	
	private static class MethodMemberNameResolver implements IMemberNameResolver {
		
		private ConcurrentHashMap<String, JValue> resolved; 

		@Override
		public JValue resolve(String id) {
			if (resolved == null) {
				initialize();
			}
			
			return resolved.get(id);
		}

		@Override
		public void save(String id, JValue val) {
			if (resolved == null) {
				initialize();
			}
			
			resolved.put(id, val == null ? VoidValue.DEFAULT : val);
		}
		
		private synchronized void initialize() {
			if (resolved == null) {
				resolved = new ConcurrentHashMap<String, JValue>();
			}
		}
		
	}

	private NamespacePool nsPool;
	
	private ICompoundType containingType;
	
	private boolean isStatic;
	
	private MethodMemberNameResolver staticResolver;
	
	private void copyFrom(MethodExecutable ie){
		super.copyFrom(ie);
		this.nsPool = ie.nsPool;
		this.containingType = ie.containingType;
		this.isStatic = ie.isStatic;
		this.ast = ie.ast;
		this.staticResolver = ie.staticResolver;
	}
	
	@Override
	public MethodExecutable clone(){
		MethodExecutable me = new MethodExecutable();
		me.copyFrom(this);
		return me;
	}
	
	private MethodExecutable(){
		super(null, null, false, false);
	}
	
	public MethodExecutable(String name, AstInfo<? extends ParserRuleContext> ast, ICompoundType ofType, boolean isStatic) {
		super(name, ast, false, true);
		containingType = ofType;
		// Load shared namespace
		nsPool = containingType.getNamespacePool();
		this.isStatic = isStatic;
		if (isStatic) {
			this.staticResolver = new MethodMemberNameResolver();
		}
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
		IFuncValue func,
		MemoryArea frame, 
		MemoryArea heap,
		IVariableTable varTable, 
		ITypeTable typTable, 
		InternalTypeResolver typResolver,
		IModuleManager mm,
		NamespacePool namespaces,
		StandardIO io,
		JThread jthread){
		// If it's a static method, we can reuse the resolver. For instance method, must create a new resolver for each call, 
		// because the instance members are held in distinct storage.
		MethodMemberNameResolver mres = isStatic ? this.staticResolver : new MethodMemberNameResolver();
		return new MethodContext(
			func, frame, heap, varTable, typTable, typResolver, mm, namespaces, io, jthread, 
			containingType, isStatic, ExecutionContextType.InMethodBody, mres); 
			// Technically setting exe-context type to InMethodBody is not correct. We should
			// deduce this value from the context/runtime. But it's fine so far because 
			// calling a method on any un-vetted types within an annotation expression (the 
			// other exe-context type) would be forbidden in the first place. 
	}
	
	@Override
	protected void prepareArguments(Argument[] args, Context ctxt, IFuncValue func) {
		IVariableTable varTable = ctxt.getVarTable();
		
		// Add the first argument
		if (args.length > 0) {
			Argument firstArg = args[0];
			String firstName = firstArg.getName();
			JValue firstVal = firstArg.getValue();
			
			if (Keywords.THIS.equals(firstName) && this.isStatic) { // TODO: Consider always creating reference wrapping this value.
				// Special treatment for 'this' arg in extension method (which is static):
				// Wrap it in a ref value stored in the current frame
				JValue firstValDeref = firstVal.deref();
				JValueBase val = new RefValue(
					ctxt.getFrame(),
					(ObjectValue)firstValDeref,
					(ICompoundType)firstValDeref.getType());
				
				firstVal = val;
			}
			
			varTable.addVariable(firstName, firstVal);
		}
		
		// Add the rest arguments
		for(int i = 1; i < args.length; i++){
			Argument arg = args[i];
			varTable.addVariable(arg.getName(), arg.getValue());
		}
		
		super.addLocalBindings(ctxt, func);
	}
	
	//---------------------------- IStackFrameInfo ----------------------------//

	@Override
	public JType getContainingType() {
		return this.containingType;
	}
}
