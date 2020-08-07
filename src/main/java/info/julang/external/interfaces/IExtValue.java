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

public interface IExtValue {

	/**
	 * Get the kind of this value.
	 * <p/>
	 * If the value is of any non-basic type (i.e. class type), this will always return {@link JValueKind#OBJECT}. 
	 * To know if it is an array, string or other built-in class type, use <code>{@link info.julang.memory.
	 * value.ObjectValue#getBuiltInValueKind() ObjectValue.getBuiltInValueKind()}</code>.
	 * @return
	 */
	JValueKind getKind();
	
	/**
	 * @return true if it is a basic type (int, bool, float, char)
	 */
	boolean isBasic();
	
	/**
	 * Get the mutability of this value.
	 * @return true if it is constant.
	 */
	boolean isConst();
	
	/**
	 * Whether this value is NULL.
	 * <p/>
	 * Basic values always return false; reference value returns true if it point to null.
	 * @return true if it is null.
	 */
	boolean isNull();
	
	//--------------- External interfaces for values ---------------//
	
	public static interface IIntVal {
		int getIntValue();
	}
	
	public static interface IFloatVal {
		float getFloatValue();
	}
	
	public static interface IByteVal {
		byte getByteValue();
	}
	
	public static interface IBoolVal {
		boolean getBoolValue();
	}
	
	public static interface ICharVal {
		char getCharValue();
	}

	public static interface IObjectVal {
		JValueKind getBuiltInValueKind();
	}
	
	public static interface IStringVal extends IObjectVal {
		String getStringValue();
	}
	
	public static interface IArrayVal extends IObjectVal {
		IExtValue get(int index);
		int getLength();
	}
	
	public static interface IEnumVal extends IObjectVal {
		int getOrdinal();
	}
	
	public static interface IHostedVal extends IObjectVal {
		Object getHostedObject();
	}
	
	public static interface IRefVal {
		IObjectVal getReferred();
	}
}
