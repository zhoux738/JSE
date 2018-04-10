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

package info.julang.modulesystem;

import info.julang.interpretation.syntax.ClassDeclInfo;

public class ClassInfo {

	private String name;
	
	private String fqName;
	
	private ClassDeclInfo info;
	
	private ScriptInfo scriptInfo;

	/**
	 * Create a new class info instance with name, the class info and the script info.
	 * 
	 * @param fqName
	 * @param name
	 * @param info
	 * @param scriptInfo
	 */
	public ClassInfo(String fqName, String name, ClassDeclInfo info, ScriptInfo scriptInfo) {
		this.fqName = fqName;
		this.name = name;
		this.info = info;
		this.scriptInfo = scriptInfo;
	}

	public String getName() {
		return name;
	}
	
	public String getFQName() {
		return fqName;
	}
	
	public ScriptInfo getScriptInfo() {
		return scriptInfo;
	}

	public ClassDeclInfo getClassDeclInfo() {
		return info;
	}

	//---------------------------------- Object ----------------------------------//
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fqName == null) ? 0 : fqName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassInfo other = (ClassInfo) obj;
		if (fqName == null) {
			if (other.fqName != null)
				return false;
		} else if (!fqName.equals(other.fqName))
			return false;
		return true;
	}
	
	@Override
	public String toString(){
		return fqName;
	}
}
