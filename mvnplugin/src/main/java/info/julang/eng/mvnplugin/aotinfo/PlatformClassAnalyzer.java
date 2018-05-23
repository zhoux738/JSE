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

package info.julang.eng.mvnplugin.aotinfo;

import info.julang.hosting.HostedMethodProviderFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.EnumSet;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

public class PlatformClassAnalyzer {

	private static final String FACTORY_CLASSNAME;
	
	static {
		String fname = HostedMethodProviderFactory.class.getName();
		int last = fname.lastIndexOf('.');
		FACTORY_CLASSNAME = fname.substring(last + 1, fname.length());
	}
	
	/**
	 * Analyze the source file and try to find a public static member of the primary type (of same name as file)
	 * that has type HostedMethodProviderFactory.
	 * 
	 * @param javaSrcFile
	 * @return the full reference to the static member.
	 * @throws FileNotFoundException
	 */
	public static String findFactoryReference(File javaSrcFile) throws FileNotFoundException {
		FileInputStream fis = new FileInputStream(javaSrcFile);
        CompilationUnit cu = JavaParser.parse(fis);
        
        TypeDeclaration<?> decl = null;
        NodeList<TypeDeclaration<?>> types = cu.getTypes();
        for(TypeDeclaration<?> tdecl : types){
        	if (tdecl.getModifiers().contains(Modifier.PUBLIC)){
        		decl = tdecl;
        		break;
        	}
        }
        
		if(decl == null){
			return null;
		}
		
        NodeList<BodyDeclaration<?>> mems = decl.getMembers();
        for(BodyDeclaration<?> bdecl : mems){
        	if (bdecl.isFieldDeclaration()){
        		FieldDeclaration fdecl = bdecl.asFieldDeclaration();
        		EnumSet<Modifier> mods = fdecl.getModifiers();
        		if (mods.contains(Modifier.PUBLIC) && mods.contains(Modifier.STATIC)){
              		NodeList<VariableDeclarator> vdecls = fdecl.getVariables();
            		for(VariableDeclarator vdecl : vdecls){
            			String type = vdecl.getType().asString();
            			if (FACTORY_CLASSNAME.equals(type)) {
            		        String pkgName = cu.getPackageDeclaration().get().getName().asString();
            		        String className = decl.getName().toString();
                			String memberName = vdecl.getName().asString();
                			return pkgName + "." + className + "." + memberName;
            			}
            		}			
        		}
        	}
        }
        
        return null;
	}
	
}
