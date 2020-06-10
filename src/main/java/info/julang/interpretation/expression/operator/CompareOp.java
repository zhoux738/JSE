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

package info.julang.interpretation.expression.operator;

import static info.julang.langspec.Operators.EQ;
import static info.julang.langspec.Operators.GT;
import static info.julang.langspec.Operators.GTEQ;
import static info.julang.langspec.Operators.LT;
import static info.julang.langspec.Operators.LTEQ;
import static info.julang.langspec.Operators.NEQ;

import info.julang.external.interfaces.JValueKind;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.expression.Operand;
import info.julang.interpretation.expression.Operator;
import info.julang.memory.value.BoolValue;
import info.julang.memory.value.ByteValue;
import info.julang.memory.value.FloatValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.StringValue;
import info.julang.memory.value.TempValueFactory;

/**
 * Operators: >, >=, <, <=, ==, !=
 * 
 * @author Ming Zhou
 */
public abstract class CompareOp extends Operator {
	
	private CompareOp(String op, int precedence, Associativity associativity) {
		super(op, 2, precedence, associativity);
	}

	@Override
	protected Operand doApply(Context context, Operand[] operands) {
		JValue lval = getValue(context, operands[0]);
		JValue rval = getValue(context, operands[1]);

		Operand res = Operand.createOperand(compare(lval, rval));
		
		return res;
	}

	private JValue compare(JValue lval, JValue rval){
		switch(lval.getKind()){
		case INTEGER:
			switch(rval.getKind()){
			case INTEGER:
				boolean result = compareIntToInt(
					((IntValue) lval).getIntValue(), 
					((IntValue) rval).getIntValue());
				return TempValueFactory.createTempBoolValue(result);
			case BYTE:
				result = compareIntToInt(
					((IntValue) lval).getIntValue(), 
					((ByteValue) rval).getByteValue());
				return TempValueFactory.createTempBoolValue(result);
			case FLOAT:
				result = compareDoubleToDouble(
					((IntValue) lval).getIntValue(), 
					((FloatValue) rval).getFloatValue());
				return TempValueFactory.createTempBoolValue(result);	
			default:
			}
			break;
		case FLOAT:
			switch(rval.getKind()){
			case INTEGER:
				boolean result = compareDoubleToDouble(
					((FloatValue) lval).getFloatValue(), 
					((IntValue) rval).getIntValue());
				return TempValueFactory.createTempBoolValue(result);
			case BYTE:
				result = compareDoubleToDouble(
					((FloatValue) lval).getFloatValue(), 
					((ByteValue) rval).getByteValue());
				return TempValueFactory.createTempBoolValue(result);
			case FLOAT:
				result = compareDoubleToDouble(
					((FloatValue) lval).getFloatValue(), 
					((FloatValue) rval).getFloatValue());
				return TempValueFactory.createTempBoolValue(result);	
			default:
			}
			break;
		case BYTE:
			switch(rval.getKind()){
			case INTEGER:
				boolean result = compareIntToInt(
					((ByteValue) lval).getByteValue(), 
					((IntValue) rval).getIntValue());
				return TempValueFactory.createTempBoolValue(result);
			case BYTE:
				result = compareIntToInt(
					((ByteValue) lval).getByteValue(), 
					((ByteValue) rval).getByteValue());
				return TempValueFactory.createTempBoolValue(result);
			case FLOAT:
				result = compareDoubleToDouble(
					((ByteValue) lval).getByteValue(), 
					((FloatValue) rval).getFloatValue());
				return TempValueFactory.createTempBoolValue(result);	
			default:
			}
			break;
		case STRING:
			if(rval.getKind() == JValueKind.STRING){
				boolean result = compareStringToString(
					((StringValue) lval).getStringValue(), 
					((StringValue) rval).getStringValue());
				return TempValueFactory.createTempBoolValue(result);
			}
		default:
		}

		return TempValueFactory.createTempBoolValue(compareSpecialTypes(lval, rval));
	}
	
	protected abstract boolean compareStringToString(String lvalue, String rvalue);

	protected abstract boolean compareIntToInt(int lvalue, int rvalue);
	
	protected abstract boolean compareDoubleToDouble(double lvalue, double rvalue);
	
	protected boolean compareSpecialTypes(JValue lval, JValue rval){
		return lval.isEqualTo(rval);
	}

//	private void throwException(JValue lval, JValue rval){
//		throw new IllegalOperandsException(
//			"The operator '" + this.toString() + "' cannot apply on operands of type " + 
//			lval.getType() + " and " + rval.getType());
//	}
	
	//------------- Implementation for the following operators -------------//
	//  Less Than or Equal(<=), 
	//  Less Than(<), 
	//  Greater Than or Equal (>=), 
	//  Greater Than (>), 
	//  Equal (==), 
	//  Not Equal (!=)  
	//----------------------------------------------------------------------//
	
	public static class LessThanOp extends CompareOp {
		
		public LessThanOp() {
			super("<", LT.precedence, LT.associativity);
		}

		@Override
		protected boolean compareStringToString(String lvalue, String rvalue) {
			int ll = lvalue.length();
			int rl = rvalue.length();
			int ml = Math.min(ll, rl);
			
			char[] lchars = lvalue.toLowerCase().toCharArray();
			char[] rchars = rvalue.toLowerCase().toCharArray();
			
			for(int i=0; i < ml; i++){
				if(lchars[i] < rchars[i]){
					return true;
				} else if (lchars[i] > rchars[i]){
					return false;
				} 
			}
			
			if(ll < rl){
				return true;
			}
			
			return false;
		}

