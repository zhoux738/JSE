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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.julang.external.binding.BooleanBinding;
import info.julang.external.binding.CharacterBinding;
import info.julang.external.binding.IBinding;
import info.julang.external.binding.IntegerBinding;
import info.julang.external.binding.StringBinding;
import info.julang.external.interfaces.IExtVariableTable;

/**
 * Parse cmdline variable in the format of name[:type][=value]. Forbid use of names which do not 
 * comply with Julian variable naming rules or conflict with keywords or otherwise reserved names.
 * 
 * @author Ming Zhou
 */
class CLVariableParser {

	private String _name;
	private IBinding _binding;
	
	private Pattern varPattern;
	private Pattern varNamePattern;
	private Set<String> forbiddenSet;
	
	CLVariableParser(){
		varPattern = Pattern.compile("([^:=]+):?([^=]*)=?(.*)");
		varNamePattern = Pattern.compile("[_a-zA-Z][_a-zA-Z0-9]*");
		forbiddenSet = new HashSet<String>();
		forbiddenSet.add("this");
		forbiddenSet.add("super");
		forbiddenSet.add(IExtVariableTable.KnownVariableName_Arguments);
		forbiddenSet.add("System");
	}
	
	/**
	 * Perform reentrant parsing. As long as this returns successfully, {@link #getName()} 
	 * and {@link #getBinding()} can be called later with guaranteed non-null values.
	 */
	void parse(String s) throws CLParsingException {
		this._name = null;
		this._binding = null;
		
		Matcher m = varPattern.matcher(s);
		if (!m.matches()){
			throw new CLParsingException("Cannot recognize variable " + s + ". Use format -v name[:type][=value].", false);
		}
		
		String name = m.group(1);
		checkName(name);
		
		String type = m.group(2);
		String value = m.group(3);
		
		IBinding binding = null;
		int defaultInt = 0;
		boolean defaultBool = false;
		
		// Infer the type from value
		if ("".equals(type)){
			boolean succ = false;
			if (!"".equals(value)){
				// Try int
				try {
					defaultInt = Integer.parseInt(value);
					value = "";
					type = "int";
					succ = true;
				} catch (NumberFormatException e) {
				}
				
				// Try bool
				if (!succ){
					// Must not use Boolean.parseBoolean() which always returns false if the string is not "true"
					String lc = value.toLowerCase(); 
					if ("true".equals(lc)){
						value = "";
						type = "bool";
						succ = true;
						defaultBool = true;
					} else if ("false".equals(lc)){
						value = "";
						type = "bool";
						succ = true;
						defaultBool = false;
					}
				}
			}
			
			if (!succ){
				type = "string";
			}
		}
		
		
		try {
			switch(type.toLowerCase()){
			case "int":
			case "integer": 
				binding = new IntegerBinding("".equals(value) ? defaultInt : Integer.parseInt(value)); break;
			case "bool":
			case "boolean":
				binding = new BooleanBinding("".equals(value) ? defaultBool : Boolean.parseBoolean(value)); break;
			case "char":
			case "character":
				binding = new CharacterBinding("".equals(value) ? '\0' : (char)value.getBytes()[0]); break;
			case "string": 
				binding = new StringBinding(value); break;
			default:
				throw new CLParsingException("Cannot recognize variable " + s + ". Type " + type + " is not supported.", false);
			}
		} catch (NumberFormatException e) {
			throw new CLParsingException("Cannot recognize variable " + s + ". The value is incompitable with the type.", false);
		}
		
		this._name = name;
		this._binding = binding;
	}

	private void checkName(String name) throws CLParsingException {
		Matcher m = varNamePattern.matcher(name);
		if (!m.matches()){
			throw new CLParsingException("Cannot use variable name \"" + name + "\". The name must comply with variable naming rules, in regex: [_a-zA-Z][_a-zA-Z0-9]*", false);
		}
		
		if (forbiddenSet.contains(name)){
			throw new CLParsingException("Cannot use variable name \"" + name + "\". This name is reserved.", false);
		}
	}

	/**
	 * If the previous {@link #parse(String)} call failed, returns null.
	 */
	String getName() {
		return _name;
	}
	
	/**
	 * If the previous {@link #parse(String)} call failed, returns null.
	 */
	IBinding getBinding() {
		return _binding;
	}
}
