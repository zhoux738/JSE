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

package info.julang.typesystem.jclass.jufc.System.Util;

import info.julang.execution.Argument;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.StaticNativeExecutor;
import info.julang.memory.value.ByteValue;
import info.julang.memory.value.FloatValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.typesystem.JArgumentException;

/**
 * The native implementation of <font color="green">System.Util.Math</font>.
 * <p/>
 * This implementation is backed by {@link java.lang.Math}.
 *  
 * @author Ming Zhou
 */
public class JMath {
	
	public final static String FullTypeName = "System.Util.Math";
	
	//------------ IRegisteredMethodProvider -------------//

	public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FullTypeName){

		@Override
		protected void implementProvider(SimpleHostedMethodProvider provider) {
			provider
				.add("log", new LogExecutor())
				.add("sin", new SinExecutor())
				.add("cos", new CosExecutor())
				.add("arcsin", new ArcSinExecutor())
				.add("arccos", new ArcCosExecutor())
				.add("power", new PowerExecutor());
		}
		
	};
	
	private static class PowerExecutor extends StaticNativeExecutor<JMath> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, Argument[] args) throws Exception {
			JValue val1 = args[0].getValue().deref();
			JValue val2 = args[1].getValue().deref();
			double d1 = toDouble(val1);
			double d2 = toDouble(val2);
			double r = Math.pow(d1, d2);
			return convert(r);
		}
		
		private double toDouble(JValue val){
			double d = 0;
			switch (val.getKind()){
			case BYTE:
				d = ((ByteValue)val).getByteValue();
				break;
			case FLOAT:
				d = ((FloatValue)val).getFloatValue();
				break;
			case INTEGER:
				d = ((IntValue)val).getIntValue();
				break;
			default:
				throw new JArgumentException("power(var, var)");
			}
			
			return d;
		}
	}
	
	private static abstract class DoubleToDoubleExecutor extends StaticNativeExecutor<JMath> {

		private String name;
		
		protected DoubleToDoubleExecutor(String name) {
			this.name = name;
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, Argument[] args) throws Exception {
			JValue val = args[0].getValue().deref();
			double d = 0;
			switch (val.getKind()){
			case BYTE:
				d = processByte(((ByteValue)val).getByteValue());
				break;
			case FLOAT:
				d = processFloat(((FloatValue)val).getFloatValue());
				break;
			case INTEGER:
				d = processInt(((IntValue)val).getIntValue());
				break;
			default:
				throw new JArgumentException(name);
			}
			
			return convert(d);
		}
		
		protected abstract double processByte(byte b);
		protected abstract double processInt(int b);
		protected abstract double processFloat(float b);
	}
	
	private static class LogExecutor extends DoubleToDoubleExecutor {
		protected LogExecutor() { super("log(var)"); }
		@Override
		protected double processByte(byte v) { return Math.log(v); }
		@Override
		protected double processInt(int v) { return Math.log(v); }
		@Override
		protected double processFloat(float v) { return Math.log(v); }
	}
	
	private static class SinExecutor extends DoubleToDoubleExecutor {
		protected SinExecutor() { super("sin(var)"); }
		@Override
		protected double processByte(byte v) { return Math.sin(v); }
		@Override
		protected double processInt(int v) { return Math.sin(v); }
		@Override
		protected double processFloat(float v) { return Math.sin(v); }
	}
	
	private static class CosExecutor extends DoubleToDoubleExecutor {
		protected CosExecutor() { super("cos(var)"); }
		@Override
		protected double processByte(byte v) { return Math.cos(v); }
		@Override
		protected double processInt(int v) { return Math.cos(v); }
		@Override
		protected double processFloat(float v) { return Math.cos(v); }
	}
	
	private static class ArcSinExecutor extends DoubleToDoubleExecutor {
		protected ArcSinExecutor() { super("arcsin(var)"); }
		@Override
		protected double processByte(byte v) { return Math.asin(v); }
		@Override
		protected double processInt(int v) { return Math.asin(v); }
		@Override
		protected double processFloat(float v) { return Math.asin(v); }
	}
	
	private static class ArcCosExecutor extends DoubleToDoubleExecutor {
		protected ArcCosExecutor() { super("arccos(var)"); }
		@Override
		protected double processByte(byte v) { return Math.acos(v); }
		@Override
		protected double processInt(int v) { return Math.acos(v); }
		@Override
		protected double processFloat(float v) { return Math.acos(v); }
	}
	
	private static FloatValue convert(double d){
		return TempValueFactory.createTempFloatValue((float)d);
	}
}
