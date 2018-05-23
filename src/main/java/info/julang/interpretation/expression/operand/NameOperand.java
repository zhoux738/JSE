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

package info.julang.interpretation.expression.operand;

import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.expression.Operand;
import info.julang.langspec.Keywords;

/**
 * A name operand contains a partially qualified name. Such operands are typically created 
 * from identifiers.
 * <p/>
 * To get access to the value represented by this name, need first resolve the name in a
 * given context.
 *
 * @author Ming Zhou
 */
public class NameOperand extends Operand {
	
	private String name;
	
	private boolean composite;
	
	private boolean mutable;
	
	public static final NameOperand THIS = new NameOperand(Keywords.THIS, false);
	
	public static final NameOperand SUPER = new NameOperand(Keywords.SUPER, false);
	
	public String getName() {
		return name;
	}
	
	public NameOperand(String name, boolean mutable){
		this.name = name;
		this.mutable = mutable;
	}

	@Override
	public OperandKind getKind() {
		return OperandKind.NAME;
	}
	
	public void addPart(String part){
		if (mutable) {
			composite = true;
			this.name += "." + part;
		} else {
			throw new JSEError("Trying to mutate immutable name operand: " + this.name);
		}
	}

	/**
	 * Check if this name is a composite name, namely containing more than one part, separated by '.'
	 * @return
	 */
	public boolean isComposite() {
		return composite;
	}
	
	// Mainly for debugging
	@Override
	public String toString(){
		return "(Name Operand) " + this.name;
	}
	
}
