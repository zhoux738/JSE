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

import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.syntax.ClassSubtype;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.ICompoundTypeBuilder;
import info.julang.typesystem.jclass.IDefinedType;
import info.julang.typesystem.jclass.JDefinedClassType;
import info.julang.typesystem.jclass.JDefinedInterfaceType;
import info.julang.typesystem.jclass.builtin.JAttributeType;
import info.julang.typesystem.jclass.builtin.JEnumType;

import java.util.ArrayList;
import java.util.List;

public class LoadingState implements ILoadingState {

	private Thread owner;
	
	private boolean parsed;
	
	private boolean faulted;
	
	private Exception exception;
	
	private List<String> deps;
	
	private ICompoundType type;
	
	private LoadingInitiative initiative;
	
	LoadingState(Thread owner, String typName, ClassSubtype subtype, LoadingInitiative initiative){
		this.owner = owner;
		this.initiative = initiative;
		switch(subtype){
		case CLASS:
			this.type = JDefinedClassType.startNewClass(typName);
			break;
		case INTERFACE:
			this.type = JDefinedInterfaceType.startNewInterface(typName);
			break;
		case ENUM:
			this.type = new JEnumType(typName);
			break;
		case ATTRIBUTE:
			this.type = new JAttributeType(typName);
			break;
		default:
			throw new JSEError("Unsupported class subtype: " + subtype.name());
		}
	}
	
	@Override
	public LoadingInitiative getInitiative(){
		return initiative;
	}
	
	@Override
	public IDefinedType getType(){
		return (IDefinedType)type;
	}
	
	@Override
	public List<String> getDependentTypeNames(){
		return deps;
	}
	
	@Override
	public String getTypeName(){
		return type.getName();
	}
	
	@Override
	public boolean isAttributeType(){
		return JAttributeType.isAttributeType(type);
	}
	
	@Override
	public boolean isSealed(){
		return isParsed() || isFaulted();
	}
	
	@Override
	public boolean isParsed(){
		return parsed;
	}
	
	@Override
	public void setParsed(){
		parsed = true;
	}
	
	@Override
	public boolean isFaulted(){
		return faulted;
	}
	
	@Override
	public void setFaulted(Exception ex){
		faulted = true;
		exception = ex;
	}
	
	@Override
	public void addDependency(String dep){
		if(deps==null){
			deps = new ArrayList<String>();
		}
		
		deps.add(dep);
	}
	
	@Override
	public Thread getOwner(){
		return owner;
	}

	@Override
	public ICompoundTypeBuilder getBuilder() {
		return ((IDefinedType)type).getBuilder();
	}

	@Override
	public Exception getException() {
		return exception;
	}
	
	//------------------ Java Object ------------------//
	
	@Override
	public  String toString(){
		return type.getName();
	}
}
