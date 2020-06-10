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
import java.util.List;

import info.julang.VersionUtility;
import info.julang.clapp.update.Downloader;
import info.julang.clapp.update.WrapperScriptUpdater;

/**
 * Install the parser for JSE cmdline executable.
 * 
 * @author Ming Zhou
 */
public class CLParserConfig {

	private static final String SECRET_OPTION = "so-";
	private CLVariableParser vparser;
	private CLParser parser;
	
	CLParserConfig(CLParser parser){
		this.parser = parser;
	}
	
	void config(){				
		List<CLParameter> list = new ArrayList<CLParameter>();
		final StringBuilder appHelpMsg = new StringBuilder();		
		list.add(
			new SwitchParameter("h", "help", "Show help message.", false, false){
			@Override
			public void doProcess(CLEnvironment env, String raw, boolean showHelp) {
				if(showHelp){
					env.setTerminate(0);
					System.out.println(appHelpMsg.toString());
				}
			}
		});
		
		configMain(list);
		
		RestParameter restParam = getRestParam();
		
		String engineVersion = VersionUtility.getVersion();	
		engineVersion = engineVersion == null ? "" : "-" + engineVersion;
		String mainCmd = String.format("java -jar JSE%1$s.jar ", engineVersion);
		
		StringBuilder appCmdAllInOne = new StringBuilder();
		StringBuilder appArgsOnePerLine = new StringBuilder();
		
		appCmdAllInOne.append(mainCmd);
		for(CLParameter clp : list){
			String sn = clp.getShortName();
			if (!sn.startsWith(SECRET_OPTION)){
				// 0. short
				// 1. long
				// 2/3. necessity: [ ]
				// 4. plurality: *
				// 5. switch or value: +/- | <value>
				String fmtArg = String.format(
					"%3$s-%1$s|--%2$s%6$s%4$s%5$s ", 
					sn,
					clp.getLongName(),
					clp.isRequired() ? "" : "[",
					clp.isRequired() ? "" : "]",
					clp.allowMultiple() ? "*" : "",
					clp.isSwitch() ? "+/-" : " <value>"
				);
				appCmdAllInOne.append(fmtArg);
				
				String helpArg = String.format(
					"    -%1$-3s%2$-25s%3$s\n", 
					clp.getShortName(),
					String.format("(--%1$s)", clp.getLongName()), 
					clp.getHelp());
				appArgsOnePerLine.append(helpArg);
			}
			
			parser.addParam(clp);
		}
		
		if(restParam != null){
			String restFmtArg = String.format(
				"%2$s%1$s%3$s [argument]*",
				restParam.getLongName(),
				restParam.isRequired() ? "" : "[",
				restParam.isRequired() ? "" : "]"
			);
			appCmdAllInOne.append(restFmtArg);
			parser.setRestParam(restParam);
		}
		
		// Aggregate help message
		appHelpMsg.append(appCmdAllInOne.toString());
		appHelpMsg.append("\noptions:\n");
		appHelpMsg.append(appArgsOnePerLine.toString());
		
		parser.addAlias("?", "h", true);
	}

	//------ The following code is the only place in this package that is subject to continuous changes ------//
	
	// Add all the main parameters here. 
	private void configMain(List<CLParameter> list){
		/*
		 * Parameter meanings:
		 *   String shortName, 
		 *   String longName,
		 *   String help,
		 *   boolean isRequired,
		 *   boolean allowMultiple
		 */
		
		list.add(
			new SwitchParameter("i", "interactive", "Launch interactive console.", false, false){
			@Override
			public void doProcess(CLEnvironment env, String raw, boolean value) {
				env.setInteractiveMode(value);
			}
		});
		
		list.add(
			new StringParameter("f", "file", "The script file to run. This will overwrite the free argument (script-file) at the end.", false, false){
			@Override
			public void doProcess(CLEnvironment env, String raw, String value) {
				env.setScriptFile(value);
			}
		});
		
		list.add(
			new StringArrayParameter("mp", "module-path", "Add one or more module paths.", false, true){
			@Override
			public void doProcess(CLEnvironment env, String raw, String[] values) {
				for(String s : values){
					String sv = s.trim();
					if(!"".equals(sv)){
						env.addModulePath(sv);					
					}
				}
			}
		});

		list.add(
			new SwitchParameter("q", "quiet", "Do not print the result.", false, false){
			@Override
			public void doProcess(CLEnvironment env, String raw, boolean value) {
				env.setQuietMode(value);
			}
		});
		
		list.add(
			new StringParameter("s", "snippet", "The script snippet to run. This will overwrite any script file arguments.", false, false){
			@Override
			public void doProcess(CLEnvironment env, String raw, String value) {
				// To ease use, auto-complete a semicolon should the given script not come with one 
				env.setScript(value.endsWith(";") ? value : value + ";");
			}
		});
		
		// Accept parameter in format of -v name[:type][=value]. Examples: 
		//   -v age:int=34          Explicitly bind age with an int value, initialized with 34
		//   -v name:string=Mike    Explicitly bind name with a string value
		//   -v age=34              Implicitly bind age with an int value, initialized with 34  
		//   -v flag=false          Implicitly bind name with a bool value
		//   -v age:int             Explicitly bind age with an int value, initialized with 0
		//   -v name                Implicitly bind name with a string value, initialized with "" 
		list.add(
			new StringArrayParameter("v", "variable", 
			"Add one or more script vairables, in the format of name[:type][=value]. Types are: bool, char, int, string.", false, true) {
			@Override
			public void doProcess(CLEnvironment env, String raw, String[] values) throws CLParsingException {
				if (vparser == null){
					vparser = new CLVariableParser();
				}
				
				for(String s : values){					
					vparser.parse(s);
					env.addBinding(vparser.getName(), vparser.getBinding());
				}
			}
		});
		
		//---------------------------------- non-public options ----------------------------------//
		
		list.add(
			new SwitchParameter(SECRET_OPTION + 0, "so-support-ansi-escaping", "", false, false){
			@Override
			public void doProcess(CLEnvironment env, String raw, boolean value) {
				env.setSupportForANSIEscaping(value);
			}
		});
		
		list.add(
			new StringParameter(SECRET_OPTION + 1, "so-upgrade", "", false, false){
			@Override
			public void doProcess(CLEnvironment env, String raw, String value) {
				Downloader downloader = new Downloader(value);
				boolean downloaded = downloader.download();
				env.setTerminate(downloaded ? 0 : 1);
			}
		});
		
		list.add(
			new SwitchParameter(SECRET_OPTION + 2, "so-emit-wrapper", "", false, false){
			@Override
			public void doProcess(CLEnvironment env, String raw, boolean value) {
				WrapperScriptUpdater updater = new WrapperScriptUpdater();
				int exitCode = updater.update();
				env.setTerminate(exitCode);
			}
		});
	}
	
	// Set the rest parameter here. 
	private RestParameter getRestParam() {
		// This will not overwrite the script file set by -f option
		return new RestParameter("script-file", false, true){
			@Override
			public void process(CLEnvironment env, List<String> values) throws CLParsingException {
				int size = values.size();
				int offset = 0;
				// If script file is not set and no snippet is provided, treat the first arg as the script file's path.
				// Otherwise, treat everything remaining as arguments.
				if (env.getScriptFile() == null 
					&& env.getScript() == null 
					&& size >= 1) {
					String filePath = values.get(0);
					env.setScriptFile(filePath);	
					offset++;
					size--;	
				}
				
				String[] args = new String[size];
				for (int i = 0; i < size; i++) {
					args[i] = values.get(i + offset);
				}
				
				env.setArguments(args);
			}
		};
	}
	
}
