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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import info.julang.execution.symboltable.TypeTable;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.builtin.doc.JulianDoc;
import info.julang.typesystem.jclass.builtin.doc.JulianFieldMemberDoc;
import info.julang.typesystem.jclass.builtin.doc.JulianFieldMembersDoc;

/**
 * A base class that provides a framework to parse built-in types.
 * 
 * @author Ming Zhou
 */
public abstract class BuiltInDocParserBase {
	
	protected static String JulianDocAnnotationName = JulianDoc.class.getSimpleName();	
	protected static String JulianFieldMembersDocAnnotationName = JulianFieldMembersDoc.class.getSimpleName();
	protected static String JulianFieldMemberDocAnnotationName = JulianFieldMemberDoc.class.getSimpleName();

	protected Log logger;
	private File builtInRoot;
	private ISerializationHelper serializer;
	private ModuleContext mc;
	
	BuiltInDocParserBase(File srcDirectory, ISerializationHelper serializer, ModuleContext mc, Log logger){
		builtInRoot = new File(srcDirectory, getRelativeSrcDir());
		this.logger = logger;
		this.serializer = serializer;
		this.mc = mc;
	}
	
	/** Collect documentations from built-in types written in Java. */
	void collectBuiltIn(Pattern pat, TypeTable tt) throws MojoExecutionException {
		File[] files = builtInRoot.listFiles();
		for (File file : files) {
			if (file.isDirectory() || !file.getName().endsWith(".java") || shouldSkip(file)){
				continue;
			}
			
	        try {
				FileInputStream fis = new FileInputStream(file);
		        CompilationUnit cu = JavaParser.parse(fis);
		        
		        NodeList<TypeDeclaration<?>> types = cu.getTypes();
		        for(TypeDeclaration<?> typ : types){
		        	Optional<AnnotationExpr> anno = typ.getAnnotationByName(JulianDocAnnotationName);
		        	if (anno.isPresent()){
		        		// Collect annotation properties
		        		AnnotationKVMap map = new AnnotationKVMap(anno.get());
		        		
		        		// Create a model to hold doc data
		        		DocModel.Type model = createDocModel(map);
		        		model.visibility = Accessibility.PUBLIC;
		        		
		        		// Collect doc for members
		        		collectDocForMethods(typ, model, tt);
		        		
			        	Optional<AnnotationExpr> anno2 = typ.getAnnotationByName(JulianFieldMembersDocAnnotationName);
			        	if (anno2.isPresent()){
			        		List<AnnotationKVMap> list = new ArrayList<AnnotationKVMap>();
			        		AccumulatingFieldMembersVisitor visitor = new AccumulatingFieldMembersVisitor();
			        		anno2.get().accept(visitor, list);
			        		collectDocForFields(typ, model, tt, list);
			        	}
			        	
			        	// Add doc
			        	mc.setBuildInDoc(model);
			        	
			        	// Built-in type's full name is same to its simple name
						String nm1 = map.getString(AnnotationKVMap.Keys.NAME);
						String nm2 = map.getString(AnnotationKVMap.Keys.ALIAS);
						if (!(
							pat.matcher(nm1).matches() || // name matches
						    (!"".equals(nm2) && pat.matcher(nm2).matches())) // alias exists and matches
						) {
							continue;
						}
						
		        		// Serialize
			        	String modName = "";
		        		File modRoot = serializer.getModuleRoot(modName, SerializationType.RAW);
		        		model.setModuleName(modName);
		        		serializer.serialize(modRoot, model, SerializationType.RAW);
		        	}
		        }
			} catch (FileNotFoundException e) {
				throw new MojoExecutionException("Cannot process the source file: " + file.getAbsolutePath(), e);
			}
		}
	}
	
	//---------------- customizable parts ----------------//
	
	protected abstract String getRelativeSrcDir();
	
	protected abstract DocModel.Type createDocModel(AnnotationKVMap map);

	protected void collectDocForMethods(TypeDeclaration<?> typ, DocModel.Type model, TypeTable tt){
		// To be optionally implemented by subclass
	}
	
	protected void collectDocForFields(TypeDeclaration<?> typ, DocModel.Type model, TypeTable tt, List<AnnotationKVMap> fields){
		// To be optionally implemented by subclass
	}
	
	protected boolean shouldSkip(File file) {
		// To be optionally implemented by subclass
		return false;
	}
	
	//---------------- processing field member annotations ----------------//
	// @JulianFieldMembersDoc(
	//   @JulianFieldMemberDoc(
	// 		name = "n1",
	// 		summary = "s1"
	// 	 )
	// )
	// <OR>
	// @JulianFieldMembersDoc(
	//   {
	//	   @JulianFieldMemberDoc(
	// 	  	 name = "n1",
	// 	 	 summary = "s1"
	// 	   ), 
	//     @JulianFieldMemberDoc(
	// 	  	 name = "n1",
	// 	  	 summary = "s1"
	// 	   )
	//   }
	// )
	private static class AccumulatingFieldMembersVisitor extends VoidVisitorAdapter<List<AnnotationKVMap>> {
		
	    @Override
	    public void visit(SingleMemberAnnotationExpr smAnno, List<AnnotationKVMap> list) {
	    	if (JulianFieldMembersDocAnnotationName.equals(smAnno.getName().asString())) {
	    		Expression expr = smAnno.getMemberValue();
	    		if (expr.isArrayInitializerExpr()){
	    			ArrayInitializerExpr aie = expr.asArrayInitializerExpr();
	    			NodeList<Expression> exprs = aie.getValues();
	    			for(Expression e : exprs) {
		    			addDocForField(e, list);
	    			}
	    		} else {
	    			addDocForField(expr, list);
	    		}
	    	}
	    }
	    
	    private void addDocForField(Expression expr, List<AnnotationKVMap> list){
	    	if (expr.isAnnotationExpr()) {
	    		AnnotationExpr ae = expr.asAnnotationExpr();
		    	if (JulianFieldMemberDocAnnotationName.equals(ae.getName().asString())) {
		    		AnnotationKVMap map = new AnnotationKVMap(expr.asAnnotationExpr());
		    		list.add(map);
		    	}
    		}
	    }
	}
}
