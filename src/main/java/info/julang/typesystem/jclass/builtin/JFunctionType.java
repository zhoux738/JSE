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
import info.julang.interpretation.context.Context;
import info.julang.interpretation.internal.NewObjExecutor;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.ArrayValueFactory;
import info.julang.memory.value.FuncValue;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.memory.value.TypeValue;
import info.julang.modulesystem.naming.FQName;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.BuiltinTypes;
import info.julang.typesystem.JType;
import info.julang.typesystem.VoidType;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.BuiltinTypeBootstrapper.TypeFarm;
import info.julang.typesystem.jclass.ExecutableType;
import info.julang.typesystem.jclass.ICompoundTypeBuilder;
import info.julang.typesystem.jclass.JClassConstructorMember;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JClassTypeBuilder;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.JReturn;
import info.julang.typesystem.jclass.MethodExecutable;
import info.julang.typesystem.jclass.TypeBootstrapper;
import info.julang.typesystem.jclass.builtin.JObjectType.MethodNames;
import info.julang.typesystem.jclass.builtin.doc.JulianDoc;
import info.julang.typesystem.jclass.jufc.SystemTypeUtility;
import info.julang.typesystem.jclass.jufc.System.ScriptType;
import info.julang.typesystem.jclass.jufc.System.Reflection.ScriptParam;

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
public class JFunctionType extends JClassType implements ExecutableType, IDeferredBuildable {
	
	public static FQName FQNAME = new FQName("Function");
	public static String MethodName_invoke = "invoke";
	public static String MethodName_getReturnType = "getReturnType";
	public static String MethodName_getParameters = "getParameters";
	public static String MethodName_getFunctionKind = "getFunctionKind";
	
	private static String System_Reflection_FunctionKind = "System.Reflection.FunctionKind";
	
	public static BoostrapingBuilder PrototypeBuilder = new BoostrapingBuilder();
	
	private static JFunctionType DEFAULT = null;
	
	public static JFunctionType getInstance() {
		return DEFAULT;
	}

	/**
	 * The default function type is used mainly for typing operations such as type comparison.
	 * Its parameter list is empty, the executable a NOP, and returns nothing.
	 */
	private static JFunctionType PROTO = PrototypeBuilder.providePrototype();
	
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
			
