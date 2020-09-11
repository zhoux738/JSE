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

import java.util.HashMap;
import java.util.Map;

import info.julang.external.exceptions.JSEError;

/**
 * Enforces the configured policies. Mainly for restricting platform access via using {@link PlatformAccessPolicy}.
 * But since the policy can be customized through {@link IEnginePolicy}, this class may also be used for enforcing
 * other aspects of the runtime, such as limiting the max number of threads in use.
 * 
 * @author Ming Zhou
 */
public class EnginePolicyEnforcer {
	
	private Map<String, IEnginePolicy<?>> map;
	
	private boolean defaultToPass;
	
	public EnginePolicyEnforcer() { 
		defaultToPass = true;
	}
	
	public void setDefault(boolean pass) {
		defaultToPass = pass;
	}

	public void addPolicy(IEnginePolicy<?> pol) {
		if (map == null) {
			map = new HashMap<String, IEnginePolicy<?>>();
		}
		
		map.put(pol.getName(), pol);
	}
	
	/**
	 * Check whether a system limit is (to be) violated.
	 * 
	 * @param limit the limit category, associated with a defined max/min value.
	 * @param value the current value.
	 * @throws RuntimeQuotaException if the limit is violated.
	 */
	public void checkLimit(EngineLimit limit, int value) {
		IEnginePolicy<?> pol = null;
		if (map != null) { 
			pol = map.get(limit.getName());
		}
		
		if (pol != null) {
			if (pol instanceof EngineLimitPolicy) {
				EngineLimitPolicy polt = (EngineLimitPolicy)pol;
				CheckResult result = polt.check(value);
				if (result.getKind() == CheckResultKind.DENY) {
					throw new RuntimeQuotaException(result.getMessage());
				}
			} else {
				throw new JSEError(
					"A policy matching the given EngineLimit's name is not of type EngineLimitPolicy.", EnginePolicyEnforcer.class);
			}
		}
	}
	
	/**
	 * Get the absolute value of a system limit.
	 * 
	 * @param limit
	 * @return {@link EngineLimit#UNDEFINED} if the limit is not set.
	 */
	public int getLimit(EngineLimit limit) {
		IEnginePolicy<?> pol = null;
		if (map != null) { 
			pol = map.get(limit.getName());
		}
		
		if (pol != null) {
			if (pol instanceof EngineLimitPolicy) {
				EngineLimitPolicy polt = (EngineLimitPolicy)pol;
				return polt.getValue();
			} else {
				throw new JSEError(
					"A policy matching the given EngineLimit's name is not of type EngineLimitPolicy.", EnginePolicyEnforcer.class);
			}
		}
		
		return EngineLimit.UNDEFINED;
	}
	
	/**
	 * Check whether a platform access restriction is violated.
	 * 
	 * @param name the access category
	 * @param action the operation defined under this category
	 */
	public void checkAccess(String name, String action){
		IEnginePolicy<?> pol = null;
		if (map != null) { 
			pol = map.get(name.toLowerCase());
		}

		if (pol == null) {
			if (defaultToPass) {
				return;
			} else {
				throw new UnderprivilegeException(name, action);
			}
		}
		
		if (pol instanceof PlatformAccessPolicy) {
			PlatformAccessPolicy polt = (PlatformAccessPolicy)pol;
			
			CheckResult result = polt.check(action.toLowerCase());
			switch(result.getKind()) {
			case ALLOW: return;
			case DENY:
				throw new UnderprivilegeException(polt.getOfficialName(), action, result.getMessage());
			case UNDEFINED:
				if (defaultToPass) {
					return;
				} else {
					throw new UnderprivilegeException(polt.getOfficialName(), action);
				}
			}
		} else {
			throw new JSEError(
				"A policy matching the given category/operation is not of type PlatformAccessPolicy.", EnginePolicyEnforcer.class);
		}	
	}
}
