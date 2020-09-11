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

package info.julang.hosting.execution;

import info.julang.execution.threading.ThreadRuntime;

public interface IPolicyChecker {

	void checkPolicy(ThreadRuntime rt);
	
}

class PlatformAccessPolicyChecker implements IPolicyChecker {
	
	private String category;
	private String operation;
	
	PlatformAccessPolicyChecker(String category, String operation) {
		this.category = category;
		this.operation = operation;
	}

	@Override
	public void checkPolicy(ThreadRuntime rt) {
		rt.getModuleManager().getEnginePolicyEnforcer().checkAccess(category, operation);
	}
}
