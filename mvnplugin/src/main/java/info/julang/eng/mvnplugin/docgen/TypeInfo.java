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

package info.julang.eng.mvnplugin.docgen;

import info.julang.interpretation.syntax.ClassSubtype;

/**
 * A static alternative to JType.
 * 
 * @author Ming Zhou
 */
public class TypeInfo {
	
	private String moduleName;
	private String simpleName;
	private int dim;
	private ClassSubtype subtyp;
	private DocModel.Type doc;
	
	public TypeInfo(String moduleName, String simpleName, int dim, ClassSubtype subtyp, DocModel.Type doc) {
		this.moduleName = moduleName;
		this.simpleName = simpleName;
		this.dim = dim;
		this.subtyp = subtyp;
		this.doc = doc;
	}
	
	public String getModuleName(){
		return moduleName;
	}
	
	public String getSimpleName(){
		return simpleName;
	}

	public int getDimension(){
		return dim;
	}
	
	public ClassSubtype getSubType(){
		return subtyp;
	}
	
	public DocModel.Type getTypeDoc(){
		return doc;
	}
	
	//------------ Helper Methods ------------//
	
	public boolean isBasic(){
		return subtyp == null;
	}
	
	public String getFullName(){
		String name = (moduleName != null && !"".equals(moduleName)) ? moduleName + "." + simpleName : simpleName;
		int d = dim;
		while(d>0) {
			name += "[]";
			d--;
		}
		
		return name;
	}
	
	@Override
	public String toString(){
		return getFullName();
	}
}
