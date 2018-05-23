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

package info.julang.interpretation.expression;

import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.RuntimeCheckException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.IHasLocationInfo;
import info.julang.interpretation.expression.operand.OperandKind;
import info.julang.interpretation.expression.operand.ValueOperand;
import info.julang.interpretation.expression.sub.AddExpression;
import info.julang.interpretation.expression.sub.AndExpression;
import info.julang.interpretation.expression.sub.AssignExpression;
import info.julang.interpretation.expression.sub.BitwiseAndExpression;
import info.julang.interpretation.expression.sub.BitwiseOrExpression;
import info.julang.interpretation.expression.sub.BitwiseShiftExpression;
import info.julang.interpretation.expression.sub.BitwiseXorExpression;
import info.julang.interpretation.expression.sub.CallFuncExpression;
import info.julang.interpretation.expression.sub.CastExpression;
import info.julang.interpretation.expression.sub.CompareExpression;
import info.julang.interpretation.expression.sub.DotExpression;
import info.julang.interpretation.expression.sub.EqualExpression;
import info.julang.interpretation.expression.sub.IncrementExpression;
import info.julang.interpretation.expression.sub.IndexExpression;
import info.julang.interpretation.expression.sub.IsExpression;
import info.julang.interpretation.expression.sub.LambdaExpression;
import info.julang.interpretation.expression.sub.MultiplyExpression;
import info.julang.interpretation.expression.sub.NewExpression;
import info.julang.interpretation.expression.sub.OrExpression;
import info.julang.interpretation.expression.sub.PrimaryExpression;
import info.julang.interpretation.expression.sub.TertiaryExpression;
import info.julang.interpretation.expression.sub.TypeofExpression;
import info.julang.interpretation.expression.sub.UnaryExpression;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.memory.value.BoolValue;
import info.julang.memory.value.ByteValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.parser.AstInfo;
import info.julang.typesystem.basic.BoolType;

public abstract class ExpressionBase implements IExpression {
	
	protected Operator op;
	
	protected AstInfo<ExpressionContext> ec;
	
	protected ThreadRuntime rt;
	
	public ExpressionBase(ThreadRuntime rt, AstInfo<ExpressionContext> ec, Operator op){
		this.op = op;
		this.ec = ec;
		this.rt = rt;
	}
	
	@Override
	public IHasLocationInfo getLocation(){
		return ec != null ? ec : new IHasLocationInfo(){
			@Override
			public String getFileName() {
				return "<unknown>";
			}
			@Override
			public int getLineNumber() {
				return -1;
			}
		};
	}

	//------------------------------------- Utility methods for subclasses -------------------------------------//
	
	protected IExpression getExpression(AstInfo<ExpressionContext> subAst){
		// By using labels in ANTLR definition, all sub expressions have a corresponding class
		IExpression expr = null;
		String name = subAst.getAST().getClass().getSimpleName();
		switch(name) {
		case "E_primaryContext":
			expr = new PrimaryExpression(rt, subAst);
			break;
		case "E_indexerContext":
			expr = new IndexExpression(rt, subAst);
			break;
		case "E_function_callContext":
			expr = new CallFuncExpression(rt, subAst);
			break;
		case "E_dotContext":
			expr = new DotExpression(rt, subAst);
			break;
		case "E_incrementContext":
			expr = new IncrementExpression(rt, subAst);
			break;
		case "E_newContext":
			expr = new NewExpression(rt, subAst);
			break;
		case "E_typeofContext":
			expr = new TypeofExpression(rt, subAst);
			break;
		case "E_castContext":
			expr = new CastExpression(rt, subAst);
			break;
		case "E_unaryContext": 
			expr = new UnaryExpression(rt, subAst);
			break;
		case "E_multiplyContext":
			expr = new MultiplyExpression(rt, subAst);
			break;
		case "E_addContext":
			expr = new AddExpression(rt, subAst);
			break;
		case "E_bitwise_shiftContext":
			expr = new BitwiseShiftExpression(rt, subAst);
			break;
		case "E_compareContext":
			expr = new CompareExpression(rt, subAst);
			break;
		case "E_isContext":
			expr = new IsExpression(rt, subAst);
			break;
		case "E_equalContext":
			expr = new EqualExpression(rt, subAst);
			break;
		case "E_bitwise_andContext":
			expr = new BitwiseAndExpression(rt, subAst);
			break;
		case "E_bitwise_xorContext":
			expr = new BitwiseXorExpression(rt, subAst);
			break;
		case "E_bitwise_orContext":
			expr = new BitwiseOrExpression(rt, subAst);
			break;
		case "E_andContext":
			expr = new AndExpression(rt, subAst);
			break;
		case "E_orContext":
			expr = new OrExpression(rt, subAst);
			break;
		case "E_tertiaryContext":
			expr = new TertiaryExpression(rt, subAst);
			break;
		case "E_assignContext":
			expr = new AssignExpression(rt, subAst);
			break;
		case "E_lambdaContext":
			expr = new LambdaExpression(rt, subAst);
			break;
		default:
		}
		
		if (expr == null){
			throw new JSEError("Unrecognzied expression: " + name);
		} 

		return expr;
	}

	/**
	 * Convert an operand to {@link JValue value}. Throws if the operand cannot be resolved to a value.
	 */
	protected JValue getValue(Context context, Operand od){
		if (od.getKind() == OperandKind.VALUE){
			ValueOperand vo = (ValueOperand) od;
			return vo.getValue();
		} else {
			Operand[] lod = new Operand[]{ od };
			od = KnownOperators.EVAL.apply(context, lod);
			if (od.getKind() == OperandKind.VALUE){
				ValueOperand vo = (ValueOperand) od;
				return vo.getValue();
			}
		}
		
		throw new RuntimeCheckException("Expression must produce a value.");
	}
	
	/**
	 * Convert an operand to a Java boolean value. Throws if the operand is not of Julian's Bool type.
	 */
	protected boolean asBoolean(Context context, Operand od){
		JValue value = getValue(context, od);
		if (value.getType() == BoolType.getInstance()) {
			BoolValue bv = (BoolValue)value;
			return bv.getBoolValue();
		}
		
		throw new RuntimeCheckException("Expected a value of bool type. But saw type of " + value.getType());
	}
	
	/**
	 * Convert an operand to a Java boolean value. Throws if the operand is not of Julian's Int or Byte type.
	 */
	protected int asInt(Context context, Operand od){
		JValue value = getValue(context, od);
		switch (value.getKind()){
		case INTEGER:
			IntValue iv = (IntValue)value;
			return iv.getIntValue();
		case BYTE:
			ByteValue bv = (ByteValue)value;
			return bv.getByteValue();
		default:
		}
		
		throw new RuntimeCheckException("Expected a value of whole number type. But saw type of " + value.getType());
	}
}
