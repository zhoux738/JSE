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

package info.julang.typesystem.jclass.jufc;

/**
 * Contains name of types which do not have platform backend.
 * 
 * @author Ming Zhou
 */
public class SystemTypeNames {

	public static final String System_Util_ArrayIterator = "System.Util.ArrayIterator";
	public static final String System_Util_StringIterator = "System.Util.StringIterator";
	public static final String System_Util_Entry = "System.Util.Entry";
	public static final String System_Util_IIndexable = "System.Util.IIndexable";
	public static final String System_Util_IMapInitializable = "System.Util.IMapInitializable";
	public static final String System_Util_IComparable = "System.Util.IComparable";
	public static final String System_Util_IIterator = "System.Util.IIterator";
	public static final String System_Util_IIterable = "System.Util.IIterable";
	
	public static final class MemberNames {
		public static final String AT = "at";
		public static final String SIZE = "size";
		public static final String GET_ITERATOR = "getIterator";
		public static final String INIT_BT_MAP = "initByMap";
	}
}
