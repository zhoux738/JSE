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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import info.julang.JSERuntimeException;
import info.julang.execution.Argument;
import info.julang.execution.Result;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.symboltable.RestrictedTypeTable;
import info.julang.execution.symboltable.VariableTable;
import info.julang.execution.threading.SystemInitiatedThreadRuntime;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.exceptions.JSEError;
import info.julang.external.exceptions.JSEException;
import info.julang.external.interfaces.JValueKind;
import info.julang.hosting.HostedMethodExecutable;
import info.julang.hosting.HostedMethodManager;
import info.julang.hosting.IHostedMethodProvider;
import info.julang.hosting.attributes.BridgedHostedAttribute;
import info.julang.hosting.attributes.HostedAttribute;
import info.julang.hosting.attributes.HostedAttributeType;
import info.julang.hosting.attributes.HostedAttributeUtil;
import info.julang.hosting.execution.INativeExecutor;
import info.julang.hosting.mapped.NewTypeGroup;
import info.julang.hosting.mapped.inspect.MappedTypeInfo;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.context.ExecutionContextType;
import info.julang.interpretation.context.MethodContext;
import info.julang.interpretation.internal.NewObjExecutor;
import info.julang.interpretation.statement.ExpressionStatement;
import info.julang.langspec.ast.JulianParser.AnnotationContext;
import info.julang.langspec.ast.JulianParser.ArgumentContext;
import info.julang.langspec.ast.JulianParser.Atrribute_initializationContext;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.langspec.ast.JulianParser.ProgramContext;
import info.julang.memory.value.AttrValue;
import info.julang.memory.value.FuncValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.JValueBase;
import info.julang.memory.value.TempValueFactory;
import info.julang.memory.value.TypeValue;
import info.julang.memory.value.VoidValue;
import info.julang.modulesystem.ClassInfo;
import info.julang.modulesystem.ModuleManager;
import info.julang.parser.AstInfo;
import info.julang.typesystem.JType;
import info.julang.typesystem.UnknownTypeException;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.JClassConstructorMember;
import info.julang.typesystem.jclass.JClassFieldMember;
import info.julang.typesystem.jclass.JClassInitializerMember;
import info.julang.typesystem.jclass.JClassMember;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassStaticConstructorMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JDefinedClassType;
import info.julang.typesystem.jclass.MemberType;
import info.julang.typesystem.jclass.annotation.IllegalAttributeUsageException;
import info.julang.typesystem.jclass.annotation.JAnnotation;
import info.julang.typesystem.jclass.annotation.MetaAnnotation;
import info.julang.typesystem.jclass.builtin.JAttributeType;
import info.julang.typesystem.jclass.builtin.JConstructorType;
import info.julang.typesystem.jclass.builtin.JEnumType;
import info.julang.typesystem.jclass.builtin.JMethodType;
import info.julang.util.Box;
import info.julang.util.Pair;

/**
 * The type loader which actually loads the type from modules.
 * <p>
 * The type loader in Julian engine adopts a very simple hierarchical & isolation model 
 * in comparison to Java's class loader. It essentially contains only two loaders: the
 * system loader and the application loader. The system loader loads every type of 
 * <code style="color: green">System</code> module. The application loaders loads other classes.
 * 
 * @author Ming Zhou
 */
public class TypeLoader {
	
	private final Argument[] initArgs = new Argument[0];
	
	private Incubator incubator;
	private TypeLoader parent;
	private MappedTypeLoader mtloader;
	
	TypeLoader(TypeLoader parent){
		incubator = new Incubator();
		this.parent = parent;
		mtloader = new MappedTypeLoader(this);
	}
	
