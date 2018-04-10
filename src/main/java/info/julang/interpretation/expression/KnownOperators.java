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

import info.julang.interpretation.expression.operator.AddOp;
import info.julang.interpretation.expression.operator.AssignOp;
import info.julang.interpretation.expression.operator.BitwiseOp;
import info.julang.interpretation.expression.operator.CompareOp;
import info.julang.interpretation.expression.operator.CondOp;
import info.julang.interpretation.expression.operator.EvalOp;
import info.julang.interpretation.expression.operator.IsOp;
import info.julang.interpretation.expression.operator.LogicalOp;
import info.julang.interpretation.expression.operator.MultiplyOp;
import info.julang.interpretation.expression.operator.OnSelf1Op;
import info.julang.interpretation.expression.operator.OnSelf2Op;
import info.julang.interpretation.expression.operator.SelectOp;
import info.julang.interpretation.expression.operator.SubOp;

public class KnownOperators {

	public static final Operator ADD = new AddOp();
	public static final Operator SUB = new SubOp();	
	
	public static final Operator MULTIPLY = new MultiplyOp();
	//public static final Operator DIVIDE = new DivideOp(); // Need RT
	
	//public static final Operator INDEX = new IndexOp(); // Need RT
	//public static final Operator DOT = new DotOp(); // Need RT
	//public static final Operator CAST = new CastOp(); // Need RT
	
	public static final Operator  ASSIGN = new AssignOp();
	
	public static final OnSelf1Op INCREMENT = new OnSelf1Op.IncrementOp();
	public static final OnSelf1Op DECREMENT = new OnSelf1Op.DecrementOp();
	public static final Operator  MINUS = new OnSelf1Op.MinusOp();
	public static final Operator  PLUS = new OnSelf1Op.PlusOp();
	public static final Operator  COMPLEMENT = new OnSelf1Op.ComplementOp();
	
	public static final OnSelf2Op ADD_SELF = new OnSelf2Op.AddToSelfOp();
	public static final OnSelf2Op SUB_SELF = new OnSelf2Op.SubToSelfOp();
	public static final OnSelf2Op MULTIPLY_SELF = new OnSelf2Op.MulToSelfOp();
	public static final OnSelf2Op BITWISE_AND_SELF = new OnSelf2Op.BitwiseAndToSelfOp();
	public static final OnSelf2Op BITWISE_OR_SELF = new OnSelf2Op.BitwiseOrToSelfOp();
	public static final OnSelf2Op BITWISE_XOR_SELF = new OnSelf2Op.BitwiseXorToSelfOp();
	public static final OnSelf2Op BITWISE_LSHIFT_SELF = new OnSelf2Op.BitwiseLShiftToSelfOp();
	public static final OnSelf2Op BITWISE_RSHIFT_SELF = new OnSelf2Op.BitwiseRShiftToSelfOp();
	
	public static final CompareOp LT = new CompareOp.LessThanOp();
	public static final CompareOp LT_EQ = new CompareOp.LessThanEqualOp();
	public static final CompareOp GT = new CompareOp.GreaterThanOp();
	public static final CompareOp GT_EQ = new CompareOp.GreaterThanEqualOp();
	public static final Operator  IS = new IsOp();
	
	public static final CompareOp EQUAL = new CompareOp.EqualOp();
	public static final CompareOp NOT_EQUAL = new CompareOp.NotEqualOp();
	
	public static final BitwiseOp BITWISE_AND = new BitwiseOp.BandOp();
	public static final BitwiseOp BITWISE_OR = new BitwiseOp.BorOp();
	public static final BitwiseOp BITWISE_XOR = new BitwiseOp.BxorOp();
	public static final BitwiseOp BITWISE_LSHIFT = new BitwiseOp.BLShiftOp();
	public static final BitwiseOp BITWISE_RSHIFT = new BitwiseOp.BRShiftOp();
	
	public static final LogicalOp AND = new LogicalOp.AndOp();
	public static final LogicalOp OR = new LogicalOp.OrOp();
	public static final LogicalOp NOT = new LogicalOp.NegateOp();
	
	public static final CondOp    COND = new CondOp();
	public static final SelectOp  SELECT = new SelectOp();
	
	public static final Operator  EVAL = new EvalOp();	
	
}
