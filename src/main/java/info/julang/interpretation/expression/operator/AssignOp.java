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

import static info.julang.langspec.Operators.ASSIGN;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.expression.Operand;
import info.julang.interpretation.expression.Operator;
import info.julang.interpretation.expression.operand.IndexOperand;
import info.julang.interpretation.expression.operand.OperandKind;
import info.julang.memory.value.AttemptToAssignToNonLeftValueException;
import info.julang.memory.value.JValue;
import info.julang.memory.value.indexable.JIndexable;

/**
 * Assignment operation, such as <code>a = 5</code>. Returns the assignee.
 * 
 * @author Ming Zhou
 */
public class AssignOp extends Operator {

	public AssignOp() {
		super("=", 2, ASSIGN.precedence, ASSIGN.associativity);
	}

	@Override
	protected Operand doApply(Context context, Operand[] operands) {
		Operand assignee = operands[0];
		checkLeftValue(assignee);
		JValue lval = getValue(context, assignee, false, false);// do not unwrap the left value
		JValue rval = getValue(context, operands[1]); 

		Operand res = Operand.createOperand(
			assignee.getKind() == OperandKind.INDEX ?
				assignToIndex((IndexOperand)assignee, lval, rval) : 
				assign(lval, rval));
		
		return res;
	}

	private void checkLeftValue(Operand assignee) {
		switch(assignee.getKind()){
		case NAME:
		case INDEX:
		case IMEMBER:
		case SMEMBER:
			return;
		default:
			throw new AttemptToAssignToNonLeftValueException();
		}
	}

	private JValue assignToIndex(IndexOperand assignee, JValue lval, JValue rval){
		// When the left value is index-addressable, we must assign it using the method 
		// exposed on JIndexable. This is not only for uniformity but more importantly the 
		// correctness. Since System.Collection.List and System.Collection.Map are indexable,
		// we cannot modify an indexed value by directly assigning to the one returned
		// by get() method, since that is merely an on-stack copy of the original value due
		// to replication at the end of method call.
		JIndexable base = assignee.getBase();
		JValue index = assignee.getIndex();
		lval = base.setByIndex(index, rval);
		return lval;
	}
	
	private JValue assign(JValue lval, JValue rval){
		rval.assignTo(lval);
		return lval;
	}

}
