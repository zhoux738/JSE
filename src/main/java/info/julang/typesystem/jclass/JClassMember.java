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
import info.julang.typesystem.jclass.annotation.IAnnotated;
import info.julang.typesystem.jclass.annotation.JAnnotation;

/**
 * This class describes the following properties of a class member:
 * <li>name: the name, as in script source, of this member </li>
 * <li>accessibility: the visibility of this type to other types </li>
 * <li>static: whether this member is static </li>
 * <li>type: the type of this member </li>
 * <p/>
 * 
 *  @author Ming Zhou
 */
public abstract class JClassMember implements IAnnotated {

	private String name;
	
	private Accessibility accessibility;
	
	private boolean _static;
	
	private JType type;

	private MemberType memberType;
	
	private JAnnotation[] annotations;
	
	private ICompoundType definingType;
	
	/**
	 * Create a new class member.
	 * 
	 * @param definingType	the type in which this member is defined.
	 * @param name			the name of this member.
	 * @param accessibility	the accessibility.
	 * @param isStatic		true if static; false if per-instance.
	 * @param memberType	such as {@link MemberType#FIELD FIELD} or {@link MemberType#METHOD METHOD}.
	 * @param type			the type of this member, with different semantics based on the member type.
	 * @param annotations	the annotations on this member.
	 */
	public JClassMember(
		ICompoundType definingType,
		String name, 
		Accessibility accessibility, 
		boolean isStatic, 
		MemberType memberType, 
		JType type, 
		JAnnotation[] annotations) {
		this.definingType = definingType;
		this.name = name;
		this.accessibility = accessibility;
		this._static = isStatic;
		this.type = type;
		this.memberType = memberType;
		this.annotations = annotations;
	}
	
	/**
	 * The type of this member, such as {@link MemberType#FIELD FIELD} or {@link MemberType#METHOD METHOD}.
	 */
	public MemberType getMemberType(){
		return memberType;
	}
	
	/**
	 * Get the type in which this member is originally defined.
	 */
	public ICompoundType getDefiningType(){
		return definingType;
	}
	
	/**
	 * The name, as in script source, of this member.
	 */
	public String getName() {
		return name;
	}

	/**
	 * The visibility of this type to other types. 
	 * 
	 * @see Accessibility
	 */
	public Accessibility getAccessibility() {
		return accessibility;
	}

	/**
	 * Whether this member is static.
	 * 
	 * @return true is static
	 */
	public boolean isStatic() {
		return _static;
	}

	/**
	 * The type of this member. Semantics dependent on the concrete class.
	 */
	public JType getType() {
		return type;
	}
	
	/**
	 * Get the annotations for this member. Null if no annotation.
	 */
	public JAnnotation[] getAnnotations(){
		return annotations;
	}

	/**
	 * Used to compare the equality between class members <b>within the same class</b>.
	 * <p/>
	 * A class member a is equal to b when both have same name and are of same type, plus meeting any 
	 * additional conditions as required by different member types.
	 */
	public MemberKey getKey(){
		return new MemberKeyBase(this);
	}
}
