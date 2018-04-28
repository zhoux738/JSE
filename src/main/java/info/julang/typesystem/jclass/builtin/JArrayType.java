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

import info.julang.execution.symboltable.ITypeTable;
import info.julang.external.exceptions.JSEError;
import info.julang.typesystem.BuiltinTypes;
import info.julang.typesystem.JType;
import info.julang.typesystem.conversion.Convertibility;
import info.julang.typesystem.jclass.BuiltinTypeBootstrapper.TypeFarm;
import info.julang.typesystem.jclass.JClassType;

/**
 * The Array type is for both single and multi-dimensional arrays defined in script
 * <p/>
 * This class doesn't provide public constructor. To make an array type, call static 
 * method {@link #createJArrayType(JType)} or {@link #createJArrayType(JType, int)}. 
 * This is because array type declaration is essentially implicit declaration of a 
 * new class type that inherits JArrayType, and we must prevent duplicated definition.
 * <p/>
 * This class also provides a set of static APIs to facilitate array-type-related 
 * operations, such as {@link #getArrayDimension(JArrayType) getting the dimension of 
 * an array type} or {@link #isArrayType(JType)} determining if a type is array type. 
 * 
 * @author Ming Zhou
 */
public class JArrayType extends JArrayBaseType {
	
	private JType elementType;
	private boolean covariant;
	
	public JType getElementType() {
		return elementType;
	}
	
	//------------- Public static factory methods for creating array types -------------//

	/**
	 * Create a new array type with specified element type. This has the side effect of caching
	 * the new array type into type table.
	 * 
	 * @param tt
	 * @param elementType
	 * @param covariant if true, an array type T1 is considered convertible to this one as long as 
	 * T1's element type is a subclass of this element type. Note there is no support for such type
	 * conversion elsewhere in the code, and this is only used by hosting API to allow late type
	 * binding with the platform. 
	 * @return
	 */
	public static JArrayType createJArrayType(ITypeTable tt, JType elementType, boolean covariant){
		return createJArrayType0(tt, elementType, null, covariant);
	}
	
	/**
	 * Create a new multi-dimensional array type with specified element type. If the type has been created previously,
	 * return the existing one.
	 * 
	 * @param elementType
	 * @param dimension must be greater than 0. If dimension == 1, this is same to call 
	 * {@link #createJArrayType(JType)}. 
	 * @return
	 */
	public static JArrayType createJArrayType(ITypeTable tt, JType elementType, int dimension){
		if(dimension <= 0){
			throw new JSEError("Cannot create an array type with 0 dimension.", JArrayType.class);
		}
		
		while(dimension > 0){
			elementType = createJArrayType(tt, elementType, false);
			dimension--;
		}
		
		return (JArrayType) elementType;
	}
	
	//------------- Public static utility methods for array-type-related operations -------------//

	/**
	 * A utility method to determine if a type is array.
	 * @param type
	 * @return true if the type is an array.
	 */
	public static boolean isArrayType(JType type) {
		if(!type.isObject()){
			return false;
		}
		
		JClassType ctype = (JClassType) type;
		
		return ctype.getParent() == JArrayBaseType.getInstance();
	}
	
	//------------- Special internal-only methods -------------//
	
	/**
	 * Create a new array type with specified element type. Used only during bootstrapping.
	 * <p/>
	 * This is needed because during bootstrapping the singleton for array base type is not ready yet,
	 * and we can only reference it from the type farm.
	 * @return
	 */
	static JArrayType createJArrayTypeOnBootstrapping(ITypeTable tt, JType elementType, TypeFarm farm){
		return createJArrayType0(tt, elementType, farm.getStub(BuiltinTypes.ARRAY), false);
	}
	
	//------------- private methods -------------//
	
	private static JArrayType createJArrayType0(ITypeTable tt, JType elementType, JClassType parentType, boolean covariant){
		JArrayType jat = tt.getArrayType(elementType);
		if (jat == null){
			jat = new JArrayType(elementType, parentType);
			tt.addArrayType(jat);
			jat.covariant = covariant;
		}
		
		return jat;
	}
	
	// UGLY - this is public only because we need call this from bootstrapper.
	public JArrayType(JType elementType, JClassType parentType) {
		super("[" + elementType.getName() + "]", parentType);
		this.elementType = elementType;
	}
	
	@Override
	public Convertibility getConvertibilityTo(JType another){
		if (isArrayType(another)){
			JArrayType jat = (JArrayType) another;
			if (jat.covariant) {
				JType et1 = this.getElementType();
				JType et2 = jat.getElementType();
				return et1.getConvertibilityTo(et2);
			}
		}
		
		return super.getConvertibilityTo(another);
	}
}
