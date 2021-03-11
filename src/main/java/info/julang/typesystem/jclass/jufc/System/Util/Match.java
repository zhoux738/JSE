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
 * The native implementation of <font color="green">System.Util.Match</font>.
 * <p/>
 * This implementation is backed by {@link java.util.regex.Matcher}.
 *  
 * @author Ming Zhou
 */
package info.julang.typesystem.jclass.jufc.System.Util;

import info.julang.execution.Argument;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.execution.threading.ThreadRuntimeHelper;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.InstanceNativeExecutor;
import info.julang.interpretation.internal.FuncCallExecutor;
import info.julang.memory.value.ArrayIndexOutOfRangeException;
import info.julang.memory.value.FuncValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassType;

import java.util.regex.Matcher;

public class Match {

	public final static String FullTypeName = "System.Util.Match";
	
	//------------ IRegisteredMethodProvider -------------//

	public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FullTypeName){
						
		@Override
		protected void implementProvider(SimpleHostedMethodProvider provider) {
			provider
				.add("getText", new GetTextExecutor())
				.add("getGroup", new GetGroupExecutor())
				.add("next", new NextExecutor());
		}
		
	};
	
	//----------------- native executors -----------------//
	
	protected static class GetTextExecutor extends InstanceNativeExecutor<Match> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, Match reg, Argument[] args) throws Exception {
			String str = reg.getText();
			return str != null ? TempValueFactory.createTempStringValue(str) : TempValueFactory.createTempNullRefValue();
		}
		
	}
	
	protected static class GetGroupExecutor extends InstanceNativeExecutor<Match> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, Match reg, Argument[] args) throws Exception {
			int index = getInt(args, 0);
			String str = reg.getGroup(index);
			return str != null ? TempValueFactory.createTempStringValue(str) : TempValueFactory.createTempNullRefValue();
		}
		
	}

	protected static class NextExecutor extends InstanceNativeExecutor<Match> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, Match reg, Argument[] args) throws Exception {
			ObjectValue ov = this.getObject(args, 0);
			boolean res = reg.next(rt, ov);
			return TempValueFactory.createTempBoolValue(res);
		}
		
	}
	
	//-------------------------- implementation at native end -------------------------//

	private Matcher matcher;

	void setMatcher(Matcher m) {
		this.matcher = m;
	}
	
	public String getText() {
		try {
			return matcher.group();
		} catch (IllegalStateException e) {
			return null;
		}
	}

	public String getGroup(int index) {
		try {
			return matcher.group(index);
		} catch (IllegalStateException e) {
			return null;
		} catch (IndexOutOfBoundsException  e) {
			throw new ArrayIndexOutOfRangeException(index, matcher.groupCount());
		}
	}

	public boolean next(ThreadRuntime rt, ObjectValue ov) {
		boolean res = matcher.find();
		
		FuncCallExecutor exec = new FuncCallExecutor(rt);
		JClassType sysUtilMatchTyp = (JClassType)ThreadRuntimeHelper.loadSystemType(rt, Match.FullTypeName);
		JClassMethodMember sysUtilMatchUpdate = (JClassMethodMember)sysUtilMatchTyp.getInstanceMemberByName("update");
		
		Argument[] args = getArguments(ov, matcher, res, !res);
		
		exec.invokeFunction(FuncValue.DUMMY, sysUtilMatchUpdate.getMethodType(), "update", args);
		return res;
	}
	
	static Argument[] getArguments(ObjectValue thisValue, Matcher m, boolean matched, boolean complete){
		int offset = thisValue == null ? 0 : 1;
		Argument[] args = new Argument[offset + 5];
		if (offset == 1) { // has 'this'
			args[0] = Argument.CreateThisArgument(thisValue);
		}
		
		args[offset + 0] = new Argument("matched", TempValueFactory.createTempBoolValue(matched));
		args[offset + 1] = new Argument("start", TempValueFactory.createTempIntValue(matched ? m.start() : -1));
		args[offset + 2] = new Argument("end", TempValueFactory.createTempIntValue(matched ? m.end() : -1));
		args[offset + 3] = new Argument("groups", TempValueFactory.createTempIntValue(matched ? m.groupCount() : 0));
		args[offset + 4] = new Argument("complete", TempValueFactory.createTempBoolValue(complete));
		
		return args;
	}
}
