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

import info.julang.typesystem.jclass.builtin.JMethodType;

/**
 * A hidden member for initializing a class.
 * <p>
 * The class static constructor is a special type of static method that runs when a class is loaded 
 * (referenced for the first time). A class can have up to one such constructor which has a syntax
 * as follows:<pre><code>class MyClass {
 *   static MyClass () {
 *     ...
 *   }
 * }</code></pre>
 * Note it cannot have any other modifier than the required one <code>'static'</code>, and its parameter
 * list must be empty.
 * <p>
 * The static constructor is invoked after all the static field initializers, regardless of where it 
 * appears in the class definition relative to those fields declaration.
 * 
 * @author Ming Zhou
 */
public class JClassStaticConstructorMember extends JClassMethodMember {

	public JClassStaticConstructorMember(JMethodType type) {
		super(null, "__init_<ctor>", Accessibility.HIDDEN, true, false, type, null);
	}

	@Override
	public MemberType getMemberType(){
		return MemberType.STATIC_CONSTRUCTOR;
	}
}
