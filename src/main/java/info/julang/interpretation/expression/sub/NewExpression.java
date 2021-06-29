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

package info.julang.interpretation.expression.sub;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import info.julang.execution.security.EngineLimit;
import info.julang.execution.security.EnginePolicyEnforcer;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.JSEError;
import info.julang.hosting.interop.JSEObjectWrapper;
import info.julang.interpretation.IllegalOperandsException;
import info.julang.interpretation.RuntimeCheckException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.expression.DelegatingExpression;
import info.julang.interpretation.expression.ExpressionBase;
import info.julang.interpretation.expression.Operand;
import info.julang.interpretation.expression.operand.ValueOperand;
import info.julang.interpretation.internal.NewObjExecutor;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.interpretation.syntax.SyntaxHelper;
import info.julang.langspec.ast.JulianParser.ArgumentContext;
import info.julang.langspec.ast.JulianParser.Argument_listContext;
import info.julang.langspec.ast.JulianParser.Array_creation_expressionContext;
import info.julang.langspec.ast.JulianParser.Array_initializerContext;
import info.julang.langspec.ast.JulianParser.Base_typeContext;
import info.julang.langspec.ast.JulianParser.Builtin_typeContext;
import info.julang.langspec.ast.JulianParser.Class_typeContext;
import info.julang.langspec.ast.JulianParser.Composite_idContext;
import info.julang.langspec.ast.JulianParser.Created_type_nameContext;
import info.julang.langspec.ast.JulianParser.CreatorContext;
import info.julang.langspec.ast.JulianParser.E_lambdaContext;
import info.julang.langspec.ast.JulianParser.E_newContext;
import info.julang.langspec.ast.JulianParser.E_primaryContext;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.langspec.ast.JulianParser.Kvp_initializerContext;
import info.julang.langspec.ast.JulianParser.Map_initializerContext;
import info.julang.langspec.ast.JulianParser.Object_creation_expressionContext;
import info.julang.langspec.ast.JulianParser.PrimaryContext;
import info.julang.langspec.ast.JulianParser.TypeContext;
import info.julang.langspec.ast.JulianParser.Var_initializerContext;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.ArrayValueFactory;
import info.julang.memory.value.CharValue;
import info.julang.memory.value.DynamicValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.StringValue;
import info.julang.parser.ANTLRHelper;
import info.julang.parser.AstInfo;
import info.julang.typesystem.JType;
import info.julang.typesystem.basic.BasicType;
import info.julang.typesystem.basic.CharType;
import info.julang.typesystem.basic.NumberKind;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.builtin.JArrayType;
import info.julang.typesystem.jclass.builtin.JDynamicType;
import info.julang.typesystem.jclass.jufc.SystemTypeNames;
import info.julang.util.OSTool;

/**
 * The expression to create a new object. There are two use cases:
 * <ul>
 * <li>creating a new object of some class type by invoking a constructor, 
 * and optionally, <code style="color:green">System.Util.IMapInitializable.initByMap()</code></li>
 * <li>creating an array of some type, with or without an initializer</li>
 * </ul>
 * Syntax:<br>
 * <code>new MyObj();</code><br>
 * <code>new int[2];</code><br>
 * <code>new string[]{"a", "b"};</code><br>
 * 
 * @author Ming Zhou
 */
public class NewExpression extends ExpressionBase {

	private EnginePolicyEnforcer ep;
	
	public NewExpression(ThreadRuntime rt, AstInfo<ExpressionContext> ec) {
		super(rt, ec, null);
	}

	public Operand evaluate(Context context){
		// If to check memory usage, store the policy enforcer for later use.
		EnginePolicyEnforcer epf = rt.getModuleManager().getEnginePolicyEnforcer();
		if (epf.getLimit(EngineLimit.MAX_USED_MEMORY_IN_BYTE) != EngineLimit.UNDEFINED) {
			this.ep = epf;
		}
		
		E_newContext nec = (E_newContext)ec.getAST();
		CreatorContext cc = nec.creator();
		
		// 1) type to instantiate
		JType type = null;
		Map_initializerContext mic = cc.map_initializer();
		if (mic == null) {
			ParsedTypeName ptn = getTypeName(cc);
			type = context.getTypeResolver().resolveType(ptn);
		}
		
		// 2) object or array?
		ObjectValue ov = null;
		Object_creation_expressionContext objContext = cc.object_creation_expression();
		if (mic != null) {
			// The map initializer may come from either of the two places:
			// 1) right after new (in the case of implicit Dynamic), or ...
			ov = newDynamic(context, mic);
		} else if (objContext != null){
			// 2) ... right after the ctor's argument list.
			ov = newObject(context, type, objContext);
		} else {
			Array_creation_expressionContext arrContext = cc.array_creation_expression();
			ov = newArray(context, type, arrContext);
		}
		
		return new ValueOperand(ov);
	}

