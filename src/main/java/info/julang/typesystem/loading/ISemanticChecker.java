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

import info.julang.interpretation.RuntimeCheckException;
import info.julang.interpretation.syntax.ClassSubtype;
import info.julang.langspec.ast.JulianParser.Class_definitionContext;
import info.julang.langspec.ast.JulianParser.ProgramContext;
import info.julang.modulesystem.naming.FQName;
import info.julang.parser.AstInfo;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JClassTypeBuilder;
import info.julang.typesystem.jclass.JInterfaceType;
import info.julang.typesystem.jclass.JInterfaceTypeBuilder;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.builtin.JObjectType;

import java.util.List;

/**
 * Perform semantic checking on a particular aspect of type definition.
 * 
 * @author Ming Zhou
 */
public interface ISemanticChecker {

	void check();
	
}

/**
 * A mapped class can only inherit from Object or another mapped class
 * 
 * @author Ming Zhou
 */
class MappedTypeParentsChecker implements ISemanticChecker {
	
	private JInterfaceTypeBuilder builder;
	
	public MappedTypeParentsChecker(JInterfaceTypeBuilder builder) {
		this.builder = builder;
	}

	@Override
	public void check() {
		JInterfaceType jct = builder.getStub();
		JClassType par = jct.getParent();
		if (par != JObjectType.getInstance()){
			Class<?> parent = par.getMappedPlatformClass();
			if (parent == null){
				throw new IllegalClassDefinitionException(
					jct.getName(), ClassSubtype.CLASS, "A mapped type must not inherit from a non-mapped type.", builder.getLocationInfo());
			} else if (!parent.isAssignableFrom(jct.getMappedPlatformClass())){
				throw new IllegalClassDefinitionException(
					jct.getName(), ClassSubtype.CLASS, "The inheritance of mapped type doesn't align with their platform counterparts.", builder.getLocationInfo());
			}
		}
	}	
}

/**
 * A non-abstract class should not contain any abstract methods.
 * 
 * @author Ming Zhou
 */
class AbstractMethodChecker implements ISemanticChecker {
	
	private JClassTypeBuilder builder;
	private FQName fullName;
	private AstInfo<ProgramContext> ainfo;
	private Class_definitionContext ast;
	
	public AbstractMethodChecker(
		JClassTypeBuilder builder, FQName fullName, AstInfo<ProgramContext> ainfo, Class_definitionContext ast) {
		this.builder = builder;
		this.fullName = fullName;
		this.ainfo = ainfo;
		this.ast = ast;
	}

	@Override
	public void check() {
		// Abstract methods checking
		// + If this is an interface or abstract class, skip;
		// + If this is a concrete class, then
		//      - if it doesn't implement any interface and its parent class is concrete, skip;
		//      - otherwise, this class must implement all abstract methods defined by parent or any ancestral class, as well as any interfaces.
		
		JClassType stub = builder.getStub();
		if(!stub.isClassType() || stub.getClassProperties().isAbstract()){
			return;
		}
		
		if(!stub.getParent().getClassProperties().isAbstract() && stub.getInterfaces().length == 0){
			return;
		}
		
		List<JClassMethodMember> members = builder.getAbstractMethods();
		int len = members.size();
		if(len > 0){
			StringBuilder sb = new StringBuilder();
			sb.append("Non-abstract class ");
			sb.append(fullName.toString());
			sb.append(" contains abstract methods: ");
			for(int i = 0; i < len; i++){
				JClassMethodMember mem = members.get(i);
				sb.append(mem.getName());
				sb.append("(");
				JParameter[] params = mem.getMethodType().getParams();
				if (params.length > 1){ // Skip "this"
					sb.append(params[1].getType().getName());
					if (params.length > 2){
						for(int j = 2; j < params.length; j++){
							JParameter jp = params[j];
							sb.append(",");
							sb.append(jp.getType().getName());
						}
					}
				}
				sb.append(")");
				if(i < len - 1){
					sb.append(", ");
				}
			}
			
			throw new RuntimeCheckException(sb.toString(), ainfo.create(ast));
		}
	}
}
