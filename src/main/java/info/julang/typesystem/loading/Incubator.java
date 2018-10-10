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

import info.julang.MultipleJSERuntimeException;
import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.syntax.ClassSubtype;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.IDefinedType;
import info.julang.typesystem.jclass.JDefinedClassType;
import info.julang.typesystem.jclass.JDefinedInterfaceType;
import info.julang.typesystem.loading.depresolving.HardDependencyResolver;
import info.julang.typesystem.loading.depresolving.IDependencyResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * An incubator is where types are parsed and built.
 * <p>
 * When the type loader loads a particular type, it sets up the incubator and adds the type stub to it, 
 * and continues building the type on top of that stub. At some point it may see a reference to another
 * new type and has to start loading that type too, by first adding its stub to the incubator. This way,
 * all the new types that are depended, either directly or indirectly, by the very first type will be 
 * hatched in the incubator.
 * <p>
 * {@link #mature()} will be called at the end of main loading logic, when there is no new type to be 
 * added. According to LCP, we check the loading state of each type and if any of them failed, the 
 * entire litter will be exterminated.
 * 
 * @author Ming Zhou
 */
public class Incubator {

	static enum Status {
		
		NOT_BEING_LOADED,
		
		LOADING_BY_CURRENT_THREAD
		
	}
	
	private Map<String, ILoadingState> farm;
	
	Incubator(){
		farm = new HashMap<String, ILoadingState>();
	}
	
	void addType(String typName, ClassSubtype subtype, LoadingInitiative initiative){
		if(farm.containsKey(typName)){
			throw new JSEError("Trying to load a type twice.", this.getClass());
		}
		farm.put(typName, new LoadingState(Thread.currentThread(), typName, subtype, initiative));
	}
	
	/**
	 * If exception is not null, this type is faulted (loading failure).
	 * 
	 * @param typName
	 * @param ex
	 */
	void sealType(String typName, Exception ex){
		ILoadingState state = null;
		if((state = farm.get(typName)) == null){
			throw new JSEError("Trying to update a type which is not being loaded.", this.getClass());
		}

		if(ex == null){
			state.setParsed();
		} else {
			state.setFaulted(ex);
		}
	}
	
	Status getStatus(String typName){
		ILoadingState lstate = farm.get(typName);
		
		if(lstate == null){
			return Status.NOT_BEING_LOADED;
		}
		
		if(lstate.getOwner() == Thread.currentThread()){
			return Status.LOADING_BY_CURRENT_THREAD;
		}
		
		throw new JSEError("The type is being loaded by another thread.");
	}

	JType getStub(String typName) {
		IClassOrInterface idt = farm.get(typName).getType();
		return (JType) idt;
	}
	
	ILoadingState getState(String typeName) {
		return farm.get(typeName);
	}
	
	List<ICompoundType> mature() {
		List<Exception> exes = null;
		
		for(ILoadingState state : farm.values()){
			if(state.isFaulted()){
				if(exes == null){
					exes = new ArrayList<Exception>();
				}
				//exes.add(state.getException()); 
				exes.add(new ClassLoadingException(state));
			}
		}
		
		if(exes != null){
			// Clean things up. If this thread fails to reload the classes it intended to, 
			// it must remove all the residues from the working place.
			Thread currThread = Thread.currentThread();
			List<String> toRemove = new ArrayList<String>();
			for(Entry<String, ILoadingState> entry : farm.entrySet()){
				if(entry.getValue().getOwner() == currThread){
					toRemove.add(entry.getKey());
				}
			}
			for(String key : toRemove){
				farm.remove(key);
			}
			throw new MultipleJSERuntimeException(exes);
		}
		
		List<ICompoundType> newTypes = null;
		try {
			IDependencyResolver resolver = new HardDependencyResolver();
			newTypes = new LinkedList<ICompoundType>();
			// Since Java doesn't support covariance for return type, we must first force an erasing of generics (cast to Object) to satisfy the compiler. 
			// The second casting (Object to List<ILoadingState>) is safe as long as the implementation of resolver returns a list of ILoadingState.
			@SuppressWarnings("unchecked")
			List<ILoadingState> ordered = (List<ILoadingState>)(Object)resolver.resolve(farm.values());
			for(ILoadingState ios : ordered){
				IDefinedType idt = ios.getType();
				newTypes.add(idt.isClassType() ? (JDefinedClassType)idt : (JDefinedInterfaceType)idt);
			}
		} catch (Exception e) {
			throw new ClassLoadingException(e);
		}
		
		farm.clear();
		
		return newTypes;
	}
	
	/**
	 * @return A map keyed by full type name
	 */
	Map<String, ILoadingState> getLoadingStates(){
		HashMap<String, ILoadingState> farm2 = new HashMap<String, ILoadingState>();
		farm2.putAll(farm);
		return farm2;
	}
}
