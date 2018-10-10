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

import info.julang.typesystem.jclass.annotation.JAnnotation;
import info.julang.typesystem.jclass.builtin.JConstructorType;

/**
 * A constructor member in a class.
 * 
 * @author Ming Zhou
 *
 */
public class JClassConstructorMember extends JClassMember {

	/**
	 * The class contains information about the forward call (this() or super()) 
	 * this constructor is to make before executing the main body.
	 * 
	 * @author Ming Zhou
	 */
	public static class ForwardInfo {
		
		private boolean isSuper;
		
		private ConstructorForwardExecutable executable;
		
		public ForwardInfo(ConstructorForwardExecutable executable, boolean isSuper){
			this.executable = executable;
			this.isSuper = isSuper;
		}

		public boolean isSuper() {
			return isSuper;
		}
		
		public boolean isThis() {
			return !isSuper;
		}
		
		public ConstructorForwardExecutable getExecutable(){
			return executable;
		}
		
	}
	
	private JConstructorType ftype;
	
	private boolean defaultCtor;
	
	private ForwardInfo forwardInfo;
	
	public JClassConstructorMember(
		ICompoundType definingType, 
		String name, 
		Accessibility accessibility, 
		boolean isStatic,
		JConstructorType type,
		ForwardInfo forwardInfo,
		boolean isDefault, 
		JAnnotation[] annotations) {
		super(definingType, name, accessibility, isStatic, MemberType.CONSTRUCTOR, type, annotations);
		ftype = type;
		defaultCtor = isDefault;
		this.forwardInfo = forwardInfo;
	}
	
	/**
	 * A quicker way than calling {@link #getType()} to cast from.
	 */
	public JConstructorType getCtorType(){
		return ftype;
	}

	/**
	 * @return true if this is a default constructor (defined implicitly by scripting engine as user didn't provide one)
	 */
	public boolean isDefault() {
		return defaultCtor;
	}

	/**
	 * Get the forward info about this constructor member. 
	 * @return null if no forward call is explicitly declared for this constructor.
	 */
	public ForwardInfo getForwardInfo() {
		return forwardInfo;
	}
	
	@Override
	public String toString(){
		return ftype.toString();
	}
	
	@Override
	public MemberKey getKey(){
		return new ExecutableMemberKey(this.getCtorType(), this);
	}
}
