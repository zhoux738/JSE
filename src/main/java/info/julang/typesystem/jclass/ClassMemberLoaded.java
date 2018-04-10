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
 * The class member that is actually loaded to a class type during runtime.
 * 
 * @author Ming Zhou
 */
public class ClassMemberLoaded {

	private JClassMember member;
	
	private int rank;
	
	public ClassMemberLoaded(JClassMember member, int rank) {
		this.member = member;
		this.rank = rank;
	}

	/**
	 * Get the class member.
	 * @return
	 */
	public JClassMember getClassMember() {
		return member;
	}

	/**
	 * Whether it is inherited from ancestors.
	 * @return true if inherited.
	 */
	public boolean isInherited() {
		return rank > 0;
	}
	
	/**
	 * The rank of this member as per its place of definition in the class hierarchy.
	 * @return
	 */
	public int getRank(){
		return rank;
	}

}
