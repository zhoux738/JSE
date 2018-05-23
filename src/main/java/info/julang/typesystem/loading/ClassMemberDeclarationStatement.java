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

import info.julang.execution.symboltable.ITypeTable;
import info.julang.external.exceptions.JSEError;
import info.julang.hosting.HostedExecutable;
import info.julang.hosting.HostedMethodExecutable;
import info.julang.hosting.attributes.HostedAttributeType;
import info.julang.hosting.attributes.HostedAttributeUtil;
import info.julang.interpretation.BadSyntaxException;
import info.julang.interpretation.IllegalLiteralException;
import info.julang.interpretation.RuntimeCheckException;
import info.julang.interpretation.errorhandling.ILocationInfoAware;
import info.julang.interpretation.syntax.ClassDeclInfo;
import info.julang.interpretation.syntax.CtorDeclInfo;
import info.julang.interpretation.syntax.DeclInfo;
import info.julang.interpretation.syntax.FieldDeclInfo;
import info.julang.interpretation.syntax.MemberDeclInfo;
import info.julang.interpretation.syntax.MethodDeclInfo;
import info.julang.interpretation.syntax.MethodDeclInfo.TypeAndName;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.interpretation.syntax.SyntaxHelper;
import info.julang.langspec.Keywords;
import info.julang.langspec.ast.JulianParser.Attribute_definitionContext;
import info.julang.langspec.ast.JulianParser.Class_definitionContext;
import info.julang.langspec.ast.JulianParser.Class_member_declarationContext;
import info.julang.langspec.ast.JulianParser.Enum_definitionContext;
import info.julang.langspec.ast.JulianParser.Enum_member_declaration_bodyContext;
import info.julang.langspec.ast.JulianParser.Enum_member_declaration_initializerContext;
import info.julang.langspec.ast.JulianParser.Enum_member_declarationsContext;
import info.julang.langspec.ast.JulianParser.Expression_statementContext;
import info.julang.langspec.ast.JulianParser.Field_declarationContext;
import info.julang.langspec.ast.JulianParser.Function_callContext;
import info.julang.langspec.ast.JulianParser.Interface_definitionContext;
import info.julang.langspec.ast.JulianParser.Interface_member_declarationContext;
import info.julang.langspec.ast.JulianParser.Last_enum_member_declarationContext;
import info.julang.langspec.ast.JulianParser.Method_bodyContext;
import info.julang.langspec.ast.JulianParser.Ordinary_enum_member_declarationContext;
import info.julang.langspec.ast.JulianParser.ProgramContext;
import info.julang.modulesystem.naming.FQName;
import info.julang.parser.ANTLRHelper;
import info.julang.parser.AstInfo;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.JType;
import info.julang.typesystem.VoidType;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.ConstructorForwardExecutable;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.ICompoundTypeBuilder;
import info.julang.typesystem.jclass.InitializerExecutable;
import info.julang.typesystem.jclass.JClassConstructorMember;
import info.julang.typesystem.jclass.JClassConstructorMember.ForwardInfo;
import info.julang.typesystem.jclass.JClassFieldMember;
import info.julang.typesystem.jclass.JClassInitializerMember;
import info.julang.typesystem.jclass.JClassMember;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassStaticConstructorMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JClassTypeBuilder;
import info.julang.typesystem.jclass.JInterfaceTypeBuilder;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.MethodExecutable;
import info.julang.typesystem.jclass.annotation.JAnnotation;
import info.julang.typesystem.jclass.builtin.JArrayType;
import info.julang.typesystem.jclass.builtin.JAttributeBaseType;
import info.julang.typesystem.jclass.builtin.JConstructorType;
import info.julang.typesystem.jclass.builtin.JEnumType;
import info.julang.typesystem.jclass.builtin.JMethodType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * This statement parses the body of type declaration, which can be a class, enum or attribute.
 * <p/>
 * The parse starts from '{' and ends with '}'. For example, it parses a class definition 
 * with PC pointed as shown below before {@link #parse(LoadingContext)} is called.
 * <p/>
 * <pre><code> class C : P { 
 *            ^ (PC)
 *   ... 
 * } </code></pre>
 * <p/>
 * By the end it will have populated all the type members in the stub, which then becomes ready 
 * to be loaded into type table.
 * 
 * @author Ming Zhou
 */
public class ClassMemberDeclarationStatement extends ClassLoadingStatement {
	
	@Override
	public void parse(LoadingContext context) {
		AstInfo<ProgramContext> ainfo = context.getAstInfo();
		ClassDeclInfo declInfo = context.getClassDeclInfo();
		
		switch(declInfo.getSubtype()){
		case CLASS:
			parseClass(context, ainfo, declInfo);
			break;
		case INTERFACE:
			parseInterface(context, ainfo, declInfo);
			break;
		case ENUM:
			parseEnum(context, ainfo, declInfo);
			break;
		case ATTRIBUTE:
			parseAttribute(context, ainfo, declInfo);
			break;
		default:
			throw new JSEError("Unsupported class subtype: " + declInfo.getSubtype().name());
		}
		
		context.getTypeBuilder().setParsed();
	}

	private class EnumValue {
		
		String literal;
		int ordinal;
		
		private EnumValue(String literal, int ordinal) {
			this.literal = literal;
			this.ordinal = ordinal;
		}

	}
	
	private void parseClass(LoadingContext context, AstInfo<ProgramContext> ainfo, ClassDeclInfo declInfo){
		JClassTypeBuilder builder = (JClassTypeBuilder) context.getTypeBuilder();
		FQName fullName = context.getClassDeclInfo().getFQName();
		boolean hasCtor = false;
		
		Class_definitionContext ast = (Class_definitionContext)declInfo.getAST();
		List<Class_member_declarationContext> memList = ast.class_body().class_member_declaration();
		
		List<MemberDeclInfo> mems = SyntaxHelper.parseClassMemberDeclarations(memList, ainfo, fullName, false);
		for (MemberDeclInfo decl : mems){
			switch(decl.getMemberType()){
			case FIELD:
				JClassMember member = parseField(decl, ainfo, context, builder);
				addMember(builder, member, decl);
				break;
			case METHOD:
				member = parseMethod(decl, ainfo, context, builder, fullName, false);
				addMember(builder, member, decl);
				break;
			case CONSTRUCTOR:
				JClassConstructorMember cmember = parseCtor(decl, ainfo, context, builder, fullName);
				if(cmember != null){
					builder.addInstanceConstructor(cmember);
					hasCtor = true;
				}
				break;
			case INITIALIZER:
			case STATIC_CONSTRUCTOR:
				throw new JSEError("Impossible member type of a class.");
			}
		}
		
		if(!hasCtor){
			createDefaultConstructor(builder, ainfo, fullName);
		}
		
		// Defer the check
		// checkAbstractMethods(builder, fullName, ainfo, ast);
		builder.addSemanticChecker(new AbstractMethodChecker(builder, fullName, ainfo, ast));
	}
	
	private void parseInterface(LoadingContext context, AstInfo<ProgramContext> ainfo, ClassDeclInfo declInfo){
		JInterfaceTypeBuilder builder = (JInterfaceTypeBuilder) context.getTypeBuilder();
		FQName fullName = context.getClassDeclInfo().getFQName();
		
		Interface_definitionContext ast = (Interface_definitionContext)declInfo.getAST();
		List<Interface_member_declarationContext> infMemDecls = ast.interface_body().interface_member_declaration();
		List<Class_member_declarationContext> memList = new ArrayList<Class_member_declarationContext>();
		for (Interface_member_declarationContext infMemDecl : infMemDecls){
			Class_member_declarationContext cmd = new Class_member_declarationContext(null, 0); // ctor args are not important here.
			List<ParseTree> children = infMemDecl.children;
			for (ParseTree pt : children) {
				cmd.addChild((RuleContext)pt);
			}
			memList.add(cmd);
		}
		
		List<MemberDeclInfo> mems = SyntaxHelper.parseClassMemberDeclarations(memList, ainfo, fullName, false);
		for (MemberDeclInfo decl : mems){
			switch(decl.getMemberType()){
			case METHOD:
				JClassMember member = parseMethod(decl, ainfo, context, builder, fullName, true);
				addMember(builder, member, decl);
				break;
			case FIELD:
				throw new IllegalClassDefinitionException(
					context, "An interface cannot have field member: " + decl.getName(), declInfo);
			case CONSTRUCTOR:
				throw new IllegalClassDefinitionException(
					context, "An interface cannot have constructor.", declInfo);
			case STATIC_CONSTRUCTOR:
				throw new IllegalClassDefinitionException(
					context, "An interface cannot have static constructor.", declInfo);
			case INITIALIZER:
				throw new JSEError("Impossible member type of an interface.");
			}
		}
	}
	
	private void parseEnum(LoadingContext context, AstInfo<ProgramContext> ainfo, ClassDeclInfo declInfo){
		int ordIndex = -1;
		List<EnumValue> list = new ArrayList<EnumValue>();
		Set<String> names = new HashSet<String>();
		
		//enum_definition
	    //  : annotations? modifiers? ENUM IDENTIFIER enum_body
	    //  ;
	    
	    //enum_body
	    //  : '{' enum_member_declarations? '}'
	    //  ;

	    //enum_member_declarations
	    //  : ordinary_enum_member_declaration* last_enum_member_declaration
	    //  ;
	    
	    //ordinary_enum_member_declaration
	    //  : enum_member_declaration_body ','
	    //  ;

	    //last_enum_member_declaration
	    //  : enum_member_declaration_body ','?
	    //  ;
	    
	    //enum_member_declaration_body
	    //  : IDENTIFIER enum_member_declaration_initializer?
	    //  ;
	    
	    //enum_member_declaration_initializer
	    //  : '=' INTEGER_LITERAL
	    //  ;
		
		Enum_definitionContext enumDef = (Enum_definitionContext)declInfo.getAST();
		Enum_member_declarationsContext enc = enumDef.enum_body().enum_member_declarations();
		
		if (enc != null) {
			boolean firstTime = true;
			List<Ordinary_enum_member_declarationContext> members = enc.ordinary_enum_member_declaration();
			if (members != null){
				for(Ordinary_enum_member_declarationContext mem : members){
					Enum_member_declaration_bodyContext mbody = mem.enum_member_declaration_body();
					EnumValue ev = parseEnumValue(context, ainfo, declInfo, mbody, ordIndex, firstTime, names);
					ordIndex = ev.ordinal;
					firstTime = false;
					list.add(ev);
				}
			}
			
			Last_enum_member_declarationContext lmem = enc.last_enum_member_declaration();
			Enum_member_declaration_bodyContext mbody = lmem.enum_member_declaration_body();
			EnumValue ev = parseEnumValue(context, ainfo, declInfo, mbody, ordIndex, firstTime, names);
			list.add(ev);
		}
		
		int size = list.size();
		if (size == 0) {
			throw new IllegalClassDefinitionException(
				context,
				"No enum fields found when parsing an Enum type: " + declInfo.getFQName(),
				declInfo);
		}
		String[] literals = new String[size];
		int[] ordinals = new int[size];
		
		int index = 0;
		for(EnumValue ev : list){
			literals[index] = ev.literal;
			ordinals[index] = ev.ordinal;
			index++;
		}
		
		JEnumType.populateEnumType(
			declInfo.getFQName(), 
			declInfo.getAccessibility(), 
			literals, 
			ordinals,
			(JEnumType) context.getTypeBuilder().getStub(),
			(JClassTypeBuilder) context.getTypeBuilder());
	}

	private EnumValue parseEnumValue(
		LoadingContext context, AstInfo<ProgramContext> ainfo, ClassDeclInfo declInfo, 
		Enum_member_declaration_bodyContext mbody, int ordIndex, boolean firstTime, Set<String> names) {
		String svalue = mbody.IDENTIFIER().getText();
		Enum_member_declaration_initializerContext init = mbody.enum_member_declaration_initializer();
		int ivalue = 0;
		if (init != null){
			try {
				ivalue = ANTLRHelper.parseIntLiteral(init.INTEGER_LITERAL().getText());
			} catch (IllegalLiteralException e) {
				e.setLocationInfo(ainfo.create(mbody));
			}
			if(ivalue <= ordIndex && !firstTime){
				throw new RuntimeCheckException(
					"Cannot assign an ordinal value less than or equal to any enum values appearing before. " +
					"Enum type: " + declInfo.getFQName() + "; Enum value: " + svalue,
					context.getAstInfo().create(mbody));
			}
		} else {
			ivalue = firstTime ? 0 : ordIndex + 1;
		}
		
		if(names.contains(svalue)){
			throw new IllegalClassDefinitionException(
				context,
				"Duplicate enum fields found when parsing an Enum type: " + declInfo.getFQName() + "; Enum key: " + svalue,
				declInfo);
		} else {
			names.add(svalue);
		}
		
		EnumValue ev = new EnumValue(svalue, ivalue);
		return ev;
	}
	
	private void parseAttribute(LoadingContext context, AstInfo<ProgramContext> ainfo, ClassDeclInfo declInfo) {
		JClassTypeBuilder builder = (JClassTypeBuilder) context.getTypeBuilder();
		builder.setParent(JAttributeBaseType.getInstance());
		
		FQName fullName = context.getClassDeclInfo().getFQName();
		
		// Convert field declarations to member declarations.
		Attribute_definitionContext ast = (Attribute_definitionContext)declInfo.getAST();
		List<Field_declarationContext> fdcs = ast.attribute_body().field_declaration();
		List<Class_member_declarationContext> memList = new ArrayList<Class_member_declarationContext>();
		for (Field_declarationContext fdc : fdcs){
			Class_member_declarationContext cmd = new Class_member_declarationContext(null, 0); // ctor args are not important here.
			cmd.addChild(fdc);
			memList.add(cmd);
		}
		
		List<MemberDeclInfo> mems = SyntaxHelper.parseClassMemberDeclarations(memList, ainfo, fullName, false);
		for (MemberDeclInfo decl : mems){
			switch(decl.getMemberType()){
			case FIELD:
				JClassMember fmember = parseField(decl, ainfo, context, builder);
				addMember(builder, fmember, decl);
				break;
			default:
				throw new IllegalClassDefinitionException(context, "Cannot contain non-field member in an attribute declaration.", declInfo);
			}
		}
		
		createDefaultConstructor(builder, ainfo, fullName);
	}
	
	private JClassMember parseField(
		MemberDeclInfo decl, 
		AstInfo<ProgramContext> ainfo, 
		LoadingContext context, 
		JClassTypeBuilder builder){
		FieldDeclInfo fieldDecl = (FieldDeclInfo) decl;
		boolean sta = fieldDecl.isStatic();
		if(fieldDecl.hasInitializer()){
			JClassType stub = builder.getStub();
			// 1) return type
			JType retType = AnyType.getInstance();
			
			// 2) parameter type				
			JParameter[] ptypesArray = sta ? new JParameter[0] : makeThisParams(builder);
			
			// 3) executable
			InitializerExecutable exec = new InitializerExecutable(
				ainfo.create((Expression_statementContext)fieldDecl.getAST()), stub, sta);
			
			// 4) assemble the type
			JMethodType mType = new JMethodType(
				"<init>-" + fieldDecl.getName(), ptypesArray, retType, exec, stub);
			
			JClassInitializerMember mmember = new JClassInitializerMember(
				stub,
				fieldDecl.getName(),  // field name
				sta, 				  // static
				mType);
			builder.addInitializerMember(mmember);
		}

		// Annotations
		JAnnotation[] annos = getAttributesAsArray(fieldDecl, context);
		
		ParsedTypeName typeName = fieldDecl.getTypeName();
		JType type = null;
		if(typeName != ParsedTypeName.ANY){
			type = loadMemberType(context, typeName);
			if (type.isObject()){
				Accessibility.checkTypeVisibility((ICompoundType)type, builder.getStub(), true);
			} else if (type == VoidType.getInstance()) {
				throw new IllegalClassDefinitionException(context, "Cannot use void as field type.", decl);
			}
		}
		
		JClassMember fmember = new JClassFieldMember(
			builder.getStub(),
			fieldDecl.getName(), 
			fieldDecl.getAccessibility(), 
			sta, 
			fieldDecl.isConst(), // static and const can co-exist.
			type, // if this is null, it is an untyped field.
			annos);	// annotations
		
		return fmember;
	}

	private void parseStaticCtor(
		CtorDeclInfo ctorDecl,
		AstInfo<ProgramContext> ainfo, 
		LoadingContext context, 
		JClassTypeBuilder builder, 
		FQName fullName){
		
		// 1) sanity check (ignore check for accessibility)
		if (builder.getStub().getClassStaticConstructor() != null){
			throw new BadSyntaxException("A class can have up to one static constructor: " + fullName);
		}
		if (ctorDecl.isAbstract() || ctorDecl.isHosted()){
			throw new BadSyntaxException("A static constructor can only have static modifier: " + fullName);
		}
		
		// 2) return type
		JType retType = VoidType.getInstance();
		
		// 3) parameter type				
		JParameter[] ptypesArray = new JParameter[0];
		
		// 4) executable
		MethodExecutable exec = getMethodExecutable(ctorDecl, ainfo, builder, true);
		
		// 5) assemble the type
		JMethodType mType = new JMethodType("<static-ctor-" + fullName + ">", ptypesArray, retType, exec, builder.getStub());
		
		// 6) create and set the member
		JClassStaticConstructorMember jccm = new JClassStaticConstructorMember(mType);
		builder.setClassStaticConstructorMember(jccm);
	}
	
	private JClassMember parseMethod(
		MemberDeclInfo decl, 
		AstInfo<ProgramContext> ainfo, 
		LoadingContext context, 
		ICompoundTypeBuilder builder, 
		FQName fullName,
		boolean forInterface){
		
		MethodDeclInfo methodDecl = (MethodDeclInfo) decl;
		
		// modifiers check
		boolean sta = methodDecl.isStatic();
		boolean abs = methodDecl.isAbstract();
		boolean hos = methodDecl.isHosted();
		Accessibility acc = methodDecl.getAccessibility();
		
		// special checks for interface methods
		if(forInterface){
			abs = true; // Override the result from parsing. (Methods defined in interface are always abstract)
			if (sta){
				throw new IllegalClassDefinitionException(
					context,
					"Cannot have static modifier on a method definition.", 
					decl);
			}			
			if (hos){
				throw new IllegalClassDefinitionException(
					context,
					"Cannot have hosted modifier on a method definition.", 
					decl);
			}
			
			if (acc == null){
				acc = Accessibility.PUBLIC;
			} else {
				switch(acc){		
				case PUBLIC:
					break;
				case MODULE:
					acc = Accessibility.PUBLIC;
					break;
				case PROTECTED:
				case PRIVATE:
					throw new IllegalClassDefinitionException(
						context,
						"Must define a method with public visibility.",
						decl);
				default:
					throw new JSEError("Unrecognized access modifier.");
				}
			}
		}
		
		if(sta && abs){
			throw new IllegalClassDefinitionException(
				context,
				"Cannot have both abstract and static modifiers on a method definition.",
				decl);
		}
		
		if(!forInterface && abs && !builder.getStub().getClassProperties().isAbstract()){
			throw new IllegalClassDefinitionException(
				context,
				"Non-abstract class cannot contain abstract method: " + methodDecl.getName(),
				decl);		
		}
		
		// Whether the method's return value is untyped. It can be untyped if
		//   (1) [Untyped] is applied, OR
		//   (2) generic type identifier 'var' is used as return type
		boolean untyped = false;
		JType retType = null;
		
		// 1) return type
		ParsedTypeName retTypeName = methodDecl.getReturnTypeName();
		if(retTypeName == ParsedTypeName.ANY){
			untyped = true;
		} else {
			retType = loadMemberType(context, retTypeName);
			if (retType.isObject()){
				Accessibility.checkTypeVisibility((ICompoundType)retType, builder.getStub(), true);
			}
		}
		
		// 2) parameter type				
		JParameter[] ptypesArray = collectParams(methodDecl, context, builder);
		
		// 3) annotations
		JAnnotation[] annos = getAttributesAsArray(methodDecl, context);
		
		// 4) executable
		MethodExecutable exec = null;
		HostedExecutable hexec = null;
		Method_bodyContext mbc = (Method_bodyContext)methodDecl.getAST();
		if(!hos){
			if(abs){
				if(mbc != null){
					throw new IllegalClassDefinitionException(
						context, 
						"Abstract method cannot contain a method body.",
						decl);
				}
			} else {
				// Attributes for hosting language interface
				if(annos != null){
					for(JAnnotation anno : annos){
						HostedAttributeType hat = HostedAttributeUtil.getHostedType(anno);
						if(hat == HostedAttributeType.UNTYPED){
							untyped = true;
						}
					}
				}
				
				if(mbc == null){
					throw new IllegalClassDefinitionException(
						context, 
						"Non-abstract/non-hosted method doesn't contain a method body.",
						decl);
				}
				
				exec = getMethodExecutable(methodDecl, ainfo, builder, sta);
			}				
		} else {				
			if(mbc != null){
				throw new IllegalClassDefinitionException(
					context, 
					"Hosted method cannot contain a method body.",
					decl);
			}
			
			// Attributes for hosting language interface
			if(annos != null){
				HostedAttributeType hattr = null;
				for(JAnnotation anno : annos){
					HostedAttributeType hat = HostedAttributeUtil.getHostedType(anno);
					if(hat != null){
						if(hat == HostedAttributeType.UNTYPED){
							untyped = true;
						} else {
							hattr = hat;
						}
					}
				}		
				
				if(hattr != null){
					hexec = new HostedMethodExecutable(fullName, methodDecl.getName(), hattr, sta);					
				}
			}

			if(hexec == null){
				throw new IllegalClassDefinitionException(
					context,
					"Method " + methodDecl.getName() + " is declared as hosted method but is not annotated accordingly.",
					decl);
			}
		}
		
		// 5) assemble the type
		JMethodType mType = null;
		if(exec != null || abs){
			mType = new JMethodType(methodDecl.getName(), ptypesArray, retType, exec, builder.getStub(), untyped);
		} else if(hexec != null){
			mType = new JMethodType(methodDecl.getName(), ptypesArray, retType, hexec, builder.getStub(), untyped);
		} else {
			throw new JSEError(
				"Cannot initialize method " + methodDecl.getName() + 
				" of type " + fullName + " due to missing executation body.");
		}
		
		JClassMember mmember = new JClassMethodMember(
			builder.getStub(),
			methodDecl.getName(), 
			acc, 
			sta, // static
			abs, // abstract
			mType,
			annos);	// annotations
		
		return mmember;
	}
	
	// Return null if it parsed and set a class static constructor.
	private JClassConstructorMember parseCtor(
		MemberDeclInfo decl, 
		AstInfo<ProgramContext> ainfo, 
		LoadingContext context, 
		JClassTypeBuilder builder, 
		FQName fullName){
		
		CtorDeclInfo ctorDecl = (CtorDeclInfo) decl;
		
		// modifiers check
		boolean sta = ctorDecl.isStatic();
		if(sta){
			parseStaticCtor(ctorDecl, ainfo, context, builder, fullName);
			return null;
		}
		
		boolean abs = ctorDecl.isAbstract();
		if(abs){
			throw new IllegalClassDefinitionException(
				context,
				"Cannot declare a constructor as abstract.",
				decl);
		}
		
		boolean hos = ctorDecl.isHosted();
		
		// 2) parameter type				
		JParameter[] ptypesArray = collectParams(ctorDecl, context, builder);
		
		// 3) annotations
		JAnnotation[] annos = getAttributesAsArray(ctorDecl, context);
		
		// 4) executable
		MethodExecutable exec = null;
		HostedExecutable hexec = null;
		ForwardInfo finfo = null;
		Function_callContext fcAst = ctorDecl.getForwardCallAst();
		
		if(!hos){
			// 4.1) Check if the constructor has forward call (super()/this())
			if(fcAst != null){
				// Assemble forward executable
				ConstructorForwardExecutable cfExe = 
					new ConstructorForwardExecutable(ainfo.create(ctorDecl.getForwardCallAst()), builder.getStub());
				finfo = new ForwardInfo(cfExe, !ctorDecl.isForwardingToThis());
			}
			
			// 4.2) main call
			ParserRuleContext prc = ctorDecl.getAST();
			exec = new MethodExecutable(ainfo.create(prc), builder.getStub(), sta);
		} else {
			if(fcAst != null){
				throw new IllegalClassDefinitionException(context, "A hosted constructor cannot call another constructor.",
						decl);
			}
			
			Method_bodyContext prc = (Method_bodyContext)ctorDecl.getAST();
			
			if(prc != null){
				throw new IllegalClassDefinitionException(context, "Hosted constructor cannot contain a method body.",
						decl);
			}
			
			// Attributes for hosting language interface
			if(annos != null){
				for(JAnnotation anno : annos){
					HostedAttributeType hattr = HostedAttributeUtil.getHostedType(anno);
					if(hattr != null){
						hexec = new HostedMethodExecutable(fullName, ctorDecl.getName(), hattr, false);
						break;
					}
				}					
			}

			if(hexec == null){
				throw new IllegalClassDefinitionException(
					context, 
					"Constructor is declared as hosted method but is not annotated accordingly.",
					decl);
			}
		}
		
		// 5) assemble the type
		JConstructorType cType = null;
		if(exec != null){
			cType = new JConstructorType("<ctor-" + fullName + ">", ptypesArray, exec, builder.getStub());
		} else if(hexec != null){
			cType = new JConstructorType("<ctor-" + fullName + ">", ptypesArray, hexec, builder.getStub());
		} else {
			throw new JSEError(
				"Cannot initialize constructor of type " + fullName + " due to missing executation body.");
		}
		
		JClassConstructorMember cmember = new JClassConstructorMember(
			builder.getStub(),
			ctorDecl.getName(), 
			ctorDecl.getAccessibility(), 
			sta, // static - always false here
			cType,
			finfo,
			false,
			annos);	// annotations
		
		return cmember;
	}
	
	private void createDefaultConstructor(JClassTypeBuilder builder, AstInfo<ProgramContext> ainfo, FQName fullName){
		// If no constructor has been found, automatically add a parameter-less public constructor.
		JParameter[] ptypesArray = makeThisParams(builder);
		
		Method_bodyContext mbc = new Method_bodyContext(null, 0); // Argument irrelevant here
		MethodExecutable exec = new MethodExecutable(ainfo.create(mbc), builder.getStub(), false);
		
		JConstructorType cType = new JConstructorType("<ctor-" + fullName + ">", ptypesArray, exec, builder.getStub());
		JClassConstructorMember cmember = new JClassConstructorMember(
			builder.getStub(),
			fullName.getSimpleName(), 
			Accessibility.PUBLIC, 
			false,
			cType,
			null,
			true, // This is a default constructor
			null);	// annotations
		
		// Do not add it yet; just store it for now. We are yet to determine whether the default ctor should be added. (Mapped type doesn't want this)
		builder.setDefaultInstanceConstructor(cmember);
	}
	
	private JParameter[] makeThisParams(JClassTypeBuilder builder){
		JParameter[] ptypesArray = new JParameter[1];
		ptypesArray[0] = new JParameter(
			Keywords.THIS, // "this"
			builder.getStub()); // self-reference
		return ptypesArray;
	}

	private JParameter[] collectParams(MethodDeclInfo methodDecl, LoadingContext context, ICompoundTypeBuilder builder){
		List<TypeAndName> list = methodDecl.getParameters();
		int orgSize = list == null ? 0 : list.size();
		
		// If it is static, we use the given parameter list;
		// If it is non-static, we add one more parameter to the list - this. 
		int size = methodDecl.isStatic() ? orgSize : orgSize + 1;
		
		List<JParameter> ptypes = new ArrayList<JParameter>();
		
		// Add "this" parameter for instance method.
		if(!methodDecl.isStatic()){
			ptypes.add(
				new JParameter(
					Keywords.THIS, // "this"
					builder.getStub() // self-reference
				)
			);
		}
		
		if(methodDecl.getParameters() != null){
			for(TypeAndName tan : methodDecl.getParameters()){
				ParsedTypeName ptn = tan.getTypeName();
				
				JType ptyp = null;
				JParameter jpm = ptn == ParsedTypeName.ANY ?
					new JParameter(tan.getParamName()):
					new JParameter(tan.getParamName(), ptyp = loadMemberType(context, tan.getTypeName()));
					
				if (ptyp != null && ptyp.isObject()){
					Accessibility.checkTypeVisibility((ICompoundType)ptyp, builder.getStub(), true);
				} else if (ptyp == VoidType.getInstance()) {
					throw new IllegalClassDefinitionException(context, "Cannot use void as parameter type.", methodDecl);
				}
				
				SyntaxHelper.checkVarTypeConflict(context.getContext(), jpm.getName());
				
				ptypes.add(jpm);
			}			
		}
		
		JParameter[] ptypesArray = new JParameter[size];
		ptypes.toArray(ptypesArray);
		
		return ptypesArray;
	}
	
	private JAnnotation[] getAttributesAsArray(MemberDeclInfo declInfo, LoadingContext context){
		JAnnotation[] annos = null;
		List<JAnnotation> list = getAttributes(declInfo.getAttributes(), context);
		if(list != null){
			annos = new JAnnotation[list.size()];
			list.toArray(annos);
		}
		return annos;
	}
	
	private void addMember(ICompoundTypeBuilder builder, JClassMember cmember, DeclInfo decl) {
		try {
			if(cmember.isStatic()){
				builder.addStaticMember(cmember);
			} else {
				builder.addInstanceMember(cmember);
			}
		} catch (Exception ex) {
			if (ex instanceof ILocationInfoAware) {
				ILocationInfoAware aware = (ILocationInfoAware)ex;
				aware.setLocationInfo(decl);
			}
			
			throw ex;
		}
	}

	// This is different from the one in ClassLoadingStatement which doesn't permit array-type loading.
	private JType loadMemberType(LoadingContext context, ParsedTypeName typeName) {
		ITypeTable tt = context.getTypeTable();
		JType type = typeName.getBasicType();
		
		// 1) built-in type?
		if(type != null){
			return loadScalarOrArrayType(tt, type, typeName);
		}
		
		// 2) type loaded already?
		String tName = typeName.getFQName().toString();
		type = context.getTypeTable().getType(tName);
		if(type != null){
			return loadScalarOrArrayType(tt, type, typeName);
		}
		
		// 3) type not loaded
		// Note TypeResolver will load it as array type if necessary, so we don't call loadScalarOrArrayType here.
		type = context.getTypeResolver().resolveType(context.getContext(), context.getNamespacePool(), typeName);
		
		return type;
	}
	
	private JType loadScalarOrArrayType(ITypeTable tt, JType type, ParsedTypeName typeName){
		if(typeName.isArray()){
			return JArrayType.createJArrayType(tt, type, typeName.getDimensionNumber());
		} else {
			return type;
		}
	}
	
	private MethodExecutable getMethodExecutable(
		MethodDeclInfo methodDecl, 
		AstInfo<ProgramContext> ainfo, 
		ICompoundTypeBuilder builder,
		boolean isStatic) {
		AstInfo<Method_bodyContext> ast = ainfo.create((Method_bodyContext)methodDecl.getAST());	
		MethodExecutable exec = new MethodExecutable(ast, builder.getStub(), isStatic);	
		
		return exec;
	}
}