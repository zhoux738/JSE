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

package info.julang.typesystem.jclass.builtin;

import info.julang.execution.Argument;
import info.julang.execution.ArgumentUtil;
import info.julang.execution.Executable;
import info.julang.execution.Result;
import info.julang.execution.namespace.NamespacePool;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.exceptions.JSEError;
import info.julang.hosting.HostedExecutable;
import info.julang.memory.value.FuncValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.modulesystem.naming.FQName;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.BuiltinTypes;
import info.julang.typesystem.JType;
import info.julang.typesystem.VoidType;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.BuiltinTypeBootstrapper.TypeFarm;
import info.julang.typesystem.jclass.ExecutableType;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JClassTypeBuilder;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.JReturn;
import info.julang.typesystem.jclass.TypeBootstrapper;
import info.julang.typesystem.jclass.builtin.JObjectType.MethodNames;
import info.julang.typesystem.jclass.builtin.doc.JulianDoc;

/**
 * The Function type is for function defined in script.
 * 
 * @author Ming Zhou
 */
@JulianDoc(
name = "Function",
summary = 
    "/*"
+ "\n * Function is the base class for all global functions, methods and lambdas defined in Julian."
+ "\n * "
+ "\n * Function has the first-class property in Julian. Like all other classes, it inherits [Object]. What"
+ "\n * sets it apart from other classes is the ability to execute. To invoke a function, use \"()\" grammar"
+ "\n * with argument expressions listed between ( and ). For example, "
+ "\n * [code]"
+ "\n * fun(1, \"a\", someVal);"
+ "\n * [code: end]"
+ "\n * "
+ "\n * Since function is first-class instance, it can be assigned and passed around:"
+ "\n * [code]"
+ "\n * void fun(){};"
+ "\n * var f = fun;"
+ "\n * f();"
+ "\n * [code: end]"
+ "\n * "
+ "\n * The same can be done for methods (both instance and static) and lambdas."
+ "\n * "
+ "\n * In general, Function in Julian doesn't enforce a strong typing. When a Function type is declared somewhere,"
+ "\n * a function instance of any given signature can be passed in. By the time a function is invoked, the arguments"
+ "\n * will be checked strickly, and when the function returns, the return value's type will also be checked against"
+ "\n * the declared type. This behavior can be relaxed a little by [dynamic invocation](#invoke)."
+ "\n * "
+ "\n * This class is neither instantiable nor extensible. For more detailed description on Function, "
+ "\n * see [tutorial on Function](tutorial: function)."
+ "\n */"
)
public class JFunctionType extends JClassType implements ExecutableType {
	
	public static FQName FQNAME = new FQName("Function");
	public static String MethodName_invoke = "invoke";
	
	public static BoostrapingBuilder PrototypeBuilder = new BoostrapingBuilder();
	
	/**
	 * The default function type is used mainly for typing operations such as type comparison.
	 * Its parameter list is empty, the executable a NOP, and returns nothing.
	 */
	public static JFunctionType DEFAULT = PrototypeBuilder.providePrototype();
	
	public static class BoostrapingBuilder implements TypeBootstrapper {
		
		private JFunctionType proto;
		
		@Override
		public JFunctionType providePrototype(){
			if(proto == null){
				proto = new JFunctionType("<Function>", new JParameter[0], VoidType.getInstance(), new Executable(){
					@Override
					public Result execute(ThreadRuntime runtime, Argument[] args) throws EngineInvocationError {
						return Result.Void;
					}
				});
			}
			return proto;
		}
		
