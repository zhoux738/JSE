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
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.BuiltinTypeBootstrapper.TypeFarm;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JClassTypeBuilder;
import info.julang.typesystem.jclass.TypeBootstrapper;

/**
 * A very special built-in type that's only used to serve as a type placeholder for the data object 
 * containing the static members for a type. 
 * <p>
 * When a type is loaded into the runtime, an instance of static data object is created and stored
 * on engine's heap area. This object contains all the static members of the type, as well as the 
 * type metadata for that type.
 * 
 * @author Ming Zhou
 */
public class JTypeStaticDataType extends JClassType {

	private static JTypeStaticDataType INSTANCE;
	
	private JTypeStaticDataType(){
		
	}

	public static JTypeStaticDataType getInstance() {
		return INSTANCE;
	}

	@Override
	public boolean isBuiltIn() {
		return true;
	}
	
	public static class BoostrapingBuilder implements TypeBootstrapper {
		
		private JTypeStaticDataType proto;
		
		@Override
		public JTypeStaticDataType providePrototype(){
			if(proto == null){
				proto = new JTypeStaticDataType();
			}
			return proto;
		}
		
		@Override
		public void implementItself(JClassTypeBuilder builder, TypeFarm farm){
			//Parent
			builder.setParent(farm.getStub(BuiltinTypes.OBJECT));

			//Make sure this type is not usable.
			builder.setFinal(true);
			builder.setAccessibility(Accessibility.PRIVATE);
		}
		
		@Override
		public void boostrapItself(JClassTypeBuilder builder){
			if(JTypeStaticDataType.INSTANCE == null){
				JTypeStaticDataType.INSTANCE = (JTypeStaticDataType) builder.build(true);
			}
		}
		
		@Override
		public String getTypeName() {
			return "TypeStaticData";
		}
	
		@Override
		public boolean initiateArrayType() {
			return false;
		}
	}
}
