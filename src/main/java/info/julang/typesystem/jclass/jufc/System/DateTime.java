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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import info.julang.execution.Argument;
import info.julang.execution.security.PACON;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.CtorNativeExecutor;
import info.julang.hosting.execution.InstanceNativeExecutor;
import info.julang.hosting.execution.StaticNativeExecutor;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.RefValue;
import info.julang.util.Pair;

/**
 * The native implementation of <font color="green">System.DateTime</font>.
 * <p/>
 * This implementation is backed by {@link java.util.Calendar}, which has a tricky part: its month
 * is 0-based. So a +1/-1 conversion is performed at this layer to make Julian API more user friendly.
 *  
 * @author Ming Zhou
 */
public class DateTime {
	
	public final static String FullTypeName = "System.DateTime";
	
	//----------------- IRegisteredMethodProvider -----------------//
	
	public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FullTypeName){

		@Override
		protected void implementProvider(SimpleHostedMethodProvider provider) {
			provider
				.add("ctor", new InitExecutor())
				.add("getNow", new GetNowExecutor())
				.add("diff", new DiffExecutor())
				.add("toString", new ToStringExecutor())
				.add("getYear", new GetYearExecutor())
				.add("getMonth", new GetMonthExecutor())
				.add("getDay", new GetDayExecutor())
				.add("getHour", new GetHourExecutor())
				.add("getMinute", new GetMinuteExecutor())
				.add("getSecond", new GetSecondExecutor())
				.add("getMilli", new GetMilliExecutor());
		}
		
	};
	
	//----------------- native executors -----------------//
	
	private static class InitExecutor extends CtorNativeExecutor<DateTime> {

		@Override
		protected void initialize(ThreadRuntime rt, HostedValue hvalue, DateTime datetime, Argument[] args) {
			int year = getInt(args, 0);
			int month = getInt(args, 1);
			int day = getInt(args, 2);
			int hour = getInt(args, 3);
			int minute = getInt(args, 4);
			int second = getInt(args, 5);
			int milli = getInt(args, 6);
			datetime.init(year, month, day, hour, minute, second, milli);
		}
		
	}
	
	private static class GetNowExecutor extends StaticNativeExecutor<DateTime> {

		GetNowExecutor(){
			super(PACON.Environment.Name, PACON.Environment.Op_read);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, Argument[] args) {
			Pair<HostedValue, RefValue> pair = createHostedValue(FullTypeName, rt, true);
			
			DateTime dt = new DateTime();
			dt.init();
			pair.getFirst().setHostedObject(dt);
			
			return pair.getSecond();
		}
		
	}
	
	private static class DiffExecutor extends InstanceNativeExecutor<DateTime> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, DateTime datetime, Argument[] args) throws Exception {
			HostedValue another = getHosted(args, 0);
			int diff = datetime.diff((DateTime)another.getHostedObject());
			return createInt(rt, diff);
		}
		
	}
	
	private static class ToStringExecutor extends InstanceNativeExecutor<DateTime> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, DateTime datetime, Argument[] args) throws Exception {
			String str = datetime.getString(null);
			return createString(rt, str);
		}
		
	}
	
	private static class GetYearExecutor extends InstanceNativeExecutor<DateTime> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, DateTime datetime, Argument[] args) throws Exception {
			int rawValue = datetime.getYear();
			return createInt(rt, rawValue);
		}
		
	}
	
	private static class GetMonthExecutor extends InstanceNativeExecutor<DateTime> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, DateTime datetime, Argument[] args) throws Exception {
			int rawValue = datetime.getMonth();
			return createInt(rt, rawValue);
		}
		
	}
	
	private static class GetDayExecutor extends InstanceNativeExecutor<DateTime> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, DateTime datetime, Argument[] args) throws Exception {
			int rawValue = datetime.getDay();
			return createInt(rt, rawValue);
		}
		
	}
	
	private static class GetHourExecutor extends InstanceNativeExecutor<DateTime> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, DateTime datetime, Argument[] args) throws Exception {
			int rawValue = datetime.getHour();
			return createInt(rt, rawValue);
		}
		
	}
	
	private static class GetMinuteExecutor extends InstanceNativeExecutor<DateTime> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, DateTime datetime, Argument[] args) throws Exception {
			int rawValue = datetime.getMinute();
			return createInt(rt, rawValue);
		}
		
	}
	
	private static class GetSecondExecutor extends InstanceNativeExecutor<DateTime> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, DateTime datetime, Argument[] args) throws Exception {
			int rawValue = datetime.getSecond();
			return createInt(rt, rawValue);
		}
		
	}
	
	private static class GetMilliExecutor extends InstanceNativeExecutor<DateTime> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, DateTime datetime, Argument[] args) throws Exception {
			int rawValue = datetime.getMilli();
			return createInt(rt, rawValue);
		}
		
	}

	//----------------- implementation at native end -----------------//
	
	private Calendar calendar;
	
	private void init(int year, int month, int day, int hours, int min, int sec, int millisec) {
		Calendar.Builder builder = new Calendar.Builder();
		builder.setDate(year, month - 1, day); // month in Calendar is 0-based; Julian's DateTime is 1-based.
		builder.setTimeOfDay(hours, min, sec, millisec);
		calendar = builder.build();
	}
	
	private void init(){
		calendar = Calendar.getInstance();
	}
	
	private int diff(DateTime dt){
		long thisTime = calendar.getTimeInMillis();
		long thatTime = dt.calendar.getTimeInMillis();
		int diff = (int)(thisTime - thatTime); // Precision loss
		return diff;
	}
	
	private String getString(String format){
		if (format == null){
			format = "yyyy/MM/dd-hh:mm:ss.SSS";
		}
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		String formatted = formatter.format(calendar.getTime());
		return formatted;
	}
	
	private int getYear(){
		return calendar.get(Calendar.YEAR);
	}
	
	private int getMonth(){
		return calendar.get(Calendar.MONTH) + 1; // month in Calendar is 0-based; Julian's DateTime is 1-based.
	}
	
	private int getDay(){
		return calendar.get(Calendar.DAY_OF_MONTH);
	}
	
	private int getHour(){
		return calendar.get(Calendar.HOUR_OF_DAY);
	}
	
	private int getMinute(){
		return calendar.get(Calendar.MINUTE);
	}
	
	private int getSecond(){
		return calendar.get(Calendar.SECOND);
	}
	
	private int getMilli(){
		return calendar.get(Calendar.MILLISECOND);
	}
	
}
