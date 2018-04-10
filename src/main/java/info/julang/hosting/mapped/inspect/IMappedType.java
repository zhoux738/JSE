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

package info.julang.hosting.mapped.inspect;

/**
 * An interface to represent the script type a platform shall be mapped to.
 * <p>
 * At the point where we inspect and filter a platform type to get all the
 * members of which we are to add to the mapped Julian type, it's possible 
 * that we don't know if a platform type we encounter can be successfully 
 * mapped, so we must encapsulate the information and defer the decision 
 * to a later stage while building the class. This extra layer of wrapping
 * is also needed even if the type is known to be mappable, but we are
 * yet unable to get a handle of its mapped Julian type. For example, a 
 * field with type being the class type itself is surely mappable:<pre><code> class Node {
 *   Node n;
 * }</code></pre>
 * But since we don't have {@link JType} instance for this very type, we 
 * can only use an abstract layer to carry the information down the road.
 * 
 * @author Ming Zhou
 */
public interface IMappedType {

	/**
	 * Returns true if the mapped type is a platform type. Can be safely cast to {@link DeferredMappedType}.
	 */
	boolean isExternal();
	
	/**
	 * 0 if scalar; 1+ if this is an array type.
	 */
	int getDimension();
	
	/**
	 * Gets the original platform class. 
	 */
	Class<?> getOriginalClass();

	/**
	 * If the type serves as a parameter, this is the parameter's name. Otherwise undefined.
	 */
	String getParamName();
}
