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

package info.julang.ide.launcher;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import info.julang.ide.Constants;
import info.julang.ide.builder.JulianBuilder;
import info.julang.ide.nature.JulianNature;

/**
 * A tester used by plugin.xml to introduce customized predicates regarding script files in a Julian project.
 * <p>
 * All the tests would fail if any of the following condition is not met:<br/>
 * (1) The project to which the tested file belongs is not a Julian one (requiring the nature)<br/>
 * (2) The file being tested is not ended with ".jul"
 * 
 * @author Ming Zhou
 */
public class JulianScriptFileTester extends PropertyTester {

	private static final String PROP_HAS_LEGAL_SYNTAX = "hasLegalSyntax";
	
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        IFile file = (IFile)receiver;
        if (Constants.FILE_EXT.equals(file.getFileExtension())) {
        	IProject proj = file.getProject();
        	boolean isJulianProject = false;
        	
        	try {
				isJulianProject = (proj != null && proj.hasNature(JulianNature.NATURE_ID));
			} catch (CoreException e1) {
				// Ignore
			}
        	
        	if (!isJulianProject) {
        		return false;
        	}
        	
        	switch (property) {
        	case PROP_HAS_LEGAL_SYNTAX:
        		// This test is not really preventing the user from running an illegal script - 
        		// he can always delete the marks or run without save. In either case JSE would 
        		// fail very early reporting the syntax error. So we are just doing them a favor.
				try {
					IMarker[] markers = file.findMarkers(JulianBuilder.MARKER_TYPE, true, IResource.DEPTH_ZERO);
	        		return markers == null || markers.length == 0;
				} catch (CoreException e) {
					// Let it go.
					return true;
				}
			default:
	        	// Default other properties to true
		        return true;
        	}
        }
        
        // If it's not a .jul file, categorically fail the test.
        return false;
	}

}
