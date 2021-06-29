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

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import info.julang.execution.namespace.NamespaceConflictException;
import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.BadSyntaxException;
import info.julang.interpretation.context.Context;
import info.julang.langspec.Keywords;
import info.julang.langspec.ast.JulianLexer;
import info.julang.langspec.ast.JulianParser;
import info.julang.langspec.ast.JulianParser.AnnotationContext;
import info.julang.langspec.ast.JulianParser.AnnotationsContext;
import info.julang.langspec.ast.JulianParser.Attribute_definitionContext;
import info.julang.langspec.ast.JulianParser.Base_typeContext;
import info.julang.langspec.ast.JulianParser.BlockContext;
import info.julang.langspec.ast.JulianParser.Builtin_typeContext;
import info.julang.langspec.ast.JulianParser.Class_definitionContext;
import info.julang.langspec.ast.JulianParser.Class_extension_definitionContext;
import info.julang.langspec.ast.JulianParser.Class_extension_listContext;
import info.julang.langspec.ast.JulianParser.Class_member_declarationContext;
import info.julang.langspec.ast.JulianParser.Class_typeContext;
import info.julang.langspec.ast.JulianParser.Composite_idContext;
import info.julang.langspec.ast.JulianParser.Constructor_declarationContext;
import info.julang.langspec.ast.JulianParser.Constructor_forward_callContext;
import info.julang.langspec.ast.JulianParser.E_lambdaContext;
import info.julang.langspec.ast.JulianParser.Enum_definitionContext;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.langspec.ast.JulianParser.Field_declarationContext;
import info.julang.langspec.ast.JulianParser.Field_initializerContext;
import info.julang.langspec.ast.JulianParser.Function_parameterContext;
import info.julang.langspec.ast.JulianParser.Function_parameter_listContext;
import info.julang.langspec.ast.JulianParser.Function_signature_mainContext;
import info.julang.langspec.ast.JulianParser.Interface_definitionContext;
import info.julang.langspec.ast.JulianParser.Lambda_parameterContext;
import info.julang.langspec.ast.JulianParser.Lambda_parameter_listContext;
import info.julang.langspec.ast.JulianParser.Lambda_signatureContext;
import info.julang.langspec.ast.JulianParser.Method_bodyContext;
import info.julang.langspec.ast.JulianParser.Method_declarationContext;
import info.julang.langspec.ast.JulianParser.Method_parameterContext;
import info.julang.langspec.ast.JulianParser.Method_parameter_listContext;
import info.julang.langspec.ast.JulianParser.Method_signature_mainContext;
import info.julang.langspec.ast.JulianParser.ModifiersContext;
import info.julang.langspec.ast.JulianParser.ProgramContext;
import info.julang.langspec.ast.JulianParser.Rank_specifierContext;
import info.julang.langspec.ast.JulianParser.TypeContext;
import info.julang.langspec.ast.JulianParser.Type_declarationContext;
import info.julang.modulesystem.naming.FQName;
import info.julang.parser.AstInfo;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.JType;
import info.julang.typesystem.JTypeKind;
import info.julang.typesystem.VoidType;
import info.julang.typesystem.basic.BoolType;
import info.julang.typesystem.basic.ByteType;
import info.julang.typesystem.basic.CharType;
import info.julang.typesystem.basic.FloatType;
import info.julang.typesystem.basic.IntType;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.builtin.JStringType;

public final class SyntaxHelper {

