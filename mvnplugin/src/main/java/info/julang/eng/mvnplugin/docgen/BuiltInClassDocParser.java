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

package info.julang.eng.mvnplugin.docgen;

import info.julang.eng.mvnplugin.docgen.DocModel.BuiltInClassType;
import info.julang.eng.mvnplugin.docgen.DocModel.BuiltInField;
import info.julang.execution.symboltable.TypeTable;
import info.julang.hosting.HostedExecutable;
import info.julang.typesystem.jclass.JClassConstructorMember;
import info.julang.typesystem.jclass.JClassFieldMember;
import info.julang.typesystem.jclass.JClassMember;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.MemberType;
import info.julang.util.Box;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.maven.plugin.logging.Log;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

/**
 * Extract and serialize documentation for built-in class types.
 * 
 * @author Ming Zhou
 */
public class BuiltInClassDocParser extends BuiltInDocParserBase {

	// HACK - there is no easy way to map the overloaded doc for ctor to its annotation since the param info
	// is hard to deduce by analyzing the source file (not impossible, but would be a huge amount of works),
	// so we rely on the fact that the constructors are added in the same order as declared in the source
	// file and use an index to correlate between declaration and ctor member retrieved from runtime.
	private int ctorIndex;
	
	BuiltInClassDocParser(File srcDirectory, ISerializationHelper serializer, ModuleContext mc, Log logger) {
		super(srcDirectory, serializer, mc, logger);
	}

	@Override
	protected String getRelativeSrcDir() {
		return "info/julang/typesystem/jclass/builtin";
	}

	@Override
	protected DocModel.Type createDocModel(AnnotationKVMap map){
		return new BuiltInClassType(map);
	}

	@Override
	protected void collectDocForFields(TypeDeclaration<?> typ, DocModel.Type mod, TypeTable tt, List<AnnotationKVMap> fields){
		if (fields.size() == 0) {
			return;
		}
		
		BuiltInClassType model = (BuiltInClassType)mod;
		
		// Get runtime type
		JClassType jtyp = (JClassType)tt.getType(mod.name);
		
		// For each documented field, synthesize information from both runtime type and doc annotation.
		for (AnnotationKVMap map : fields){
			String fname = map.getString(AnnotationKVMap.Keys.NAME);
			boolean stat = map.is(AnnotationKVMap.Keys.STATIC);
			JClassMember jcm = stat ? jtyp.getStaticMemberByName(fname) : jtyp.getInstanceMemberByName(fname);
			if (jcm == null || jcm.getMemberType() != MemberType.FIELD){
				logger.warn(
					"Member " + fname + " is not " + (stat ? "a static" : "an instance") + " field. " + 
					"Specify isStatic in JulianDoc annotation for this field if it's a static member.");
				return;
			}
			
			JClassFieldMember jcmm = (JClassFieldMember) jcm;
			
			BuiltInField f = new BuiltInField(fname, jcmm, map);
			
			model.fields.add(f);
		}
	}
	
	@Override
	protected void collectDocForMethods(TypeDeclaration<?> typ, DocModel.Type mod, TypeTable tt){
		BuiltInClassType model = (BuiltInClassType)mod;
		
		// Get runtime type
		JClassType jtyp = (JClassType)tt.getType(model.name);
		
		// Synthesize method information from both runtime type and doc annotation.
		ctorIndex = 0;
		List<FieldDeclaration> fields = typ.getFields();
		for (FieldDeclaration fd : fields) {
			if (fd.getModifiers().contains(Modifier.STATIC)){
				collectMethodDoc(fd, model, jtyp);
			}
		}
	}
	
