1. This IDE plugin is developed on 2019/03 version of Eclipse.

2. Start developing.
	(1) The plugin has a dependency on JSE jar. Generate it and copy over.
		(1.1) from JSE root dir, mvn package
		(1.2) cp -f target/JSE-*.jar tools/ide/eclipse/plugin/lib/
	(2) Import this plugin project as an existing project into Eclipse
	
3. The JSE jar is referenced via buil.properties => bin.includes. When developing the IDE for the first time, 
   or whenever the JSE project itself has been updated, you must update the jar by following step 2.(1).
   
   If the JSE version is updated, must also reflect that change in buil.properties file.

4. When launching the plugin, you may encounter validation issues. 
To alleviate those, 
	(1) Go to Run Configurations, 
	(2) Select Plug-ins tab, then select "plugin-ins selected below only". 
	(3) Now deselect all, then select "info.julang.ide" (this plugin). Also select JDT.
	(4) Click on "Add Required Plug-ins" to automatically fill in the others. 
About 120+ plugins are selected this way instead of 500+ by default.

If still no luck, 
	(1) try to add whatever is reported missing from the available plugins.
	(2) try disable auto-validation prior to launch on the same configuration page.
	
5. To install the plugin to Eclipse:
	(1) Produce a JAR using the Export Wizard of PDE
	(2) Drop the JAR into Eclipse's plugins folder
	(3) Restart Eclipse to load the plugin. Then if you havn't done this before, add julianNature to the project. 
		Project => Properties => Project Natures => Add

========================================

Quick source pointers for each feature:

1. Source code editor, syntax highlighting:
info.julang.ide.editor

2. Source code parser:
info.julang.ide.builder

3. Run:
info.julang.ide.launcher
	- Run from the navigator (right click) or the script: JulianLaunchShortcut
	- Run from "Run Configurations": 
		- Logic: JulianLaunchConfigurationDelegate
		- UI: info.julang.ide.launcher.ui
	- Shared runner logic, including console IO etc.: JulianRunner
	
4. Properties pages:
- Per script file: JulianPropertyPage
- Per project file: (main) JulianProjectPropertyPage, (child) JulianProjectPropertyModulePathsPage

5. New Script wizard:
OpenNewJulianScriptWizardHandler
- open from the various menu commands: OpenNewJulianScriptWizardHandler
- ui: NewJulianScriptWizard and NewJulianScriptPage