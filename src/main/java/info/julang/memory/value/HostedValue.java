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

package info.julang.memory.value;

import info.julang.external.interfaces.IExtValue.IHostedVal;
import info.julang.external.interfaces.JValueKind;
import info.julang.memory.MemoryArea;
import info.julang.typesystem.JType;

/**
 * A value holding a reference to hosted (JVM) object.
 * 
 * @author Ming Zhou
 */
public class HostedValue extends ObjectValue implements IHostedVal {
	
	protected Object obj;

	public HostedValue(MemoryArea memory, JType type) {
		super(memory, type, false);
	}

//	@Override
//	protected void initialize(JType type, MemoryArea memory) {
//		super.initialize(type, memory);
//	}
	
	@Override
	public JValueKind getBuiltInValueKind(){
		return JValueKind.HOSTED;
	}
	
	public Object getHostedObject(){
		return obj;
	}
	
	public void setHostedObject(Object obj){
		this.obj = obj;
	}
}