	/**
	 * Collect info about the type declaration.
	 */
	public static ClassDeclInfo parseClassDeclaration(AstInfo<Type_declarationContext> ainfo, String moduleName){
		ClassDeclInfo declInfo = new ClassDeclInfo();
		Type_declarationContext decl = ainfo.getAST();
		
		ParserRuleContext prt = (ParserRuleContext) decl.children.get(0);
		declInfo.setAST(prt);
		declInfo.setLocationInfo(ainfo.create(prt));
		switch(prt.getRuleIndex()){
		case JulianParser.RULE_class_definition:
			declInfo.setSubtype(ClassSubtype.CLASS);
			Class_definitionContext classDef = (Class_definitionContext)prt;
			parseClassDef(declInfo, ainfo.create(classDef), moduleName);
			break;
		case JulianParser.RULE_interface_definition:
			declInfo.setSubtype(ClassSubtype.INTERFACE);
			Interface_definitionContext intfDef = (Interface_definitionContext)prt;
			parseInterfaceDef(declInfo, ainfo.create(intfDef), moduleName);
			break;
		case JulianParser.RULE_enum_definition:
			declInfo.setSubtype(ClassSubtype.ENUM);
			Enum_definitionContext enumDef = (Enum_definitionContext)prt;
			parseEnumDef(declInfo, ainfo.create(enumDef), moduleName);
			break;
		case JulianParser.RULE_attribute_definition:
			declInfo.setSubtype(ClassSubtype.ATTRIBUTE);
			Attribute_definitionContext attrDef = (Attribute_definitionContext)prt;
			parseAttributeDef(declInfo, ainfo.create(attrDef), moduleName);
			break;
		default:
			throw new BadSyntaxException("Illegal rule for a type definition: " + JulianParser.ruleNames[prt.getRuleIndex()] + ".");
		}
		
		return declInfo;
	}
	
	private static void parseAttributeDef(
		ClassDeclInfo declInfo,
		AstInfo<Attribute_definitionContext> ainfo, 
		String moduleName) {
		Attribute_definitionContext attrDef = ainfo.getAST();
		// 1) Annotations
		AnnotationsContext annos = attrDef.annotations();
		if (annos != null){
			readAllAttributeDeclInfo(declInfo, ainfo.create(annos));
		}
		
		// 2) Modifiers
		ModifiersContext mods = attrDef.modifiers();
		readModifiers(declInfo, mods);
		
		// 3) Name
		String className = attrDef.IDENTIFIER().getText();
		readTypeName(declInfo, moduleName, className);
	}
	
	private static void parseEnumDef(
		ClassDeclInfo declInfo,
		AstInfo<Enum_definitionContext> ainfo, 
		String moduleName) {
		Enum_definitionContext enumDef = ainfo.getAST();
		
		// 1) Annotations
		AnnotationsContext annos = enumDef.annotations();
		if (annos != null){
			readAllAttributeDeclInfo(declInfo, ainfo.create(annos));
		}
		
		// 2) Modifiers
		ModifiersContext mods = enumDef.modifiers();
		readModifiers(declInfo, mods);
		
		// 3) Name
		String className = enumDef.IDENTIFIER().getText();
		readTypeName(declInfo, moduleName, className);
	}

	private static void parseInterfaceDef(
		ClassDeclInfo declInfo,
		AstInfo<Interface_definitionContext> ainfo,
		String moduleName) {
		Interface_definitionContext intfDef = ainfo.getAST();
		// 1) Annotations
		AnnotationsContext annos = intfDef.annotations();
		if (annos != null){
			readAllAttributeDeclInfo(declInfo, ainfo.create(annos));
		}
		
		// 2) Modifiers
		ModifiersContext mods = intfDef.modifiers();
		readModifiers(declInfo, mods);
		
		// 3) Name
		String className = intfDef.IDENTIFIER().getText();
		readTypeName(declInfo, moduleName, className);
		
		// 4) Inheritance
		Class_extension_definitionContext exDefs = intfDef.class_extension_definition();
		if (exDefs != null) {
			Class_extension_listContext exDefList = exDefs.class_extension_list();
			readParentType(declInfo, exDefList);
		}
	}
	
	private static void parseClassDef(
		ClassDeclInfo declInfo,
		AstInfo<Class_definitionContext> ainfo,
		String moduleName) {
		Class_definitionContext classDef = ainfo.getAST();
		// 1) Annotations
		AnnotationsContext annos = classDef.annotations();
		if (annos != null){
			readAllAttributeDeclInfo(declInfo, ainfo.create(annos));
		}
		
		// 2) Modifiers
		ModifiersContext mods = classDef.modifiers();
		readModifiers(declInfo, mods);
		
		// 3) Name
		String className = classDef.IDENTIFIER().getText();
		readTypeName(declInfo, moduleName, className);
		
		// 4) Inheritance
		Class_extension_definitionContext exDefs = classDef.class_extension_definition();
		if (exDefs != null) {
			Class_extension_listContext exDefList = exDefs.class_extension_list();
			readParentType(declInfo, exDefList);
		}
	}