	/**
	 * Load a type using fully qualified type name.
	 * <p>
	 * This method will load the type with the given name and all the types it refers to 
	 * by class hierarchy, field type and method parameter/return type. It will not load
	 * types used by field initializer, method/constructor body and class initializer.
	 * <p>
	 * This method is thread safe. It cannot be accessed by more than one thread at any 
	 * time. The downside of this design is that it prevents parallelized loading of 
	 * class set which do not overlap with each other (for example, T1 loads A, B and C
	 * while T2 loads D, E, F). Any attempt at enabling such capability will involve a
	 * complex design using locks per each class so it's not worth investing.
	 * 
	 * @param context
	 * @param typeName
	 * @param reentry	false if used for loading a type directly during interpretation; 
	 * true if used to load a dependent class by {@link TypeLoader}.
	 * @return
	 */
	public synchronized JType loadType(Context context, String typeName, boolean reentry, LoadingInitiative initiative){
		JType loaded = null;
		ITypeTable tt = context.getTypTable();
		boolean toDelegate = false;
		
		// First check if it is already loaded.
		if((loaded = tt.getType(typeName)) != null){
			return loaded;
		}
		
		switch(incubator.getStatus(typeName)){
		case LOADING_BY_CURRENT_THREAD:
			// Since it is the same thread which is loading this type, we can return 
			// here as though it had been loaded.
			return incubator.getStub(typeName);
		case NOT_BEING_LOADED:
			// Start loading a new type
			toDelegate = parent != null && typeName.startsWith("System.");
			if(!toDelegate){
				ClassInfo cinfo = context.getModManager().getClassesByFQName(typeName);
				incubator.addType(typeName, cinfo.getClassDeclInfo().getSubtype(), initiative);
			}
			break;
		}

		JType type = null;
		try {
			if(toDelegate){
				// Delegate to parent loader
				type = parent.loadType(context, typeName, reentry, initiative);
			} else {
				// Load by this loader itself
				LoadingContext ctxt = new LoadingContext(
					context, typeName, incubator.getState(typeName));
				
				switch(ctxt.getClassDeclInfo().getSubtype()){
				case INTERFACE:
					InterfaceDeclarationStatement stmt = new InterfaceDeclarationStatement();
					stmt.parse(ctxt);
					break;
				default: // CLASS and derivatives
					ClassDeclarationStatement stmt2 = new ClassDeclarationStatement();
					stmt2.parse(ctxt);
				}
				
				type = ctxt.getTypeBuilder().build(false); // Do not seal yet
				incubator.sealType(typeName, null);
			}
		} catch (JSERuntimeException e) {
			// If class is not located, we need clean up the incubator
			if(!toDelegate){
				incubator.sealType(typeName, e);
			} else {
				if (context.getExecutionContextType() == ExecutionContextType.InAnnotation && 
					e instanceof ClassLoadingException){
					ClassLoadingException cle = (ClassLoadingException)e;
					throw cle.getJSECause();
				}
			}
		}
		
		activate(context, tt, reentry, toDelegate);
		
		return type;
	}
	
	/**
	 * Activate types built from source code.
	 * <p>
	 * At this point we have types built up from script sources. Before loading them into type table there are still
	 * a couple of issues to be taken care of: 
	 * 
	 * <li>Add implicit class members - including default ctor and mapped public APIs</li>
	 * <li>Apply class initializer and static field initializers</li>
	 * <li>Apply annotations on class and its members</li><br>
	 * 
	 * @param context
	 * @param tt
	 * @param reentry
	 * @param toDelegate
	 */
	private void activate(Context context, ITypeTable tt, boolean reentry, boolean toDelegate){
		if(parent != null){
			parent.activate(context, tt, reentry, false);
		}
		
		if(toDelegate){
			return;
		}
		
		if(!reentry){
			// Save a shallow copy of loading states
			Map<String, ILoadingState> states = incubator.getLoadingStates();
			if (states.size() == 0) {
				return;
			}
			
			// Add all the types into type table with finalized = false, so that TypeResolver won't be 
			// able to see them yet.
			List<ICompoundType> types = incubator.mature();

			JSERuntimeException ex = null;
			NewTypeGroup group = null;
			Box<ICompoundType> diagBox = new Box<ICompoundType>(null);
			try {
				group = collectMappedTypes(context, tt, types, states, diagBox);
			} catch (JSEException mex) {
				ex = createLoadingEx(mex, diagBox, states);
			} catch (JSERuntimeException rex) {
				ex = rex instanceof ClassLoadingException ? rex : createLoadingEx(rex, diagBox, states);
			}
			
			if (ex != null) {
				unregisterMappedTypes(context, group);
				throw ex;
			}
			
			List<String> typeNames = null;
			try {
				typeNames = postMature(context, tt, types, states, group, diagBox);
			} catch (JSERuntimeException rex) {
				ex = rex instanceof ClassLoadingException ? rex : createLoadingEx(rex, diagBox, states);
			}
			
			if (ex != null) {
				// If post-mature phase faulted, must clean up and throw.
				removeUnfinalizedTypes(tt, types);
				unregisterMappedTypes(context, group);
				throw ex;
			}
			
			// Finalize all the types in a single atomic operation
			for (ILoadingState ils : states.values()){
				ils.getBuilder().seal();
			}
			tt.finalizeTypes(typeNames);
		}
	}
	