		@Override
		protected boolean compareIntToInt(int lvalue, int rvalue) {
			return lvalue < rvalue;
		}
		
		@Override
		protected boolean compareDoubleToDouble(double lvalue, double rvalue) {
			return lvalue < rvalue;
		}
	}
	
	public static class LessThanEqualOp extends CompareOp {
		
		public LessThanEqualOp() {
			super("<=", LTEQ.precedence, LTEQ.associativity);
		}

		@Override
		protected boolean compareStringToString(String lvalue, String rvalue) {
			int ll = lvalue.length();
			int rl = rvalue.length();
			int ml = Math.min(ll, rl);
			
			char[] lchars = lvalue.toLowerCase().toCharArray();
			char[] rchars = rvalue.toLowerCase().toCharArray();
			
			for(int i=0; i < ml; i++){
				if(lchars[i] < rchars[i]){
					return true;
				} else if (lchars[i] > rchars[i]){
					return false;
				} 
			}
			
			if(ll <= rl){
				return true;
			}
			
			return false;
		}

		@Override
		protected boolean compareIntToInt(int lvalue, int rvalue) {
			return lvalue <= rvalue;
		}
		
		@Override
		protected boolean compareDoubleToDouble(double lvalue, double rvalue) {
			return lvalue <= rvalue;
		}
	}
	
	public static class GreaterThanOp extends CompareOp {
		
		public GreaterThanOp() {
			super(">", GT.precedence, GT.associativity);
		}

		@Override
		protected boolean compareStringToString(String lvalue, String rvalue) {
			int ll = lvalue.length();
			int rl = rvalue.length();
			int ml = Math.min(ll, rl);
			
			char[] lchars = lvalue.toLowerCase().toCharArray();
			char[] rchars = rvalue.toLowerCase().toCharArray();
			
			for(int i=0; i < ml; i++){
				if(lchars[i] < rchars[i]){
					return false;
				} else if (lchars[i] > rchars[i]){
					return true;
				} 
			}
			
			if(ll > rl){
				return true;
			}
			
			return false;
		}

		@Override
		protected boolean compareIntToInt(int lvalue, int rvalue) {
			return lvalue > rvalue;
		}
		
		@Override
		protected boolean compareDoubleToDouble(double lvalue, double rvalue) {
			return lvalue > rvalue;
		}
	}
	
	public static class GreaterThanEqualOp extends CompareOp {
		
		public GreaterThanEqualOp() {
			super(">=", GTEQ.precedence, GTEQ.associativity);
		}

		@Override
		protected boolean compareStringToString(String lvalue, String rvalue) {
			int ll = lvalue.length();
			int rl = rvalue.length();
			int ml = Math.min(ll, rl);
			
			char[] lchars = lvalue.toLowerCase().toCharArray();
			char[] rchars = rvalue.toLowerCase().toCharArray();
			
			for(int i=0; i < ml; i++){
				if(lchars[i] < rchars[i]){
					return false;
				} else if (lchars[i] > rchars[i]){
					return true;
				} 
			}
			
			if(ll >= rl){
				return true;
			}
			
			return false;
		}

		@Override
		protected boolean compareIntToInt(int lvalue, int rvalue) {
			return lvalue >= rvalue;
		}
		
		@Override
		protected boolean compareDoubleToDouble(double lvalue, double rvalue) {
			return lvalue >= rvalue;
		}
	}

	public static class EqualOp extends CompareOp {
		
		public EqualOp() {
			super("==", EQ.precedence, EQ.associativity);
		}
		
		protected EqualOp(String op, int precedence, Associativity associativity) {
			super(op, precedence, associativity);
		}

		@Override
		protected boolean compareStringToString(String lvalue, String rvalue) {			
			return lvalue.equals(rvalue);
		}

		@Override
		protected boolean compareIntToInt(int lvalue, int rvalue) {
			return lvalue == rvalue;
		}
		
		@Override
		protected boolean compareDoubleToDouble(double lvalue, double rvalue) {
			return lvalue == rvalue;
		}
		
		@Override
		protected boolean compareSpecialTypes(JValue lval, JValue rval) {
			JValueKind lkind = lval.getKind();
			JValueKind rkind = rval.getKind();
			if(lkind == JValueKind.BOOLEAN && rkind == JValueKind.BOOLEAN){
				return ((BoolValue) lval).getBoolValue() == ((BoolValue) rval).getBoolValue();
			}
			
			return super.compareSpecialTypes(lval, rval);
		}
	}
	
	public static class NotEqualOp extends EqualOp {
		
		public NotEqualOp() {
			super("!=", NEQ.precedence, NEQ.associativity);
		}

		@Override
		protected boolean compareStringToString(String lvalue, String rvalue) {			
			return !super.compareStringToString(lvalue, rvalue);
		}

		@Override
		protected boolean compareIntToInt(int lvalue, int rvalue) {
			return !super.compareIntToInt(lvalue, rvalue);
		}
		
		@Override
		protected boolean compareDoubleToDouble(double lvalue, double rvalue) {
			return !super.compareDoubleToDouble(lvalue, rvalue);
		}
		
		@Override
		protected boolean compareSpecialTypes(JValue lval, JValue rval) {
			return !super.compareSpecialTypes(lval, rval);
		}
	}
}
