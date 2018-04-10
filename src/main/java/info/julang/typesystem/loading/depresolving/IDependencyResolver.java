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

package info.julang.typesystem.loading.depresolving;

import java.util.Collection;
import java.util.List;

/**
 * A dependency resolver is used to resolve the order in which a set of types are going to be loaded and initialized.
 * 
 * @author Ming Zhou
 */
public interface IDependencyResolver {

	/**
	 * Given a collection of {@link IOrderResolvable resolvables}, return a list containing the <b>same</b> 
	 * set of data, but ordered such that any type will be loaded after all the types it depends have been 
	 * done so.
	 * <p>
	 * Note there can be more than one legal result for the input. Whether this method should return 
	 * deterministically is undefined.
	 * 
	 * @param states Essentially a list of constant elements.
	 * @return a re-ordered list of the input elements. A type in this list doesn't depend on any types 
	 * after it, but may depend on a type before it. 
	 * All the elements in this list come directly from the input without being tampered with in any way.  
	 */
	List<IOrderResolvable> resolve(Collection<? extends IOrderResolvable> states);
	
}
