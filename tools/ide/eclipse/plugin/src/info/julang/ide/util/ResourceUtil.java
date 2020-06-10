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

package info.julang.ide.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;

import info.julang.ide.nature.JulianNature;

public final class ResourceUtil {

	/**
	 * Get resource from a structured selection that contains a single selection.
	 * 
	 * @param <T> The type to convert the selection to.
	 * @param ssel The selection. only one must be selected.
	 * @param clazz The Class instance of the type to convert the selection to.
	 * @return null if the selection is null, empty, multiple, or inconvertible.
	 */
	public static <T> T toSingleResource(IStructuredSelection ssel, Class<T> clazz) {
		if (ssel != null && ssel.size() > 0) {
			Object obj = ssel.getFirstElement();
			if (obj instanceof IAdaptable) {
				IAdaptable adp = (IAdaptable)obj;
				return adp.getAdapter(clazz);
			}
		}
		
		return null;
	}
	
	/**
	 * Get all projects of Julian nature.
	 */
    public static List<IProject> getJulianProjects(boolean openOnly) {
		List<IProject> jprojs = new ArrayList<IProject>();
		try {
	    	IWorkspaceRoot workspaceRoot = (IWorkspaceRoot) ResourcesPlugin.getWorkspace().getRoot();
	    	IProject[] projects = workspaceRoot.getProjects();
	    	for (IProject project : projects) {
	            if((!openOnly || project.isOpen()) 
	            	&& project.hasNature(JulianNature.NATURE_ID)) {
	            	jprojs.add(project);
	            }
	         }
	    } catch(CoreException ce) {
	    	// Ignore
	    }
	    
		return jprojs;
	}
    
	/**
	 * Get a resource's absolute file system path. 
	 * 
	 * Note while an IProject is an IResource, this method doesn't apply because IProject doesn't have a raw location.
	 * Therefore we have an overload of this method to handle the case of IProject.
	 */
	public static String getAbsoluteFSPath(IResource resource) {
		IPath path = resource.getRawLocation();
		return getAbsoluteFSPath(path);
	}
	
	/**
	 * Get a project's absolute file system path.
	 */
	public static String getAbsoluteFSPath(IProject project) {
		IPath path = ResourcesPlugin.getWorkspace().getRoot().findMember(project.getFullPath()).getLocation();
		return getAbsoluteFSPath(path);
	}
	
	private static String getAbsoluteFSPath(IPath path) {
		return path.makeAbsolute().toFile().getAbsolutePath();
	}
}