	private static void readParentType(ClassDeclInfo declInfo, Class_extension_listContext exDefList) {
		List<Composite_idContext> cids = exDefList.composite_id();		
		if (cids != null){
			for (Composite_idContext cid : cids) {
				ParsedTypeName ptn = ParsedTypeName.makeFromFullName(cid.getText());
				declInfo.addParentTypeName(ptn);
			}
		}
	}
	
	private static void readTypeName(ClassDeclInfo declInfo, String moduleName, String className){
		declInfo.setName(className);
		declInfo.setFQName(new FQName(moduleName, className));
	}

	private static void readModifiers(DeclInfo declInfo, ModifiersContext mods) {
		if (mods != null) {
			List<ParseTree> modList = mods.children;
			for (ParseTree mod : modList) {
				TerminalNode tn = (TerminalNode) mod;
				switch(tn.getSymbol().getType()){
				case JulianParser.HOSTED:
					if(!declInfo.allowModifier(Modifier.HOSTED)){
						throw new BadSyntaxException("Illegal modifier: hosted.", declInfo);
					}
					if(declInfo.isHosted()){
						throw new BadSyntaxException("Duplicated modifiers: hosted.", declInfo);
					}
					declInfo.setHosted();
					break;
				case JulianParser.STATIC:
					if(!declInfo.allowModifier(Modifier.STATIC)){
						throw new BadSyntaxException("Illegal modifier: static.", declInfo);
					}
					if(declInfo.isStatic()){
						throw new BadSyntaxException("Duplicated modifiers: static.", declInfo);
					}
					declInfo.setStatic();
					break;
				case JulianParser.CONST:
					if(!declInfo.allowModifier(Modifier.CONST)){
						throw new BadSyntaxException("Illegal modifier: const.", declInfo);
					}
					if(declInfo.isConst()){
						throw new BadSyntaxException("Duplicated modifiers: const.", declInfo);
					}
					declInfo.setConst();
					break;
				case JulianParser.ABSTRACT:
					if(!declInfo.allowModifier(Modifier.ABSTRACT)){
						throw new BadSyntaxException("Illegal modifier: abstract.", declInfo);
					}
					if(declInfo.isAbstract()){
						throw new BadSyntaxException("Duplicated modifiers: abstract.", declInfo);
					}
					declInfo.setAbstract();
					break;
				case JulianParser.FINAL:
					if(!declInfo.allowModifier(Modifier.FINAL)){
						throw new BadSyntaxException("Illegal modifier: final.", declInfo);
					}
					if(declInfo.isFinal()){
						throw new BadSyntaxException("Duplicated modifiers: final.", declInfo);
					}
					declInfo.setFinal();
					break;
				case JulianParser.PUBLIC:
					if(!declInfo.allowModifier(Modifier.PUBLIC)){
						throw new BadSyntaxException("Illegal modifier: public.", declInfo);
					}
					if(declInfo.isAccessibilitySet()){
						throw new BadSyntaxException("Multiple accessbility modifiers.", declInfo);
					}
					declInfo.setAccessibility(Accessibility.PUBLIC);
					break;
				case JulianParser.PROTECTED:
					if(!declInfo.allowModifier(Modifier.PROTECTED)){
						throw new BadSyntaxException("Illegal modifier: protected.", declInfo);
					}
					if(declInfo.isAccessibilitySet()){
						throw new BadSyntaxException("Multiple accessbility modifiers.", declInfo);
					}
					declInfo.setAccessibility(Accessibility.PROTECTED);
					break;
				case JulianParser.PRIVATE:
					if(!declInfo.allowModifier(Modifier.PRIVATE)){
						throw new BadSyntaxException("Illegal modifier: private.", declInfo);
					}
					if(declInfo.isAccessibilitySet()){
						throw new BadSyntaxException("Multiple accessbility modifiers.", declInfo);
					}
					declInfo.setAccessibility(Accessibility.PRIVATE);
					break;
				case JulianParser.INTERNAL:
					if(!declInfo.allowModifier(Modifier.INTERNAL)){
						throw new BadSyntaxException("Illegal modifier: internal.", declInfo);
					}
					if(declInfo.isAccessibilitySet()){
						throw new BadSyntaxException("Multiple accessbility modifiers.", declInfo);
					}
					declInfo.setAccessibility(Accessibility.MODULE);
					break;
				default:
					throw new BadSyntaxException("Illegal token scanned for definition: " + tn.getText() + ".", declInfo);
				}
			}
		}
		
		if(!declInfo.isAccessibilitySet()){
			declInfo.setAccessibility(Accessibility.PUBLIC);
		}
	}

