package info.julang.ide.properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import info.julang.ide.Constants;

public final class JulianScriptProperties {
	
	private JulianScriptProperties() { }
	
	static final String ENABLED_BOOL_PROPERTY = "ENABLED_FOR_PARSING";
	static final String ENABLED_BOOL_VALUE_DISABLED = "false";
	static final String ENABLED_BOOL_VALUE_DEFAULT = "";
	
	public static boolean isEnabledForParsing(IResource resource) {
		try {
			String enabledStr = resource
				.getPersistentProperty(new QualifiedName(Constants.PLUGIN_ID, ENABLED_BOOL_PROPERTY));
			
			if (ENABLED_BOOL_VALUE_DISABLED.equals(enabledStr)) {
				return false;
			}
		} catch (CoreException e) {
			// Log?
		}
		
		// Default to true
		return true;
	}
}
