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

package info.julang.typesystem.jclass;

import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.builtin.JObjectType;

/**
 * This class describes the properties related to what a function/method can return/throw.
 *
 * @author Ming Zhou
 */
public class JReturn implements IUntyped {

	/**
	 * A return of untyped value.
	 */
	public final static JReturn UntypedReturn = new JReturn();
	
	private JType type;
	
	private boolean untyped;

	/**
	 * If {@link #isUntyped()} is true, returns {@link JObjectType}, although it doesn't really matter.
	 * @return
	 */
	public JType getReturnType() {
		return type;
	}

	void setType(JType type) {
		this.type = type;
	}

	// Only for type builder
	public JReturn(JType type) {
		this.type = type;
	}
	
	// Only for type builder
	private JReturn() {
		this.type = JObjectType.getInstance();
		this.untyped = true;
	}
	
	/**
	 * If true, {@link #getType()} returns {@link JObjectType}, although that doesn't really matter.
	 * @return
	 */
	@Override
	public boolean isUntyped() {
		return untyped;
	}
	
}
