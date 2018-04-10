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

package info.julang.typesystem.loading;

import info.julang.interpretation.BadSyntaxException;
import info.julang.interpretation.errorhandling.IHasLocationInfo;
import info.julang.interpretation.syntax.ClassSubtype;

/**
 * Exception thrown when encountering an user error in defining a compound type.
 * 
 * @author Ming Zhou
 */
public class IllegalClassDefinitionException extends BadSyntaxException {

	private static final long serialVersionUID = -8266939377109344761L;
	
	public IllegalClassDefinitionException(LoadingContext context, String msg, IHasLocationInfo linfo) {
		super(makeMsg(
			context.getClassDeclInfo().getFQName().toString(), 
			context.getClassDeclInfo().getSubtype(), 
			msg), linfo);
	}
	
	public IllegalClassDefinitionException(String fqName, ClassSubtype subtyp, String msg, IHasLocationInfo linfo) {
		super(makeMsg(fqName, subtyp, msg), linfo);
	}

	private static String makeMsg(String fqName, ClassSubtype subtyp, String msg) {
		StringBuilder sb = new StringBuilder();
		sb.append("The definition of " + (subtyp == ClassSubtype.INTERFACE ? "interface" : "class") + " \"");
		sb.append(fqName);
		sb.append("\" is incorrect: ");
		sb.append(msg);
		return sb.toString();
	}

}
