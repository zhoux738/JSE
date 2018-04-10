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

package info.julang.typesystem.jclass.annotation;

import java.util.ArrayList;
import java.util.List;

import info.julang.langspec.ast.JulianParser.AnnotationContext;
import info.julang.langspec.ast.JulianParser.Atrribute_initializationContext;
import info.julang.parser.AstInfo;
import info.julang.typesystem.jclass.builtin.JAttributeType;

/**
 * A class to carry annotation info about a type or type member.
 * 
 * @author Ming Zhou
 */
public class JAnnotation {

	private JAttributeType attrType;

	private List<Atrribute_initializationContext> ais;
	
	private AstInfo<AnnotationContext> ainfo;
	
	public JAnnotation(JAttributeType attrType, List<Atrribute_initializationContext> ais, AstInfo<AnnotationContext> ainfo) {
		this.attrType = attrType;
		this.ais = ais != null ? ais: new ArrayList<Atrribute_initializationContext>();
		this.ainfo = ainfo;
	}
	
	/**
	 * Get initializers for each field of the attribute, if specified. In
	 * <p>
	 * &nbsp;&nbsp;<code>[Author (name="Ming", year=2015)]</code>
	 * <p> 
	 * both <code>name="Ming"</code> and <code>year=2015</code> are initializers.</pre>
	 * @return
	 */
	public List<Atrribute_initializationContext> getFieldInitializers(){
		return ais;
	}
	
	public AstInfo<AnnotationContext> getAstInfo(){
		return ainfo;
	}
	
	public JAttributeType getAttributeType(){
		return attrType;
	}
	
}
