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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import info.julang.util.OSTool;

/**
 * A command-line parsing framework.
 * <p/>
 * On Windows system we accept dash (-), double dashes (--), and slash (/) as argument symbol; 
 * on others we only accept dash (-) for short name and double dashes (--) for long name.
 * 
 * @author Ming Zhou
 */
public class CLParser {

	private String[] args;
	
	private CLEnvironment env;
	
	private Set<CLParameterState> paramSet;
	private Map<String, CLParameterState> paramShortNameMap;
	private Map<String, CLParameterState> paramLongNameMap;
	
	private RestParameter restParam;
	private List<String> restArgs;
	
	private static boolean isWindows = OSTool.isWindows();
	
	public CLParser(String[] args){
		this.args = args != null ? args : new String[0];
		initialize();
	}

	/**
	 * Parse the arguments. Can be only called effectively once. 
	 * All the following calls return the one cached from the first call.
	 * 
	 * @return 
	 * @throws CLParsingException
	 */
	public CLEnvironment parse() throws CLParsingException {
		if(env != null){
			return env;
		}
		
		env = new CLEnvironment();
		CLArgStream clargs = new CLArgStream(args);
		// The REST mode indicates whether we are left with the rest of arguments, signaled 
		// by the sight of an argument that is not a parameter name (not led by - or --).
		boolean restMode = false;
		while(clargs.hasMore()){
			String raw = clargs.read();
			
			if(restMode){
				addRest(raw);
				continue;
			}
			
			CLParameterState clparam = null;
			CLArg arg = CLArg.convertFromRaw(raw);
			switch(arg.nameMode){
			case SHORT:
				clparam = paramShortNameMap.get(arg.name);
				break;
			case LONG:
				clparam = paramLongNameMap.get(arg.name);
				break;
			case BOTH:
				clparam = paramShortNameMap.get(arg.name);
				if(clparam == null){
					clparam = paramLongNameMap.get(arg.name);
				}
				break;
			case BARE: // Enter REST mode
				addRest(raw);
				restMode = true;
				continue;
			}
			
			if(clparam == null){
				throw new CLParsingException("Unrecognized parameter: " + arg.name, false);
			}
			
			processArg(clparam, arg, clargs, env);
			
			if(env.shouldTerminate()){
				// Abort if asked so
				return env;
			}
		}
		
		validate();
		
		// Validate and process the rest arguments now
		if(restParam != null){
			if(restParam.isRequired() && restArgs == null){
				throw new CLParsingException("Parameter is required: " + restParam.getLongName(), false);
			} else if (!restParam.allowMultiple() && (restArgs != null && restArgs.size() > 1)){
				throw new CLParsingException("Encountered paramter of same name twice: " + restParam.getLongName(), false);
			}
			
			if(restArgs != null && restArgs.size() > 0){
				restParam.process(env, restArgs);		
			}
		}
		
		return env;
	}

	// Only to be called by CLParserConfig
	void addParam(CLParameter clparam){
		CLParameterState state = new CLParameterState(clparam);
		paramSet.add(state);
		paramShortNameMap.put(clparam.getShortName(), state);
		paramLongNameMap.put(clparam.getLongName(), state);
	}
	
	// Only to be called by CLParserConfig
	void addAlias(String alias, String name, boolean isShort){
		if(isShort){
			CLParameterState state = paramShortNameMap.get(name);
			paramShortNameMap.put(alias, state);
		} else {
			CLParameterState state = paramLongNameMap.get(name);
			paramLongNameMap.put(alias, state);		
		}
	}
	
	// Only to be called by CLParserConfig
	void setRestParam(RestParameter restParam){
		this.restParam = restParam;
	}
	
	// Process a single argument
	private void processArg(
		CLParameterState state, 
		CLArg arg, 
		CLArgStream clargs,
		CLEnvironment env) throws CLParsingException {
		
		state.encounter(arg.name);
		
		CLParameter clparam = state.clparam;
		Object value = null;
		if(clparam.isSwitch()){
			if(arg.value != null){
				String lowered = arg.value.toLowerCase().trim();
				if("false".equals(lowered)){
					value = false;
				} else if("true".equals(lowered)){
					value = true;
				} else {
					throw new CLParsingException("A switch parameter cannot have non-boolean value: " + arg.name, false);
				}
			} else {
				// Unless it is explicitly specified as OFF, we consider it to be turned ON.
				value = arg.switchMode == SwitchMode.OFF ? false : true;		
			}
		} else if (arg.value != null){
			// Use the argument specified after ':'
			value = arg.value;
		} else {
			// Read another argument
			value = clargs.read();
		}
		
		clparam.process(env, arg.raw, value);
	}