	// Create an object of Dynamic type.
	private DynamicValue newDynamic(Context context, Map_initializerContext initContext) {
		JType dynType = JDynamicType.getInstance();
		DynamicValue dv = new DynamicValue(context.getHeap(), dynType);
		
		// It's fine we check this after creation for scalar type. 
		// The enforcement doesn't need to be precise.
		if (this.ep != null) {
			this.ep.checkLimit(EngineLimit.MAX_USED_MEMORY_IN_BYTE, dynType.getSize());
		}
		
		initObject(context, dv, initContext);
		return dv;
	}

	// Create an object of specified type.
	private ObjectValue newObject(
		Context context, JType type, Object_creation_expressionContext ast){
		if (!type.isObject()){
			throw new IllegalOperandsException("Cannot call constructor on a non-class type.");
		}
		
		Argument_listContext alc = ast.argument_list();
		List<ArgumentContext> args = alc != null ? alc.argument() : new ArrayList<ArgumentContext>();
		NewObjExecutor noe = new NewObjExecutor(rt);
		ObjectValue ov = noe.newObject(context, args, type, ec);
		
		// It's fine we check this after creation for scalar type. 
		// The enforcement doesn't need to be precise.
		if (this.ep != null) {
			this.ep.checkLimit(EngineLimit.MAX_USED_MEMORY_IN_BYTE, type.getSize());
		}
		
		Map_initializerContext initContext = ast.map_initializer();
		if (initContext != null) {
			initObject(context, ov, initContext);
		}
		
		return ov;
	}
	
	private void initObject(Context context, ObjectValue ov, Map_initializerContext initContext) {
		JClassType ctype = ov.getClassType();
		boolean isDynamic = JDynamicType.isDynamicType(ctype);
		DynamicValue dv = null;
		if (isDynamic) {
			dv = (DynamicValue)ov;
		}
		
		boolean foundInf = isDynamic;
		if (!foundInf) {
			foundInf = ctype.hasAncestor(SystemTypeNames.System_Util_IMapInitializable, false);
		}
		
		if (!foundInf) {
			throw new RuntimeCheckException(
				"Cannot instantiate with map initializer as the class (" + ctype.getName()
				+ ") doesn't implement " + SystemTypeNames.System_Util_IMapInitializable + ".");
		}
		
		List<Kvp_initializerContext> kvpList = initContext.kvp_initializer();
		if (kvpList == null || kvpList.size() == 0) {
			return;
		}
		
		NewObjExecutor newExe = new NewObjExecutor(rt);
		MemoryArea mem = rt.getHeap();
		JType entryTyp = rt.getTypeResolver().resolveType(
			Context.createSystemLoadingContext(rt),
			ParsedTypeName.makeFromFullName(SystemTypeNames.System_Util_Entry),
			true);
		
		int size = kvpList.size();
		JValue[] kvps = new JValue[size];
		for (int i = 0; i < size; i++) {
			Kvp_initializerContext kvpContext = kvpList.get(i);
			JValue key = null;
			PrimaryContext primary = kvpContext.primary();
			TerminalNode tn = primary.STRING_LITERAL();
			if (tn != null) {
				// String literal
				key = new StringValue(rt.getStackMemory().currentFrame(), ANTLRHelper.reEscapeAsString(tn.getText(), true));
			} else if ((tn = primary.IDENTIFIER()) != null) {
				// (By design) Treat id as string literal
				key = new StringValue(rt.getStackMemory().currentFrame(), tn.getText());
			} else {
				if (isDynamic) {
					// SPECIAL: If the type is Dynamic, the key must be either string or char.
					ExpressionContext exprContext = null;
					if ((tn = primary.CHAR_LITERAL()) != null) {
						// Char literal
						key = new StringValue(rt.getStackMemory().currentFrame(), "" + ANTLRHelper.reEscapeAsChar(tn.getText(), true));
					} else {
						exprContext = primary.expression();
					}
					
					if (exprContext != null) {					
						DelegatingExpression dex = new DelegatingExpression(rt, ec.create(exprContext));
						JValue resVal = dex.getResult(context);
						// String evaluated from expression
						key = StringValue.dereference(resVal, false);
						if (key == null && resVal.deref().getType() == CharType.getInstance()) {
							// Char evaluated from expression
							key = new StringValue(rt.getStackMemory().currentFrame(), ((CharValue)resVal.deref()).toString());
						}
					}
					
					if (key == null) {
						throw new RuntimeCheckException(
							"Can only create Dynamic object with a map initializer where the key is of type string or char.");
					}
				} else {
					// GENERIC: evaluates all the initializers; it's up to the implementation to decide what should be done with each pair.
					E_primaryContext syncExpr = ANTLRHelper.synthesizeDegenerateAST(primary, E_primaryContext.class);
					DelegatingExpression dex = new DelegatingExpression(rt, ec.create(syncExpr));
					key = dex.getResult(context);
				}
			}
			
			ExpressionContext valExprContext = kvpContext.expression();

			if (dv != null && !dv.shouldBindToAnyFunction()) {
				// If autobind is not turned on, only bind if a lambda literal is given as the value.
				if (valExprContext instanceof E_lambdaContext) {
					dv.addIndexToBind(i);
				}
			}
			
			DelegatingExpression dex = new DelegatingExpression(rt, ec.create(valExprContext));
			JValue value = dex.getResult(context);
			ObjectValue entryValue = newExe.newObject(mem, entryTyp, new JValue[] {key, value});
			kvps[i] = entryValue;
		}
		
		// With all the pairs substantiated, we can now create an array value that holds them
		ArrayValue arrValue = ArrayValueFactory.createArrayValue(context.getHeap(), context.getTypTable(), entryTyp, size);
		for (int i = 0; i < size; i++) {
			kvps[i].assignTo(arrValue.getValueAt(i));
		}
		
		// Now call System.Util.IMapInitializable.initByMap()
		SysUtilIMapInitializableWrapper wrapper = new SysUtilIMapInitializableWrapper(rt, ctype.getName(), ov);
		try {
			wrapper.initByMap(arrValue);
		} finally {
			// Special treatment for Dynamic: if it's marked as sealed, do not allow overwriting from this point on.
			// This must be done regardless of the result of initByMap(), so no second attempt by calling map initializer can be exploited.
			if (isDynamic) {
				dv.completeInit();
			}
		}
	}
	
