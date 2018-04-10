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

import info.julang.interpretation.syntax.ClassSubtype;
import info.julang.typesystem.jclass.ICompoundTypeBuilder;
import info.julang.typesystem.jclass.IDefinedType;
import info.julang.typesystem.jclass.JClassTypeBuilder;
import info.julang.typesystem.jclass.JDefinedClassType;
import info.julang.typesystem.jclass.JDefinedInterfaceType;
import info.julang.typesystem.jclass.JInterfaceTypeBuilder;
import info.julang.typesystem.jclass.builtin.JAttributeType;
import info.julang.typesystem.jclass.builtin.JEnumType;
import info.julang.typesystem.loading.depresolving.IOrderResolvable;

import java.util.List;

/**
 * An interface defining some properties of a loading state.
 * 
 * @author Ming Zhou
 */
public interface ILoadingState extends IOrderResolvable {

	/**
	 * Get the type being loaded.
	 * @return {@link JDefinedClassType}, {@link JDefinedInterfaceType}
	 */
	IDefinedType getType();
	
	boolean isSealed();
	
	boolean isParsed();
	
	void setParsed();
	 
	boolean isFaulted();
	
	void setFaulted(Exception ex);
	
	void addDependency(String dep);
	
	/**
	 * Get the loading thread.
	 * @return
	 */
	Thread getOwner();
	
	/**
	 * Get the builder for this class type. This builder may be one of the following types:
	 * <p/>
	 * For a class: {@link JDefinedClassType} <br/>
	 * For an enum: {@link JEnumType} <br/>
	 * For an attribute: {@link JAttributeType} <br/>
	 * <p/>
	 * See {@link ClassSubtype}.
	 * @return {@link JClassTypeBuilder} or {@link JInterfaceTypeBuilder}
	 */
	ICompoundTypeBuilder getBuilder();
	
	Exception getException();
}