	//------------------------- Cleanup upon initialization failures -------------------------//
	
	private ClassLoadingException createLoadingEx(Exception ex, Box<ICompoundType> diagBox, Map<String, ILoadingState> states){
		// Try to create a loading exception with loading state as argument - this ctor can derive more info from the exception. 
		ICompoundType typ = diagBox.get();
		if (typ != null) {
			ILoadingState stat = states.get(typ.getName());
			if (stat != null) {
				stat.setFaulted(ex);
				return new ClassLoadingException(stat);
			}
		}
		
		return new ClassLoadingException(ex);
	}
	
	private void removeUnfinalizedTypes(ITypeTable tt, List<ICompoundType> types){
		if (types != null) {
			List<String> names = new ArrayList<String>();
			for(ICompoundType typ : types) {
				names.add(typ.getName());
			}
			
			tt.removeUnfinalizedTypes(names);
		}
	}
	
	private void unregisterMappedTypes(Context context, NewTypeGroup group){
		if (group != null) {
			HostedMethodManager hmm = context.getModManager().getHostedMethodManager();
			Collection<Pair<ICompoundType, MappedTypeInfo>> ntypes = group.listAll();
			for(Pair<ICompoundType, MappedTypeInfo> pair : ntypes){
				JClassType jct = (JClassType)pair.getFirst();
				String pcname = jct.getMappedPlatformClass().getName();
				hmm.removeMappedPlatformClass(pcname);
			}
		}
	}

	//----------------------------- Pre-induction initialization -----------------------------//
	
	// Note here we must go through several iterations in this very strict order, since any step
	// would require access to certain values that must have been initialized from a previous
	// step. In fact, some compromise has to be made to eliminate cross references to even make 
	// this work.
	//
	// collectMappedTypes (1) => postMature (2 - 5)
	
	private NewTypeGroup collectMappedTypes(
		Context context, ITypeTable tt, List<ICompoundType> types, Map<String, ILoadingState> states, Box<ICompoundType> diag) throws JSEException {
		// 1) mapped members
		NewTypeGroup group = null;
		for(ICompoundType typ : types){
			// Can throw MappedTypeConversionException
			diag.set(typ);
			group = mtloader.checkMappedType(typ, context, group, states);
		}
		
		if (group != null) {
			Collection<Pair<ICompoundType, MappedTypeInfo>> ntypes = group.listAll();
			for(Pair<ICompoundType, MappedTypeInfo> pair : ntypes){
				MappedTypeInfo mti = pair.getSecond();
				if (mti != null) {
					ICompoundType typ = pair.getFirst();
					diag.set(typ);
					// Can throw MappedTypeConversionException
					mtloader.addMappedMembers(pair.getFirst(), mti, context, states, group);
				}
			}
		}
		
		return group;
	}
	
	private List<String> postMature(
		Context context, ITypeTable tt, List<ICompoundType> types, Map<String, ILoadingState> states, NewTypeGroup group, Box<ICompoundType> diag) {
		// 2) create type values - CRUCIAL: this will materialize the type definitions into type objects 
		//    and get them allocated on heap memory. Although we have not sealed the types yet, after this 
		//    step the majority of methods on type builders become essentially useless.  
		List<String> typeNames = new ArrayList<String>();
		for(JType typ : types){
			ILoadingState state = states.get(typ.getName());
			if (state.getInitiative() == LoadingInitiative.ATTRIBUTE_MEMBER) {
				// Only allow a subset of types
				if (!RestrictedTypeTable.isAllowedInAttributContext(typ)){
					throw new IllegalAttributeUsageException(
						"Trying to load a type which is not allowed in Attribute initializer: " + typ.getName());
				}
			}
			
			String name = typ.getName();
			tt.addType(name, typ);
			typeNames.add(name);
		}
	
		// 3) static initializers for Enum types - this is to allow Annotation to refer to Enum values.
		for(ICompoundType typ : types){
			if (JEnumType.isEnumType(typ)){
				diag.set(typ);
				TypeValue tvalue = tt.getValue(typ.getName());
				applyInitializers(typ, tvalue, context);
			}
		}

		// 4) annotations
		for(ICompoundType typ : types){
			diag.set(typ);
			TypeValue tvalue = tt.getValue(typ.getName());
			applyAnnotations(typ, tvalue, context);
		}
		
		// 5) static initializers for other types
		for(ICompoundType typ : types){
			if (!JEnumType.isEnumType(typ)){
				diag.set(typ);
				TypeValue tvalue = tt.getValue(typ.getName());
				applyInitializers(typ, tvalue, context);
			}
		}
		
		// 6) mapping annotations were already computed at step 1). Just add them now.
		if (group != null) {
			Collection<Pair<ICompoundType, MappedTypeInfo>> ntypes = group.listAll();
			for(Pair<ICompoundType, MappedTypeInfo> pair : ntypes){
				ICompoundType typ = pair.getFirst();
				diag.set(typ);
				AttrValue av = pair.getSecond().getAttributeValue();
				if (av != null) {
					TypeValue tvalue = tt.getValue(typ.getName());
					tvalue.addClassAttrValue(av);
				}
			}
		}
		
		return typeNames;
	}

