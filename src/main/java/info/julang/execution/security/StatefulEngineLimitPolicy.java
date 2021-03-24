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
 * Stateful EngineLimitPolicy where the checking is based off a tracked value against the limit.
 * Always checked with a delta. If allowed, the delta will be accumulated into the tracked value. 
 * 
 * @author Ming Zhou
 */
public class StatefulEngineLimitPolicy extends EngineLimitPolicy {

	private int currVal;
	
	public StatefulEngineLimitPolicy(EngineLimit lim, int value) {
		super(lim, value);
	}

	@Override
	public CheckResult check(Integer delta) {
		int newVal = currVal + delta;
		int limVal = getValue();
		boolean violated = lim.isMaxOrMin() ? newVal > limVal : newVal < limVal;
		
		if (violated) {
			return CheckResult.deny(String.format(lim.getMessageTemplate(), limVal));
		}
		
		currVal = newVal;
		return CheckResult.allow();
	}
	
	public void reset() {
		currVal = 0;
	}
}
