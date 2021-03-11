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

package info.julang.typesystem;

import info.julang.JSERuntimeException;
import info.julang.interpretation.errorhandling.KnownJSException;

/**
 * The exception thrown when trying to reference a type number in an unexpected way.
 * <p/>
 * Call any of the static methods to create a new instance of this exception.
 * 
 * @author Ming Zhou
 */
public class IllegalMemberAccessException extends JSERuntimeException {

	private enum AccessExceptionType {
		ACC_INSTANCE_MEMBER_FROM_STATIC_CONTEXT,
		ACC_INVISIBLE_MEMBER,
		ACC_RESERVED_FOR_SYSTEM_USE_MEMBER,
		ACC_ATTEMPT_MEMBER_ACCESS_ON_NON_OBJECT,
		ACC_ATTEMPT_OVERWRITE_SEALED_DYNAMIC
	}
	
	private static final long serialVersionUID = -1921742899860918060L;

	public static IllegalMemberAccessException tryToReferenceUnboundThis() {
		throw new IllegalMemberAccessException("Cannot access to unbound 'this'.");
	}
	
	private IllegalMemberAccessException(String msg) {
		super(msg);
	}
	
	/**
	 * Create a new IllegalMemberAccessException with type and member name.
	 * @param tName
	 * @param mName
	 */
	private IllegalMemberAccessException(String tName, String mName, AccessExceptionType typ) {
		super(createMsg(tName, mName, typ));
	}

	private static String createMsg(String tName, String mName, AccessExceptionType typ) {
		switch(typ){
		case ACC_INSTANCE_MEMBER_FROM_STATIC_CONTEXT:
			return "Cannot refer to an instance-level member (" + mName + ") in a static context. Type name: " + tName;
		case ACC_INVISIBLE_MEMBER:
			return "Cannot refer to an invisible class member (" + mName + "). Type name: " + tName;
		case ACC_RESERVED_FOR_SYSTEM_USE_MEMBER:
			return "Cannot refer to a class member (" + mName + ") reserved for system use. Type name: " + tName;
		case ACC_ATTEMPT_MEMBER_ACCESS_ON_NON_OBJECT:
			return "Cannot refer to any member on type " + tName;
		case ACC_ATTEMPT_OVERWRITE_SEALED_DYNAMIC:
			return "Cannot overwrite a property (" + mName + ") on a sealed Dynamic object.";
		}
		
		return "Illegal access to member \"" + mName + "\" of type " + tName;
	}
	
	/**
	 * Make an exception about trying to access to A.B, where A is type name but B is an instance member.
	 *  
	 * @param tName type name
	 * @param mName member name
	 * @return
	 */
	public static IllegalMemberAccessException referInstMemInStaticCtxtEx(String tName, String mName){
		return new IllegalMemberAccessException(tName, mName, AccessExceptionType.ACC_INSTANCE_MEMBER_FROM_STATIC_CONTEXT);
	}
	
	/**
	 * Make an exception about trying to access to A.B, where B's accessibility is restricted at the callsite.
	 *  
	 * @param tName type name
	 * @param mName member name
	 * @return
	 */
	public static IllegalMemberAccessException referInvisibleMemberEx(String tName, String mName){
		return new IllegalMemberAccessException(tName, mName, AccessExceptionType.ACC_INVISIBLE_MEMBER);
	}
	
	/**
	 * Make an exception about trying to access to A.B, where B is reserved for system use.
	 *  
	 * @param tName type name
	 * @param mName member name
	 * @return
	 */
	public static IllegalMemberAccessException referReservedMemberEx(String tName, String mName){
		return new IllegalMemberAccessException(tName, mName, AccessExceptionType.ACC_RESERVED_FOR_SYSTEM_USE_MEMBER);
	}
	
	/**
	 * Make an exception about trying to access to A.B, where A is not an Object.
	 * 
	 * @param tName
	 * @return
	 */
	public static IllegalMemberAccessException referMemberOnNonObjectEx(String tName){
		return new IllegalMemberAccessException(tName, null, AccessExceptionType.ACC_ATTEMPT_MEMBER_ACCESS_ON_NON_OBJECT);
	}
	
	/**
	 * Make an exception about trying to write to A.B, where A is a Dynamic object that was initialized with sealed == true.
	 * 
	 * @param mName
	 * @return
	 */
	public static IllegalMemberAccessException overwriteSealedDynamicEx(String mName){
		return new IllegalMemberAccessException(null, mName, AccessExceptionType.ACC_ATTEMPT_OVERWRITE_SEALED_DYNAMIC);
	}

	@Override
	public KnownJSException getKnownJSException() {
		return KnownJSException.IllegalMemberAccess;
	}

}
