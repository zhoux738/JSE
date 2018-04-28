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
import java.util.LinkedList;
import java.util.List;

import info.julang.typesystem.jclass.annotation.MetaAnnotation;

/**
 * A very simple implementation that will load attributes first 
 * and then other types without honoring the dependencies.
 * 
 * @author Ming Zhou
 */
public class SimpleDependencyResolver implements IDependencyResolver {

	@Override
	public List<IOrderResolvable> resolve(Collection<? extends IOrderResolvable> states) {
		List<IOrderResolvable> ordered = new LinkedList<IOrderResolvable>();
		IOrderResolvable atType = null;
		int index = 0;
		for(IOrderResolvable state : states){			
			if(MetaAnnotation.AttributeTypeName.equals(state.getTypeName())){
				atType = state;
			} else {
				if(state.isAttributeType()){
					ordered.add(index, state);
					index++;
				} else {
					ordered.add(ordered.size(), state);
				}
			}
		}
		
		if(atType != null){
			ordered.add(0, atType);	
		}
		
		return ordered;
	}

}