	private void applyInitializers(ICompoundType typ, TypeValue tvalue, Context context){
		SystemInitiatedThreadRuntime rt = new SystemInitiatedThreadRuntime(context);
		rt.getThreadStack().setNamespacePool(typ.getNamespacePool());
		
		// Invoke static initializers, if any
		JClassInitializerMember[] initializers = typ.getClassInitializers(true);
		if(initializers.length > 0){
			for(JClassInitializerMember initializer : initializers){
				JMethodType mtype = initializer.getMethodType();
				try {
					Result res = mtype.getExecutable().execute(rt, FuncValue.DUMMY, initArgs);
					JValue val = res.getReturnedValue(true);
					if (val != VoidValue.DEFAULT){ // enum field initializer returns void
						val.assignTo(tvalue.getMemberValue(initializer.getFieldName()));
					}
				} catch (EngineInvocationError e) {
					throw new JSEError(
						"An error occurs while invoking initializer for field " + 
						initializer.getFieldName() + " of class " + typ.getName());
				}
				
				String fieldName = initializer.getFieldName();
				JClassFieldMember field = 
					(JClassFieldMember) typ.getStaticMemberByName(fieldName);
				if(field.isConst()){
					tvalue.setMemberConst(fieldName);
				}
			}
		}
		
		// Invoke class static constructor
		if (typ.isClassType()){
			JDefinedClassType jdt = (JDefinedClassType) typ;
			JClassStaticConstructorMember staCtor = jdt.getClassStaticConstructor();
			if (staCtor != null){
				try {
					JMethodType mtype = staCtor.getMethodType();
					mtype.getExecutable().execute(rt, FuncValue.DUMMY, initArgs);
				} catch (EngineInvocationError e) {
					throw new JSEError(
						"An error occurs while invoking static constructor of class " + typ.getName());
				}			
			}
		}
	}
	
	private void applyAnnotations(ICompoundType typ, TypeValue tvalue, Context context){		
		SystemInitiatedThreadRuntime rt = new SystemInitiatedThreadRuntime(context);
		rt.getThreadStack().setNamespacePool(typ.getNamespacePool());
		
		// Add attribute values to the class type's type value
		VariableTable vt = new VariableTable(null);
		MethodContext ctxt = MethodContext.duplicateContext(
			context,
			FuncValue.DUMMY,
			rt.getStackMemory(),
			vt,
			typ.getNamespacePool(),
			context.getStandardIO(),
			typ,
			true, // treat as static
			ExecutionContextType.InAnnotation);
		
		if (MetaAnnotation.AttributeTypeName.equals(typ.getName())){
			// Special handling: for System.AttributeType, we need initialize it internally since it cannot annotate itself.
			initializeAttributeType(ctxt, rt, tvalue);
		} else {
			JDefinedClassType jdt = null;
			if (typ.isClassType()){
				jdt = (JDefinedClassType) typ;
			}
			
			HostedAttribute ha = addAttributeToType(typ.getAnnotations(), ctxt, rt, vt, tvalue, null);
			if(ha != null){
				if (jdt != null){
					jdt.getBuilder().setHosted(true);
				} else {
					throw new IllegalAttributeUsageException(
						"Interface definition cannot be annotated with hosted: " + typ.getName());
				}
			}
			
			HostedMethodManager hmm = ((ModuleManager)context.getModManager()).getHostedMethodManager();
			
			// Add attribute values to the class type's type value for each member
			JClassMember[] members = typ.getClassInstanceMembers();
			addAttributeToMembers(members, ctxt, rt, vt, typ, tvalue, ha, hmm);
			
			members = typ.getClassStaticMembers();
			addAttributeToMembers(members, ctxt, rt, vt, typ, tvalue, ha, hmm);
			
			if (jdt != null){
				members = jdt.getClassConstructors();
				addAttributeToMembers(members, ctxt, rt, vt, typ, tvalue, ha, hmm);
			}
		}
	}
	
