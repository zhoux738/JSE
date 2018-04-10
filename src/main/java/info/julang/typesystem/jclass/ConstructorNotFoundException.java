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

import java.util.List;

import info.julang.interpretation.RuntimeCheckException;

public class ConstructorNotFoundException extends RuntimeCheckException {

	private static final long serialVersionUID = -7113197855009671339L;

	/**
	 * Cannot find a constructor to call for the given class type.
	 *  
	 * @param classWithoutCtor
	 * @param ctorCallChain can be null. If not null, this list contains the call chain of constructors up to <code>classWithoutCtor</code> 
	 */
	public ConstructorNotFoundException(JClassType classWithoutCtor, List<JClassType> ctorCallChain) {
		super(makeMsg(classWithoutCtor, ctorCallChain));
	}

	private static String makeMsg(JClassType classWithoutCtor, List<JClassType> ctorCallChain) {
		StringBuilder sb = new StringBuilder("Cannot find an appropriate constructor to call for class ");
		sb.append(classWithoutCtor.getName());
		if (ctorCallChain != null && ctorCallChain.size() > 0){
			sb.append(" when calling constructors in the order: ");
			sb.append(System.lineSeparator());
			sb.append("  ");
			for (JClassType jct : ctorCallChain){
				sb.append(jct.getName());
				sb.append(" => ");
			}
			sb.append(classWithoutCtor.getName());
		}
		
		return sb.toString();
	}

}
