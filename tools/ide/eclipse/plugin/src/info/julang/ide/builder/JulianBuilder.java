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

package info.julang.ide.builder;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.ui.progress.IProgressConstants2;

import info.julang.ide.Constants;
import info.julang.ide.properties.JulianScriptProperties;
import info.julang.ide.util.ResourceUtil;
import info.julang.interpretation.BadSyntaxException;
import info.julang.interpretation.errorhandling.IHasLocationInfoEx;
import info.julang.modulesystem.ModuleManager;
import info.julang.modulesystem.prescanning.RawScriptInfo;
import info.julang.parser.ANTLRParser;

/**
 * A builder that is invoked upon resources change inside a Julian project.
 * 
 * As of 0.0.1, this builder only performs syntax check.
 * 
 * @author Ming Zhou
 */
public class JulianBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = Constants.PLUGIN_ID + ".julianBuilder";

	public static final String MARKER_TYPE = Constants.PLUGIN_ID + ".julianProblem";
	
	@Override
	protected IProject[] build(
		int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		
		return null;
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		getProject().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
	}
	
	//--------------- Helper for triggering build programmatically ---------------//
	
	private static class ProjectBuildAction extends BuildAction {

		private IProject project;

		public ProjectBuildAction(IShellProvider shellProvider, int type, IProject project) {
			super(shellProvider, type);
			this.project = project;
		}

		@Override
		protected List<? extends IResource> getSelectedResources() {
			return Arrays.asList(this.project);
		}
	}
	
	public static void buildAll(IWorkbenchWindow window, IProject project) {
		WorkspaceJob cleanJob = new WorkspaceJob("Build all ...") {
			@Override
			public boolean belongsTo(Object family) {
				return ResourcesPlugin.FAMILY_MANUAL_BUILD.equals(family);
			}
			
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				ProjectBuildAction build = new ProjectBuildAction(
					window,
					IncrementalProjectBuilder.FULL_BUILD,
					project);
				
				build.runInBackground(
					ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
				
				return Status.OK_STATUS;
			}
		};
		
		cleanJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		cleanJob.setProperty(IProgressConstants2.SHOW_IN_TASKBAR_ICON_PROPERTY, Boolean.TRUE);
		cleanJob.schedule();
	}
	
	//--------------- Processing a Single File ---------------//

	private void process(IResource resource, ParsingLevel lvl) {
		if (lvl != ParsingLevel.LEXICAL 
			&& resource instanceof IFile 
			&& resource.getName().endsWith("." + Constants.FILE_EXT)) {
			
			deleteMarkers(resource);

			IFile file = (IFile) resource;
			if (JulianScriptProperties.isEnabledForParsing(file)) {
				try {
					if (lvl == ParsingLevel.SYNTAX) {
						InputStream text = file.getContents();
						ANTLRParser parser = new ANTLRParser("<unknown>", text, false);
						parser.parse(false, true);
					} else if (lvl == ParsingLevel.ADV_SYNTAX) {
						// We are not sure if this is a module file. So must parse without assuming the module name.
						RawScriptInfo rsinfo = ModuleManager.loadScriptInfoFromPath(null, ResourceUtil.getAbsoluteFSPath(file));
						BadSyntaxException bse = rsinfo.getAstInfo().getBadSyntaxException();
						if (bse != null) {
							this.report(file, bse.getMessage(), bse, true);
						}
					}
				} catch (BadSyntaxException bex) {
					this.report(file, bex.getMessage(), bex, true);
				} catch (CoreException | FileNotFoundException e) {
					// TODO - report a problem, but not mark
				}
			}
		}
	}

	private void deleteMarkers(IResource file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
			// Log?
		}
	}
	
	private void report(IFile file, String msg, IHasLocationInfoEx ex, boolean isErrorOrWarning) {
		try {
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, msg);
			marker.setAttribute(IMarker.SEVERITY, isErrorOrWarning ? IMarker.SEVERITY_ERROR : IMarker.SEVERITY_WARNING);
			
			// First try offset
			int offset = ex.getOffset();
			if (offset >= 0) {
				marker.setAttribute(IMarker.CHAR_START, offset);
				marker.setAttribute(IMarker.CHAR_END, offset + ex.getLength());
			} else {
				// If unavailable, try line number
				int lineNo = ex.getLineNumber();
				if (lineNo <= 0) {
					lineNo = 1;
				}
				marker.setAttribute(IMarker.LINE_NUMBER, lineNo);
			}
		} catch (CoreException e) {
			// Log?
		}
	}
	
	//--------------- Building strategy ---------------//

	private void fullBuild(IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		ParsingLevel lvl = ParsingLevel.loadFromProject(project, ParsingLevel.SYNTAX);
		project.accept(new JulianProjectVisitor(lvl));
	}

	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		delta.accept(new JulianProjectDeltaVisitor());
	}

	// Visitor for incremental build
	private class JulianProjectDeltaVisitor implements IResourceDeltaVisitor {
		
		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			IProject proj = resource.getProject();
			ParsingLevel lvl = ParsingLevel.loadFromProject(proj, ParsingLevel.SYNTAX);
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// Resource is added.
				process(resource, lvl);
				break;
			case IResourceDelta.REMOVED:
				// Resource is removed.
				// This doesn't affect syntax validity of the remaining files. So nothing to be done here.
				break;
			case IResourceDelta.CHANGED:
				// Resource is changed.
				process(resource, lvl);
				break;
			}
			
			// Return true to continue visiting children.
			return true;
		}
	}

	// Visitor for full build
	private class JulianProjectVisitor implements IResourceVisitor {
		
		private ParsingLevel lvl;
		
		private JulianProjectVisitor(ParsingLevel lvl) {
			this.lvl = lvl;
		}
		
		public boolean visit(IResource resource) {
			
			process(resource, lvl);
			
			// Return true to continue visiting children.
			return true;
		}
	}
}
