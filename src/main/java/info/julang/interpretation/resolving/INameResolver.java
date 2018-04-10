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

package info.julang.interpretation.resolving;

import info.julang.memory.value.JValue;

/**
 * Name resolver is a context-sensitive class that is used to resolve an identifier encountered bin script code. 
 * In Julian we have several different situations where the strategy for resolving identifiers can vary.
 * <p/>
 * <li>In the loose code, i.e. the code not present in method, the thread is running in a global environment and 
 * resolve names against a lexically scoped variable table. The bottom level of this table is the global variable 
 * table (<b>GVT</b>).
 * <li>Within the functions which are defined in loose code, we are resolving using two variables tables:
 * a local lexically scoped variable table (frame VT, or <b>FVT</b>) that belongs to the current frame, 
 * and <b>GVT</b>.
 * </li>
 * <li>For instance methods/constructors/initializers, the names are resolved against two parts: <b>FVT</b> 
 * and instance fields. And, of course, the special handling of 'this' is also included.
 * </li>
 * <li>For static methods and initializers, the names are resolved against two parts: <b>FVT</b> and class fields.
 * </li>
 * <br/><br/>
 * Name resolver is only used to resolve a simple name, but never a composite name, such as "a.b". This is because
 * for "a.b" it is either "a" getting resolved first by name resolver, in which case the part "b" will be interpreted
 * by {@link info.julang.interpretation.expression.operator.DotOp dot operator} ("."); or "a.b" getting 
 * resolved in entirety to be a type value by dot operator. Since name can be resolved either in name resolver or
 * by dot operator, the accessibility check must be performed at both places.
 * <br/><br/>
 * @author Ming Zhou
 */
public interface INameResolver {

	/**
	 * Resolve an identifier to a value.
	 * @param id Note we intentionally elect to not use 
	 * {@link info.julang.interpretation.expression.operand.NameOperand NameOperand} or 
	 * {@link info.julang.token.IdToken IdToken} here since they are too restrictive.
	 * @return
	 */
	JValue resolve(String id);
	
}
