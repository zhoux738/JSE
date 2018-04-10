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

package info.julang.jsr223;

import info.julang.VersionUtility;
import info.julang.typesystem.jclass.jufc.System.JConsole;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

public class JulianScriptingEngineFactory implements ScriptEngineFactory {

	private String engineVersion;
	
	@Override
	public String getEngineName() {
		return (String) getParameter(ScriptEngine.ENGINE);
	}

	@Override
	public String getEngineVersion() {
		return (String) getParameter(ScriptEngine.ENGINE_VERSION);
	}

	@Override
	public List<String> getExtensions() {
		List<String> list = new ArrayList<String>();
		list.add("jul");
		return list;
	}

	@Override
	public List<String> getMimeTypes() {
		return new ArrayList<String>();
	}

	@Override
	public List<String> getNames() {
		List<String> list = new ArrayList<String>();
		list.add("Julian");
		list.add("julian");
		return list;
	}

	@Override
	public String getLanguageName() {
		return (String) getParameter(ScriptEngine.LANGUAGE);
	}

	@Override
	public String getLanguageVersion() {
		return (String) getParameter(ScriptEngine.LANGUAGE_VERSION);
	}

	@Override
	public Object getParameter(String key) {
        if (key.equals(ScriptEngine.NAME)) {
            return "Julian";
        } else if (key.equals(ScriptEngine.ENGINE)) {
            return "Julian";
        } else if (key.equals(ScriptEngine.ENGINE_VERSION)) {
        	if(engineVersion == null){
            	engineVersion = VersionUtility.getVersion();
            	if(engineVersion == null){
            		engineVersion = "unknown";
            	}
        	}
        	return engineVersion;
        } else if (key.equals(ScriptEngine.LANGUAGE)) {
            return "Julian";
        } else if (key.equals(ScriptEngine.LANGUAGE_VERSION)) {
            return "1.0.0";
        } else if (key.equals("THREADING")) {
            return "MULTITHREADED";
        } else {
            throw new IllegalArgumentException("Invalid key");
        }
	}

	@Override
	public String getMethodCallSyntax(String obj, String method, String... args) {
		StringBuilder sb = new StringBuilder();
		sb.append(obj);
		sb.append(".");
		sb.append(method);
		sb.append("(");

        int len = args != null ? args.length : 0;
		for(int i = 0; i < len - 1; i++){
			sb.append(args[i]);
			sb.append(",");
		}
		
		if(len > 0){
			sb.append(args[len - 1]);
		}
		
		sb.append(")");
		
        return sb.toString();
	}

	@Override
	public String getOutputStatement(String toDisplay) {
		return getMethodCallSyntax(JConsole.FQCLASSNAME, "println", toDisplay) + ";";
	}

	@Override
	public String getProgram(String... statements) {
		StringBuilder sb = new StringBuilder();

        int len = statements != null ? statements.length : 0;
		for(int i = 0; i < len; i++){
			String stmt = statements[i];
			sb.append(stmt);
			if(!stmt.trim().endsWith(";")){
				sb.append(";\n");
			}
		}
		
        return sb.toString();
	}

	@Override
	public ScriptEngine getScriptEngine() {
		JulianScriptingEngine jse = new JulianScriptingEngine();
		jse.setEngineFactory(this);
        return jse;
	}

}
