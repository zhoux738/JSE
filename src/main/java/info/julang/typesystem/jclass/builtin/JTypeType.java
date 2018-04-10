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

package info.julang.typesystem.jclass.builtin;

import info.julang.typesystem.BuiltinTypes;
import info.julang.typesystem.jclass.BuiltinTypeBootstrapper.TypeFarm;
import info.julang.typesystem.jclass.JClassMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JClassTypeBuilder;
import info.julang.typesystem.jclass.TypeBootstrapper;

/**
 * A <i>Type</i> type meta-circularly represents a Julian class type.
 * <p/>
 * This type is used with type value, a particular kind of value stored in memory
 * that contains runtime data about a class type.
 * 
 * @author Ming Zhou
 */
public class JTypeType extends JClassType {

	private static JTypeType INSTANCE;

	public JTypeType(String name) {
		super(name, JTypeType.getInstance(), new JClassMember[0]);
	}
	
	private JTypeType(){
		
	}

	public static JTypeType getInstance() {
		return INSTANCE;
	}

	@Override
	public boolean isBuiltIn() {
		return true;
	}
	
	public static class BoostrapingBuilder implements TypeBootstrapper {
		
		private JTypeType proto;
		
		@Override
		public JTypeType providePrototype(){
			if(proto == null){
				proto = new JTypeType();
			}
			return proto;
		}
		
		@Override
		public void implementItself(JClassTypeBuilder builder, TypeFarm farm){
			//Parent
			builder.setParent(farm.getStub(BuiltinTypes.OBJECT));

			//Disallow inheritance from this class in script
			builder.setFinal(true);
			
			//Method
			//TODO: add methods in future after the inheritance system is complete
			/*
			 * equalsTo
			 * toString
			 * getType
			 */
		}
		
		@Override
		public void boostrapItself(JClassTypeBuilder builder){
			if(JTypeType.INSTANCE == null){
				JTypeType.INSTANCE = (JTypeType) builder.build(true);
			}
		}
		
		@Override
		public String getTypeName() {
			return "Type";
		}
	
		@Override
		public boolean initiateArrayType() {
			return false;
		}
	}
}