		@Override
		public void implementItself(JClassTypeBuilder builder, TypeFarm farm){
			//Parent
			builder.setParent(farm.getStub(BuiltinTypes.OBJECT));
			
			//Disallow inheritance from this class in script
			builder.setFinal(true);
			
			//Field
			
			//TODO: add methods in future after the type system is complete
			/*
			 * getType
			 * getParameters()
			 * getReturnType()
			 */
			
			//Methods
			
			JClassType functionType = farm.getStub(BuiltinTypes.FUNCTION);
			JClassType stringType = farm.getStub(BuiltinTypes.STRING);
			
			//toString
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), 
					MethodNames.toString.name(), Accessibility.PUBLIC, false, false,
					new JMethodType(
						MethodNames.toString.name(),
						new JParameter[]{
							new JParameter("this", functionType)
						}, 
						stringType, 
					    METHOD_toString, 
					    functionType),
				    null));
			
			//Function.invoke
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), 
					MethodName_invoke, Accessibility.PUBLIC, false, false,
					new JMethodType(
						MethodName_invoke,
						new JParameter[0], 
						AnyType.getInstance(), 
						METHOD_invoke, 
						functionType),
				    null));
		}
		
		@Override
		public void boostrapItself(JClassTypeBuilder builder){
			// Do nothing
		}

		@Override
		public String getTypeName() {
			return "Function";
		}
		
		@Override
		public boolean initiateArrayType() {
			return false;
		}
	}
	
	// toString() : string
	private static HostedExecutable METHOD_toString = new HostedExecutable(FQNAME, MethodNames.toString.name()) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			//TODO: method like this should delegate to its Type's corresponding members in future after the type system is complete
			
			FuncValue thisVal = ArgumentUtil.<FuncValue>getThisValue(args);
			JFunctionType jft = (JFunctionType)thisVal.getType();
			
			StringBuilder sb = new StringBuilder(jft.getName());
			sb.append("(");
			
			JParameter[] params = jft.getParams();
			int last = params.length - 1;

			for(int i = 0; i < last; i++){
				JParameter p = params[i];
				sb.append(p.getType().getName());
				sb.append(" ");
				sb.append(p.getName());
				sb.append(", ");
			}
			
			if (last >= 0){
				JParameter p = params[last];
				sb.append(p.getType().getName());
				sb.append(" ");
				sb.append(p.getName());
			}
			
			sb.append(")");
			
			return new Result(TempValueFactory.createTempStringValue(sb.toString()));
		}
	};
	
	// invoke
	// (SPECIAL - CallFuncOp has special handling for this member. So no logic is implemented here. This method should never be called.)
	@JulianDoc(
		name = "invoke",
		summary =   "/*"
				+ "\n * Invoke the function dynamically."
				+ "\n * "
				+ "\n * When calling a function using Julian's built-in grammar, namely in the form of \"fun(a, b)\","
				+ "\n * the arguments are checked for each parameter's type at corresponding position, and the count"
				+ "\n * of arguments are also checked against that of parameters."
				+ "\n * "
				+ "\n * When invoking a function dynamically, however, the count of arguments are not checked. If the"
				+ "\n * arguments are less than required, the missing ones will be initialized with the default value"
				+ "\n * of the declared parameeter type. In the contrast, if the arguments are more than required, the"
				+ "\n * excessive one will be discarded. For the arguments that fall into the range of paramters, their"
				+ "\n * types are still checked as in the normal calling procedure."
				+ "\n * "
				+ "\n * Another property of dynamic invocation is that if the function doesn't return a value (void), "
				+ "\n * a null value will be returned instead."
				+ "\n */",
		params = { "The same arguments that would be passed to the function when calling it normally, except the count"
				+ " of arguments doesn't have to be the same as that of parameters. The notation for this parameter's type" 
				+ " should not be taked literately, as Julian doesn't support array type of [Any]." 
				},
		returns = "The same value the function would return when being called normally, except if the function returns"
				+ " nothing (void), a null will be returned instead."
	)
	private static HostedExecutable METHOD_invoke = new HostedExecutable(FQNAME, MethodName_invoke) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			throw new JSEError("Calling invoke() on platform.");
		}
	};
	
	private JParameter[] params;
	
	protected JReturn ret;
	
	private Executable executable;
	
	public JParameter[] getParams() {
		return params;
	}

	public JReturn getReturn() {
		return ret;
	}
	
	public JType getReturnType() {
		return ret.getReturnType();
	}

	@Override
	public Executable getExecutable() {
		return executable;
	}
	
	public boolean isTyped(){
		return true;
	}

	/**
	 * Create a new Function type.
	 * 
	 * @param name function's name
	 * @param params the parameter list
	 * @param returnType the return type
	 * @param executable an {@link Executable executable} to run when invoking this function 
	 */
	protected JFunctionType(String name, JParameter[] params, JType returnType, Executable executable) {
		this(name, params, returnType, null, executable);
	}
	
	/**
	 * Create a new Function type with given namespace pool.
	 *  
	 * @param name
	 * @param params
	 * @param returnType
	 * @param nsPool
	 * @param executable
	 */
	public JFunctionType(String name, JParameter[] params, JType returnType, NamespacePool nsPool, Executable executable) {
		super(name, DEFAULT, null); //set parent to be default function (used to be JObjectType.getInstance())
		this.params = params;
		if(returnType != null && returnType != AnyType.getInstance()){
			this.ret = new JReturn(returnType);
		} else {
			this.ret = JReturn.UntypedReturn;
		}
		this.executable = executable;
		if (nsPool != null){
			this.setNamespacePool(nsPool);
		}
	}
	
	public FunctionKind getFunctionKind(){
		return FunctionKind.FUNCTION;
	}
	
	//--------------- Object (for debugging) ---------------//
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder(super.toString());
		
		JParameter[] params = getParams();
		int len = params.length - 1;
		sb.append("( ");
		for(int i = 0; i < len; i++){
			JParameter jp = params[i];
			sb.append(jp.toString());
			sb.append(", ");
		}
		if (len > 0){
			sb.append(params[len]);
		}
		sb.append(" )");
		
		return sb.toString();
	}
}
