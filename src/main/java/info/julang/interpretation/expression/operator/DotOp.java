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

package info.julang.interpretation.expression.operator;

import static info.julang.langspec.Operators.DOT;

import java.util.List;

import info.julang.execution.symboltable.TypeTable;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.JSEError;
import info.julang.external.interfaces.JValueKind;
import info.julang.interpretation.IllegalOperandsException;
import info.julang.interpretation.JNullReferenceException;
import info.julang.interpretation.RuntimeCheckException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.context.ContextType;
import info.julang.interpretation.context.MethodContext;
import info.julang.interpretation.errorhandling.JSExceptionFactory;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.interpretation.expression.Operand;
import info.julang.interpretation.expression.Operator;
import info.julang.interpretation.expression.operand.InstMemberOperand;
import info.julang.interpretation.expression.operand.NameOperand;
import info.julang.interpretation.expression.operand.OperandKind;
import info.julang.interpretation.expression.operand.StaticMemberOperand;
import info.julang.interpretation.expression.operand.TypeOperand;
import info.julang.interpretation.expression.operand.ValueOperand;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.langspec.Keywords;
import info.julang.memory.value.FuncValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.MethodGroupValue;
import info.julang.memory.value.MethodValue;
import info.julang.memory.value.ObjectMember;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.memory.value.TypeValue;
import info.julang.memory.value.UntypedValue;
import info.julang.memory.value.indexable.IIndexable;
import info.julang.memory.value.operable.InitArgs;
import info.julang.typesystem.IllegalMemberAccessException;
import info.julang.typesystem.JType;
import info.julang.typesystem.JTypeKind;
import info.julang.typesystem.UnknownMemberException;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.JClassMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.MemberType;
import info.julang.typesystem.jclass.builtin.JDynamicType;
import info.julang.typesystem.jclass.builtin.JObjectType;
import info.julang.util.OneOrMoreList;

/**
 * Operator (<code>.</code>) for addressing a member of type/variable.
 * <pre><code>
 * a.b, a.fun(), Math.Pi
 * </code></pre>
 *
 * @author Ming Zhou
 */
public class DotOp extends Operator {

	private ThreadRuntime rt;
	
	public DotOp(ThreadRuntime rt) {
		super(".", 2, DOT.precedence, DOT.associativity);
		this.rt = rt;
	}

