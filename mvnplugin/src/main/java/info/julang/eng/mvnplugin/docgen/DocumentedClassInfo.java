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

import info.julang.eng.mvnplugin.docgen.DocModel.TypeRef;
import info.julang.interpretation.syntax.ClassSubtype;
import info.julang.modulesystem.prescanning.RawClassInfo;
import info.julang.typesystem.loading.depresolving.IOrderResolvable;

import java.util.ArrayList;
import java.util.List;

/**
 * Built on top of {@link RawClassInfo}, this class implements {@link IOrderResolvable} so that it
 * can be resolved to get a loading order, which is required to properly fill in inherited documentation.
 *  
 * @author Ming Zhou
 */
public class DocumentedClassInfo extends RawClassInfo implements IOrderResolvable {

	public DocumentedClassInfo(String modName, RawClassInfo rci) {
		super(rci.getName(), rci.getDeclInfo());
		this.modName = modName;
	}

	private String modName;
	private DocModel.Type typeDoc;
	
	public void setDoc(DocModel.Type typeDoc){
		this.typeDoc = typeDoc;
	}
	
	public DocModel.Type getDoc(){
		return this.typeDoc;
	}
	
	public String getModuleName() {
		return modName;
	}

	//------------- IOrderResolvable -------------//
	
	@Override
	public String getTypeName() {
		return modName + "." + typeDoc.name;
	}

	@Override
	public List<String> getDependentTypeNames() {
		List<String> list = new ArrayList<String>();
		switch(typeDoc.subtype){
		case CLASS:
			DocModel.TypeRef ptr = ((DocModel.ClassType)typeDoc).parent;
			if (ptr != null) {
				list.add(ptr.getFullName());
			}
			// Fall through
		case INTERFACE:
			List<TypeRef> infs = ((DocModel.InterfaceType)typeDoc).interfaces;
			for (TypeRef tr : infs) {
				list.add(tr.getFullName());
			}
			break;
		case ENUM:
		case ATTRIBUTE:
		default:
			break;
		}
		
		return list;
	}

	@Override
	public boolean isAttributeType() {
		return typeDoc.subtype == ClassSubtype.ATTRIBUTE;
	}
}
