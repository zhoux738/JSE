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

package info.julang.interpretation.internal;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import info.julang.execution.Argument;
import info.julang.execution.Executable;
import info.julang.execution.MultiValueResult;
import info.julang.execution.Result;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.RuntimeCheckException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.expression.DelegatingExpression;
import info.julang.langspec.ast.JulianParser.ArgumentContext;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.AttrValue;
import info.julang.memory.value.DynamicValue;
import info.julang.memory.value.FuncValue;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.JValueBase;
import info.julang.memory.value.ObjectMember;
import info.julang.memory.value.ObjectValue;
import info.julang.parser.AstInfo;
import info.julang.typesystem.JType;
import info.julang.typesystem.JTypeKind;
import info.julang.typesystem.jclass.ConstructorForwardExecutable;
import info.julang.typesystem.jclass.ConstructorNotFoundException;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.JClassConstructorMember;
import info.julang.typesystem.jclass.JClassConstructorMember.ForwardInfo;
import info.julang.typesystem.jclass.JClassInitializerMember;
import info.julang.typesystem.jclass.JClassProperties;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JClassTypeUtil;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.builtin.JAttributeBaseType;
import info.julang.typesystem.jclass.builtin.JAttributeType;
import info.julang.typesystem.jclass.builtin.JConstructorType;
import info.julang.typesystem.jclass.builtin.JDynamicType;
import info.julang.typesystem.jclass.builtin.JMethodType;
import info.julang.typesystem.jclass.builtin.JObjectType;

/**
 * A facade class that creates a new object of certain class. It allocates memory,
 * initializes the fields and calls constructor, either by matching the best one based
 * on the input arguments or using whatever is specified. See the overloaded implementations of 
 * <code>newObject</code> for more info.
 * <p>
 * When calling a constructor, it will first call forward constructor if there is any. A forward constructor 
 * refers to calling another constructor from either the same class (by <code>this</code>) or the parent class
 * (by <code>super</code>).
 * 
 * @author Ming Zhou
 */
public class NewObjExecutor {

	private ThreadRuntime rt;
	
	/**
	 * Create a new object creation executor.
	 * @param rt
	 */
	public NewObjExecutor(ThreadRuntime rt) {
		this.rt = rt;
	}
	
	/**
	 * Execute a stream which contains the argument list for an object <code>new</code> expression.
	 * 
	 * @param rt thread runtime
	 * @param context interpretation context
	 * @param stream a stream with contents being the argument list for constructor, in form of "<code>(Expr0, Expr1, ..., ExprN)</code>"
	 * @param type the type of instance to create. Must be a class type.
	 * @return An instance of the specified type, 
	 * allocated at heap memory referred by context and initialized along the entire inheritance hierarchy.
	 * If the type is {@link JAttributeType}, the returned value will have type of (in other words, is cast-able to) {@link AttrValue}; otherwise, the value
	 * is always an {@link ObjectValue}.
	 */
	public static ObjectValue createObject(
		ThreadRuntime rt, Context context, List<ArgumentContext> alist, JClassType type, AstInfo<? extends ParserRuleContext> ainfo){
		NewObjExecutor noe = new NewObjExecutor(rt);
		return noe.newObject(context, alist, type, ainfo);
	}
	
	/**
	 * Instantiate an object of given type using a list of argument ASTs which contains constructor argument list in Julian script.
	 * <p>
	 * Each argument AST contains an expression that should evaluate to a value. While this method is intended for script-initiated 
	 * object creation, sometimes the engine may synthesize a script snippet and call this to get the object.
	 * 
	 * @param context
	 * @param argAsts
	 * @param type
	 * @return the newly allocated and initialized object of the given type.
	 */
	public ObjectValue newObject(
		final Context context, final List<ArgumentContext> argAsts, JType type, final AstInfo<? extends ParserRuleContext> ainfo){
		IArgumentAdder adder = new IArgumentAdder(){

			@Override
			public void addMoreArgs(List<JValue> argList) {
				for (ArgumentContext ac : argAsts) {
					DelegatingExpression de = new DelegatingExpression(rt, ainfo.create(ac.expression()));
					JValue val = de.getResult(context);
					argList.add(val);
				}
			}
			
		};
		
		return newObject(context.getHeap(), type, adder);
	}
	
	/**
	 * Instantiate an object of given type using a list of argument values.
	 * <p>
	 * This is mainly used by engine internals.
	 * 
	 * @param mem Where to allocate the memory for the new object
	 * @param argAsts
	 * @param args The argument values
	 * @return the newly allocated and initialized object of the given type.
	 */
	public ObjectValue newObject(final MemoryArea mem, JType type, final JValue[] args){
		IArgumentAdder adder = new IArgumentAdder(){

			@Override
			public void addMoreArgs(List<JValue> argList) {
				for(JValue v : args) {
					argList.add(v);
				}
			}
			
		};
		
		return newObject(mem, type, adder);
	}
	
