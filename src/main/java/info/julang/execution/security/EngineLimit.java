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

/**
 * The various limits enforced by the engine during runtime.
 * Exceeding these limits will cause <code style="color:green">System.UnderprivilegeExcetion</code>.
 * <p>
 * This class can be partially used by the JSE Java programmer. In particular, one may call 
 * {@link #getPublicName} to get a name for the limit that is recognized by the engine API such as 
 * {@link info.julang.external.JulianScriptEngine#setLimit(String, int) setLimit(String, int)}.
 * 
 * @author Ming Zhou
 */
public enum EngineLimit {
	
	MAX_THREADS(true, "About to exceed max threads allowed (%d).", false, 1),
	MAX_USED_MEMORY_IN_BYTE(true, "About to exceed max memory allowed (%d bytes).", true, 512),
	
	;
	
	EngineLimit(boolean maxOrMin, String msgTmpl, boolean stateful, int minValue){
		this.maxOrMin = maxOrMin;
		this.msgTmpl = msgTmpl;
		this.stateful = stateful;
		this.minValue = minValue;
	}
	
	public static final int UNDEFINED = Integer.MIN_VALUE;
	
	private boolean maxOrMin;
	private String msgTmpl;
	private boolean stateful;
	private int minValue;
	
	private static final String s_prefix = "System.Limit.";
	private static final String s_prefix_upper = s_prefix.toUpperCase().replace('.', '_');
	private static Map<String, EngineLimit> s_lmap = null;
	
	public String getName() {
		return s_prefix + this.name();
	}
	
	public boolean isMaxOrMin() {
		return maxOrMin;
	}
	
	public boolean isStateful() {
		return stateful;
	}
	
	public String getMessageTemplate() {
		return msgTmpl;
	}
	
	/**
	 * Get the name that can be used by when invoking JSE through Java API, 
	 * for both {@link info.julang.external.JulianScriptEngine JulianScriptEngine} 
	 * and {@link info.julang.jsr223.JulianScriptingEngine JSR-223 implementation}.
	 * 
	 * @return The public name for this limit.
	 */
	public String getPublicName() {
		return this.name().toLowerCase().replace('_', '.');
	}

	public int sanitize(int value) {
		return Math.max(minValue, value);
	}
	
	/**
	 * Can recognize the following formats:<pre>
	 * [System.Limit.]MAX[_|.]THREADS
	 * [System.Limit.]max[_|.]threads</pre>
	 * @param str the string notation of the limit
	 * @return null if the string is not recognized as a limit.
	 */
	public static EngineLimit fromString(String str) {
		String fmtStr = str.trim().toUpperCase().replace('.', '_');
		if (fmtStr.startsWith(s_prefix_upper)) {
			fmtStr = fmtStr.substring(s_prefix_upper.length());
		}
		
		fmtStr = s_prefix + fmtStr;
		
		if (s_lmap == null) {
			initAllLimits();
		}
		
		return s_lmap.get(fmtStr);
	}

	private synchronized static void initAllLimits() {
		if (s_lmap == null) {
			s_lmap = new HashMap<String, EngineLimit>();
			s_lmap.put(MAX_THREADS.getName(), MAX_THREADS);
			s_lmap.put(MAX_USED_MEMORY_IN_BYTE.getName(), MAX_USED_MEMORY_IN_BYTE);
		}
	}
}
