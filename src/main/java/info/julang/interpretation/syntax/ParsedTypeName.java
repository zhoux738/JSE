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

package info.julang.interpretation.syntax;

import info.julang.modulesystem.naming.FQName;
import info.julang.typesystem.JType;

public class ParsedTypeName {

	/**
	 * A special ParsedTypeName used for untyped variable.
	 */
	public static final ParsedTypeName ANY = new ParsedTypeName((JType)null){
		public void addSection(String name){
		}
		public void addArrayDimension(){
		}
	};
	
	private int dim = 0;
	
	private StringBuilder fullName;
	
	private int sections = 0;
	
	private JType basicType;
	
	public static ParsedTypeName makeFromFullName(String fullName){
		String[] sections = fullName.split(".");
		if(sections.length == 0){
			sections = new String[]{ fullName };
		}
		ParsedTypeName ptn = new ParsedTypeName(sections[0]);
		for(int i=1;i<sections.length;i++){
			ptn.addSection(sections[i]);	
		}
		return ptn;
	}
	
	public ParsedTypeName(String start){
		fullName = new StringBuilder(start);
		sections++;
	}
	
	public ParsedTypeName(JType typ){
		basicType = typ;
		sections++;
	}
	
	public void addSection(String name){
		fullName.append(".");
		fullName.append(name);
		sections++;
	}
	
	public void addArrayDimension(){
		dim++;
	}
	
	void setArrayDimension(int dim){
		this.dim = dim;
	}	
	
	/**
	 * The total count of sections. 3 for "A.B.C".
	 * @return
	 */
	public int getSections(){
		return sections;
	}
	
	public FQName getFQName(){
		return new FQName(fullName.toString());
	}
	
	/**
	 * Get the fully qualified name in the form of string. This method is mainly 
	 * intended for informational use; it doesn't return array dimension info.
	 */
	public String getFQNameInString(){
		return fullName != null ? 
			fullName.toString() : 
			(basicType != null ? basicType.getName() : "<unknown>");
	}
	
	public boolean isArray(){
		return dim > 0;
	}

	/**
	 * @return 0 if it is NOT an array type.
	 */
	public int getDimensionNumber(){
		return dim;
	}
	
	/**
	 * @return null if it is not a basic type.
	 */
	public JType getBasicType(){
		return basicType;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		if (basicType != null){
			sb.append(basicType.getName());
		} else if (fullName != null){
			sb.append(fullName.toString());
		} else if (this == ANY){
			sb.append("var");
		} else {
			sb.append("(unknown)");
		}
		
		for(int i = 0; i < dim; i++){
			sb.append("[]");
		}
		
		return sb.toString();
	}
}
