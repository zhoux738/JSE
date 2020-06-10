package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getIntValue;
import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;

public class System_Datetime_Tests {

	private static final String FEATURE = "Foundation";
	
	@Test
	public void datetimeGetNowTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		Date time1 = Calendar.getInstance().getTime();
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "datetime_1.jul"));
		Date time2 = Calendar.getInstance().getTime();
		
		int year   = getIntValue(gvt.getVariable("year"));
		int month  = getIntValue(gvt.getVariable("month"));
		int day    = getIntValue(gvt.getVariable("day"));
		int hour   = getIntValue(gvt.getVariable("hour"));
		int minute = getIntValue(gvt.getVariable("minute"));
		int second = getIntValue(gvt.getVariable("second"));
		int milli  = getIntValue(gvt.getVariable("milli"));
		
		Calendar.Builder b = new Calendar.Builder();
		b.setDate(year, month - 1, day);
		b.setTimeOfDay(hour, minute, second, milli);
		Calendar c = b.build();
		Date time = c.getTime();
		
//		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd-hh:mm:ss.SSS");
//		System.out.println(formatter.format(time1) + " " + time1.getTime());
//		System.out.println(formatter.format(time) + " " + time.getTime());
//		System.out.println(formatter.format(time2) + " " + time2.getTime());
		
		long min    = time1.getTime();
		long actual = time.getTime();
		long max    = time2.getTime();
		
		Assert.assertTrue("min = " + min + " actual = " + actual, min <= actual);
		Assert.assertTrue("max = " + min + " actual = " + actual, actual <= max);
	}
	
	@Test
	public void datetimeCtorTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "datetime_2.jul"));
		
		validateIntValue(gvt.getVariable("year"), 1999);
		validateIntValue(gvt.getVariable("month"), 12);
		validateIntValue(gvt.getVariable("day"), 31);
		validateIntValue(gvt.getVariable("hour"), 23);
		validateIntValue(gvt.getVariable("minute"), 59);
		validateIntValue(gvt.getVariable("second"), 59);
		validateIntValue(gvt.getVariable("milli"), 999);
	}
	
	@Test
	public void datetimeFormatTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "datetime_3.jul"));
		
		validateStringValue(gvt.getVariable("str1"), "2014/03/25-08:30:05.026");
		validateStringValue(gvt.getVariable("str2"), "14/3/25-8:30:5.26");
		validateStringValue(gvt.getVariable("str3"), "8:0:5.6");
	}
}