			/*
			 * The following are added upon build completion:
			 *   - getParameters()
			 *   - getReturnType()
			 */
		}
		
		@Override
		public void bootstrapItself(JClassTypeBuilder builder){
			if(DEFAULT == null){
				JFunctionType jabt = (JFunctionType) builder.build(false);
				jabt.setBuilder(builder);
				DEFAULT = jabt;
			}
		}
		
		@Override
		public void reset() {
			proto = null;
			DEFAULT = null;
			PROTO = this.providePrototype();
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
	
	// getFunctionKind()
	@JulianDoc(
		name = "getFunctionKind",
		summary =   "/*"
				+ "\n * Get the kind of this function."
				+ "\n */",
		params = { },
		returns = "An [enum](System.Reflection.FunctionKind) value representing the kind of the function."
	)
	private static HostedExecutable METHOD_getFunctionKind = new HostedExecutable(FQNAME, MethodName_getFunctionKind) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// this value
			ObjectValue thisVal = ArgumentUtil.<ObjectValue>getThisValue(args);
			
			// => this function's type
			JFunctionType funcTyp = (JFunctionType)thisVal.getType();
			
			// => the (internal) function kind
			FunctionKind funcKind = funcTyp.getFunctionKind();
			
			// => System.Reflection.FunctionKind
			// String literals used below are based on the enum values defined in
			// /info/julang/typesystem/jclass/jufc/System/Reflection/FunctionKind.jul
			
			String enumName; // Do not initialize. Let the compiler analyze its initialization.
			switch(funcKind) {
			case FUNCTION:
				enumName = "GLOBAL";
				break;

			case LAMBDA:
				enumName = "LAMBDA";
				break;

			case CONSTRUCTOR:
				enumName = "CONSTRUCTOR";
				break;
				
			case METHOD:
				JMethodType jmt = (JMethodType)funcTyp;
				boolean sta = jmt.isHosted() ? jmt.getHostedExecutable().isStatic() : jmt.getMethodExecutable().isStatic();
				if (sta) {
					enumName = "STATIC_METHOD";
					break;
				} else {
					enumName = "INSTANCE_METHOD";
					break;
				}
				
			case METHOD_GROUP:
				JMethodGroupType jmgt = (JMethodGroupType)funcTyp;
				JMethodType[] metTyps = jmgt.getJMethodTypes();
				if (metTyps.length > 0) {
					MethodExecutable me = (MethodExecutable)metTyps[0].getExecutable();
					if (!me.isStatic()) {
						enumName = "INSTANCE_METHOD_GROUP";
						break;
					}
				}
				
				enumName = "STATIC_METHOD_GROUP";
				break;
				
			default:
				throw new JSEError("Unrecognized FunctionKind: " + funcKind);
			}
			
			// => the type value for the return type
			RefValue rv = SystemTypeUtility.createEnumValue(System_Reflection_FunctionKind, runtime, enumName);

			return new Result(rv);
		}
	};
	
	// getReturnType()
	@JulianDoc(
		name = "getReturnType",
		summary =   "/*"
				+ "\n * Get the return type for this function."
				+ "\n * "
				+ "\n * A lambda doesn't have an explicitly defined return type. So this method would return null."
				+ "\n */",
		params = { },
		returns = "An object representing the return type of the function."
	)
	private static HostedExecutable METHOD_getReturnType = new HostedExecutable(FQNAME, MethodName_getReturnType) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// this value
			ObjectValue thisVal = ArgumentUtil.<ObjectValue>getThisValue(args);
			
			// => this function's type
			JFunctionType funcTyp = (JFunctionType)thisVal.getType();
			
			// => the return type
			if (funcTyp.getFunctionKind() == FunctionKind.LAMBDA) {
				// Special: return null if it's a lambda.
				return new Result(RefValue.NULL);
			}
			
			JReturn ret = funcTyp.getReturn();
			JType retTyp = ret.isUntyped() ? AnyType.getInstance() : funcTyp.getReturnType();
			
			// => the type value for the return type
			TypeValue tv = runtime.getTypeTable().getValue(retTyp.getName());
			
			// Wrap in System.Type
			ObjectValue ov = tv.getScriptTypeObject(runtime);
			
			// Convert the result to Julian type
			return new Result(TempValueFactory.createTempRefValue(ov));
		}
	};
	
	// getParameters()
	@JulianDoc(
		name = "getParameters",
		summary =   "/*"
				+ "\n * Get the parameters of this function."
				+ "\n * "
				+ "\n * If it's an instance method, the resultant array would contain as the first parameter the type"
				+ "\n * of the instance's class, with the name of 'this'. If it's an extension method, the first element"
				+ "\n * will be of the extended class, also with the name of 'this'. This applies no matter how the"
				+ "\n * function object is obtained (either from an extended intance, such as ```Function f = ext.exfun;```,"
				+ "\n * or from the extension class directly, i.e. ```Function f = ExtClass.exfun;```)."
				+ "\n * "
				+ "\n * Since a method group contains multiple function objects differentiated exactly by the parameter"
				+ "\n * list, this method simply returns those for the first function. The caller is therefore advised"
				+ "\n * to call [getFunctionKind()](#getFunctionKind) to understand what the returned value means."
				+ "\n */",
		params = { },
		returns = "An array of [System.Parameter] representing the parameters of this function."
	)
	private static HostedExecutable METHOD_getParameters = new HostedExecutable(FQNAME, MethodName_getParameters) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// this value
			ObjectValue thisVal = ArgumentUtil.<ObjectValue>getThisValue(args);
			
			// => this function's type
			JFunctionType funcTyp = (JFunctionType)thisVal.getType();
			
			// => the params
			JParameter[] params;
			if (funcTyp.getFunctionKind() == FunctionKind.METHOD_GROUP) {
				params = ((JMethodGroupType)funcTyp).getJMethodTypes()[0].getParams();
			} else {
				params = funcTyp.getParams();
			}
			
			// => System.Reflection.Parameter[]
			JClassType systemReflectionParameter = (JClassType) SystemTypeUtility.ensureTypeBeLoaded(runtime, ScriptParam.FQCLASSNAME);
						
			ArrayValue av = ArrayValueFactory.createArrayValue(
				runtime.getStackMemory().currentFrame(), runtime.getTypeTable(), systemReflectionParameter, params.length);

			JClassConstructorMember paramCtor = systemReflectionParameter.getClassConstructors()[0];
			
			for (int i = 0; i < params.length; i++) {
				// Create a Julian object of type System.Reflection.Argument
				NewObjExecutor noe = new NewObjExecutor(runtime);
				ObjectValue ov = noe.newObjectInternal(systemReflectionParameter, paramCtor, new Argument[0]);

				// Create a native object for the Argument
				ScriptParam sp = new ScriptParam();
				sp.setParam(params[i]);
				
				// Bind the native object with the Julian object
				HostedValue hv = (HostedValue)ov;
				hv.setHostedObject(sp);
				
				// Set to the array member
				ov.assignTo(av.getValueAt(i));
			}
			
			// Convert the result to Julian type
			return new Result(av);
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
		super(name, PROTO, null); //set parent to be default function (used to be JObjectType.getInstance())
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
	
	/**
	 * Get a signature of this function, including name and param list.
	 */
	public String getSignature() {
		return getName() + "(" + JParameter.getSignature(this.getParams(), false) + ")";
	}
	
	//--------------- IDeferredBuildable ---------------//
	
	private ICompoundTypeBuilder builder;
	
	@Override
	public boolean deferBuild(){
		return true;
	}
	
	@Override
	public void completeBuild(Context context) {
		if (builder != null) {
			// System.Type
			JClassType systemType = (JClassType)context.getTypeResolver().resolveType(
				ParsedTypeName.makeFromFullName(ScriptType.FQCLASSNAME));
			
			// System.Reflection.Parameter
			JClassType systemReflectionParameter = (JClassType)context.getTypeResolver().resolveType(
				ParsedTypeName.makeFromFullName(ScriptParam.FQCLASSNAME));
			

			// System.Reflection.Parameter
			JClassType systemReflectionFunctionKind = (JClassType)context.getTypeResolver().resolveType(
				ParsedTypeName.makeFromFullName(System_Reflection_FunctionKind));
			
			// System.Reflection.Parameter[]
			JArrayType systemReflectionParameterArray = JArrayType.createJArrayType(
				context.getTypTable(), systemReflectionParameter, 1);
			
			// getFunctionKind()
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), MethodName_getFunctionKind, Accessibility.PUBLIC, false, false,
					new JMethodType(
						MethodName_getFunctionKind,
						new JParameter[]{
							new JParameter("this", builder.getStub())
						}, 
						systemReflectionFunctionKind, 
						METHOD_getFunctionKind, 
						builder.getStub()), 
					null));
			
			// getReturnType()
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), MethodName_getReturnType, Accessibility.PUBLIC, false, false,
					new JMethodType(
						MethodName_getReturnType,
						new JParameter[]{
							new JParameter("this", builder.getStub())
						}, 
						systemType, 
						METHOD_getReturnType, 
						builder.getStub()), 
					null));
		
			// getParameters()
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), MethodName_getParameters, Accessibility.PUBLIC, false, false,
					new JMethodType(
						MethodName_getParameters,
						new JParameter[]{
							new JParameter("this", builder.getStub())
						}, 
						systemReflectionParameterArray, 
						METHOD_getParameters, 
						builder.getStub()), 
					null));
			
			builder.seal();
			builder = null;
		}
	}

	@Override
	public void setBuilder(ICompoundTypeBuilder builder) {
		this.builder = builder;
	}

	@Override
	public void preInitialize() {
		this.initialized = true;
	}
}
