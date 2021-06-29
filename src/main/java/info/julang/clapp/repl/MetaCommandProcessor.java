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

package info.julang.clapp.repl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import info.julang.clapp.CLParsingException;
import info.julang.clapp.repl.meta.HelpMetaCommand;
import info.julang.clapp.repl.meta.RunMetaCommand;
import info.julang.external.JulianScriptEngine;
import info.julang.external.exceptions.JSEError;
import info.julang.util.Pair;

/**
 * Process the dot-led meta commands.
 * 
 * @author Ming Zhou
 */
public class MetaCommandProcessor {

	private static MetaCommandProcessor INSTANCE;
	
	static MetaCommandProcessor getInstance(ConsoleSession session, boolean isPosix){
		if (INSTANCE == null){
			INSTANCE = new MetaCommandProcessor(session, isPosix);
		}
		
		return INSTANCE;
	}
	
	private Map<String, MetaCommand> map;
	private RunMetaCommand rmc; // very special meta-command
	private ConsoleSession session;
	
	private MetaCommandProcessor(ConsoleSession session, boolean isPosix){
		this.session = session;
		map = new TreeMap<String, MetaCommand>();
		
		register("help", new HelpMetaCommand(this));
		register("clear");
		register("exit");
		register("load");
		if (isPosix) {
			register("history");
		}
		
		rmc = new RunMetaCommand(isPosix);
	}
	
	private void register(String command){
		register(command, null);
	}
	
	private void register(String command, MetaCommand comm){
		map.put(command, comm);
	}
	
	void runCommand(String input, IConsole console, JulianScriptEngine engine) throws CLParsingException {
		String[] inputs = input.split("\\s+");
		String cmd = inputs[0];
		
		if (cmd.startsWith("!") && cmd.length() > 1){
			rmc.setCmd(cmd);
			rmc.execute(console, engine);
			return;
		}
		

		cmd = cmd.substring(1, cmd.length()); // Strip off the leading '.'
		
		if ("".equals(cmd)){
			throw new REPLParsingException("Meta-command is incomplete.");
		} else {
			cmd = cmd.toLowerCase();
		}
		
		MetaCommand mc = map.get(cmd);
		if (mc == null) {
			if (map.containsKey(cmd)) {
				// Initialize
				mc = initCommand(cmd);
				map.put(cmd, mc);
			}
		}
		
		if (mc == null) {
			throw new REPLParsingException("Meta-command \"" + cmd + "\" is not recognized.");
		} else {
			resetArgs(mc, inputs);
			mc.execute(console, engine);
		}
	}

	private MetaCommand initCommand(String cmd) throws REPLParsingException {
		try {
			Class<? extends MetaCommand> clazz = getMCClass(cmd);
			return clazz.getDeclaredConstructor().newInstance();
		} catch (InstantiationException 
			| IllegalAccessException 
			| ClassNotFoundException 
			| IllegalArgumentException 
			| InvocationTargetException 
			| NoSuchMethodException
			| SecurityException e) {
			throw new JSEError("Meta-command \"" + cmd + "\" cannot be initialized. Error: " + e.getMessage());
		}
	}
	
