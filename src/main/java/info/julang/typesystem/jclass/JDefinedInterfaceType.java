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
 * An interface type that can be built up gradually.
 * 
 * @author Ming Zhou
 */
public class JDefinedInterfaceType extends JInterfaceType implements IDefinedType {

	protected JInterfaceTypeBuilder builder;
	
	protected JDefinedInterfaceType(String name){
		this.builder = new JInterfaceTypeBuilder(name, this);
	}
	
	public JInterfaceTypeBuilder getBuilder(){
		return builder;
	}
	
	/**
	 * Start defining a new interface, with only name known.
	 * <p>
	 * The caller must use the associated builder (call {@link getBuilder()} to get it) 
	 * to populate interface member and attributes.
	 * 
	 * @param name
	 * @return
	 */
	public static JDefinedInterfaceType startNewInterface(String name){
		return new JDefinedInterfaceType(name);
	}
	
}
