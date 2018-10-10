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

package info.julang.typesystem.loading;

/**
 * The reason for which a type is getting loaded.
 * 
 * @author Ming Zhou
 */
public enum LoadingInitiative {

	/**
	 * Loading triggered by encountering the type usage in source code. Note "import MODULE" 
	 * doesn't trigger type loading; only by the time a particular type is referenced at a 
	 * source line will that type be loaded.
	 */
	SOURCE,
	
	/**
	 * Loading explicitly initiated through reflection API.
	 */
	DYNAMIC,
	
	/**
	 * Loading triggered due to reference to another type in the type definition. This causes
	 * re-entrance of loading process and expands the closure of loading set.
	 */
	TYPE_REFERENCE,
	
	/**
	 * Loading triggered due to reference to another type in the the member definition of an 
	 * Attribute type. This is similar to {@link #TYPE_REFERENCE} but some additional check
	 * will be performed at the end of loading to ensure certain rules regarding type usage
	 * on an Attribute definition.
	 */
	ATTRIBUTE_MEMBER
}