	private static void readAllAttributeDeclInfo(
		DeclInfo declInfo,
		AstInfo<AnnotationsContext> ainfo) {
		AnnotationsContext annos = ainfo.getAST();
		List<AnnotationContext> annoList = annos.annotation();
		for (AnnotationContext anno : annoList){
			AttributeDeclInfo attrDecl = new AttributeDeclInfo(ainfo.create(anno));
			declInfo.addAttribute(attrDecl);
		}
	}
	
	/**
	 * Parse a list of member declarations into {@link MethodDeclInfo}.
	 * <p>
	 * This methods returns a preliminary parsing result from the declaration. The information
	 * includes the annotation, modifiers, type (in case of method, the return type), name, etc.
	 * In particular, no preemptive parsing will be done for the executable body.
	 * 
	 * @param decls original AST nodes for each member declaration.
	 * @param stream
	 * @param className
	 * @param preserveWholeAst if true, store the while AST for the member definition into 
	 * declaration info. Otherwise, only store the executable part, which can be null, such as
	 * in case of initializer-free field or abstract method.
	 * @return a list of member declarations into {@link MethodDeclInfo}.
	 */
	public static List<MemberDeclInfo> parseClassMemberDeclarations(
		List<Class_member_declarationContext> decls, AstInfo<ProgramContext> ainfo, FQName className, boolean preserveWholeAst){
		List<MemberDeclInfo> infos = new ArrayList<MemberDeclInfo>();

		for (Class_member_declarationContext decl : decls) {
			MemberDeclInfo info = parseClassMemberDeclaration(decl, ainfo, className, preserveWholeAst);
			infos.add(info);
		}

		return infos;
	}
	
	// Parse a single member declarations into MethodDeclInfo.
	private static MemberDeclInfo parseClassMemberDeclaration(
		Class_member_declarationContext decl, AstInfo<ProgramContext> ainfo, FQName className, boolean preserveWholeAst){

		ParserRuleContext prt = (ParserRuleContext) decl.children.get(0);
		switch(prt.getRuleIndex()){
		case JulianParser.RULE_constructor_declaration:
			Constructor_declarationContext ctorDecl = decl.constructor_declaration();
			return parseCtorMemberDeclaration(ainfo.create(ctorDecl), className, preserveWholeAst);
		case JulianParser.RULE_method_declaration:
			Method_declarationContext metDecl = decl.method_declaration();
			return parseMethodMemberDeclaration(ainfo.create(metDecl), className, preserveWholeAst);
		case JulianParser.RULE_field_declaration:
			Field_declarationContext fieldDecl = decl.field_declaration();
			return parseFieldMemberDeclaration(ainfo.create(fieldDecl), className, preserveWholeAst);
		default:
			// Impossible if grammar is not changed.
			throw new JSEError(
				"Unknown syntax rule encountered when parsing class members: " + 
				JulianParser.ruleNames[prt.getRuleIndex()]);
		}
	}
	