	private static interface IArgumentAdder {
		void addMoreArgs(List<JValue> args);
	}
	
	private ObjectValue newObject(MemoryArea mem, JType type, IArgumentAdder argAdder){
			if(type.getKind() != JTypeKind.CLASS){
				throw new JSEError("Cannot instantite a non-class type.");
			}
			
			// Create new instance in heap memory
			ObjectValue obj = allocateObject(mem, type);
			
			JClassType jcType = (JClassType) type;
			JClassConstructorMember[] jctors = jcType.getClassConstructors();
				
			// Evaluate arguments
			List<JValue> argList = new ArrayList<JValue>();
			argList.add(obj);
			
			argAdder.addMoreArgs(argList);
			
			// Find constructor
			JValue[] argArray = argList.toArray(new JValue[0]);
			JClassConstructorMember ctor = JClassTypeUtil.findConstructors(
				jctors, argList != null ? argList.toArray(new JValue[0]) : new JValue[0], true);
			if(ctor != null){
				Argument[] args = prepareArguments(ctor, argArray);
				try {
					invokeConstructor(rt, jcType, ctor, args, obj, null);
				} catch (EngineInvocationError e) {
					throw new JSEError(
						"An error occurs while invoking constructor of class " + type.getName());
				}
			} else {
				throw new ConstructorNotFoundException(jcType, null);
			}
			
			return obj;
		}
	
	/**
	 * Instantiate a new object with specified constructor and argument list. 
	 * <p>
	 * This is reserved for internal use. 
	 * 
	 * @param jcType type of the class
	 * @param ctor the constructor of that class
	 * @param args doesn't contain the object being created (<i>this</i>).
	 * @return
	 */
	public ObjectValue newObjectInternal(JClassType jcType, JClassConstructorMember ctor, Argument[] args){
		// Create new instance in heap memory
		ObjectValue obj = allocateObject(rt.getHeap(), jcType);
		
		Argument[] args1 = new Argument[args.length + 1];
		args1[0] = Argument.CreateThisArgument(obj);
		for(int i=1;i<args1.length;i++){
			args1[i] = args[i-1];
		}
		
		try {
			invokeConstructor(rt, jcType, ctor, args1, obj, null);
		} catch (EngineInvocationError e) {
			throw new JSEError(
				"An error occurs while invoking constructor of class " + jcType.getName());
		}
		
		return obj;
	}
	
