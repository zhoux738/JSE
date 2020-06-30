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

import java.util.List;

import info.julang.JSERuntimeException;
import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.errorhandling.IHasLocationInfo;
import info.julang.interpretation.errorhandling.ILocationInfoAware;
import info.julang.interpretation.syntax.AttributeDeclInfo;
import info.julang.interpretation.syntax.ClassDeclInfo;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.ICompoundTypeBuilder;
import info.julang.typesystem.jclass.JClassProperties;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JClassTypeBuilder;
import info.julang.typesystem.jclass.JInterfaceType;
import info.julang.typesystem.jclass.annotation.JAnnotation;
import info.julang.typesystem.jclass.builtin.JObjectType;

/**
 * The statement for declaring (and defining) a class.
 * <p/>
 * Syntax:
 * <pre><code>[accessibility] [extensibility] <b>class</b> {NAME} : {PARENT_NAME}, {INTERFACE_NAME}* {
 * 
 *}</code></pre>where,
 * <ul>
 * <li>accessibility: <code>public|internal|(none)</code></li>
 * <li>extensibility: <code>final|abstract|(none)</code></li>
 * </ul>
 * The <code>{PARENT_NAME}</code> after <code>:</code> is the parent class to inherit from; 
 * <code>{INTERFACE_NAME}</code>s are the interfaces to implement.
 * <p/>
 * @author Ming Zhou
 */
public class ClassDeclarationStatement extends ClassLoadingStatement {

	@Override
	public void parse(LoadingContext context) {		
		try {
			parse0(context);
		} catch (JSERuntimeException jse) {
			// Process only if the exception doesn't have location info
			boolean proceed = true;
			if (jse instanceof IHasLocationInfo){
				IHasLocationInfo hl = (IHasLocationInfo) jse;
				proceed = hl.getLineNumber() == -1;
			}
			
			if (proceed){
				if (jse instanceof ILocationInfoAware){
					ILocationInfoAware li = (ILocationInfoAware) jse;
					li.setLocationInfo(context.getClassDeclInfo());
				}
			}
			
			throw jse;
		}
	}

	private void parse0(LoadingContext context) {		
		ClassDeclInfo declInfo = context.getClassDeclInfo();
		
		if (!Character.isUpperCase(declInfo.getName().charAt(0))) {
			throw new IllegalClassDefinitionException(
				context, "The type's name \"" + declInfo.getName() + "\" is not started with a character in uppercase.", declInfo);
		}
		
		Accessibility acc = declInfo.getAccessibility();
		boolean abs = declInfo.isAbstract();
		boolean fin = declInfo.isFinal();
		boolean sta = declInfo.isStatic();
		
		if(abs && fin){
			throw new IllegalClassDefinitionException(
				context, "Cannot have both abstract and final modifiers on a type definition.", declInfo);
		}
		
		ICompoundTypeBuilder ctb = context.getTypeBuilder();
		if (!ctb.isClassType()){
			throw new JSEError("Must build a class type with a class type builder.");
		}
		
		JClassTypeBuilder builder = (JClassTypeBuilder) ctb;
		builder.setAbstract(abs);
		builder.setFinal(fin);
		builder.setStatic(sta);
		
		if(acc == null){
			acc = Accessibility.PUBLIC;
		}
		builder.setAccessibility(acc);
		
		builder.setNamespacePool(context.getNamespacePool());
		
		List<ParsedTypeName> parents = declInfo.getParentTypes();
		boolean alreadyDefinedParentClass = false;
		if(parents != null && parents.size() > 0){
			for(ParsedTypeName ptn : parents){
				JType pt = loadType(context, ptn);
				if (!pt.isObject()){
					throw new IllegalClassDefinitionException(
						context, 
						"Type " + declInfo.getFQName() + 
						" cannot inherit or implement a non-class type (" + pt.getName() + ").", declInfo);		
				}
				
				ICompoundType type = (ICompoundType)pt;
				if (type.isClassType()){
					JClassProperties pps = type.getClassProperties();
					if (pps.isStatic() && !sta) {
						// Extension type
						builder.addExtension((JClassType)type);
					} else {
						// Parent class. Only one is allowed.
						if (alreadyDefinedParentClass) {
							throw new IllegalClassDefinitionException(
								context, 
								"Type " + declInfo.getFQName() + 
								" has a parent class defined already (" + builder.getStub().getParent().getName() + 
								"). Cannot add another one (" + type.getName() + ").", declInfo);
						} else {
							alreadyDefinedParentClass = true;
							if(pps.isFinal()){
								throw new IllegalClassDefinitionException(
									context, "Type " + declInfo.getFQName() + 
									" cannot inherit from a final type: " + type.getName() + ".", declInfo);
							}

							if(!pps.isStatic() && sta) {
								throw new IllegalClassDefinitionException(
									context, "Type " + declInfo.getFQName() + 
									" is a static class. It cannot inherit from any type other than Object: " + type.getName() + ".", declInfo);
							}
							
							Accessibility.checkTypeVisibility(type, ctb.getStub(), true);
							
							builder.setParent((JClassType)type);
						}
					}
				} else {
					// Interfaces to implement.
					builder.addInterface((JInterfaceType)type);
				}
				
				context.addDependency(type.getName());
			}
		} 
		
		// Default parent class to Object
		if (!alreadyDefinedParentClass) {
			builder.setParent(JObjectType.getInstance());
		}
		
		List<AttributeDeclInfo> list = context.getClassTypeAttributes();
		if(list != null){
			for(AttributeDeclInfo adi : list){
				JAnnotation anno = getAnnotation(adi, context);
				builder.addClassAnnotation(anno);
				context.addDependency(anno.getAttributeType().getName());
			}		
		}
		
		ClassMemberDeclarationStatement cmds = new ClassMemberDeclarationStatement();
		cmds.parse(context);
	}
	
}
