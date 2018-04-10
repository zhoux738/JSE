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

import info.julang.external.exceptions.JSEError;
import info.julang.modulesystem.naming.FQName;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 * The most basic declared information of a class or class-like type.
 * 
 * @author Ming Zhou
 */
public class ClassDeclInfo extends DeclInfo {

	private ClassSubtype subtype;
	
	private FQName fullName;
	
	private List<ParsedTypeName> parentNames;
	
	protected ParserRuleContext declTree;
	
	public ClassDeclInfo(){
		
	}
	
	void setAST(ParserRuleContext declTree){
		this.declTree = declTree;
	}
	
	public ParserRuleContext getAST(){
		return declTree;
	}
	
	// public for lazy statements
	public void addParentTypeName(ParsedTypeName name){
		if(parentNames==null){
			parentNames = new ArrayList<ParsedTypeName>();
		}
		
		parentNames.add(name);
	}
	
	/**
	 * 
	 * @return Can be null if there is no parent classes.
	 */
	public List<ParsedTypeName> getParentTypes(){
		return parentNames;
	}

	// public for lazy statements
	public void setFQName(FQName fullName) {
		this.fullName = fullName;
	}
	
	public FQName getFQName() {
		return fullName;
	}
	
	/**
	 * This will return simple name. For full name call {@link #getFQName()}.
	 */
	@Override
	public String getName(){
		return super.getName();
	}

	/**
	 * Get the subtype of this class declaration. 
	 * @return
	 */
	public ClassSubtype getSubtype() {
		return subtype;
	}

	public void setSubtype(ClassSubtype subtype) {
		this.subtype = subtype;
	}
	
	@Override
	boolean allowModifier(Modifier modifier){
		switch (subtype){
		case CLASS:
			switch (modifier){
			case PUBLIC:    return true;
			case PROTECTED: return false;
			case PRIVATE:   return false;
			case INTERNAL:  return true;
			case STATIC:    return false;
			case HOSTED:    return false;
			case FINAL:     return true;
			case CONST:     return false;
			case ABSTRACT:  return true;
			default: break;
			}
		case ATTRIBUTE:
		case ENUM:
		case INTERFACE:
			switch (modifier){
			case PUBLIC:    return true;
			case PROTECTED: return false;
			case PRIVATE:   return false;
			case INTERNAL:  return true;
			case STATIC:    return false;
			case HOSTED:    return false;
			case FINAL:     return false;
			case CONST:     return false;
			case ABSTRACT:  return false;
			default: break;
			}
		default:
			break;
		}
		
		throw new JSEError("Unknow modifier: " + modifier.name());
	}
}
