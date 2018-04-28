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

package info.julang.typesystem.jclass.jufc.System;

import info.julang.execution.Argument;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.JSEError;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.StaticNativeExecutor;
import info.julang.interpretation.internal.FuncCallExecutor;
import info.julang.memory.value.BoolValue;
import info.julang.memory.value.ByteValue;
import info.julang.memory.value.CharValue;
import info.julang.memory.value.EnumValue;
import info.julang.memory.value.FloatValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.StringValue;
import info.julang.memory.value.UntypedValue;
import info.julang.memory.value.VoidValue;
import info.julang.typesystem.jclass.ClassMemberLoaded;
import info.julang.typesystem.jclass.JClassMember;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.MemberType;
import info.julang.typesystem.jclass.builtin.JObjectType;
import info.julang.typesystem.jclass.jufc.System.IO.JSEIOException;
import info.julang.util.OneOrMoreList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * The native implementation of <font color="green">System.Console</font>.
 * 
 * @author Ming Zhou
 */
public class JConsole {
	
	public static final String FQCLASSNAME = "System.Console";
	
	//----------------- IRegisteredMethodProvider -----------------//
	
	public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FQCLASSNAME){

		@Override
		protected void implementProvider(SimpleHostedMethodProvider provider) {
			provider
				.add("print", new PrintExecutor())
				.add("println", new PrintLineExecutor())
				// .add("read", new ReadExecutor())
				.add("readln", new ReadLineExecutor());
		}
		
	};
	
	//----------------- native executors -----------------//
	
	private static class PrintLineExecutor extends StaticNativeExecutor<JConsole> {

		@Override
		protected JValue apply(ThreadRuntime rt, Argument[] args) throws Exception {
			JValue val = args[0].getValue();
			printValue(rt, val);
			println();
			return VoidValue.DEFAULT;
		}
	}
	
	private static class ReadLineExecutor extends StaticNativeExecutor<JConsole> {

		@Override
		protected JValue apply(ThreadRuntime rt, Argument[] args) throws Exception {
			return readLine(rt);
		}
	}
	
	private static class PrintExecutor extends StaticNativeExecutor<JConsole> {

		@Override
		protected JValue apply(ThreadRuntime rt, Argument[] args) throws Exception {
			JValue val = args[0].getValue();
			printValue(rt, val);
			return VoidValue.DEFAULT;
		}
	}
	
//	private static class ReadExecutor extends StaticNativeExecutor<JConsole> {
//
//		@Override
//		protected JValue apply(ThreadRuntime rt, Argument[] args) throws Exception {
//			return readChar(rt);
//		}
//	}

	//----------------- implementation at native end -----------------//
	
	private static void printObject(ThreadRuntime rt, ObjectValue ov){
		OneOrMoreList<ClassMemberLoaded> cmls = ov.getClassType().getMembers(false).getLoadedMemberByName(JObjectType.MethodNames.toString.name());
		JClassMethodMember member = null;
		if (cmls.hasOnlyOne()){
			member = (JClassMethodMember)cmls.getFirst().getClassMember();
		} else {
			for(ClassMemberLoaded cml : cmls){
				JClassMember cm = cml.getClassMember();
				if (cm.getMemberType() == MemberType.METHOD){
					JClassMethodMember jcmm = (JClassMethodMember)cm;
					if (jcmm.getMethodType().getParams().length == 1){
						member = jcmm;
						break;
					}
				}
			}		
		}
		
		if (member == null){
			throw new JSEError("Didn't see a toString() method.", JConsole.class);		
		}

		FuncCallExecutor exec = new FuncCallExecutor(rt);
		JValue res = exec.invokeFunction(member.getMethodType(), JObjectType.MethodNames.toString.name(), Argument.CreateThisOnlyArguments(ov));
		res = res.deref();
		if(res == RefValue.NULL){
			System.out.print("null");
		} else if(res instanceof StringValue){
			StringValue sv = (StringValue)res;
			System.out.print(sv.getStringValue());
		} else {
			// If toString() didn't return a string or null, it should have thrown error.
			throw new JSEError("Expected a string, but saw null.", JConsole.class);
		}
	}
	
	private static void printString(String str){
		System.out.print(str);
	}
	
	private static void printInt(int i){
		System.out.print(i);
	}
	
	private static void printChar(char c){
		System.out.print(c);
	}
	
//	private static void printDouble(double d){
//		System.out.print(d);
//	}
	
	private static void printBool(boolean b){
		System.out.print(b);
	}
	
	private static void println(){
		System.out.println();
	}

// This doesn't really work. The terminal by default works in line-mode so it will not react to a single key press. 
// So despite calling System.in.read() it will still block until a line feed is encountered. 
// To implement this we need switch the terminal into raw mode, which is an OS-dependent operation. So postpone this for now.
//
//	private static CharValue readChar(ThreadRuntime rt){
//		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//
//		try {
//			char c = (char)br.read();
//			return new CharValue(rt.getStackMemory().currentFrame(), c);
//		} catch (IOException e) {
//			throw new JSEIOException(e);
//        } finally {
//            if (br != null) {
//                try {
//                    br.close();
//                } catch (IOException e) {
//                    // Ignore close error
//                }
//            }
//        }
//	}
	
	private static StringValue readLine(ThreadRuntime rt){
		BufferedReader br = null;
        try {
        	br = new BufferedReader(new InputStreamReader(System.in));
            String s = br.readLine();
            return new StringValue(rt.getStackMemory().currentFrame(), s);
        } catch (IOException e) {
			throw new JSEIOException(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // Ignore close error
                }
            }
        }
	}
	
	private static void printValue(ThreadRuntime rt, JValue val){
		val = UntypedValue.unwrap(val);
		switch(val.getKind()){
		case BOOLEAN:
			JConsole.printBool(((BoolValue) val).getBoolValue());
			break;
		case BYTE:
			JConsole.printInt(((ByteValue) val).getByteValue());
			break;
		case CHAR:
			JConsole.printChar(((CharValue) val).getCharValue());
			break;	
		case FLOAT:
			JConsole.printString(((FloatValue) val).toString());
			break;			
		case INTEGER:
			JConsole.printInt(((IntValue) val).getIntValue());
			break;
		case REFERENCE:
			ObjectValue derefVal = RefValue.tryDereference(val);
			if(derefVal == RefValue.NULL){
				JConsole.printString(derefVal.toString());
			} else {
				printObject(rt, derefVal);
			}
			break;
		case OBJECT:
			ObjectValue ov = (ObjectValue) val;
			switch(ov.getBuiltInValueKind()){
			case STRING:
				JConsole.printString(((StringValue) ov).getStringValue());
				break;
			case ENUM:
				JConsole.printString(((EnumValue) ov).getLiteral());
				break;
			default:
				printObject(rt, ov);
				break;
			}
			break;
		default:
			throw new JSEError("Not supported value type to print.");
		}
	}
	
}
