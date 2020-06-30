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

import info.julang.execution.InContextTypeResolver;
import info.julang.execution.namespace.NamespaceConflictException;
import info.julang.execution.namespace.NamespacePool;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.interpretation.BadSyntaxException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.modulesystem.IModuleManager;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.JType;
import info.julang.typesystem.UnknownTypeException;
import info.julang.typesystem.VoidType;
import info.julang.typesystem.jclass.builtin.JArrayType;
import info.julang.util.OneOrMoreList;

/**
 * An context-free type resolver used to resolve a name to a Julian type, loading it 
 * if necessary.
 * <p/>
 * An instance of this class is shared among multiple threads and it's thread-safe. 
 * {@link InContextTypeResolver} provides a more friendly interface at the price of
 * losing thread safety.
 * 
 * @author Ming Zhou
 */
public class InternalTypeResolver {

	private TypeLoader loader;
	
	public InternalTypeResolver(){
		loader = 
			new TypeLoader(	// application loader
				new TypeLoader(null) // system loader
			);
	}
	
	/**
	 * Resolve a type, using namespace pool from runtime context.
	 * 
	 * @param context
	 * @param typeName
	 * @param throwIfNotFound
	 * @throws UnknownTypeException (if <code>throwIfNotFound</code> is true)
	 * @return the loaded type for the given name.
	 */
	public JType resolveType(Context context, ParsedTypeName typeName, boolean throwIfNotFound){
		return resolveType(context, typeName, throwIfNotFound, LoadingInitiative.SOURCE);
	}
	
	public JType resolveType(Context context, ParsedTypeName typeName, boolean throwIfNotFound, LoadingInitiative initiative){
		NamespacePool nsPool = context.getNamespacePool();
		return resolveType(context, nsPool, typeName, false, throwIfNotFound, initiative);
	}
	
	/**
	 * Resolve a type, using specified namespace pool.
	 * <p/>
	 * <b><font color="red">WARNING</font></b>: Can only be used by {@link TypeLoader} when loading a depending type.
	 * 
	 * @param context
	 * @param nsPool
	 * @param typeName
	 * @return
	 */
	JType resolveType(Context context, NamespacePool nsPool, ParsedTypeName typeName, LoadingInitiative initiative){
		return resolveType(context, nsPool, typeName, true, true, initiative);
	}
	
	/**
	 * Given a type name, resolve the type in current context.
	 * <p/><pre> (1) If the type has been loaded, go to (3)
	 * (2) If the type has not been loaded, 
	 *   (2.1) Look at the the namespace pool and make a full type name conversion.
	 *   (2.2) Try to see if a type of the assembled name can be found by module manager.
	 *   (2.3) If found, load the type; else, throw exception
	 * (3) Load the array type.
	 * </pre>
	 * @param context
	 * @param nsPool
	 * @param typeName
	 * @param reentry true if this is a re-entrance loading (load another while loading some type)
	 * @param throwIfNotFound true to throw if the type is not found
	 * @param initiative what causes this type to load
	 * @throws UnknownTypeException
	 * @return the loaded type for the given name.
	 */
	private JType resolveType(
		Context context, 
		NamespacePool nsPool, 
		ParsedTypeName typeName, 
		boolean reentry, 
		boolean throwIfNotFound, 
		LoadingInitiative initiative) {
		ITypeTable tt = context.getTypTable();
		JType basicTyp = typeName.getBasicType();
		if(basicTyp != null){
			return finishLoading(tt, basicTyp, typeName);
		}
		
		if(typeName == ParsedTypeName.ANY){
			return finishLoading(tt, AnyType.getInstance(), typeName);
		}
		
		IModuleManager mm = context.getModManager();
		
		OneOrMoreList<String> fnames = nsPool.getAllPossibleFullNames(typeName);
		if(fnames.hasOnlyOne()){
			String fname = fnames.getFirst();
			if(mm.getClassesByFQName(fname) != null){
				JType typ = tt.getType(fname, true);
				if(typ != null){
					// already loaded.
					return finishLoading(tt, typ, typeName);
				}
				typ = loadType(context, fname, true, reentry, initiative);
				return finishLoading(tt, typ, typeName);
			}
		} else {
			String nameToLoad = null;
			
			for(String fname : fnames){
				JType typ = null;
				if((typ = tt.getType(fname, true)) != null){
					return finishLoading(tt, typ, typeName);
				}
				
				// This is a precheck before we try to load the type. If a type for this name
				// doesn't exist in all the loaded modules, skip immediately.
				if(mm.getClassesByFQName(fname) == null){
					continue;
				}
				
				if(nameToLoad == null){
					nameToLoad = fname;
				} else if (!nameToLoad.equals(fname)) {
					// We already found a type of another name. The namespaces are conflicting with each other.
					throw new NamespaceConflictException(nameToLoad, fname);
				}
			}
			
			if(nameToLoad != null){
				JType typ = loadType(context, nameToLoad, true, reentry, initiative);
				if(typ != null){
					// let's bind the name, so that the next reference by the same simple name can be resolved faster.
					nsPool.addBinding(typeName.getFQName().toString(), nameToLoad);
					return finishLoading(tt, typ, typeName);
				}				
			}
		}
		
		if(throwIfNotFound){
			throw new UnknownTypeException(typeName.getFQName().toString());
		} else {
			return null;
		}
	}

	private JType loadType(Context context, String fname, boolean shouldThrow, boolean reentry, LoadingInitiative initiative) {
		try {
			JType type = loader.loadType(context, fname, reentry, initiative);
			return type;
		} catch (UnknownTypeException e) {
			if(!shouldThrow){
				return null;
			}
			throw e;
		}
	}

	private JType finishLoading(ITypeTable tt, JType typ, ParsedTypeName typeName) {
		if(typeName.getDimensionNumber() > 0){
			if (typ == VoidType.getInstance()) {
				throw new BadSyntaxException("Cannot declare array type with void as element type.");
			}
			return JArrayType.createJArrayType(tt, typ, typeName.getDimensionNumber());
		} else {
			return typ;
		}
	}
	
}
