package info.jultest.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;

public class IntRange {

	private static Pattern pattern = Pattern.compile("([\\[|\\(])(\\d*),\\s*(\\d*)([\\]|\\)])");
	
	private boolean moreOrEqual;
	private boolean lessOrEqual;
	private int min;
	private int max; 
	
	private String raw;
	
	private IntRange(){ }
	
	/**
	 * @param range in the form of [1,2)
	 */
	public static IntRange parse(String range){
		Matcher m = pattern.matcher(range);
		if(m.matches()){
			IntRange irange = new IntRange();
			irange.moreOrEqual = m.group(1).equals("[");
			irange.lessOrEqual = m.group(4).equals("[");
			irange.min = m.group(2).length() > 0 ? Integer.parseInt(m.group(2)) : Integer.MIN_VALUE;
			irange.max = m.group(3).length() > 0 ? Integer.parseInt(m.group(3)) : Integer.MAX_VALUE;
			irange.raw = range;
			return irange;
		}
		
		throw new NumberFormatException("Range \"" + range + "\" is not recognizable.");
	}
	
	public void validate(int value){
		boolean leftEnd = moreOrEqual ? value >= min : value > min;
		boolean rightEnd = lessOrEqual ? value <= max : value < max;
		Assert.assertTrue(value + " is out of range " + raw, leftEnd && rightEnd);
	}
}