	// annotations? modifiers? type IDENTIFIER ( field_initializer | ';' ) 
	private static FieldDeclInfo parseFieldMemberDeclaration(
		AstInfo<Field_declarationContext> ainfo, FQName className, boolean preserveWholeAst){
		Field_declarationContext fieldDecl = ainfo.getAST();
		FieldDeclInfo declInfo = new FieldDeclInfo();
		declInfo.setLocationInfo(ainfo);
		
		// 1) Annotations
		AnnotationsContext annos = fieldDecl.annotations();
		if (annos != null){
			readAllAttributeDeclInfo(declInfo, ainfo.create(annos));
		}
		
		// 2) Modifiers
		ModifiersContext mods = fieldDecl.modifiers();
		readModifiers(declInfo, mods);
		
		// 3) Type
		TypeContext tc = fieldDecl.type();
		ParsedTypeName ptn = parseTypeName(tc);
		declInfo.setTypeName(ptn);
		
		// 4) Name
		String name = fieldDecl.IDENTIFIER().getText();
		String simpleClassName = className.getSimpleName();
		if(!name.equals(simpleClassName)){
			declInfo.setName(name);
		} else {
			throw new BadSyntaxException("A non-constructor member must not have a name same to its class's (" + simpleClassName + ").", ainfo.create(tc));
		}
		
		// 5) Initializer
		if (preserveWholeAst) {
			declInfo.setAST(fieldDecl);
		} else {
			Field_initializerContext init = fieldDecl.field_initializer();
			if (init != null){
				declInfo.setAST(init.expression_statement());
			}
		}
		
		return declInfo;
	}
	
	// annotations? modifiers? IDENTIFIER function_signature_main constructor_forward_call? ( method_body | ';' ) 
	private static MemberDeclInfo parseCtorMemberDeclaration(
		AstInfo<Constructor_declarationContext> ainfo, FQName className, boolean preserveWholeAst) {
		Constructor_declarationContext ctorDecl = ainfo.getAST();
		CtorDeclInfo declInfo = new CtorDeclInfo();
		declInfo.setLocationInfo(ainfo);
		//declInfo.setContainingTypeName(className);
		
		// 1) Annotations
		AnnotationsContext annos = ctorDecl.annotations();
		if (annos != null){
			readAllAttributeDeclInfo(declInfo, ainfo.create(annos));
		}
		
		// 2) Modifiers
		ModifiersContext mods = ctorDecl.modifiers();
		readModifiers(declInfo, mods);
		
		// 3) Name & Type
		String name = ctorDecl.IDENTIFIER().getText();
		String simpleClassName = className.getSimpleName();
		if(name.equals(simpleClassName)){
			ParsedTypeName ptn = ParsedTypeName.makeFromFullName(simpleClassName);
			declInfo.setName(name);
			declInfo.setTypeName(ptn);
		} else {
			throw new BadSyntaxException("A constructor member must have a name same to its class's (" + simpleClassName + ").", ainfo);
		}
		
		// 4) Parameters
		Function_signature_mainContext sig = ctorDecl.function_signature_main();
		Function_parameter_listContext plist = sig.function_parameter_list();
		if (plist != null) {
			if (declInfo.isStatic()) {
				// A static constructor must not possess any parameter
				throw new BadSyntaxException("A class static constructor cannot have parameters.", ainfo);
			} else {
				parseParameterList(declInfo, plist);
			}
		}
		
		// 5) Forward call
		Constructor_forward_callContext forward = ctorDecl.constructor_forward_call();
		if (forward != null){
			boolean forwardToThis = true;
			TerminalNode tn = forward.THIS();
			if (tn == null) {
				forwardToThis = false;
				tn = forward.SUPER();
			}
			
			declInfo.setForwardCallAst(forward.function_call(), forwardToThis);
		}
		
		// 6) Body
		if (preserveWholeAst) {
			declInfo.setAST(ctorDecl);
		} else {
			Method_bodyContext body = ctorDecl.method_body();
			declInfo.setAST(body);
		}
		
		return declInfo;
	}