	private void resetArgs(MetaCommand mc, String[] inputs) throws REPLParsingException {
		Set<String> set = new HashSet<String>();
		Pair<String, String> kvp = null;
		String free = null;
		int ivalue = -1;
		for(int i = 1; i < inputs.length; i++){
			String inp = inputs[i];
			if (!inp.startsWith("-")) {
				int ind = inp.indexOf('=');
				if (ind != -1) {
					if (kvp != null) {
						throw new REPLParsingException("Unexpected argument: " + inp);
					}
					kvp = new Pair<String, String>(inp.substring(0, ind + 1), inp.substring(ind + 1, inp.length()));
				} else {
					try {
						ivalue = Integer.parseUnsignedInt(inp);
					} catch (NumberFormatException e) {
						if (free != null) {
							throw new REPLParsingException("Unexpected argument: " + inp);
						}
						
						free = inp;
					}
				}
			} else {
				try {
					ivalue = Integer.parseUnsignedInt(inp);
				} catch (NumberFormatException e) {
					set.add(inp);
				}
			}
		}
		
		Class<? extends MetaCommand> clazz = mc.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for(Field f : fields){
			MCArg[] mas = f.getAnnotationsByType(MCArg.class);
			if (mas.length > 0){
				// the field is marked as a parameter
				Class<?> ftclazz = f.getType();
				try {
					f.setAccessible(true);
					if (ftclazz == Boolean.TYPE){
						// 1) a flag
						String key = "-" + f.getName();
						boolean bval = map.containsKey(key);
						f.setBoolean(mc, bval);
					} else if (ftclazz == String.class){
						// 2) a string
						f.set(mc, free);
					} else if (ftclazz == Pair.class){
						// 3) a pair
						f.set(mc, kvp);
					} else if (ftclazz == ConsoleSession.class){
						// 4) console session object
						f.set(mc, session);
					} else if (ftclazz == Integer.TYPE){
						// 5) an int
						f.set(mc, ivalue);
					} 
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new JSEError("Meta-command cannot be initialized due to bad arguments. Error: " + e.getMessage());
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Class<? extends MetaCommand> getMCClass(String cmd) throws ClassNotFoundException {
		byte[] bs = cmd.getBytes();
		bs[0] = (byte) Character.toUpperCase((char) bs[0]);
		String s = "info.julang.clapp.repl.meta." + new String(bs) + "MetaCommand";
		Class<? extends MetaCommand> clazz = (Class<? extends MetaCommand>) Class.forName(s);
		return clazz;
	}
	
	//-------------------- help message --------------------//

	private String helpAll = null;
	
	/**
	 * Get help for a single command or all commands.
	 * 
	 * @param metaCommand if null, get help for all commands.
	 * @return The help information
	 */
	public String getHelp(String metaCommand) {
		if (metaCommand == null) {
			if (helpAll == null){
				StringBuilder sb = new StringBuilder();
				for (String name : map.keySet()){
					String m = getHelpFor(name, true);
					sb.append(m);
					sb.append(System.lineSeparator());
				}
				
				helpAll = sb.toString();
			}
			
			return helpAll;
		}
		
		if (!map.containsKey(metaCommand)) {
			return "Command \"" + metaCommand + "\" doesn't exist." + System.lineSeparator();
		} else {
			return getHelpFor(metaCommand, false);
		}
	}

	private String getHelpFor(String name, boolean oneLiner) {
		try {
			Class<? extends MetaCommand> clazz = getMCClass(name);
			MCProps[] mcps = clazz.getAnnotationsByType(MCProps.class);
			if (mcps.length > 0){
				MCProps mcp = mcps[0];
				if (oneLiner) {
					// cmd  DESC
					String desc = mcp.description();
					String res = String.format("  %-10s%s", name, desc);
					return res;
				} else {
					String res = getMultiLineHelpFor(name, clazz, mcp);
					return res;
				}
			}
		} catch (ClassNotFoundException e) {
			// Ignore
		}

		return "Command \"" + name + "\" doesn't exist." + System.lineSeparator();
	}

	private String getMultiLineHelpFor(String name, Class<? extends MetaCommand> clazz, MCProps mcp) {
		// cmd [arg0] [arg1] ...
		//     arg0  DESC
		//     arg1  DESC
		
		StringBuilder sb = new StringBuilder(name);
		StringBuilder sb2 = new StringBuilder();
		
		// 1) summary line
		Field[] fds = clazz.getDeclaredFields();
		for(Field f : fds) {
			MCArg[] mas = f.getAnnotationsByType(MCArg.class);
			if (mas.length > 0){
				// The field is marked as a parameter. But we only honor the 1st annotation.
				MCArg ma = mas[0];
				
				String key = null;
				String fname = ma.argName();
				if ("".equals(fname)){
					fname = f.getName();
				}
				
				Class<?> ftclazz = f.getType();
				if (ftclazz == Boolean.TYPE){
					// 1) a flag
					key = "-" + fname;
				} else if (ftclazz == String.class){
					// 2) a string
					key = fname + ": string";
				} else if (ftclazz == Pair.class){
					// 3) a pair
					key = "key: string = value: string";
				} else if (ftclazz == Integer.TYPE){
					// 4) an int
					key = fname + ": integer";
				} 
				
				if (key != null) {
					sb.append(" [");
					sb.append(key);
					sb.append("]");
					
					sb2.append(String.format("  %-10s", fname));
					sb2.append(ma.description());
					sb2.append(System.lineSeparator());
				}
			}
		}
		
		sb.append(System.lineSeparator());

		// 2) command description
		sb.append(mcp.description());
		sb.append(System.lineSeparator());
		
		// 3) argument description
		sb.append(sb2);
		
		return sb.toString();
	}
}