	private void initialize() {
		paramSet = new HashSet<CLParameterState>();
		paramShortNameMap = new HashMap<String, CLParameterState>();
		paramLongNameMap = new HashMap<String, CLParameterState>();
		
		CLParserConfig config = new CLParserConfig(this);
		config.config();
	}
	
	private void addRest(String raw) {
		if(restArgs == null){
			restArgs = new ArrayList<String>();
		}
		restArgs.add(raw);
	}
	
	private void validate() throws CLParsingException {
		for(CLParameterState state : paramSet){
			state.validate();
		}
	}
	
	//----------------------- private data structures -----------------------//
	
	enum NameMode {
		BARE,
		SHORT,
		LONG,
		BOTH
	}
	
	enum SwitchMode {
		UNKNOWN,
		ON,
		OFF
	}
	
	private class CLParameterState {
		// The CL Parameter represented by this state object
		CLParameter clparam;
		
		// Various states
		int encounterd;
		
		CLParameterState(CLParameter clparam){
			this.clparam = clparam;
		}
		
		void encounter(String name) throws CLParsingException {
			encounterd++;
			if(encounterd > 1 && !clparam.allowMultiple()){
				throw new CLParsingException("Encountered paramter of same name twice: " + name, false);
			}
		}
		
		void validate() throws CLParsingException {
			if(encounterd == 0 && clparam.isRequired()){
				throw new CLParsingException("Parameter is required: -" + clparam.getShortName(), false);
			}
		}
	}
	
	private static class CLArg {
		
		/** The switch mode as determined by the leading '-/--' sign. */
		NameMode nameMode;
		
		/** The switch mode as determined by the tailing '+/-' sign. */
		SwitchMode switchMode;
		
		/** The argument name  */
		String name;
		
		/** The argument value as extracted after ':' sign  */
		String value;
		
		/** The argument string as specified on command line */
		String raw;
		
		public CLArg(String name, NameMode nameMode, SwitchMode switchMode, String value, String raw) {
			this.switchMode = switchMode;
			this.name = name;
			this.nameMode = nameMode;
			this.value = value;
			this.raw = raw;
		}

		static CLArg convertFromRaw(String raw) {
			String rawArg = raw;
			SwitchMode switchMode = SwitchMode.UNKNOWN;
			NameMode nameMode = NameMode.BARE;
			String value = null;
			if(raw.startsWith("--")){
				// Long name
				nameMode = NameMode.LONG;
				raw = raw.substring(2);
			} else if(raw.startsWith("-")){
				// Short name
				nameMode = NameMode.SHORT;
				raw = raw.substring(1);
			} else if(CLParser.isWindows && raw.startsWith("/")){
				// Both are possible
				nameMode = NameMode.BOTH;
				raw = raw.substring(1);
			}
			
			// Continue processing accessories only if this is not a bare argument
			if(nameMode != NameMode.BARE){
				int colonIndex = -1;
				if((colonIndex = raw.indexOf(':')) != -1){
					value = raw.substring(colonIndex+1);
					if(value != null && value.length() == 0){
						value = null;
					}
					raw = raw.substring(0, colonIndex);
				} else {
					if(raw.endsWith("+")){
						raw = raw.substring(0, raw.length() - 1);
						switchMode = SwitchMode.ON;
					} else if(raw.endsWith("-")){
						raw = raw.substring(0, raw.length() - 1);
						switchMode = SwitchMode.OFF;
					}				
				}			
			}
			
			return new CLArg(raw, nameMode, switchMode, value, rawArg);
		}
	}
	
	private class CLArgStream {
		
		private int index;
		private int total;
		private String[] args;
		
		CLArgStream(String[] args){
			this.args = args;	
			this.total = args.length;
		}
		
		String read() throws CLParsingException {
			if(index >= total){
				throw new CLParsingException("Expected more arguments from command line.", false);
			} else {
				String next = args[index];
				index++;
				return next;
			}
		}
		
		boolean hasMore(){
			return index < total;
		}
	}
	
}
