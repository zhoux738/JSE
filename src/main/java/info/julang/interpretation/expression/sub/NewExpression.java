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

import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.JSEError;
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
import info.julang.langspec.ast.JulianParser.E_newContext;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.langspec.ast.JulianParser.Object_creation_expressionContext;
import info.julang.langspec.ast.JulianParser.TypeContext;
import info.julang.langspec.ast.JulianParser.Var_initializerContext;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.ArrayValueFactory;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.parser.AstInfo;
import info.julang.typesystem.JType;
import info.julang.typesystem.basic.BasicType;
import info.julang.typesystem.basic.NumberKind;
import info.julang.typesystem.jclass.builtin.JArrayType;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * The expression to create a new object. There are two use cases:
 * <li>creating a new object of some class type by invoking a constructor</li>
 * <li>creating an array of some type, with or without an initializer</li>
 * <p>
 * Syntax:<br>
 * <code>new MyObj();</code><br>
 * <code>new int[2];</code><br>
 * <code>new string[]{"a", "b"};</code><br>
 * 
 * @author Ming Zhou
 */
public class NewExpression extends ExpressionBase {

	public NewExpression(ThreadRuntime rt, AstInfo<ExpressionContext> ec) {
		super(rt, ec, null);
	}

	public Operand evaluate(Context context){
		E_newContext nec = (E_newContext)ec.getAST();
		CreatorContext cc = nec.creator();
		
		// 1) type to instantiate
		ParsedTypeName ptn = getTypeName(cc);
		
		// 2) object or array?
		ObjectValue ov = null;
		JType type = context.getTypeResolver().resolveType(ptn);
		Object_creation_expressionContext objContext = cc.object_creation_expression();
		if (objContext != null){
			ov = newObject(context, type, objContext);
		} else {
			Array_creation_expressionContext arrContext = cc.array_creation_expression();
			ov = newArray(context, type, arrContext);
		}
		
		return new ValueOperand(ov);
	}

	// Create an object of specified type.
	private ObjectValue newObject(Context context, JType type, Object_creation_expressionContext ast){
		if (!type.isObject()){
			throw new IllegalOperandsException("Cannot call constructor on a non-class type.");
		}
		
		Argument_listContext alc = ast.argument_list();
		List<ArgumentContext> args = alc != null ? alc.argument() : new ArrayList<ArgumentContext>();
		NewObjExecutor noe = new NewObjExecutor(rt);
		ObjectValue ov = noe.newObject(context, args, type, ec);
		
		return ov;
	}
	
	// Create an array object, and initialize elements if initializer is specified.
	private ObjectValue newArray(Context context, JType type, Array_creation_expressionContext arrContext) {
		// For array creation, either leave no length within brackets and use an initializer, 
		// or specify dimensions for all the dimensions.
		
		//array_creation_expression
		//    : LEFT_BRACKET RIGHT_BRACKET (LEFT_BRACKET RIGHT_BRACKET)* array_initializer
		//    | LEFT_BRACKET expression RIGHT_BRACKET (LEFT_BRACKET expression RIGHT_BRACKET)*
		//    ;
	
		ArrayValue arrVal = null;
		Array_initializerContext aic = arrContext.array_initializer();
		if (aic == null){
			// without initializer
			int len = arrContext.LEFT_BRACKET().size();
			int[] dims = new int[len];
			
			List<ExpressionContext> list = arrContext.expression();
			if (len != list.size()){
				throw new JSEError("Unexpected syntax error when evaluating new-array expression: dimension expressions and rank do not match.");
			}
			
			for (int i = 0; i < len; i++){
				ExpressionContext sec = list.get(i);
				DelegatingExpression de = new DelegatingExpression(rt, ec.create(sec));
				JValue value = de.getResult(context);
				if(!value.isBasic() || ((BasicType)value.getType()).getNumberKind() != NumberKind.WHOLE){ 
					// Actually accept any whole numbers (as of 0.1.6 we don't have "long" type so this is safe)
					throw new RuntimeCheckException("A dimension expression must produce a value of integer type. But it has type of " + value.getType());
				}

				dims[i] = ((IntValue)value).getIntValue();
			}
			
			arrVal = ArrayValueFactory.createArrayValue(context.getHeap(), context.getTypTable(), type, dims);
		} else {
			// with initializer
			int dim = arrContext.LEFT_BRACKET().size();
			JArrayType arrType = JArrayType.createJArrayType(context.getTypTable(), type, dim);
			
			arrVal = newArray(context, arrType, aic);
		}
		
		return arrVal;
	}

	// We deal with two kinds of initializers: the expression or a nested array initializer. For expression
	// a value can be evaluated and assigned to the element; for a nested array initializer we must recursively
	// call this with the type of direct element. For example, int[][]'s direct element type is int[].
	private ArrayValue newArray(Context context, JArrayType arrType, Array_initializerContext aic) {
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
				value = newArray(context, (JArrayType)etyp, saic);
			}
			
			values.add(value);
		}
		
		// With all the elements collected, we can now create an array value that holds them
		int count = values.size();
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

}
