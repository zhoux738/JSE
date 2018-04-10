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

package info.julang.typesystem.jclass.builtin;

import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.JDefinedClassType;

/**
 * The Attribute type as in<p/>
 * <pre><code><code>   attribute Author { 
 *     int year = Date.now().year;
 *     string name;
 *     ISBN number;
 *   }</code></code></pre> 
 * Attribute in Julian is implemented as a special class. It has <font color="green">Object</font> 
 * as parent class, and contains a number of fields, optionally with an initializer.
 * <p/>
 * The language provides a syntax for attribute declaration. The code above is actually equivalent to 
 * <pre><code>class Author : Attribute { 
 * 
 *   public int year = Date.now().year;
 *   
 *   public string name;
 *   
 *   public ISBN number;
 *   
 *   Author(int year, bool yearPassed, string name, bool namePassed, ISBN number, bool ISBNPassed){
 *     if(yearPassed){ this.year = year; }
 *     if(namePassed){ this.name = name; }
 *     if(ISBNPassed){ this.number = number; }
 *   }  
 *   
 * } </code></pre>
 * 
 * However, users cannot write this directly in their scripts.
 * <p/>
 * Attribute type is used to annotate class definitions. There are a couple of places where an attribute can be used:
 * <p/><pre>
 * <li>Class</li>
 * <code>[Author(name="Ming")]
 * class MyBook { ... }</code>
 * <li>Method</li>
 * <code>class MyLib { 
 *   [Logging(pattern="{time}: {method-name}")]
 *   void compute(){ ... } 
 * }</code>
 * <li>Field</li>
 * <code>class MyLib { 
 *   [Owner(name="Patrick")]
 *   int const id = ...;
 * }</code></pre>
 * <p/>
 * Where an attribute can be used is governed by meta-attribute <font color="green">System.Attribute.Target</font>. A 
 * more complete version of the Author example is given below: <pre><code><code>   [Target(value=new AttrTarget[]{AttrTarget.Class, AttrTarget.Method})]
 *   attribute Author { 
 *     int year = Date.now().year;
 *     string name;
 *     ISBN number;
 *   }</code></code></pre> 
 * 
 * @author Ming Zhou
 */
public class JAttributeType extends JDefinedClassType {
	
	public JAttributeType(String name) {
		super(name);
	}

	/**
	 * Check if a type is Attribute.
	 * @param typ
	 * @return
	 */
	public static boolean isAttributeType(JType typ){
		if(typ instanceof JAttributeType){
			return true;
		}
		
		return false;
	}
	
	/*
	// A native constructor
	// MyAttribute(paramT1 p1, bool p1_passed, ...){...}
	public void createConstructor(List<JClassMember> list) {
		JParameter[] ptypesArray = prepareParams(list);
		JConstructorType cType = new JConstructorType(ptypesArray, exec, builder.getStub());
		
		JClassConstructorMember cmember = new JClassConstructorMember(
			ctorDecl.getName(), 
			Accessibility.HIDDEN, 
			false,
			cType,
			null,
			false);
		builder.addInstanceConstructor(cmember);
	}
	
	private JParameter[] prepareParams(List<JClassMember> list){
		// "this" + (field + boolean) * N
		int size = 1 + list.size() * 2;
		
		List<JParameter> ptypes = new ArrayList<JParameter>();
		
		// Add "this" parameter for instance method.
		ptypes.add(
			new JParameter(
				KnownTokens.THIS_T.getLiteral(), // "this"
				builder.getStub() // self-reference
			)
		);
		
		for(JClassMember jcm : list){
			String pname = jcm.getName();
			// Add attribute field as parameter
			ptypes.add(new JParameter(pname, jcm.getType()));
			// Add a boolean field as an auxiliary parameter
			ptypes.add(new JParameter(getAuxBoolParamName(pname), BoolType.getInstance()));
		}
		
		JParameter[] ptypesArray = new JParameter[size];
		ptypes.toArray(ptypesArray);
		
		return ptypesArray;
	}
	
	private String getAuxBoolParamName(String name){
		return name + "_passed";
	}
	*/
}