	// Create an array object, and initialize elements if initializer is specified.
	private ObjectValue newArray(Context context, JType type, Array_creation_expressionContext arrContext) {
		// For array creation, either leave no length within brackets and use an initializer, 
		// or specify dimensions for all the dimensions.
		
		//array_creation_expression
		//    : LEFT_BRACKET RIGHT_BRACKET (LEFT_BRACKET RIGHT_BRACKET)* array_initializer
		//    | LEFT_BRACKET expression RIGHT_BRACKET (LEFT_BRACKET expression RIGHT_BRACKET)* (LEFT_BRACKET RIGHT_BRACKET)?
		//    ;
	
		ArrayValue arrVal = null;
		Array_initializerContext aic = arrContext.array_initializer();
		if (aic == null){
			// without initializer
			int len = arrContext.LEFT_BRACKET().size();
			int[] dims = new int[len];
			
			List<ExpressionContext> list = arrContext.expression();
			int exprLen = list.size();
			
			for (int i = 0; i < exprLen; i++){
				ExpressionContext sec = list.get(i);
				DelegatingExpression de = new DelegatingExpression(rt, ec.create(sec));
				JValue value = de.getResult(context);
				if(!value.isBasic() || ((BasicType)value.getType()).getNumberKind() != NumberKind.WHOLE){ 
					// Actually accept any whole numbers (as of 0.1.6 we don't have "long" type so this is safe)
					throw new RuntimeCheckException("A dimension expression must produce a value of integer type. But it has type of " + value.getType());
				}

				int length = ((IntValue)value).getIntValue();;
                if (length < 0) {
                    throw new RuntimeCheckException("A dimension expression must produce a non-negative integer. But it yielded " + length);
                }
				dims[i] = length;
			}
			
			// The last dimension's length is undefined
			if (len == exprLen + 1){
                dims[exprLen] = ArrayValueFactory.UndefinedLength;
			}
			
			// Must check this before creation for vector type, since the requested size can be infinitely large.
			if (this.ep != null) {
				int acc = Math.max(type.getSize(), OSTool.WordSize);
				for (int i = 0; i < dims.length; i++) {
					acc *= (dims[i] + 1);
				}
				this.ep.checkLimit(EngineLimit.MAX_USED_MEMORY_IN_BYTE, acc);
			}
			
			arrVal = ArrayValueFactory.createArrayValue(context.getHeap(), context.getTypTable(), type, dims);
		} else {
			// with initializer
			int dim = arrContext.LEFT_BRACKET().size();
			JArrayType arrType = JArrayType.createJArrayType(context.getTypTable(), type, dim);
			
			arrVal = initArray(context, arrType, aic);
		}
		
		return arrVal;
	}

