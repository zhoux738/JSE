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

package info.julang.external.exceptions;

public class ExternalBindingException extends JSEException {

	private static final long serialVersionUID = -2086553869042308077L;

	public static enum Type {
		NOT_EXIST,
		BAD_TYPE,
		ILLEGAL_TYPE,
		CONFLICT
	}
	
	public static ExternalBindingException create(String name, Type type){
		switch(type){
		case NOT_EXIST:
			return new ExternalBindingException("Binding \"" + name + "\" doesn't exist.");
		case BAD_TYPE:
			return new ExternalBindingException("Binding \"" + name + "\" is not of the expected type.");
		case ILLEGAL_TYPE:
			return new ExternalBindingException("Binding \"" + name + "\" is not allowed due to its native type.");
		case CONFLICT:
			return new ExternalBindingException("Binding to \"" + name + "\" loaded by different class loaders is not supported.");
		default:
			throw new JSEError("Unsupported ExternalBindingException type: " + type.name());
		}
	}
	
	private ExternalBindingException(String msg) {
		super(msg);
	}

}
