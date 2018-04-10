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

package info.julang.external.interfaces;

/**
 * The kind of JValue.
 * <p/>
 * JValueKind provides a quicker way to help determine what a value holds. 
 * This is different from {@link info.julang.typesystem.JTypeKind JTypeKind}, which describes 
 * the type a value is of from the perspective of runtime context. For example, in code
 * <pre>
 * <code>
 *   class C {
 *     ... ...
 *   }
 *   C c = new C();
 * </code>
 * </pre>
 * The JValueKind of the value that holds c in stack is REFERENCE, since the actual value of c, which c
 * points to, is stored in heap. On the other hand, JTypeKind of c is OBJECT, reflecting what c is meant to
 * be in both original source code and VM runtime context.
 */
public enum JValueKind {

	//--------- Basic ---------//
	
	BOOLEAN,
	
	CHAR,
	
	FLOAT,
	
	INTEGER,
	
	BYTE,
	
	//--------- Object ---------//

	OBJECT,
	
	//--------- Built-in Object ---------//

	STRING,
	
	ARRAY,
	
	FUNCTION,
	
	ENUM,
	
	ATTRIBUTE,
	
	//--------- Class member object ---------//
	
	METHOD,
	
	CONSTRUCTOR,
	
	METHOD_GROUP,
	
	//--------- Type ---------//
	
	TYPE,
	
	//--------- Reference ---------//
	
	REFERENCE,
	
	//--------- Special ---------//
	
	NONE, 
	
	HOSTED, 
	
	CUSTOMIZED,
	
	UNTYPED,
	
}