	@Override
	protected Operand doApply(Context context, Operand[] operands) {
		JValue lval = null;
		Operand lop = operands[0];
		Operand rop = operands[1];
		switch(lop.getKind()){
		case NAME:
			NameOperand nameOd = (NameOperand) operands[0];
			
			if(!nameOd.isComposite()){
				// 1) Try variable
				lval = context.getResolver().resolve(nameOd.getName());
				if(lval != null){
					break;
				}
			}
			
			// 2) Try type, using namespace pool
			// This means we will try to resolve against the very first type contained in the string.
			// For example, given "A.B.C.D", we will end up with A if a type of name "A", in combination 
			// with prefix from namespace pool, exists. Otherwise, we will try A.B, A.B.C, ... in that 
			// order. But note each attempt is performed at the corresponding dot op against the preceding
			// string operand with a single (A) or composite (A.B.C) name.
			// 
			// The attempt at resolution is also confined to loaded modules. If the module is not explicitly
			// imported (using "import" statement), a type of that module won't get resolved. In future
			// we may change the design to allow on-demand module loading.
			ParsedTypeName typeName = ParsedTypeName.makeFromFullName(nameOd.getName());
			JType typ = context.getTypeResolver().resolveType(typeName, false); // Do not throw if not found.
			if(typ != null){
				String typName = typ.getName();
				lval = context.getTypTable().getValue(typName);
				break;
			}
			
			// Add right operand to left operand as a new name ("A" . "B" -> "A.B")
			if(rop.getKind() == OperandKind.NAME){
				nameOd.addPart(((NameOperand)rop).getName());
				return nameOd;
			} else {
				throw new JSEError("Cannot apply '.' operator between a name operand and an opeand of type \"" + 
					rop.getKind().toString() + "\".");
			}
		case IMEMBER:
		case SMEMBER:
		case VALUE:
		case INDEX:
			lval = ((ValueOperand) lop).getValue();
			break;
		case TYPE:
			lval = ((TypeOperand) lop).getValue();
			break;
		default:
			throw new JSEError("Cannot apply '.' operator on a left opeand of type \"" + lop.getKind().toString() + "\".");
		}
		
		if(lval != null){
			lval = UntypedValue.unwrap(lval);
			ICompoundType leftDeclaredType = null,
			              superType = null; // To be populated only if this is a call to super method.
			if(rop.getKind() == OperandKind.NAME){
				if(lval.getKind() == JValueKind.REFERENCE){
					try {
						RefValue lvalRef = (RefValue)lval;
						leftDeclaredType = (ICompoundType)lvalRef.getType();
						lval = lvalRef.dereference();
					} catch (JNullReferenceException ex) {
						JulianScriptException jse = JSExceptionFactory.createException(
							KnownJSException.NullReference, rt, context);
						throw jse;
					}
				}
				
				if(lval.getKind() == JValueKind.OBJECT){
					String memberName = ((NameOperand)rop).getName();
					ObjectValue lov = (ObjectValue) lval;
					JValue mvalue = null; // Method value to be resolved
					FuncValue evalues = null; // Extension method values to be resolved
					boolean shouldReturnIndexOd = false;
					
					if(NameOperand.SUPER == lop){
						// SPECIAL: super.fun()
						
						if(context.getContextType() == ContextType.IMETHOD){
							MethodContext mc = (MethodContext)context;
							ICompoundType thisType = mc.getContainingType();
							if(thisType != null){
								superType = thisType.getParent();
								if(superType != null){
									checkAccessibility(superType, memberName, context, false);
									OneOrMoreList<ObjectMember> mvs = lov.getMemberValueByClass(memberName, superType, true);
									if(mvs.size() > 0){
										mvalue = mvs.getFirst().getValue();
									} else {
										evalues = getExtenionFuncValue(context, superType, memberName); 
									}
								} else if (thisType == JObjectType.getInstance()){
									throw new RuntimeCheckException(
										"The Object type doesn't have a parent type and thus cannot use super in its method.");
								} else {
									throw new JSEError("Type +\"" + thisType.getName() + "\" doesn't have a parent type.");
								}
							} else {
								throw new JSEError("Evaluation cannot continue because the type for current method is missing.");
							}		
						} else {
							throw new RuntimeCheckException(
								"Can only use super keyword in an instance method.");		
						}
						// [END] SPECIAL: super.fun()
					} else {
						// REGULAR: id.fun()
						
						if(leftDeclaredType == null){
							leftDeclaredType = lov.getClassType();
						}
						
						boolean isDynamic = JDynamicType.isDynamicType(lval);
						
						if (!isDynamic) {
							checkAccessibility(leftDeclaredType, memberName, context, false);
						}
						
						// Special. If we are accessing a member of method group, 
						//   (1) assume the member is a non-overloaded (but potentially overridden) instance method
						//   (2) create another group that contains the method for each member. 
						if (JValueKind.FUNCTION == lov.getBuiltInValueKind()){
							FuncValue fv = (FuncValue)lov;
							if (JValueKind.METHOD_GROUP == fv.getFuncValueKind()){
								MethodGroupValue mgv = (MethodGroupValue)lov;
								MethodValue[] mvs = mgv.getMethodValues();
								MethodValue[] ims = new MethodValue[mvs.length]; // instance members of same name
								for (int i = 0; i < ims.length; i++){
									OneOrMoreList<ObjectMember> overloads = mvs[i].getMemberValueByClass(memberName, null, true);
									JValue tempVal = null;
									int olSize = overloads.size();
									if (olSize == 1){
										tempVal = overloads.getFirst().getValue().deref();
									} else if (olSize > 1) {
										List<ObjectMember> ovList = overloads.getList();
										ObjectMember firstOm = ovList.get(0);
										if (firstOm.getClassRank() < ovList.get(1).getClassRank()) {
											tempVal = firstOm.getValue().deref();
										}
									}

									if (tempVal != null && tempVal instanceof MethodValue){
										ims[i] = (MethodValue)tempVal;
										continue;
									}
									
									throw new UnknownMemberException(mvs[i].getType(), memberName, false);
								}
								
								mvalue = new MethodGroupValue(context.getHeap(), ims);
							}
						} 
						
						if (mvalue == null) {
							ICompoundType thisType = null;
							if(context.getContextType() == ContextType.IMETHOD && lop.getKind() == OperandKind.NAME){
								NameOperand nameOd = (NameOperand) operands[0];
								if (Keywords.THIS.equals(nameOd.getName())){
									MethodContext mc = (MethodContext)context;
									thisType = mc.getContainingType();
								}
							}
							
							OneOrMoreList<ObjectMember> overloads = lov.getMemberValueByClass(memberName, thisType, false);
							int olSize = overloads != null ? overloads.size() : 0;
							if (olSize == 1) {
								mvalue = overloads.getFirst().getValue();
							} else if (olSize > 1) {
								mvalue = TempValueFactory.createTempMethodGroupValue(overloads);
							}
							
							// DESIGN NOTE: Extension methods do not participate in regular overloading resolution. If a member 
							// of the same name is already defined, either directly or by inheritance, on the object's type, no 
							// extension methods will be tried. 
							if (mvalue == null) {
								evalues = getExtenionFuncValue(context, leftDeclaredType, memberName); 
							}
							
							if (isDynamic && mvalue == null && evalues == null) {
								// If we don't have either regular members or extension members, try to resolve it as
								// dynamic property. This applies only when the type is, or inherits from, Dynamic.
								shouldReturnIndexOd = true;
							}
						}
						
						// [END] REGULAR: id.fun()
					}
					
					if (shouldReturnIndexOd) {
						// For Dynamic object, create an index operand, effectively converting expression << dyn.a >> to << dyn["a"] >>
						IIndexable lind = lval.asIndexer();
						lind.initialize(rt, new InitArgs(context, false));
						return Operand.createIndexOperand(
							lind, TempValueFactory.createTempStringValue(memberName));
					} else {
						if(mvalue == null && evalues == null){
							throw new UnknownMemberException(
								superType != null ? superType : lval.getType(), memberName, false);
						}
						
						return new InstMemberOperand(
							mvalue,
							superType != null ? superType :leftDeclaredType,
							evalues,
							lov,
							memberName);			
					}
				} else if (lval.getKind() == JValueKind.TYPE){
					ICompoundType typ = null;
					
					// Get type value for this type. If this is not a class type, we won't get a type value.
					JType rawTyp = ((TypeValue) lval).getValueType();
					String memberName = ((NameOperand)operands[1]).getName();
					if (rawTyp.getKind() != JTypeKind.CLASS) {
						throw new UnknownMemberException(rawTyp, memberName, true);
					} else {
						typ = (ICompoundType)rawTyp;
					}
					
					JClassMember member = typ.getStaticMemberByName(memberName);
					if(member == null){
						throw new UnknownMemberException(typ, memberName, true);
					}
					
					MemberType mt = member.getMemberType();
					if(mt == MemberType.FIELD || mt == MemberType.METHOD){
						TypeValue lov = (TypeValue) lval;
						
						if (mt == MemberType.METHOD){
							// If this member turns out to be a method, try to get all the overloaded members
							MethodValue[] mvs = lov.getMethodMemberValues(memberName);
							if (mvs.length > 1){
								// The method is overloaded
								MethodGroupValue mgv = new MethodGroupValue(context.getFrame(), mvs);
								checkAccessibility(typ, memberName, context, true);
								return new StaticMemberOperand(mgv, (JClassType)typ, memberName);
							}
						}
						
						checkAccessibility(typ, memberName, context, true);
						return new StaticMemberOperand(lov.getMemberValue(memberName), (JClassType)typ, memberName);
					} else {
						throw new UnknownMemberException(typ, memberName, true);
					}
				} else {
					throw IllegalMemberAccessException.referMemberOnNonObjectEx(lval.getType().getName());
				}
			}
		}
		
		throw new JSEError("Cannot evaluate '.' operator.");
	}

	private FuncValue getExtenionFuncValue(Context context, ICompoundType type, String memberName) {
		FuncValue evalues = null;
		
		OneOrMoreList<ObjectMember> extensions = 
			((TypeTable)context.getTypTable()).getExtensionMethodsByClass(memberName, type);
		int exSize = extensions != null ? extensions.size() : 0;
		if (exSize == 1) {
			evalues = (FuncValue)extensions.getFirst().getValue();
		} else if (exSize > 1) {
			evalues = TempValueFactory.createTempMethodGroupValue(extensions);
		}
		
		return evalues;
	}
}
