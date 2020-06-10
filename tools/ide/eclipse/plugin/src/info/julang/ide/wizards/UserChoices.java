/*
MIT License

Copyright (c) 2020 Ming Zhou

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

package info.julang.ide.wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import info.julang.ide.Constants;

/**
 * Preserve user's last choices, used for auto-fill upon opening new wizard.
 */ 
class UserChoices {

	private static final QualifiedName LAST_SET_NEW_FILE_KIND_PROPERTY = new QualifiedName(Constants.PLUGIN_ID, "NJSP_LAST_SET_NEW_FILE_KIND");
	private static final QualifiedName LAST_SET_CONTAINER_PATH_PROPERTY = new QualifiedName(Constants.PLUGIN_ID, "NJSP_LAST_SET_CONTAINER_PATH");
	private static final QualifiedName LAST_SET_MODULE_PATH_PROPERTY = new QualifiedName(Constants.PLUGIN_ID, "NJSP_LAST_SET_MODULE_PATH");
	private static final QualifiedName LAST_SET_MODULE_NAME_PROPERTY = new QualifiedName(Constants.PLUGIN_ID, "NJSP_LAST_SET_MODULE_NAME");
	private static final QualifiedName LAST_SET_NEW_TYPE_PROPERTY = new QualifiedName(Constants.PLUGIN_ID, "NJSP_LAST_SET_NEW_TYPE");
	
	private IContainer moduleBaseDir;
	private IContainer containerDir;
	private String moduleName;
	private String fileName;
	private ScriptType scriptType;
	private JulianType julianType;
	
	private IProject project;
	
	UserChoices(){ }

	void loadFromSession(IProject project) {
		if (project == null) {
			return;
		}
		
		try {
			scriptType = (ScriptType)project.getSessionProperty(LAST_SET_NEW_FILE_KIND_PROPERTY);
			containerDir = (IContainer)project.getSessionProperty(LAST_SET_CONTAINER_PATH_PROPERTY);
			moduleBaseDir = (IContainer)project.getSessionProperty(LAST_SET_MODULE_PATH_PROPERTY);
			moduleName = (String)project.getSessionProperty(LAST_SET_MODULE_NAME_PROPERTY);
			julianType = (JulianType)project.getSessionProperty(LAST_SET_NEW_TYPE_PROPERTY);
			
			this.project = project;
		} catch (CoreException e) {
			// Ignore
		}
	}
	
	void saveToSession() {
		if (project != null) {
			try {
				project.setSessionProperty(LAST_SET_NEW_FILE_KIND_PROPERTY, scriptType);
				project.setSessionProperty(LAST_SET_CONTAINER_PATH_PROPERTY, containerDir);
				project.setSessionProperty(LAST_SET_MODULE_PATH_PROPERTY, moduleBaseDir);
				project.setSessionProperty(LAST_SET_MODULE_NAME_PROPERTY, moduleName);
				project.setSessionProperty(LAST_SET_NEW_TYPE_PROPERTY, julianType);
			} catch (CoreException e) {
				// Ignore
			}
		}
	}

	public IContainer getModuleBaseDir() {
		return moduleBaseDir;
	}

	void setModuleBaseDir(IContainer moduleBaseDir) {
		this.moduleBaseDir = moduleBaseDir;
	}

	public IContainer getContainerDir() {
		return containerDir;
	}

	void setContainerDir(IContainer containerDir) {
		this.containerDir = containerDir;
	}

	public String getModuleName() {
		return moduleName;
	}

	void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public ScriptType getScriptType() {
		return scriptType;
	}

	void setScriptType(ScriptType scriptType) {
		this.scriptType = scriptType;
	}

	public JulianType getJulianType() {
		return julianType;
	}

	void setJulianType(JulianType julianType) {
		this.julianType = julianType;
	}
	
	public String getFileName() {
		return fileName;
	}

	void setFileName(String fileName) {
		this.fileName = fileName;
	}	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(scriptType == ScriptType.LOOSE 
			? "Loose script:" 
			: (scriptType == ScriptType.MODULE ? "Module script:" : "Script type unspecified:"));
		sb.append(System.lineSeparator());
		
		if (scriptType == ScriptType.LOOSE) {
			sb.append("Directory: ");
			if (containerDir != null) {
				sb.append(containerDir.getFullPath());
			}
		} else if (scriptType == ScriptType.MODULE) {
			sb.append("Root:      ");
			if (moduleBaseDir != null) {
				sb.append(moduleBaseDir.getFullPath());
			}
			sb.append(System.lineSeparator());
			sb.append("Module:    ");
			sb.append(moduleName);
			sb.append(System.lineSeparator());
			sb.append("Type:      ");
			if (julianType != null) {
				sb.append(julianType.name());
			}
		}
		
		sb.append(System.lineSeparator());
		sb.append("File:      ");
		sb.append(fileName);
		
		return sb.toString();
	}
}

enum JulianType {
	Class,
	Interface,
	Enum,
	Attribute;
	
	@Override
	public String toString() {
		return name();
	}
}

enum ScriptType {
	MODULE("module file"),
	LOOSE("non-module file");

	private String text;
	
	private ScriptType(String text) {
		this.text = text;
	}
	
	@Override
	public String toString() {
		return text;
	}
}
