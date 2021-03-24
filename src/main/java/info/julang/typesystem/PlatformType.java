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

package info.julang.typesystem;

import info.julang.hosting.HostedMethodManager;
import info.julang.interpretation.RuntimeCheckException;
import info.julang.typesystem.conversion.Convertibility;
import info.julang.util.OSTool;

/**
 * A type placeholder used by the script engine to represent a mapped type. 
 * This type by itself does nothing, other than authentically retains the 
 * information about the class being mapped. The main purpose of this type 
 * is to facilitate type compatibility check during runtime, without binding 
 * the platform type at the loading-time.
 * 
 * @author Ming Zhou
 */
public class PlatformType implements JType, IMapped {

	private Class<?> clazz;
	
	private HostedMethodManager hmm;
	private String fqname;
	
	public PlatformType(HostedMethodManager hmm, String fqname) {
		this.hmm = hmm;
		this.fqname = fqname;
	}

	@Override
	public JTypeKind getKind() {
		return JTypeKind.PLATFORM;
	}

	@Override
	public String getName() {
		return "<platform-" + fqname + ">";
	}
	
	@Override
	public String toString() {
		return "<platform-" + fqname + ">";
	}
	
	@Override
	public Class<?> getMappedPlatformClass() {
		if (clazz == null) {
			preload();
		}
		
		return clazz;
	}

	/**
	 * Returns {@link Convertibility#UNSAFE} if the target type is not ANY.
	 */
	@Override
	public Convertibility getConvertibilityTo(JType type) {
		if (type instanceof IMapped) {
			IMapped mapped = (IMapped)type;
			Class<?> mclass = mapped.getMappedPlatformClass();
			if (mclass != null) {
				Convertibility c = canBeAssignedBy(mclass, clazz);
				if (c != null){
					return c;
				}
			}
		}
		
		return Convertibility.UNCONVERTIBLE;
	}

	/**
	 * @param mappingTarget
	 * @return a safe convertibility value if assignable; null if not assignable.
	 */
	public Convertibility canBeAssignedBy(Class<?> srcClass) {
		if (clazz == null) {
			preload();
		}
		
		return canBeAssignedBy(clazz, srcClass);
	}
	
	private Convertibility canBeAssignedBy(Class<?> tgtClass, Class<?> srcClass) {
		if (tgtClass == srcClass) {
			return Convertibility.EQUIVALENT;
		} else if (tgtClass.isAssignableFrom(srcClass)){
			return Convertibility.DOWNGRADED;
		}
		return null;
	}

	private void preload() {
		clazz = hmm.preloadPlatformClass(fqname);
		if (clazz == null) {
			throw new RuntimeCheckException("Cannot load dependent class \"" + fqname + "\".");
		}
	}

	@Override
	public boolean isBasic() {
		return false;
	}

	@Override
	public boolean isObject() {
		return false;
	}

	@Override
	public boolean isBuiltIn() {
		return false;
	}

	@Override
	public int getSize() {
		return 0;
	}
}
