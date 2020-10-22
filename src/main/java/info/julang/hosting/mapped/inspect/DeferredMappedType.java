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

package info.julang.hosting.mapped.inspect;

/**
 * The mapped type is not known yet.
 * 
 * @author Ming Zhou
 */
public class DeferredMappedType implements IMappedType {

	private String fullClassName;
	private int dim;
	private boolean sameToEnclosingType;
	private Class<?> ocls;
	private String pname;
	
	public DeferredMappedType(String fullClassName, int dim, Class<?> ocls, String pName){
		this.fullClassName = fullClassName;
		this.dim = dim;
		this.ocls = ocls;
		this.pname = pName;
	}
	
	//---------------------- IMappedType ----------------------//
	
	@Override
	public boolean isExternal() {
		return true;
	}

	@Override
	public int getDimension() {
		return dim;
	}
	
	@Override
	public Class<?> getOriginalClass() {
		return ocls;
	}

	@Override
	public String getParamName() {
		return pname;
	}
	
	//---------------------- KnownMappedType ----------------------//
	
	public String getFullClassName(){
		return fullClassName;
	}

	void setSameToEnclosingType(boolean sameToEnclosingType) {
		this.sameToEnclosingType = sameToEnclosingType;
	}

	/**
	 * @return true if this type is same to the enclosing type. For example, a field or return type is exactly that of class type.
	 */
	public boolean isSameToEnclosingType(){
		return sameToEnclosingType;
	}
	
	//---------------------- Object ----------------------//

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dim;
		result = prime * result + ((fullClassName == null) ? 0 : fullClassName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeferredMappedType other = (DeferredMappedType) obj;
		if (dim != other.dim)
			return false;
		if (fullClassName == null) {
			if (other.fullClassName != null)
				return false;
		} else if (!fullClassName.equals(other.fullClassName))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		String ret = fullClassName;
		if (dim > 0) {
			int rank = dim;
			while (rank > 0) {
				ret += "[]";
			}
		}
		
		return ret;
	}
}
