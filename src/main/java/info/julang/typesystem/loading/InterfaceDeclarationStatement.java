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
import info.julang.interpretation.syntax.AttributeDeclInfo;
import info.julang.interpretation.syntax.ClassDeclInfo;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.ICompoundTypeBuilder;
import info.julang.typesystem.jclass.JClassProperties;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JInterfaceType;
import info.julang.typesystem.jclass.JInterfaceTypeBuilder;
import info.julang.typesystem.jclass.annotation.JAnnotation;

import java.util.List;

/**
 * The statement for declaring (and defining) an interface.
 * <p>
 * Syntax:
 * <pre><code>[accessibility] <b>interface</b> {NAME} : {INTERFACE_NAME}* {
 * 
 *}</code></pre>where,
 * <ul>
 * <li>accessibility: <code>public|(none)</code></li>
 * </ul>
 * The <code>{INTERFACE_NAME}</code>s after <code>:</code> are the interfaces to extend from.
 * <p>
 * @author Ming Zhou
 */
public class InterfaceDeclarationStatement extends ClassLoadingStatement {

	@Override
	public void parse(LoadingContext context) {		
		ClassDeclInfo declInfo = context.getClassDeclInfo();
		
		Accessibility acc = declInfo.getAccessibility();
		boolean abs = declInfo.isAbstract();
		if(abs){
			throw new IllegalClassDefinitionException(
				context, "Cannot have abstract modifier on an interface definition.", declInfo);
		}
		
		boolean fin = declInfo.isFinal();		
		if(fin){
			throw new IllegalClassDefinitionException(
				context, "Cannot have finally modifier on an interface definition.", declInfo);
		}
		
		ICompoundTypeBuilder ctb = context.getTypeBuilder();
		if (ctb.isClassType()){
			throw new JSEError("Must build an interface type with an interface type builder.");
		}
		
		JInterfaceTypeBuilder builder = (JInterfaceTypeBuilder) ctb;
		
		if(acc == null){
			acc = Accessibility.PUBLIC;
		}
		builder.setAccessibility(acc);
		
		builder.setNamespacePool(context.getNamespacePool());
		
		List<ParsedTypeName> parents = declInfo.getParentTypes();
		if(parents != null && parents.size() > 0){
			for(ParsedTypeName ptn : parents){
				ICompoundType type = (ICompoundType)loadType(context, ptn);
				if (type.isClassType()){

					JClassProperties pps = type.getClassProperties();
					if (pps.isStatic()) {
						// Extension type
						builder.addExtension((JClassType)type);
					} else {
						throw new IllegalClassDefinitionException(
							context, 
							"Interface " + declInfo.getFQName() + 
							" cannot have inherit from a parent class (" + type.getName() + ").", declInfo);
					}
				} else {
					// Interfaces to implement.
					builder.addInterface((JInterfaceType)type);
				}
				
				context.addDependency(type.getName());
			}
		}
		
		List<AttributeDeclInfo> list = context.getClassTypeAttributes();
		if(list != null){
			for(AttributeDeclInfo adi : list){
				JAnnotation anno = getAnnotation(adi, context);
				builder.addClassAnnotation(anno);
			}		
		}
		
		ClassMemberDeclarationStatement cmds = new ClassMemberDeclarationStatement();
		cmds.parse(context);
	}
	
}