	// We deal with two kinds of initializers: the expression or a nested array initializer. For expression
	// a value can be evaluated and assigned to the element; for a nested array initializer we must recursively
	// call this with the type of direct element. For example, int[][]'s direct element type is int[].
	private ArrayValue initArray(Context context, JArrayType arrType, Array_initializerContext aic) {
		//array_initializer
		//    :   '{' (var_initializer (',' var_initializer)* (',')? )? '}'
		//    ;

		//var_initializer
		//    :   array_initializer
		//    |   expression
		//    ;
		
		List<Var_initializerContext> elements = 
			aic == null ? new ArrayList<Var_initializerContext>() : aic.var_initializer();
		List<JValue> values = new ArrayList<JValue>(elements.size());
		
		for (Var_initializerContext vic : elements) {
			JValue value = null;
			ExpressionContext sec = vic.expression();
			if (sec != null){
				DelegatingExpression de = new DelegatingExpression(rt, ec.create(sec));
				value = de.getResult(context);
			} else {
				Array_initializerContext saic = vic.array_initializer();
				JType etyp = arrType.getElementType();
				if (!JArrayType.isArrayType(etyp)){
					throw new RuntimeCheckException("A sub-array initializer must be used when the corresponding element type is array, but it's " + etyp.getName());
				}
				value = initArray(context, (JArrayType)etyp, saic);
			}
			
			values.add(value);
		}
		
		// With all the elements collected, we can now create an array value that holds them
		int count = values.size();
		JType etype = arrType.getElementType();
		
		// Check the whole array before creation for vector type. The check for each element has already happened.
		if (this.ep != null) {
			int unit = Math.max(etype.getSize(), OSTool.WordSize);
			this.ep.checkLimit(EngineLimit.MAX_USED_MEMORY_IN_BYTE, unit * Math.max(count, 1));
		}
		
		ArrayValue array = ArrayValueFactory.createArrayValue(context.getHeap(), context.getTypTable(), arrType.getElementType(), count);
		for(int i=0; i<count; i++){
			values.get(i).assignTo(array.getValueAt(i));
		}
		
		return array;
	}
	
	private ParsedTypeName getTypeName(CreatorContext cc){
		// AST uses created_type_name in place of type to avoid ambiguity. We must reconstruct a type tree 
		// to utilize the shared Type parser.
		
		//(FROM)
		//created_type_name
	    //  : IDENTIFIER ( DOT IDENTIFIER )*
	    //  | builtin_type
	    //  ;
		
		Created_type_nameContext ctnc = cc.created_type_name();
		
		//(TO)
		//type 
		//  : base_type rank_specifier*
		//  ;
		    
		//base_type
		//  : builtin_type
		//  | class_type
		//  ;
		  
		//builtin_type 
		//  : ...
		//  ;
		  
		//class_type 
		//  : composite_id
		//  ;
		
		//composite_id
		//  : IDENTIFIER ( DOT IDENTIFIER )*
		//  ;
		
		Base_typeContext base = new Base_typeContext(null, 0);
		Builtin_typeContext bi = ctnc.builtin_type();
		if (bi != null){
			base.addChild(bi);
		} else {
			Composite_idContext cic = new Composite_idContext(null, 0);
			for (ParseTree pt : ctnc.children){
				if (pt instanceof TerminalNode){
					cic.addChild((TerminalNode)pt);
				} else if (pt instanceof RuleContext){
					cic.addChild((RuleContext)pt);
				} else {
					throw new JSEError("Can't rebuild AST from type name in a new expression.");
				}
			}
			
			Class_typeContext ctc = new Class_typeContext(null, 0);
			ctc.addChild(cic);
			
			base.addChild(ctc);
		}
		
		TypeContext tc = new TypeContext(null, 0);
		tc.addChild(base);
		
		ParsedTypeName ptn = SyntaxHelper.parseTypeName(tc);
		
		return ptn;
	}

	private class SysUtilIMapInitializableWrapper extends JSEObjectWrapper {
		
		private static final String Method_initByMap = "initByMap(System.Util.Entry[])";
		
		private SysUtilIMapInitializableWrapper(ThreadRuntime rt, String fullClassName, ObjectValue ov){
			super(fullClassName, rt, ov, false);
			
			// entryTyp := System.Util.Entry
			ITypeTable tt = rt.getTypeTable();
			JType entryTyp = tt.getType(SystemTypeNames.System_Util_Entry);
			if (entryTyp == null) {
				entryTyp = rt.getTypeResolver().resolveType(
					Context.createSystemLoadingContext(rt),
					ParsedTypeName.makeFromFullName(SystemTypeNames.System_Util_Entry),
					true);
			}
			
			// arrTyp := System.Util.Entry[]
			JArrayType arrTyp = tt.getArrayType(entryTyp);
			if (arrTyp == null) {
				tt.addArrayType(JArrayType.createJArrayType(tt, entryTyp, false));
				arrTyp = tt.getArrayType(entryTyp);
			}
			
			this.registerMethod(Method_initByMap, SystemTypeNames.MemberNames.INIT_BT_MAP, false, new JType[]{ arrTyp });
		}
		
		private void initByMap(ArrayValue arrValue){
			this.runMethod(Method_initByMap, arrValue);
		}
	}
}
