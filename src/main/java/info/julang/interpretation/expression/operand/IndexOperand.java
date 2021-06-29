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

import info.julang.memory.value.JValue;
import info.julang.memory.value.indexable.IIndexable;

/**
 * A member operand that represents the value of an indexed member of some array instance.
 * <p>
 * For now we simply only save the resolved value here. The reference to array instance 
 * and the index value are not tracked.
 * 
 * @author Ming Zhou
 */
public class IndexOperand extends ValueOperand {
	
	private IIndexable base;
	private JValue index;
	private IIndexable indexer;
	
	public IndexOperand(IIndexable base, JValue index, IIndexable indexer){
		super(null);
		this.base = base;
		this.index = index;
		this.indexer = indexer;
	}

	@Override
	public OperandKind getKind() {
		return OperandKind.INDEX;
	}
	
	@Override
	public JValue getValue() {
		if (value == null) {
			value = indexer.getByIndex(index); // This may throw ArrayOutOfRange exception
		}
		
		return value;
	}
	
	public IIndexable getBase(){
		return base;
	}
	
	public JValue getIndex(){
		return index;
	}
	
}