	private void addAttributeToMembers(
		JClassMember[] members, Context context, 
		ThreadRuntime rt, VariableTable vt, 
		ICompoundType typ, TypeValue tvalue,
		HostedAttribute classHa, HostedMethodManager hmm){ // The last two parameters are for hosted methods
		if(members == null){
			return;
		}
		
		for(JClassMember member : members){
			// We don't care any member that is not defined in this type.
			if (member.getDefiningType() != typ){
				continue;
			}
			
			BridgedHostedAttribute ha = addAttributeToType(member.getAnnotations(), context, rt, vt, tvalue, member);
			if(ha != null){
				ha.inheritFrom(classHa);
				
				MemberType mtype = member.getMemberType();
				if(mtype == MemberType.METHOD){
					JClassMethodMember mmember = (JClassMethodMember) member;
					JMethodType methodType = mmember.getMethodType();
					if(methodType.isBridged()){
						// This casting is safe for now but needs more refactoring.
						HostedMethodExecutable hexec = (HostedMethodExecutable) methodType.getHostedExecutable();
						registerBridgedMethod(hexec, ha, hmm);
					} else {
						throw new JSEError("A method annotated by hosted attribute is not marked as hosted.");
					}
				} else if (mtype == MemberType.CONSTRUCTOR) {
					JClassConstructorMember mmember = (JClassConstructorMember) member;
					JConstructorType methodType = mmember.getCtorType();
					if(methodType.isHosted()){
						// This casting is safe for now but needs more refactoring.
						HostedMethodExecutable hexec = (HostedMethodExecutable) methodType.getHostedExecutable();
						registerBridgedMethod(hexec, ha, hmm);
					} else {
						throw new JSEError("A method annotated by hosted attribute is not marked as hosted.");
					}
				} else {
					// We should be able to remove this after meta attributes (AttributeUsage) are added.
					throw new IllegalAttributeUsageException(
						"Hosted attribute can only annotate a method or constructorm but saw a " + mtype.name());
				}
			}
		}
	}
	
	private void registerBridgedMethod(HostedMethodExecutable hexec, BridgedHostedAttribute bha, HostedMethodManager hmm){
		IHostedMethodProvider provider = hmm.find(bha.getApiset());
		INativeExecutor nexe = provider.getExecutor(bha.getName());

		hexec.setNativeExecutor(nexe);
	}
	
	private void initializeAttributeType(Context context, ThreadRuntime rt, TypeValue tvalue){
		JClassType typ = (JClassType) tvalue.getValueType();
	
		NewObjExecutor noe = new NewObjExecutor(rt);
		AttrValue av = (AttrValue) noe.newObject(
			context, new ArrayList<ArgumentContext>(), typ, 
			AstInfo.succ(new ProgramContext(null, 0), "<unknown>"));
		
		JValue fVal = av.getMemberValue(MetaAnnotation.Field_Bool_AllowMultiple);
		TempValueFactory.createTempBoolValue(false).assignTo(fVal);
		
		fVal = av.getMemberValue(MetaAnnotation.Field_Int_Target);
		TempValueFactory.createTempIntValue(MetaAnnotation.Target.ATTRIBUTE).assignTo(fVal);
		
		tvalue.addClassAttrValue(av);
	}
	
