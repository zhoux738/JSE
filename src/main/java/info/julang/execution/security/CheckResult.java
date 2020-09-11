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

package info.julang.execution.security;

public class CheckResult {

	private CheckResultKind kind;
	private String errorMsg;
	
	private static final CheckResult s_allow = new CheckResult(CheckResultKind.ALLOW, "");
	private static final CheckResult s_undefined = new CheckResult(CheckResultKind.UNDEFINED, "");
	
	private CheckResult(CheckResultKind kind, String errorMsg) {
		this.kind = kind;
		this.errorMsg = errorMsg;
	}
	
	public static CheckResult deny(String errorMsg) {
		return new CheckResult(CheckResultKind.DENY, errorMsg);
	}
	
	public static CheckResult allow() {
		return s_allow;
	}
	
	public static CheckResult defer() {
		return s_undefined;
	}
	
	public String getMessage() {
		return errorMsg;
	}
	
	public CheckResultKind getKind() {
		return kind;
	}
}