	// annotations? modifiers? type IDENTIFIER function_signature_main ( method_body | ';' ) 
	private static MemberDeclInfo parseMethodMemberDeclaration(
		AstInfo<Method_declarationContext> ainfo, FQName className, boolean preserveWholeAst) {
		Method_declarationContext metDecl = ainfo.getAST();
		MethodDeclInfo declInfo = new MethodDeclInfo();
		declInfo.setLocationInfo(ainfo);
		//declInfo.setContainingTypeName(className);
		
		// 1) Annotations
		AnnotationsContext annos = metDecl.annotations();
		if (annos != null){
			readAllAttributeDeclInfo(declInfo, ainfo.create(annos));
		}
		
		// 2) Modifiers
		ModifiersContext mods = metDecl.modifiers();
		readModifiers(declInfo, mods);
		
		// 3) Name & Type
		String name = metDecl.IDENTIFIER().getText();
		String simpleClassName = className.getSimpleName();
		if(!name.equals(simpleClassName)){
			ParsedTypeName ptn = parseTypeName(metDecl.type());
			declInfo.setName(name);
			declInfo.setTypeName(ptn);
		} else {
			throw new BadSyntaxException("A method member must not have a name same to its class's (" + simpleClassName + ").", ainfo);
		}
		
		// 4) Parameters
		Method_signature_mainContext sig = metDecl.method_signature_main();
		Method_parameter_listContext plist = sig.method_parameter_list();
		if (plist != null) {
			parseParameterList(declInfo, plist);
		}

		// 5) Body
		if (preserveWholeAst) {
			declInfo.setAST(metDecl);
		} else {
			Method_bodyContext body = metDecl.method_body();
			if (body != null) {
				declInfo.setAST(body);
			}
		}

		return declInfo;
	}

	/**
	 * The following patterns match a lambda definition:
	 * <p>
	 * <pre>
	 *   1) type-safe
	 *  	(string s, int i) => ...
	 *   2) untyped
	 *  	(s, i) => ...
	 *   3) untyped and without parentheses
	 *  	s => ...</pre>
	 * The following sytax structure matches a body:
	 * <pre>
	 *   1) enclosed body
	 *      => { ... }
	 *   2) single expression
	 *      => ...;</pre>
	 * If this method returns a non-null value, PC has now moved after '=>', waiting for further parsing 
	 * by the caller. If this method returns null, then it is not a lambda.
	 * <p>
	 * @param ast
	 */
	// lambda_signature LAMBDA ( ( RETURN? expression ) | block ) # e_lambda
	public static LambdaDeclInfo parseLambdaExpression(AstInfo<E_lambdaContext> ainfo){
		//lambda_signature 
		//  : LEFT_PAREN RIGHT_PAREN
		//  | LEFT_PAREN lambda_parameter_list RIGHT_PAREN
		//  | IDENTIFIER
		//  ;
		//lambda_parameter_list 
		//  : lambda_parameter ( COMMA lambda_parameter )*
		//  ;
		//lambda_parameter 
		//  : type? IDENTIFIER
		//  ;
		E_lambdaContext ast = ainfo.getAST();
		LambdaDeclInfo lambda = new LambdaDeclInfo();
		lambda.setLocationInfo(ainfo);
		
		Lambda_signatureContext sigAst = ast.lambda_signature();
		Lambda_parameter_listContext plist = sigAst.lambda_parameter_list();
		if (plist != null) {
			List<Lambda_parameterContext> list = plist.lambda_parameter();
			for (Lambda_parameterContext lpc : list){
				TypeContext tc = lpc.type();
				String name = lpc.IDENTIFIER().getText();
				if (tc != null){
					ParsedTypeName ptn = parseTypeName(tc);
					lambda.addParameter(ptn, name);
				} else {
					lambda.addUntypedParameter(name);
				}
			}
		} else {
			TerminalNode tn = sigAst.IDENTIFIER();
			if (tn != null) {
				String name = tn.getText();
				lambda.addUntypedParameter(name);
			}
		}
		
		ExpressionContext ec = ast.expression();
		BlockContext bc = ast.block();
		LambdaDeclInfo.LambdaType ltyp = LambdaDeclInfo.LambdaType.RETURN;
		if (bc != null) {
			ltyp = LambdaDeclInfo.LambdaType.BLOCK;
		} else if (ast.THROW() != null){
			ltyp = LambdaDeclInfo.LambdaType.THROW;
		}
		lambda.setASTs(ainfo.create(ec), ainfo.create(bc), ltyp);

		return lambda;
	}
	