	/**
	 * Add attributes to the given type, either on type itself, or a certain member of that type.
	 * 
	 * @param annos
	 * @param context
	 * @param rt
	 * @param vt
	 * @param tvalue the type value
	 * @param member if null, apply the annotation to the type.
	 * @return if a hosted (and Bridged) attribute is found among the given attributes, return it.
	 */
	private BridgedHostedAttribute addAttributeToType(
		JAnnotation[] annos, Context context, ThreadRuntime rt, VariableTable vt, TypeValue tvalue, JClassMember member){
		BridgedHostedAttribute hattr = null;
		ICompoundType typ = (ICompoundType)tvalue.getValueType();
		
		if(typ.isClassType() && annos != null && annos.length > 0){
			JValueKind kind = ((JClassType)typ).getValueKindForBuiltInType();
			Set<String> allAnnos = new HashSet<String>();
			
			for(JAnnotation anno : annos){
				// Skip mapped annotation since we already handled it in a previous step when adding mapped members.
				if (anno.getAttributeType().getName().equals(HostedAttributeUtil.MAPPED)){
					continue;
				}
				
				AttrValue av = initAnnotation(anno, context, rt, vt, tvalue, allAnnos, kind, member);
				
				HostedAttributeType hat = HostedAttributeUtil.getHostedType(anno);
				if(hat != null && hat == HostedAttributeType.BRIDGED && hattr == null){
					hattr = (BridgedHostedAttribute)HostedAttributeUtil.makeHostedAttribute(hat, av, member);
				}
			}
		}
		
		return hattr;
	}

	AttrValue initAnnotation(
		JAnnotation anno,
		Context context, 
		ThreadRuntime rt,
		VariableTable vt, 
		TypeValue tvalue, 
		Set<String> allAnnos,
		JValueKind kind, 
		JClassMember member) {
		
		JAttributeType aType = anno.getAttributeType();
		AstInfo<AnnotationContext> ainfo = anno.getAstInfo();
		NewObjExecutor noe = new NewObjExecutor(rt);
		AttrValue av = (AttrValue) noe.newObject(context, new ArrayList<ArgumentContext>(), aType, ainfo);
		
		// 1) Initialize the attribute instance
		List<Atrribute_initializationContext> inits = anno.getFieldInitializers();
		for (Atrribute_initializationContext init : inits) {
			//atrribute_initialization
		    //  : IDENTIFIER '=' expression
		    //  ;
			String fname = init.IDENTIFIER().getText();
			ExpressionContext exc = init.expression();
			vt.enterScope();
			JValue mVal = null;
			try {
				ExpressionStatement es = new ExpressionStatement(rt, ainfo.create(exc));
				es.interpret(context);
				mVal = av.getMemberValue(fname);
				es.getResult().getReturnedValue(false).assignTo(mVal);
			} catch (UnknownTypeException utx) {
				throw new IllegalAttributeUsageException(
					"Trying to use a type which is not allowed in Attribute initializer: " + utx.getTypeName());
			} finally {
				// Seal the value
				if (mVal instanceof JValueBase) {
					((JValueBase)mVal).setConst(true);
				}
				
				vt.exitScope();
			}
		}
		
		// 2) Get annotations for this attribute type.
		MetaAnnotation metaAnno = MetaAnnotation.getFromAttributeType(rt.getTypeTable(), aType);
		String aName = aType.getName();
		
		// 3) Check legality of application
		if(member == null) {
			// Check A - can this attribute be applied to class/enum/attribute?
			switch(kind){
			case ATTRIBUTE:
				checkAnnotationApplicability(aName, metaAnno, allAnnos, MetaAnnotation.Target.ATTRIBUTE, "attribute");
				break;
			case ENUM:
				checkAnnotationApplicability(aName, metaAnno, allAnnos, MetaAnnotation.Target.ENUM, "enum");
				break;
			default: // CLASS
				checkAnnotationApplicability(aName, metaAnno, allAnnos, MetaAnnotation.Target.CLASS, "class");
				break;
			}
			
			if (tvalue != null) {
				tvalue.addClassAttrValue(av);
			}
		} else {
			// Check B - can this attribute be applied to field/method/constructor?
			MemberType memberType = member.getMemberType();
			int target = MetaAnnotation.convertMemberTypeToTarget(memberType);
			checkAnnotationApplicability(aName, metaAnno, allAnnos, target, memberType.name().toLowerCase());
			
			if (tvalue != null) {
				tvalue.addMemberAttrValue(member.getKey(), av);
			}
		}
		
		allAnnos.add(aName);
		return av;
	}

	private void checkAnnotationApplicability(
		String aName, MetaAnnotation metaAnno, Set<String> allAnnos, int attribute, String targetName) {
		if(!metaAnno.isAllowMultiple() && allAnnos.contains(aName)){
			throw new IllegalAttributeUsageException(
				"The attribute " + aName + " cannot be applied more than one time.");
		}
		
		boolean result = metaAnno.isApplicableTo(attribute);
		if(!result){
			throw new IllegalAttributeUsageException(
				"The attribute " + aName + " cannot be applied to " + targetName + " declaration.");
		}
	}
}
