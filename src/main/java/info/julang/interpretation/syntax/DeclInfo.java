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

import info.julang.interpretation.errorhandling.IHasLocationInfo;
import info.julang.interpretation.errorhandling.ILocationInfoAware;
import info.julang.typesystem.jclass.Accessibility;

import java.util.ArrayList;
import java.util.List;

public abstract class DeclInfo implements IHasLocationInfo, ILocationInfoAware {

	private boolean isHosted;
	
	private boolean isFinal;
	
	private boolean isConst;

	private boolean isAbstract;
	
	private boolean isStatic;
	
	private Accessibility accessibility;

	private String name;
	
	private IHasLocationInfo linfo;
	
	protected List<AttributeDeclInfo> attributes;
	
	public boolean isFinal() {
		return isFinal;
	}

	// public for lazy statements
	public void setFinal() {
		this.isFinal = true;
	}
	
	public boolean isConst() {
		return isConst;
	}

	// public for lazy statements
	public void setConst() {
		this.isConst = true;
	}
	
	public boolean isHosted() {
		return isHosted;
	}

	// public for lazy statements
	public void setHosted() {
		this.isHosted = true;
	}
	
	public boolean isAbstract() {
		return isAbstract;
	}

	// public for lazy statements
	public void setAbstract() {
		this.isAbstract = true;
	}

	public boolean isStatic() {
		return isStatic;
	}

	// public for lazy statements
	public void setStatic() {
		this.isStatic = true;
	}

	public Accessibility getAccessibility() {
		return accessibility;
	}

	// public for lazy statements
	public void setAccessibility(Accessibility accessibility) {
		this.accessibility = accessibility;
	}
	
	public boolean isAccessibilitySet(){
		return accessibility != null;
	}

	public String getName() {
		return name;
	}

	// public for lazy statements
	public void setName(String name) {
		this.name = name;
	}
	
	// public for lazy statements
	public void addAttribute(AttributeDeclInfo attr){
		if(attributes==null){
			attributes = new ArrayList<AttributeDeclInfo>();
		}
		
		attributes.add(attr);
	}
	
	public List<AttributeDeclInfo> getAttributes(){
		return attributes;
	}
	
	void copyTo(DeclInfo other) {
		other.accessibility = this.accessibility;
		other.isAbstract = this.isAbstract;
		other.isConst = this.isConst;
		other.isFinal = this.isFinal;
		other.isStatic = this.isStatic;
		other.isHosted = this.isHosted;
		other.name = this.name;
		other.attributes = this.attributes;
	}
	
	abstract boolean allowModifier(Modifier modifier);
	
	//------------------ Location info about this declaration ------------------//
	
	public String getFileName(){
		return linfo != null ? linfo.getFileName() : "";
	}
	
	public int getLineNumber(){
		return linfo != null ? linfo.getLineNumber() : -1;
	}
	
	public void setLocationInfo(IHasLocationInfo linfo){
		this.linfo = linfo;
	}
}
