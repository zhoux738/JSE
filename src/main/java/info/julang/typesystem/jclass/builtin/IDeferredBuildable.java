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

import info.julang.interpretation.context.Context;
import info.julang.typesystem.BuiltinTypes;
import info.julang.typesystem.jclass.ICompoundTypeBuilder;

/**
 * A type that implements this interface will complete its building post bootstrapping.
 * <p>
 * A built-in class type, implemented purely in Java, may have references to JuFC types which are implemented in Julian.
 * During the normal bootstrapping we don't have access to any JuFC types yet. This interface allows such built-in types
 * to have a second chance to complete its building before the engine is started.
 * 
 * @author Ming Zhou
 */
public interface IDeferredBuildable {
	
	/**
	 * The built-in type. This is the type token used before the formal establishment of runtime type system.
	 */
	BuiltinTypes getBuiltinType();
	
	/**
	 * Set the type builder.
	 * 
	 * @param builder
	 */
	void setBuilder(ICompoundTypeBuilder builder);

	/**
	 * Perform deferred type building.
	 * 
	 * @param context
	 */
	void postBuild(Context context);

	/**
	 * To be called after the type is sealed.
	 */
	void preInitialize();
	
	/**
	 * Complete type building.
	 */
	void seal();
	
}