	private void collectMethodDoc(FieldDeclaration fd, BuiltInClassType model, JClassType jtyp){
		NodeList<VariableDeclarator> nl = fd.getVariables();
		final VariableDeclarator vd = nl.size() > 0 ? fd.getVariables().get(0) : null;
		if (vd != null) {
			AnnotationKVMap map = null;

        	Optional<AnnotationExpr> anno = fd.getAnnotationByName(JulianDocAnnotationName);
        	if (anno.isPresent()){
        		map = new AnnotationKVMap(anno.get());
        	} else {
				// If not JulianDoc annotation is present, skip this field.
				return;
			}
			
        	String mn = map.getString(AnnotationKVMap.Keys.NAME);
        	if ("".equals(mn)){
        		// If the annotation doesn't provide method's name, try deduce that from the field initializer, 
        		// which should be an anonymous class instantiation.
        		mn = null;
        		
    			final Box<String> nameBox = new Box<String>(mn);
    			Optional<Expression> expr = vd.getInitializer();
    			if (expr.isPresent()){
    				expr.get().ifObjectCreationExpr(new Consumer<ObjectCreationExpr>(){
    					@Override
    					public void accept(ObjectCreationExpr newExpr) {
    						ClassOrInterfaceType citype = newExpr.getType();
    						if (citype.getName().toString().equals(HostedExecutable.class.getSimpleName())) {
    							// Try to find the name of method from the arguments (the 2nd param for HostedExecutable)
    		        			NodeList<Expression> args = newExpr.getArguments();
    		        			if (args.size() >= 2) {
    		        				Expression argExpr = args.get(1);
    		        				String[] sections = argExpr.toString().split("\\.");
    		        				String methodName = null;
    		        				int len = sections.length;
    		        				if (len == 1){
    		        					methodName = sections[0];
    		        					if (methodName.startsWith("\"")){ // CASE I: "startsWith"
    		        						methodName = methodName.substring(1, methodName.length() - 1);
    		        					}
    		        				} else if (len > 1){
    		        					for(int i = len - 1; i >= 0; i--){
    		        						String str = sections[i];
    		        						if (str.equals("name()") && i - 1 >= 0){ // CASE II: MethodNames.equals.name()
    				        					methodName = sections[i - 1];
    				        					break;
    		        						}
    		        					}
    		        				} 
    		        				
    		        				if (methodName == null) {
    		        					logger.warn("The argument expression, " + newExpr.toString() + 
    		        						", doesn't contain information about the method's name. If this is a constructor, specify the name in annotation.");
    		        				} else {
    			        				nameBox.set(methodName);
    		        				}
    		        			}
    						}
    					}
    				});
    			}
    			
    			mn = nameBox.get();
    			if (mn == null) {
    				logger.warn(
    					"Field " + vd.getName() + 
    					" doesn't contain a deducible method name in its initializer." +
    					" An object-creation expression of type " + HostedExecutable.class.getName() + 
    					" is expected with Julian method's name in either string literals or Enum.name() call as the 2nd argument.");
    				return;
    			}
        	}

			// Add to the doc model
        	if (mn.equals(jtyp.getName())){
    			addCtorDoc(model, mn, map, jtyp, ctorIndex);
    			ctorIndex++;
        	} else {
    			addMethodDoc(model, mn, map, jtyp);
        	}
		}
	}

	private void addCtorDoc(BuiltInClassType model, String mn, AnnotationKVMap map, JClassType jtyp, int index) {
		JClassConstructorMember[] ctors = jtyp.getClassConstructors();
		JClassConstructorMember ctor = ctors[index];
		
		try {
			DocModel.Constructor m = new DocModel.BuiltInConstructor(ctor, map);
			model.ctors.add(m);
		} catch (Exception dge) {
			logger.warn("Couldn't extract doc for constructor. " + dge.getMessage());
		}
	}

	private void addMethodDoc(BuiltInClassType model, String name, AnnotationKVMap map, JClassType jtyp) {
		boolean stat = map.is(AnnotationKVMap.Keys.STATIC);
		JClassMember jcm = stat ? jtyp.getStaticMemberByName(name) : jtyp.getInstanceMemberByName(name);
		if (jcm == null || jcm.getMemberType() != MemberType.METHOD){
			logger.warn(
				"Member " + name + " is not " + (stat ? "a static" : "an instance") + " method. " + 
				"Specify isStatic in JulianDoc annotation for this field if it's a static member.");
			return;
		}
		
		JClassMethodMember jcmm = (JClassMethodMember) jcm;
		try {
			DocModel.Method m = new DocModel.BuiltInMethod(name, jcmm, map);
			model.methods.add(m);
		} catch (Exception dge) {
			logger.warn("Couldn't extract doc for method \'" + name + "\'. " + dge.getMessage());
		}
	}

}
