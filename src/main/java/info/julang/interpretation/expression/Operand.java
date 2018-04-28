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

import info.julang.interpretation.expression.operand.IndexOperand;
import info.julang.interpretation.expression.operand.NameOperand;
import info.julang.interpretation.expression.operand.OperandKind;
import info.julang.interpretation.expression.operand.TypeOperand;
import info.julang.interpretation.expression.operand.ValueOperand;
import info.julang.memory.value.JValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.memory.value.TypeValue;
import info.julang.memory.value.indexable.IIndexable;

/**
 * The operand pushed into the expression stack.
 * <p/>
 * To create a new operand, either call {@link #createOperand(JValue)} by passing along a known value,
 * or any of direct-from-literal factory methods to which the literal value is provided. For example,
 * To create an integer operand with value = 5, call {@link Operand#createIntOperand(int) createIntOperand(5)};
 *
 * @author Ming Zhou
 */
public abstract class Operand {
	
	/**
	 * A placeholder operand solely to meet the expectation of the evaluation engine.
	 */
	public static final Operand EMPTY = new Operand(){
		@Override
		public OperandKind getKind() {
			// Not really used
			return OperandKind.VALUE;
		}
	};
	
	/**
	 * The type of this operand.
	 */
	public abstract OperandKind getKind();
	
	public static Operand createOperand(JValue value){
		return new ValueOperand(value);
	}

	public static Operand createIntOperand(int i){
		return new ValueOperand(TempValueFactory.createTempIntValue(i));
	}
	
	public static Operand createFloatOperand(float f){
		return new ValueOperand(TempValueFactory.createTempFloatValue(f));
	}
	
	public static Operand createCharOperand(char c){
		return new ValueOperand(TempValueFactory.createTempCharValue(c));
	}
	
	public static Operand createBoolOperand(boolean z){
		return new ValueOperand(TempValueFactory.createTempBoolValue(z));
	}
	
	public static Operand createStringOperand(String s){
		return new ValueOperand(TempValueFactory.createTempStringValue(s));
	}

	public static Operand createTypeOperand(TypeValue typeVal){
		return new TypeOperand(typeVal);
	}
	
	public static Operand createNameOperand(String name){
		return new NameOperand(name);
	}
	
	public static Operand createIndexOperand(IIndexable base, JValue index, IIndexable iind){
		return new IndexOperand(base, index, iind);
	}
	
	public final static ValueOperand NullOperand = 
		new ValueOperand(TempValueFactory.createTempRefValue(RefValue.NULL));
	
//	public static Operand getNullOperand(){
//		return new ValueOperand(RefValue.NULL);
//	}
}
