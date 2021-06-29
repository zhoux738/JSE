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

/**
 * The native implementation of <code style="color:green">System.Util.Regex</code>.
 * <p>
 * This implementation is backed by {@link java.util.regex.Pattern}.
 *  
 * @author Ming Zhou
 */
package info.julang.typesystem.jclass.jufc.System.Util;

import info.julang.execution.Argument;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.execution.threading.ThreadRuntimeHelper;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.CtorNativeExecutor;
import info.julang.hosting.execution.InstanceNativeExecutor;
import info.julang.interpretation.internal.NewObjExecutor;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.memory.value.VoidValue;
import info.julang.parser.ANTLRHelper;
import info.julang.typesystem.jclass.JClassConstructorMember;
import info.julang.typesystem.jclass.JClassType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JRegex {

	public final static String FullTypeName = "System.Util.Regex";
	
	//------------ IRegisteredMethodProvider -------------//

	public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FullTypeName){
			
		@Override
		protected void implementProvider(SimpleHostedMethodProvider provider) {
			provider
				.add("ctor", new InitExecutor())
				.add("matchNext", new MatchNextExecutor())
				.add("matchAll", new MatchAllExecutor());
		}
		
	};
	
	//----------------- native executors -----------------//
	
	private static class InitExecutor extends CtorNativeExecutor<JRegex> {

		@Override
		protected void initialize(ThreadRuntime rt, HostedValue hvalue, JRegex reg, Argument[] args) throws Exception {
			String pattern = getString(args, 0);
			reg.init(pattern);
			setOverwrittenReturnValue(VoidValue.DEFAULT);
		}
		
	}
	
	protected static class MatchAllExecutor extends InstanceNativeExecutor<JRegex> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JRegex reg, Argument[] args) throws Exception {
			String input = getString(args, 0);
			ObjectValue ov = reg.matchAll(rt, input);
			return TempValueFactory.createTempRefValue(ov);
		}
		
	}
	
	protected static class MatchNextExecutor extends InstanceNativeExecutor<JRegex> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JRegex reg, Argument[] args) throws Exception {
			String input = getString(args, 0);
			ObjectValue ov = reg.matchNext(rt, input);
			return TempValueFactory.createTempRefValue(ov);
		}
		
	}

	//-------------------------- implementation at native end -------------------------//
	
	private Pattern pattern;
	
	public void init(String input) {
		String output = RegexSanitizer.sanitize(input);
		this.pattern = Pattern.compile(output);
	}

	public ObjectValue matchAll(ThreadRuntime rt, String input) {		
		// Match the input
		Matcher m = pattern.matcher(input);
		Argument[] args = Match.getArguments(null, m, m.matches(), true);
		
		// Create a Match object with match result
		ObjectValue val = createMatchObject(rt, m, args);
		
		return val;
	}

	public ObjectValue matchNext(ThreadRuntime rt, String input) {
		// Match the input
		Matcher m = pattern.matcher(input);
		Argument[] args = Match.getArguments(null, m, m.find(), false);
		
		// Create a Match object with match result
		ObjectValue val = createMatchObject(rt, m, args);
		
		return val;
	}
	
	private ObjectValue createMatchObject(ThreadRuntime rt, Matcher m, Argument[] args){
		JClassType sysUtilMatchTyp = (JClassType)ThreadRuntimeHelper.loadSystemType(rt, Match.FullTypeName);
		JClassConstructorMember sysUtilMatchTypCtor = sysUtilMatchTyp.getClassConstructors()[0];
		NewObjExecutor noe = new NewObjExecutor(rt);
		ObjectValue val = noe.newObjectInternal(sysUtilMatchTyp, sysUtilMatchTypCtor, args);
		HostedValue hv = (HostedValue)val;
		Match match = new Match();
		match.setMatcher(m);
		hv.setHostedObject(match);
		
		return val;
	}
	
	/**
	 * From the regex literal (enclosed by '/' and '/'), create a System.Util.Regex object.
	 */
	public static ObjectValue createRegexObjectFromRegexLiteral(String literal, ThreadRuntime rt){
		// Sanitize the input
		String pattern = ANTLRHelper.convertRegexLiteral(literal);
		pattern = RegexSanitizer.sanitize(pattern);
		
		// Prepare to call Regex's ctor
		JClassType sysUtilRegexTyp = (JClassType)ThreadRuntimeHelper.loadSystemType(rt, JRegex.FullTypeName);
		JClassConstructorMember sysUtilRegexTypCtor = sysUtilRegexTyp.getClassConstructors()[0];
		NewObjExecutor noe = new NewObjExecutor(rt);
		
		// Call Regex's ctor with the sanitized pattern
		Argument[] args = new Argument[] { new Argument("pattern", TempValueFactory.createTempStringValue(pattern)) };
		ObjectValue val = noe.newObjectInternal(sysUtilRegexTyp, sysUtilRegexTypCtor, args);
		
		return val;
	}
}
