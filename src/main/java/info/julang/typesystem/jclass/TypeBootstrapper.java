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

import info.julang.typesystem.jclass.BuiltinTypeBootstrapper.TypeFarm;

public interface TypeBootstrapper {

	/**
	 * Provide a prototype which can serve as a stub during bootstrapping process.
	 * <p/>
	 * This method should always returns the same object.
	 * 
	 * @return
	 */
	JClassType providePrototype();
	
	/**
	 * Implement the type: add fields, methods, constructors and initializers to the type prototype.
	 * <p/>
	 * To refer to other types, use type farm. 
	 * 
	 * @param builder the builder used to build this type.
	 * @param farm a collection of type suites. Each suite contains a bootstrapping class's stub and builder.
	 */
	void implementItself(JClassTypeBuilder builder, TypeFarm farm);
	
	/**
	 * Bootstrap the type. This usually means setting the default instance for the corresponding class.
	 * <p/>
	 * Calling this method marks the end of type initializing.
	 * 
	 * @param builder
	 */
	void bootstrapItself(JClassTypeBuilder builder);
	
	/**
	 * Reset the built type. Used for tests.
	 */
	void reset();

	/**
	 * The fully qualified type name in Julian's type system.
	 * @return
	 */
	String getTypeName();
	
	/**
	 * Whether the one-dimensional array type for this type should also be initiated. If true, the array type will
	 * become available in {@link TypeFarm}, and they will be added to type table after bootstrapping stage.
	 * 
	 * @return
	 */
	boolean initiateArrayType();
}
