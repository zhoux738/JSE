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

package info.julang.clapp;

import java.util.List;

abstract class ParameterBase {
	
	private String longName;
	private boolean isRequired;
	private boolean allowMultiple;

	/**
	 * @param longName A long name is no longer than 20 characters (to avoid unaligned display of help message).
	 * @param isRequired
	 * @param allowMultiple
	 */
	protected ParameterBase(
		String longName,
		boolean isRequired,
		boolean allowMultiple){
		this.longName = longName;
		this.isRequired = isRequired;
		this.allowMultiple = allowMultiple;
	}

	public String getLongName() {
		return longName;
	}

	public boolean isRequired() {
		return isRequired;
	}
	
	public boolean allowMultiple() {
		return allowMultiple;
	}
	
}

abstract class CLParameter extends ParameterBase {

	private String shortName;
	private boolean isSwitch;
	private String help;

	/**
	 * 
	 * @param shortName A short name is no longer than 2 characters (to avoid unaligned display of help message).
	 * @param longName A long name is no longer than 20 characters (to avoid unaligned display of help message).
	 * @param help
	 * @param isRequired
	 * @param allowMultiple
	 * @param isSwitch
	 */
	public CLParameter(
		String shortName, 
		String longName,
		String help,
		boolean isRequired,
		boolean allowMultiple,
		boolean isSwitch){
		super(longName, isRequired, allowMultiple);
		this.shortName = shortName;
		this.help = help;
		this.isSwitch = isSwitch;
	}
	
	public abstract void process(CLEnvironment env, String rawArg, Object value) throws CLParsingException;
	
	public String getShortName() {
		return shortName;
	}

	public boolean isSwitch() {
		return isSwitch;
	}

	public String getHelp() {
		return help;
	}
	
}

/**
 * Switch parameter
 */
abstract class SwitchParameter extends CLParameter {

	SwitchParameter(
		String shortName, String longName, String help,
		boolean isRequired, boolean allowMultiple) {
		super(shortName, longName, help, isRequired, allowMultiple, true);
	}

	@Override
	public void process(CLEnvironment env, String rawArg, Object value) throws CLParsingException {
		if(value instanceof Boolean){
			boolean bv = (boolean)value;
			doProcess(env, rawArg, bv);
		} else {
			throw new CLParsingException("Switch parameter " + rawArg + " doesn't specify on or off.", false);
		}
	}

	protected abstract void doProcess(CLEnvironment env, String rawArg, boolean bv);
	
}

/**
 * Comma-separated parameter
 */
abstract class StringArrayParameter extends CLParameter {

	StringArrayParameter(
		String shortName, String longName, String help,
		boolean isRequired, boolean allowMultiple) {
		super(shortName, longName, help, isRequired, allowMultiple, false);
	}

	@Override
	public void process(CLEnvironment env, String rawArg, Object value) throws CLParsingException {
		if(value instanceof String){
			String sv = (String)value;
			String[] strs = sv.split(",");
			doProcess(env, rawArg, strs);
		} else {
			throw new CLParsingException("Parameter " + rawArg + " is incorrect.", false);		
		}
	}

	protected abstract void doProcess(CLEnvironment env, String rawArg, String[] strArgs) throws CLParsingException;
	
}

/**
 * String parameter
 */
abstract class StringParameter extends CLParameter {

	StringParameter(
		String shortName, String longName, String help,
		boolean isRequired, boolean allowMultiple) {
		super(shortName, longName, help, isRequired, allowMultiple, false);
	}

	@Override
	public void process(CLEnvironment env, String rawArg, Object value) throws CLParsingException {
		if(value instanceof String){
			String sv = (String)value;
			doProcess(env, rawArg, sv);
		} else {
			throw new CLParsingException("Parameter " + rawArg + " is incorrect.", false);		
		}
	}

	protected abstract void doProcess(CLEnvironment env, String rawArg, String sv);
	
}

/**
 * Rest free parameters on the command line
 */
abstract class RestParameter extends ParameterBase {
	
	RestParameter(String longName, boolean isRequired, boolean allowMultiple) {
		super(longName, isRequired, allowMultiple);
	}
	
	public abstract void process(CLEnvironment env, List<String> values) throws CLParsingException;
}