	/**
	 * Invoke a constructor in 3 steps:
	 * (1) if there is a forward call (this/super), call it first, recursively by this method
	 * (2) if this is the first time a constructor of this class is called, call all the initializers
	 * (3) execute the main body of constructor
	 * 
	 * @param type Type of the class
	 * @param ctor The constructor member definition
	 * @param args The argument list
	 * @param obj The object against which the constructor is called
	 * @throws EngineInvocationError
	 */
	private void invokeConstructor(
		ThreadRuntime rt,
		JClassType type,
		JClassConstructorMember ctor, 
		Argument[] args,
		ObjectValue obj,
		List<JClassType> typeChain) 
		throws EngineInvocationError{

		boolean firstCallToThisClass = true;
		ForwardInfo finfo = ctor.getForwardInfo();
		if(finfo != null){
			// If the forward is for parent, it means this is the first time a constructor of this class is called.
			firstCallToThisClass = finfo.isSuper();
			
			// (1) We have a forward call coming before main body
			JClassType ftype = finfo.isSuper() ? type.getParent() : type;
			
			if(ftype == null){
				throw new JSEError(
					"Trying to make a call to the constructor of parent class, but no parent class is defined.");
			}
			
			// Evaluate argument expression for forward call.
			ConstructorForwardExecutable cfexe = finfo.getExecutable();
			MultiValueResult mvResult = (MultiValueResult) cfexe.execute(rt, FuncValue.DUMMY, args);
			
			// Find the best matching constructor.
			JValue[] mvVals = mvResult.getReturnedValues();
			JValue[] values = new JValue[mvVals.length + 1];
			// Demote runtime type to its parent type.
			//obj.setRuntimeType(ftype);
			values[0] = obj; // set "this" argument
			for(int i = 0; i < mvVals.length; i++){
				values[i+1] = mvVals[i];
			}
			JClassConstructorMember fcCtor = JClassTypeUtil.findConstructors(
				ftype.getClassConstructors(), values, false);
			
			if (typeChain == null){
				typeChain = new LinkedList<JClassType>();
				typeChain.add(type);
			}
			
			callForwardCtor(rt, obj, ftype, values, fcCtor, typeChain);
			
			// Restore runtime type
			//obj.resetRuntimeType();
		} else {
			// If this ctor doesn't have a forward call, but has a non-Object parent class, we must call that class' param-less ctor. 
			JClassType ptyp = type.getParent(); 
			if (ptyp != null && ptyp != JObjectType.getInstance() && ptyp != JAttributeBaseType.getInstance()){
				JValue[] fargs = new JValue[]{ obj };
				JClassConstructorMember fcCtor = JClassTypeUtil.findConstructors(
					ptyp.getClassConstructors(), fargs, false);
				
				if (typeChain == null){
					typeChain = new LinkedList<JClassType>();
					typeChain.add(type);
				}
				
				callForwardCtor(rt, obj, ptyp, fargs, fcCtor, typeChain);
			}
		}
		
		if(firstCallToThisClass){
			JClassInitializerMember[] initializers = type.getClassInitializers(false);
			// (2) Call each initializer in its declared order
			if(initializers.length > 0){
				Argument[] initArgs = Argument.CreateThisOnlyArguments(obj);
				for(JClassInitializerMember initializer : initializers){
					JMethodType mtype = initializer.getMethodType();
					try {
						Result res = mtype.getExecutable().execute(rt, FuncValue.DUMMY, initArgs);
						JValue val = res.getReturnedValue(false);
						
						String name = initializer.getFieldName();
						ICompoundType typ = initializer.getDefiningType();
						ObjectMember om = obj.getMemberValueByClass(name, typ, true).getFirst();
						JValue target = om.getValue();
						
						// If the value is a const, temporarily enable mutation
						JValueBase jvb = null;
						try {
							if (target.isConst()) {
								jvb = (JValueBase)target;
								jvb.setConst(false);
							}
							
							val.assignTo(target);
						} finally {
							if (jvb != null) {
								jvb.setConst(true);
							}
						}
					} catch (EngineInvocationError e) {
						throw new JSEError(
							"An error occurs while invoking initializer for field " + 
							initializer.getFieldName() + " of class " + type.getName());
					}
				}			
			}
		}
		
		// (3) Execute the main body
		JConstructorType ctorTyp = ctor.getCtorType();
		Executable exe = (Executable) ctorTyp.getExecutable();
		try {
			exe.execute(rt, FuncValue.DUMMY, args);
		} catch (JulianScriptException jse){
			// Capture JSE (step 2/2):
			// At this point we have method's name and parameter information, so we can add a stack trace into the exception.
			String fn = jse.getFileName();
			int lineNo = jse.getLineNumber();
			jse.addStackTrace(rt.getTypeTable(), ctorTyp.getName(), JParameter.getParamNames(ctor.getCtorType().getParams()), fn, lineNo);
			throw jse;
		} 
	}

	private void callForwardCtor(
		ThreadRuntime rt, ObjectValue obj, JClassType ftype, JValue[] args, 
		JClassConstructorMember fcCtor, List<JClassType> typeChain) 
		throws EngineInvocationError {
		// Cannot find a target constructor to call
		if(fcCtor == null){
			throw new ConstructorNotFoundException(ftype, typeChain);
		}
		
		// Prepare arguments
		Argument[] fcArgs = prepareArguments(fcCtor, args);
		
		// Call the target constructor, recursively
		invokeConstructor(rt, ftype, fcCtor, fcArgs, obj, typeChain);
	}
	
	private Argument[] prepareArguments(JClassConstructorMember fcCtor, JValue[] values){
		JConstructorType fcCtorTyp = fcCtor.getCtorType();
		JParameter[] params = fcCtorTyp.getParams();
		Argument[] fcArgs = new Argument[values.length];
		for(int i=0; i<fcArgs.length; i++){
			fcArgs[i] = new Argument(params[i].getName(), values[i]);
		}
		
		FuncCallExecutor.checkArgTypes(fcCtorTyp.getName(), fcArgs, params);
		
		return fcArgs;
	}
	
	private ObjectValue allocateObject(MemoryArea heap, JType type){
		// Create new instance in heap memory
		ObjectValue obj = null;
		
		JClassType jat = JClassTypeUtil.isDerivingFrom(type, JAttributeBaseType.getInstance(), true);
		if(jat != null){
			if (jat instanceof JAttributeType) {
				obj = new AttrValue(heap, (JAttributeType) jat);
			} else {
				throw new RuntimeCheckException("Cannot instantiate Attribute class.");
			}
		} else {
			jat = (JClassType) type;
			JClassProperties props = jat.getClassProperties();
			if(props.isAbstract()){
				throw new RuntimeCheckException("Cannot instantiate an abstract class: " + jat.getName());
			}
			if(props.isHosted()){
				obj = new HostedValue(heap, type);
			} else if (JDynamicType.isDynamicType(jat)) {
				obj = new DynamicValue(heap, type);
			} else {
				obj = new ObjectValue(heap, type, false);
			}
		}
		
		return obj;
	}
}
