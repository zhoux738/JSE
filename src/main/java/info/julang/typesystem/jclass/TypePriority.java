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

package info.julang.typesystem.jclass;

/**
 * Used as the key in an ordered and deduplicated container storing types.
 * 
 * Uses the ancestor type itself for hashing, and a combination of rank, 
 * in-rank order, and total order to determine the sorting order in the container. 
 * 
 * @author Ming Zhou
 */
class TypePriority implements Comparable<TypePriority> {
	private int rank;
	private int order;
	private int torder;
	private JInterfaceType typ;
	
	TypePriority(JInterfaceType typ, int torder){
		this.typ = typ;
		this.torder = torder;
	}
	
	JInterfaceType getType() {
		return typ;
	}
	
	void setRank(int rank, int order){
		this.rank = rank;
		this.order = order;
	}
	
	@Override
	public int hashCode() {
		return typ.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		
		TypePriority other = (TypePriority) obj;
		
		return typ.equals(other.typ);
	}

	@Override
	public int compareTo(TypePriority o) {
		if (rank < o.rank) return -1;
		if (rank > o.rank) return 1;
		
		// Equal rank -> check order within the rank
		if (order < o.order) return -1;
		if (order > o.order) return 1;
		
		// Equal order within the rank -> check the total order
		if (torder < o.torder) return -1;
		if (torder > o.torder) return 1;
		
		return 0;
	}
	
	@Override
	public String toString() {
		return typ.getName() + " [rank=" + rank + ", in-rank order=" + order + ", total order=" + torder + "]";
	}
}
