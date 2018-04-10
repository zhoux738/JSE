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

import info.julang.execution.symboltable.SymbolUndefinedException;
import info.julang.external.exceptions.JSEError;
import info.julang.external.interfaces.JValueKind;
import info.julang.interpretation.BadSyntaxException;
import info.julang.interpretation.IllegalOperandsException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.context.ContextType;
import info.julang.interpretation.context.LambdaContext;
import info.julang.interpretation.context.MethodContext;
import info.julang.interpretation.expression.operand.NameOperand;
import info.julang.interpretation.expression.operand.TypeOperand;
import info.julang.interpretation.expression.operand.ValueOperand;
import info.julang.memory.value.JValue;
import info.julang.memory.value.TypeValue;
import info.julang.memory.value.UntypedValue;
import info.julang.memory.value.ValueUtilities;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.ICompoundType;

public abstract class Operator {

	public static enum Associativity {
		
		LEFT,
		
		RIGHT,
	}
	
	private String text;
	
	protected int arity;
	
	private int precedence;
	
	private Associativity associativity;

	public int getArity() {
		return arity;
	}

	/**
	 * Get the precedence of this operator. The lesser the higher (0 being highest).
	 * @return
	 */
	public int getPrecedence() {
		return precedence;
	}

	public Associativity getAssociativity() {
		return associativity;
	}

	public Operator(String text, int arity, int precedence, Associativity associativity) {
		this.text = text;
		this.arity = arity;
		this.precedence = precedence;
		this.associativity = associativity;
	}
	
	/**
	 * Apply this operator on given operands.
	 * <p/>
	 * Where the operands come from doesn't concern this method. The number of operands provided,
	 * however, must be equal to {@link #getArity()}.
	 * 
	 * @param context the thread context
	 * @param operands in the same order these operands were pushed into the stack.
	 * @return
	 */
	public Operand apply(Context context, Operand[] operands){
		if(operands==null){
			throw new JSEError("No operand is provided for operator '" + text + "'", Operator.class);
		}
		if(operands.length != arity){
			throw new BadSyntaxException("Operator '" + text + "' must apply on " + operands.length + " operators.");
		}
		
		return doApply(context, operands);
	}
	
	/**
	 * The core logic for applying this operator on a given array of operands. 
	 * 
	 * @param context
	 * @param operands
	 * @return
	 */
	protected abstract Operand doApply(Context context, Operand[] operands);
	
	/**
	 * Convert an operand to JValue
	 * 
	 * @param varTable
	 * @param operand
	 * @return
	 */
	protected JValue getValue(Context context, Operand operand) {
		return getValue(context, operand, false, true);
	}
	
	/**
	 * Convert an operand to JValue
	 * 
	 * @param varTable
	 * @param operand
	 * @param toReplicate
	 * @return
	 */
	protected JValue getValue(Context context, Operand operand, boolean toReplicate, boolean unwrapUntyped) {
		JValue val = null;
		switch(operand.getKind()){
		case NAME:
			String name = ((NameOperand) operand).getName();			
			val = context.getResolver().resolve(name);
			if(val == null){
				throw new SymbolUndefinedException(name);
			}
			break;
		case VALUE:
		case INDEX:
		case IMEMBER:
		case SMEMBER:
			val = ((ValueOperand) operand).getValue();
			if(val == null){
				throw new JSEError("An operand contains null value.");
			}
			break;
		case TYPE:
			val = ((TypeOperand) operand).getValue(); // this may return null if it is basic type.
			break;
		default:
		}
		
		if(val != null){
			if(toReplicate){
				val = replicateValue(val, context);
			}
			if(unwrapUntyped && val.getKind() == JValueKind.UNTYPED){
				val = ((UntypedValue)val).getActual();
			}
			return val;
		}
		
		throw new JSEError("Cannot apply '" + text + "' on a value that is neither a name nor a value.");
	}
	
	/**
	 * Convert an operand to Julian type.
	 * 
	 * @param context
	 * @param operand
	 * @return
	 */
	protected JType getType(Context context, Operand operand) {
		JType type = null;
		JValue val = null;
		switch(operand.getKind()){
		case NAME:
			String name = ((NameOperand) operand).getName();			
			val = context.getResolver().resolve(name);
			if(val == null){
				throw new SymbolUndefinedException(name);
			}
			break;
		case VALUE:
		case INDEX:
		case IMEMBER:
		case SMEMBER:
			val = ((ValueOperand) operand).getValue();
			if(val == null){
				throw new JSEError("An operand contains null value.");
			}
			break;
		case TYPE:
			TypeOperand top = (TypeOperand) operand; // this may return null if it is basic type.
			type = top.getType();
			break;
		default:
		}
		
		if(type != null){
			return type;
		}
		
		if(val != null && val.getKind() == JValueKind.TYPE){
			return ((TypeValue) val).getType();
		}
		
		throw new JSEError("Cannot apply '" + text + "' on a value that is neither a name nor a value.");
	}
	
	/**
	 * Replicate a value in current frame.
	 * <p/>
	 * If the value is of basic type, or string, make a copy by value (call-by-value).
	 * <p/>
	 * If the value if of other types, make a reference to hold that value (call-by-reference).
	 * 
	 * @param val
	 */
	protected JValue replicateValue(JValue val, Context context) {
		return ValueUtilities.replicateValue(val, null, context.getFrame());
	}

	protected void throwIllegalOperandsException(JValue lval, JValue rval){
		throw new IllegalOperandsException(
			"The operator '" + this.toString() + "' cannot apply on operands of type " + 
			lval.getType() + " and " + rval.getType());
	}
	
	protected void checkAccessibility(ICompoundType runtimeType, String memberName, Context context, boolean isStatic){
		ContextType ct = context.getContextType();
		switch(ct){
		case IMETHOD:
		case SMETHOD:
			MethodContext mc = (MethodContext) context;
			Accessibility.checkMemberAccess(runtimeType, memberName, mc.getContainingType(), context, ct, isStatic, true);
			return;
		case FUNCTION:
			Accessibility.checkMemberAccess(runtimeType, memberName, null, context, ct, isStatic, true);
			return;
		case LAMBDA:
			LambdaContext lc = (LambdaContext) context;
			Accessibility.checkMemberAccess(runtimeType, memberName, lc.getContainingType(), context, lc.getDefiningContextType(), isStatic, true);
		}
	}
	
	@Override
	public String toString(){
		return text;
	}
}
