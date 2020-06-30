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

import info.julang.execution.Result;
import info.julang.execution.symboltable.VariableTable;
import info.julang.execution.threading.SystemInitiatedThreadRuntime;
import info.julang.external.exceptions.JSEException;
import info.julang.external.interfaces.JValueKind;
import info.julang.hosting.HostedMethodManager;
import info.julang.hosting.attributes.HostedAttributeType;
import info.julang.hosting.attributes.HostedAttributeUtil;
import info.julang.hosting.attributes.MappedHostedAttribute;
import info.julang.hosting.mapped.MappedTypeConversionException;
import info.julang.hosting.mapped.NewTypeGroup;
import info.julang.hosting.mapped.PlatformConversionUtil;
import info.julang.hosting.mapped.exec.MappedConstructorExecutable;
import info.julang.hosting.mapped.exec.MappedInitializerExecutable;
import info.julang.hosting.mapped.exec.MappedMethodExecutable;
import info.julang.hosting.mapped.inspect.IMappedType;
import info.julang.hosting.mapped.inspect.KnownMappedType;
import info.julang.hosting.mapped.inspect.MappedConstructorInfo;
import info.julang.hosting.mapped.inspect.MappedFieldInfo;
import info.julang.hosting.mapped.inspect.MappedMethodInfo;
import info.julang.hosting.mapped.inspect.MappedTypeInfo;
import info.julang.interpretation.context.Context;
import info.julang.langspec.Keywords;
import info.julang.memory.value.AttrValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.modulesystem.ModuleManager;
import info.julang.modulesystem.naming.FQName;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.ICompoundTypeBuilder;
import info.julang.typesystem.jclass.JClassConstructorMember;
import info.julang.typesystem.jclass.JClassFieldMember;
import info.julang.typesystem.jclass.JClassInitializerMember;
import info.julang.typesystem.jclass.JClassMember;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JClassTypeBuilder;
import info.julang.typesystem.jclass.JInterfaceType;
import info.julang.typesystem.jclass.JInterfaceTypeBuilder;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.MemberKey;
import info.julang.typesystem.jclass.MethodExecutable;
import info.julang.typesystem.jclass.annotation.JAnnotation;
import info.julang.typesystem.jclass.builtin.JConstructorType;
import info.julang.typesystem.jclass.builtin.JMethodType;
import info.julang.typesystem.jclass.builtin.JObjectType;
import info.julang.util.Box;
import info.julang.util.OneOrMoreList;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MappedTypeLoader {

	private static final String PlatformObjectFullName = "System.PlatformObject";
	private TypeLoader typeLoader;
	
	public MappedTypeLoader(TypeLoader typeLoader) {
		this.typeLoader = typeLoader;
	}
	
	/**
	 * Check if the type is mapped. If so, add it to {@link NewTypeGroup}. If not, add default constructor if necessary.
	 * 
	 * @param typ the type to be checked.
	 * @param context
	 * @param group if null and this type is determined to be a mapped type, a new group object 
	 * will be allocated by this method and get returned, so the caller should always use the 
	 * returned value instead of the keeping referring to the argument it has passed.
	 * @return unchanged from the passed argument <code>group</code> if this type is not a mapped one.
	 * @throws MappedTypeConversionException
	 */
	NewTypeGroup checkMappedType(
		ICompoundType typ, Context context, NewTypeGroup group, Map<String, ILoadingState> states) 
		throws JSEException {
		String cname = typ.getName();	
		ICompoundTypeBuilder builder = states.get(cname).getBuilder();
		
		JAnnotation[] annos = typ.getAnnotations();
		int len = annos != null ? annos.length : 0;
		if(len > 0){
			for(int i = 0; i < len; i++) {
				JAnnotation anno = annos[i];

				// We only care about one particular annotation: System.Mapped on a class type definition.
				if (anno.getAttributeType().getName().equals(HostedAttributeUtil.MAPPED)) {
					// 1) Check annotation target: cannot map to interface, enum, or attribute
					JInterfaceTypeBuilder cbuilder = (JInterfaceTypeBuilder)builder;
					boolean isClass = typ.isClassType();
					JValueKind kind = JValueKind.OBJECT;
					if (isClass) {
						kind = ((JClassType)typ).getValueKindForBuiltInType();
						if (kind != JValueKind.OBJECT) {
							throw new MappedTypeConversionException("Can only map to a class or interface.", builder.getLocationInfo());
						}
						
						JClassConstructorMember ctor = ((JClassTypeBuilder)cbuilder).getDefaultInstanceConstructor();
						if (ctor == null) {
							// If we don't have a default ctor, it means we have some explicitly defined ctors.
							throw new MappedTypeConversionException(
								"A mapped class must not contain user-defined constructors.", builder.getLocationInfo());
						}
						
						// Extra check - no instance fields are allowed.
						Map<String, OneOrMoreList<JClassMember>> mmap = cbuilder.getDeclaredInstanceMembers();
						if (mmap != null) {
							for(OneOrMoreList<JClassMember> mem : mmap.values()){
								switch(mem.getFirst().getMemberType()) {
								case FIELD:
									throw new MappedTypeConversionException(
										"Mapped type cannot have explicitly defined fields.", builder.getLocationInfo());
								default:
									// Allowed
								}
							}
						}
					}
					
					// 2) Prepare an execution environment
					SystemInitiatedThreadRuntime rt = new SystemInitiatedThreadRuntime(context);
					rt.getThreadStack().setNamespacePool(typ.getNamespacePool());
					VariableTable vt = new VariableTable(null);

					// 3) Compute annotation
					Set<String> allAnnos = new HashSet<String>();
					AttrValue av = typeLoader.initAnnotation(anno, context, rt, vt, null, allAnnos, kind, null);
					
					// 4) Collect members from the platform type
					MappedHostedAttribute hattr = (MappedHostedAttribute)
						HostedAttributeUtil.makeHostedAttribute(HostedAttributeType.MAPPED, av, null);
					HostedMethodManager hmm = ((ModuleManager)context.getModManager()).getHostedMethodManager();
					FQName pfqName = new FQName(cname);
					MappedTypeInfo mti = hmm.mapPlatformType(pfqName, isClass, hattr, av);

					// 5) Add type along with its mapping info
					if (group == null) {
						group = new NewTypeGroup();
					}
					
					group.add(typ, mti);
					cbuilder.setMappedPlatformClass(mti.getPlatformClass());
					MappedTypeParentsChecker checker = new MappedTypeParentsChecker(cbuilder);
					builder.addSemanticChecker(checker);
					
					return group;
				}
			}
		}
		
		// Conclude building non-mapped type
		if (builder instanceof JClassTypeBuilder) {
			JClassTypeBuilder cbuilder = (JClassTypeBuilder)builder;
			JClassConstructorMember ctor = cbuilder.getDefaultInstanceConstructor();
			if (ctor != null) {
				// This type doesn't have other explicitly defined ctors, must add the default one.
				cbuilder.addInstanceConstructor(ctor);
			}
		}
		
		builder.runSemanticCheckers();
		
		return group;
	}
	
	void addMappedMembers(
		ICompoundType typ, MappedTypeInfo mti, Context context, Map<String, ILoadingState> states, NewTypeGroup group) 
		throws MappedTypeConversionException {
		String cname = typ.getName();	
		JInterfaceTypeBuilder builder = (JInterfaceTypeBuilder)states.get(cname).getBuilder();
		builder.setHosted(true);
		
		boolean implPO = false;
		JInterfaceType[] intfs = typ.getInterfaces();
		if (intfs != null) {
			for(JInterfaceType intf : intfs){
				if (intf.getName().equals(PlatformObjectFullName)){ 
					// Can only do a name check. The PlatformObject type may not be added to type table yet.
					implPO = true;
					break;
				}
			}
		}
		
		addMappedMember(builder, mti, context, group, implPO);
		
		// Conclude building mapped type
		builder.runSemanticCheckers();
	}

	private void addMappedMember(JInterfaceTypeBuilder builder, MappedTypeInfo mti, Context context, NewTypeGroup group, boolean implPO) 
		throws MappedTypeConversionException {
		ICompoundType typ = builder.getStub();
		HostedMethodManager hmm = ((ModuleManager)context.getModManager()).getHostedMethodManager();
		
		// Fields
		// Only map final static built-in fields, so that we can just make a value copy from the platform type.
		List<MappedFieldInfo> fields = mti.getFields();
		for (MappedFieldInfo mfi : fields) {
			if (!(mfi.isStatic() && mfi.isFinal())){
				continue;
			}
			
			IMappedType mt = mfi.getType();
			if (mt.isExternal()){
				continue;
			}
			
			KnownMappedType kmt = (KnownMappedType) mt;
			if (mt.getDimension() > 0){ // As of 0.1.10, do not support array
				continue;
			}
			
			String fname = mfi.getName();
			
			// Add member
			JClassFieldMember member = new JClassFieldMember(
				typ, 
				fname, 
				Accessibility.PUBLIC, 
				true,          // isStatic
				true,          // isConst 
				kmt.getType(), // Script type mapped from platform type 
				null);         // No annotation
			builder.addStaticMember(member);
			
			// Add initializer		
			JParameter[] ptypesArray = new JParameter[0];
			MappedInitializerExecutable exec = new MappedInitializerExecutable(typ, kmt, mfi.getFieldMember(), true);
			JMethodType mType = new JMethodType("<init>-" + fname, ptypesArray, kmt.getType(), exec, typ);
			JClassInitializerMember initializer = new JClassInitializerMember(typ, fname, true, mType);
			builder.addInitializerMember(initializer);
		}
		
		// Methods
		List<MappedMethodInfo> methods = mti.getMethods();
		Map<String, OneOrMoreList<JClassMember>> instMembers = builder.getDeclaredInstanceMembers();
		for (MappedMethodInfo mmi : methods) {
			// Return type
			IMappedType mt = mmi.getType();
			JType retType = PlatformConversionUtil.fromPlatformType(mt.getOriginalClass(), context, group);
			
			// Filter out special members
			boolean sta = mmi.isStatic();
			String memberName = null;
			if (!sta) {
				Box<String> filtered = filterSepcialMember(mmi, typ, implPO);
				if (filtered == null) {
					// Skip this member
					continue;
				} else {
					memberName = filtered.get();
				}
			}
			
			// Parameter types
			IMappedType[] mts = mmi.getParamTypes();
			JParameter[] ptypesArray = getParamTypes(context, mts, sta ? null : typ, group, hmm);
			
			// Executable
			MethodExecutable exec = new MappedMethodExecutable(typ, mmi.getMethodMember(), sta);
			if (memberName == null) {
				memberName = mmi.getName();
			} else {				
				if (memberName.endsWith("quals")) {
					// SPECIAL CASE. equals/pfEquals as per interface have param type Object, but a mapped API can only
					// generate late-binding param type, which in this case would be <platform-java.lang.Object>. Note 
					// script type Object does NOT map to java.lang.Object. So we need insert another layer to separate 
					// type checking and method execution.
					ForwardingMethodExecutable fme = new ForwardingMethodExecutable(
						typ, sta, new Result(TempValueFactory.createTempBoolValue(false)));
					fme.setForwardingExecutable(exec);
					JParameter[] ptypesArray2 = new JParameter[2];
					ptypesArray2[0] = ptypesArray[0];
					ptypesArray2[1] = new JParameter("another", JObjectType.getInstance());
					JMethodType mtype = new JMethodType(
						memberName, ptypesArray2, retType, fme, typ, false);
					JClassMember mmember = new JClassMethodMember(
						typ,
						memberName, 
						Accessibility.PUBLIC, 
						sta,           // isStatic
						false,         // isAbstract 
						mtype,
						null);	       // annotations
					
					checkInstMemberExistence(instMembers, memberName, mmember, implPO);
					builder.addInstanceMember(mmember);
					
					continue;
				}
			}
			
			// Method type
			JMethodType mtype = new JMethodType(
				memberName, ptypesArray, retType, exec, typ, false);
			
			// Class member
			JClassMember mmember = new JClassMethodMember(
				typ,
				memberName, 
				Accessibility.PUBLIC, 
				sta,           // isStatic
				false,         // isAbstract 
				mtype,
				null);	       // annotations
			
			if (sta) {
				builder.addStaticMember(mmember);
			} else {
				checkInstMemberExistence(instMembers, memberName, mmember, implPO);
				builder.addInstanceMember(mmember);
			}
		}
		
		// Constructors
		if (builder.isClassType()){
			List<MappedConstructorInfo> ctors = mti.getConstructors();
			for (MappedConstructorInfo mci : ctors) {
				// Parameter types
				boolean sta = mci.isStatic();
				IMappedType[] mts = mci.getParamTypes();
				JParameter[] ptypesArray = getParamTypes(context, mts, sta ? null : typ, group, hmm);
				
				// Executable
				MappedConstructorExecutable exec = new MappedConstructorExecutable(typ, mci.getConstructorMember());
				// Ctor type
				JConstructorType cType = new JConstructorType("<ctor-" + typ.getName() + ">", ptypesArray, exec, typ);
				
				// Class member	
				JClassConstructorMember cmember = new JClassConstructorMember(
					builder.getStub(),
					typ.getName(), 
					Accessibility.PUBLIC, 
					false, // isStatic
					cType,
					null,  // forwardInfo - the platform ctor may have redirection, but that's beyond our management.
					false, // isDefault
					null); // annotations
				
				((JClassTypeBuilder)builder).addInstanceConstructor(cmember);
			}	
		}
	}

	/**
	 * Check if a method with same signature has been added. If so, throw MappedTypeConversionException.
	 */
	private void checkInstMemberExistence(
		Map<String, OneOrMoreList<JClassMember>> instMembers,
		String memberName, 
		JClassMember mmember, 
		boolean implPO) throws MappedTypeConversionException {
		if (instMembers != null && instMembers.containsKey(memberName)){
			// To add a member, first verify that a number of same signature has not been added already.
			MemberKey mk = mmember.getKey();
			OneOrMoreList<JClassMember> mems = instMembers.get(memberName);
			for(JClassMember mem : mems) {
				if (mem.getKey().equals(mk)){
					throw new MappedTypeConversionException(
						"When " + (implPO ? "implementing " + PlatformObjectFullName + " on a mapped type" : "mapping a type") 
						+ ", the method \"" + memberName + "\" must not be explicitly defined.");
				}
			}
		}
	}
	
	private Box<String> filterSepcialMember(MappedMethodInfo mmi, ICompoundType typ, boolean implPO) {
		String name = mmi.getName();
		switch(name){
		case "getClass":
		case "notify":
		case "notifyAll":
			return checkSepcialMember(name, mmi.getMethodMember(), new Class<?>[0], implPO, true);
		case "wait":
			Method m = mmi.getMethodMember();
			int plen = m.getParameterTypes().length;
			switch(plen){
			case 0: 
				return checkSepcialMember(name, m, new Class<?>[0], implPO, true);
			case 1: 
				return checkSepcialMember(name, m, new Class<?>[]{long.class}, implPO, true);
			case 2: 
				return checkSepcialMember(name, m, new Class<?>[]{long.class, int.class}, implPO, true);
			default:
			}
			break;
		case "equals":
			return checkSepcialMember(name, mmi.getMethodMember(), new Class<?>[]{Object.class}, implPO, false);
		case "hashCode":
		case "toString":
			return checkSepcialMember(name, mmi.getMethodMember(), new Class<?>[0], implPO, false);
		default:		
		}
		
		// Doesn't match
		return new Box<String>(name);
	}
	
	/**
	 * @return 
	 *   null if it should be filtered out, 
	 *   or a box with member name if this member is deemed to be special, 
	 *   or an empty box if this member should be treated normally. 
	 */
	private Box<String> checkSepcialMember(String name, Method m, Class<?>[] ptypes, boolean implPO, boolean shouldFilterOut) {
		Class<?>[] typs = m.getParameterTypes();
		int len = typs.length;
		if (len != ptypes.length){
			// Do not match
			return new Box<String>(null);
		}
		for (int i = 0; i < len; i++){
			if (typs[i] != ptypes[i]){ // sufficient to use == to compare since all the types involved are basic JVM types.
				// Do not match
				return new Box<String>(null);
			}
		}
		
		if (shouldFilterOut) {
			return null;
		}
		
		String mname = name;
		if (implPO) {
			// toString => pfToString
			mname = "pf" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
		}
		
		return new Box<String>(mname);
	}

	private JParameter[] getParamTypes(
		Context context, IMappedType[] mts, JType thisType, NewTypeGroup group, HostedMethodManager hmm) 
		throws MappedTypeConversionException{
		
		boolean sta = thisType == null;
		JParameter[] ptypesArray = new JParameter[sta ? mts.length : mts.length + 1];
		int i = sta ? 0 : 1;
		if (!sta) {
			ptypesArray[0] = new JParameter(Keywords.THIS, thisType);
		}
		for (int j = 0; j < mts.length; j++, i++) {
			JType ptyp = PlatformConversionUtil.fromPlatformType(mts[j].getOriginalClass(), context, group);
			ptypesArray[i] = new JParameter(mts[j].getParamName(), ptyp);
		}
		
		return ptypesArray;
	}
}