	/**
	 * Convert a Type AST node to ParsedTypeName.
	 * 
	 * @param tc the original AST node, comprising of a base type and an optional rank specifier.
	 * @return never null
	 */
	public static ParsedTypeName parseTypeName(TypeContext tc){
		ParsedTypeName typeName;
		List<Rank_specifierContext> ranksc = tc.rank_specifier();
		int rank = 0;
		if (ranksc != null){
			rank = ranksc.size();
		}
		
		// 1) Type base
		Base_typeContext btc = tc.base_type();
		Builtin_typeContext builtin = btc.builtin_type();
		JType type = null;
		if (builtin != null){
			switch(builtin.start.getType()){
			case JulianLexer.INT: // int ...
				type = IntType.getInstance();
				break;
			case JulianLexer.BOOL: // bool ...
				type = BoolType.getInstance();
				break;		
			case JulianLexer.BYTE: // byte ...
				type = ByteType.getInstance();
				break;
			case JulianLexer.FLOAT: // float ...
				type = FloatType.getInstance();
				break;
			case JulianLexer.CHAR: // char ...
				type = CharType.getInstance();
				break;
			case JulianLexer.STRING: // string ...
				type = JStringType.getInstance();
				break;
			case JulianLexer.VOID:
				type = VoidType.getInstance();
				break;
			case JulianLexer.VAR:
				type = AnyType.getInstance();
				break;
			default:
				break;
			}
		}

		if (type != null) {
			if (type.getKind() == JTypeKind.ANY && rank == 0) {
				typeName = ParsedTypeName.ANY;
			} else {
				typeName = new ParsedTypeName(type);
			}
		} else {
			Class_typeContext classTyp = btc.class_type();
			typeName = ParsedTypeName.makeFromFullName(classTyp.composite_id().getText());
		}

		// 2) Array rank
		typeName.setArrayDimension(rank);
		
		return typeName;
	}
	
	/**
	 * Ensure that there is not variable-type conflict.
	 * 
	 * @param context
	 * @param localName
	 */
	public static void checkVarTypeConflict(Context context, String localName){
		if (Character.isUpperCase(localName.charAt(0))){
			JType typ = context.getTypeResolver().resolveType(ParsedTypeName.makeFromFullName(localName), false);
			if (typ != null) {
				throw new NamespaceConflictException(localName + " (variable)", localName + " (type)");
			}
		}
	}
	
	private static void parseParameterList(MethodDeclInfo declInfo, Function_parameter_listContext plist){	
		List<Function_parameterContext> pclist = plist.function_parameter();
		for (Function_parameterContext fpc : pclist) {
			TerminalNode name = fpc.IDENTIFIER();
			TypeContext type = fpc.type();
			
			String pName = name.getText();
			ParsedTypeName pType = parseTypeName(type);
			declInfo.addParameter(pType, pName);
		}
	}
	
	private static void parseParameterList(MethodDeclInfo declInfo, Method_parameter_listContext plist){
		List<Method_parameterContext> pclist = plist.method_parameter();
		if (pclist != null) {
			for (int i = 0; i < pclist.size(); i++) {
				Method_parameterContext fp = pclist.get(i);

				String pName;
				TerminalNode id = fp.IDENTIFIER();
				if (id != null) {
					pName = id.getText();
				} else {
					// the name is 'this'
					if (!declInfo.isStatic() || i > 0) {
						throw new BadSyntaxException(
							"Keyword 'this' can only be used as the first parameter's name for static extension method.", declInfo);
					} else {
						pName = Keywords.THIS.toString();
					}
				}
				
				ParsedTypeName pType = parseTypeName(fp.type());
				declInfo.addParameter(pType, pName);
			}
		}
	}
}