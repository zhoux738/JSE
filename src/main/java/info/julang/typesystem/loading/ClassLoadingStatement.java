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

import info.julang.interpretation.BadSyntaxException;
import info.julang.interpretation.RuntimeCheckException;
import info.julang.interpretation.syntax.AttributeDeclInfo;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.interpretation.syntax.SyntaxHelper;
import info.julang.langspec.ast.JulianParser.AnnotationContext;
import info.julang.langspec.ast.JulianParser.Atrribute_initializationContext;
import info.julang.langspec.ast.JulianParser.Atrribute_initialization_listContext;
import info.julang.langspec.ast.JulianParser.Attributes_initalizationContext;
import info.julang.langspec.ast.JulianParser.TypeContext;
import info.julang.modulesystem.naming.FQName;
import info.julang.parser.AstInfo;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JClassTypeUtil;
import info.julang.typesystem.jclass.annotation.JAnnotation;
import info.julang.typesystem.jclass.builtin.JAttributeBaseType;
import info.julang.typesystem.jclass.builtin.JAttributeType;

import java.util.ArrayList;
import java.util.List;

public abstract class ClassLoadingStatement {

	public abstract void parse(LoadingContext context);
	
	protected JAnnotation getAnnotation(AttributeDeclInfo declInfo, LoadingContext context){
		FQName fqcName = context.getClassDeclInfo().getFQName();
		JAnnotation anno = null;
		AstInfo<AnnotationContext> ainfo = declInfo.getAstInfo();
		AnnotationContext ac = ainfo.getAST();
		
		//annotation
	    //  : '[' type attributes_initalization? ']'
	    //  ;
	    //
		//attributes_initalization
	    //  : '(' atrribute_initialization_list? ')'
	    //  ;
	    //
		//atrribute_initialization_list
	    //  : atrribute_initialization ( ',' atrribute_initialization ) *
	    //  ;
	    //
		//atrribute_initialization
	    //  : IDENTIFIER '=' expression
	    //  ;
		
		TypeContext tc = ac.type();
		ParsedTypeName typeName = SyntaxHelper.parseTypeName(tc);
		if(typeName == null){
			throw new BadSyntaxException("Illegal attribute declaration for class " + fqcName);
		}
		
		JType jtype = loadType(context, typeName);
		JClassType jattrTyp = null;
		if((jattrTyp = JClassTypeUtil.isDerivingFrom(jtype, JAttributeBaseType.getInstance(), false)) != null){
			// 1) Attribute type
			JAttributeType attrTyp = (JAttributeType) jattrTyp;
			
			// 2) Attribute field initializers
			List<Atrribute_initializationContext> ais = null;
			Attributes_initalizationContext aic = ac.attributes_initalization();
			if (aic != null) {
				Atrribute_initialization_listContext ailc = aic.atrribute_initialization_list();
				if (ailc != null) {
					ais = ailc.atrribute_initialization();
				}
			}

			anno = new JAnnotation(attrTyp, ais, ainfo);
		} else {
			throw new RuntimeCheckException(
				"An attribute declaration must be of a type deriving from Attribute. But it is " + 
				jtype.getName() + ".", ainfo.create(tc));			
		}
		
		return anno;
	}

	protected List<JAnnotation> getAttributes(List<AttributeDeclInfo> attrDeclInfos, LoadingContext context){
		if(attrDeclInfos == null){
			return null;
		}
		
		List<JAnnotation> list = new ArrayList<JAnnotation>();
		//FQName fqcName = context.getClassDeclInfo().getFQName();
		
		for(AttributeDeclInfo adi : attrDeclInfos){
			AstInfo<AnnotationContext> ainfo = adi.getAstInfo();
			AnnotationContext ac = ainfo.getAST();
			
			TypeContext tc = ac.type();
			ParsedTypeName typeName = SyntaxHelper.parseTypeName(tc);
			JType jtype = loadType(context, typeName);
			
			JClassType jattrTyp = null;
			if((jattrTyp = JClassTypeUtil.isDerivingFrom(jtype, JAttributeBaseType.getInstance(), false)) != null){
				// 1) Attribute type
				JAttributeType attrTyp = (JAttributeType) jattrTyp;
				
				// 2) Attribute field initializers
				Attributes_initalizationContext aic = ac.attributes_initalization();

				List<Atrribute_initializationContext> ais = null;
				if (aic != null) {
					Atrribute_initialization_listContext ailc = aic.atrribute_initialization_list();
					if (ailc != null) {
						ais = ailc.atrribute_initialization();
					}
				}
				
				JAnnotation anno = new JAnnotation(attrTyp, ais, ainfo);
				list.add(anno);
			} else {
				throw new BadSyntaxException(
					"An attribute declaration must be of a type deriving from Attribute. But it is " + 
					jtype.getName() + ".");			
			}
		}
		
		return list;
	}
	
	/**
	 * Load a type from a type name.
	 * @param context
	 * @param typeName
	 * @return
	 */
	protected JType loadType(LoadingContext context, ParsedTypeName typeName) {
		JType type = typeName.getBasicType();
		
		// 1) built-in type?
		if(type != null){
			throw new BadSyntaxException(
				"Cannot declare a basic type as parent type: " + type.getName() + ".");
		}
		
		// 2) type loaded already?
		String tName = typeName.getFQName().toString();
		type = context.getTypeTable().getType(tName);
		if(type == null){
			// 3) type not loaded
			type = context.getTypeResolver().resolveType(context.getContext(), context.getNamespacePool(), typeName);
		}
		
		if(typeName.isArray()){
			throw new BadSyntaxException(
				"Cannot declare an array type as parent type: " + typeName.getFQNameInString() + "[].");
		} 
		return type;
	}
	
}
