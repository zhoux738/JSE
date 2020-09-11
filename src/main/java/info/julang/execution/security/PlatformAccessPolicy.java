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

/**
 * The most common engine policy, used to restrict the platform access, such as the entirety 
 * or part of File System.
 * <p>
 * This class is expected to be initiated with two sets of operation names which are mutually 
 * exclusive. If by any chance they are not, the denial trumps admission.
 * 
 * @author Ming Zhou
 */
public class PlatformAccessPolicy implements IEnginePolicy<String> {

	private String[] allowOps;
	private String[] denyOps;
	private String category;
	
	public PlatformAccessPolicy(String category, String[] allowOps, String[] denyOps) {
		this.allowOps = allowOps;
		this.category = category;
		this.denyOps = denyOps;
	}
	
	@Override
	public String getName() {
		return category;
	}
	
	/**
	 * Get the official name preserving the documented case.
	 */
	public String getOfficialName() {
		String[] secs = category.split("\\.");
		StringBuilder sb = new StringBuilder();
		for (String sec : secs) {
			sb.append(Character.toUpperCase(sec.charAt(0)));
			sb.append(sec.substring(1));
			sb.append('.');
		}
		
		return sb.substring(0, sb.length() - 1);
	}

	@Override
	public CheckResult check(String action) {
		if (denyOps != null) {
			for(String op : denyOps) {
				if (WILDCARD.equals(op) || op.equals(action)) {
					return CheckResult.deny(null);
				}
			}
		}
		
		if (allowOps != null) {
			for(String op : allowOps) {
				if (WILDCARD.equals(op) || op.equals(action)) {
					return CheckResult.allow();
				}
			}
		}
		
		return CheckResult.defer();
	}
}
