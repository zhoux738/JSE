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

package info.julang.typesystem.jclass.builtin.doc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to add source code documentation to built-in types which do not have Julian source.
 * This annotation is used only by documentation extractor and thus do not preserve beyond compile time.
 * 
 * @author Ming Zhou
 */
@Target(value={ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface JulianDoc {

	/** 
	 * The overall description for the documented element. Since this it to be processed in the same way as
	 * the summary section for Julian code, it's expected to be enclosed by Julian block comment sequences,
	 * with each line optionally started by an '*'.  
	 */
	String summary();
	
	/**
	 * The alternative name supported directly by the language.
	 */
	String alias() default "";
	
	/**
	 * The formal name for this type/member. For a member, if this is not specified, the doc-gen tool will
	 * try to deduce it from the initializer. In the case of constructor, this field must be specified with
	 * with the class's name.
	 */
	String name() default "";
	
	/**
	 * Whether this member is static. Default to false (instance-scoped). Always use a boolean literal since 
	 * dog-gen tool cannot process otherwise.
	 * @return
	 */
	boolean isStatic() default false;
	
	/**
	 * Detailed description about the returned value. 
	 * Ignored if not annotating a field which contains the method implementation.
	 */
	String returns() default "";
	
	/**
	 * Detailed description about the parameters. 
	 * Ignored if not annotating a field which contains the method implementation.
	 * <p>
	 * Each element is a description corresponding to the parameter at the same index.
	 */
	String[] params() default {};
	
	/**
	 * The type for each parameter. The length should be aligned with that of {@link #params()}.
	 * Ignored if not annotating a field which contains the method implementation.
	 * <p>
	 * Each element is a Type name corresponding to the parameter at the same index.
	 */
	String[] paramTypes() default {};
	
	/**
	 * Detailed description about the exceptions to be thrown. 
	 * Ignored if not annotating a field which contains the method implementation.
	 * <p>
	 * Each element has the format "exception type: description".
	 */
	String[] exceptions() default {};
	
	/**
	 * Other related types.
	 * <p>
	 * Each element is a fully qualified type name.
	 */
	String[] references() default {};
	
	/**
	 * Interfaces implemented/extended by this type.
	 */
	String[] interfaces() default {};
	
}
