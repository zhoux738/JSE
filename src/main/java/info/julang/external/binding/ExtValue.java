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

package info.julang.external.binding;

import info.julang.external.interfaces.IExtValue;
import info.julang.external.interfaces.JValueKind;

public abstract class ExtValue implements IExtValue {

	public boolean isBasic() {
		return true;
	}
	
	public boolean isConst() {
		return false;
	}
	
	public boolean isNull(){
		return false;
	}
	
	public static abstract class ExtObjValue extends ExtValue implements IObjectVal {
		
		@Override
		public boolean isBasic() {
			return false;
		}

		@Override
		public JValueKind getKind() {
			return JValueKind.OBJECT;
		}
	}
	
	public static class ExtIntValue extends ExtValue {
		
		private int value;
		
		public ExtIntValue(int value){
			this.value = value;
		}
		
		public int getValue(){
			return value;
		}

		@Override
		public JValueKind getKind() {
			return JValueKind.INTEGER;
		}
	}
	
	public static class ExtBoolValue extends ExtValue {
		
		private boolean value;
		
		public ExtBoolValue(boolean value){
			this.value = value;
		}
		
		public boolean getValue(){
			return value;
		}

		@Override
		public JValueKind getKind() {
			return JValueKind.BOOLEAN;
		}
	}
	
	public static class ExtFloatValue extends ExtValue {
		
		private float value;
		
		public ExtFloatValue(float value){
			this.value = value;
		}
		
		public float getValue(){
			return value;
		}

		@Override
		public JValueKind getKind() {
			return JValueKind.FLOAT;
		}
	}
	
	public static class ExtCharValue extends ExtValue {
		
		private char value;
		
		public ExtCharValue(char value){
			this.value = value;
		}
		
		public char getValue(){
			return value;
		}

		@Override
		public JValueKind getKind() {
			return JValueKind.CHAR;
		}
	}
	
	public static class ExtStringValue extends ExtObjValue {
		
		private String value;
		
		public ExtStringValue(String value){
			this.value = value;
		}
		
		public String getValue(){
			return value;
		}

		@Override
		public JValueKind getBuiltInValueKind() {
			return JValueKind.STRING;
		}
	}

	public static class ExtHostedValue extends ExtObjValue {
		
		private Object value;
		
		public ExtHostedValue(Object value){
			this.value = value;
		}
		
		public Object getValue(){
			return value;
		}

		@Override
		public JValueKind getBuiltInValueKind() {
			return JValueKind.HOSTED;
		}
	}
}
