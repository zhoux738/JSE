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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.progress.IProgressConstants2;

import info.julang.ide.Constants;
import info.julang.ide.JulianPlugin;
import info.julang.ide.editors.JulianEditor;
import info.julang.ide.properties.JulianScriptProperties;
import info.julang.ide.util.ResourceUtil;
import info.julang.interpretation.BadSyntaxException;
import info.julang.interpretation.errorhandling.IHasLocationInfoEx;
import info.julang.modulesystem.ModuleManager;
import info.julang.modulesystem.prescanning.RawScriptInfo;
import info.julang.parser.ANTLRParser;
import info.julang.parser.LazyAstInfo;

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
	
	private static final String EXTNAME = "." + Constants.FILE_EXT;
	
	@Override
	protected IProject[] build(
		int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		IProject proj = getProject();
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(proj);
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(proj, delta, monitor);
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
	
	/**
	 * Build all files with project-specific configuration.
	 * 
	 * @param window
	 * @param project
	 */
	public static void buildAll(IWorkbenchWindow window, IProject project) {
		WorkspaceJob buildAllJob = new WorkspaceJob("Build all ...") {
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
		
		buildAllJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		buildAllJob.setProperty(IProgressConstants2.SHOW_IN_TASKBAR_ICON_PROPERTY, Boolean.TRUE);
		buildAllJob.schedule();
	}
	
	/**
	 * Build a single file with maximum parsing level.
	 * 
	 * @param file
	 * @param continuation the action to perform after a successful build.
	 */
	public static void buildSingle(IFile file, Runnable continuation) {
		WorkspaceJob parseSingleJob = new WorkspaceJob("Parsing ...") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				parse(file, ParsingLevel.ADV_SYNTAX);
				
				continuation.run();
				
				return Status.OK_STATUS;
			}
		};
		
		parseSingleJob.schedule();
	}
	
	//--------------- Processing a Single File ---------------//

	private IFile process(IResource resource, ParsingLevel lvl) {
		if (resource instanceof IFile 
			&& resource.getName().endsWith("." + Constants.FILE_EXT)) {
			IFile file = (IFile) resource;
			
			return parse(file, lvl);
		}
		
		// Not a file
		return null;
	}
	
	/**
	 * Parse the Julian file and mark problems. Store AST if enabled.
	 * <p>
	 * May get the parsing level from project-specific configuration with the following code:
	 * <pre><code>
	 *   ParsingLevel lvl = ParsingLevel.loadFromProject(proj, ParsingLevel.SYNTAX);
	 * </code></pre>
	 * 
	 * @param file
	 * @param lvl The parsing level.
	 * @return
	 */
	private static IFile parse(IFile file, ParsingLevel lvl) {
		if (lvl != ParsingLevel.LEXICAL) {
			deleteMarkers(file);

			if (JulianScriptProperties.isEnabledForParsing(file)) {
				try {
					if (lvl == ParsingLevel.SYNTAX) {
						InputStream text = file.getContents();
						ANTLRParser parser = new ANTLRParser("<unknown>", text, false);
						parser.parse(false, true);
					} else if (lvl == ParsingLevel.ADV_SYNTAX) {
						// We are not sure if this is a module file. So must parse without assuming the module name.
						RawScriptInfo rsinfo = ModuleManager.loadScriptInfoFromPath(null, ResourceUtil.getAbsoluteFSPath(file));
						LazyAstInfo ainfo = rsinfo.getAstInfo();
						BadSyntaxException bse = ainfo.getBadSyntaxException();
						JulianPlugin.getASTRepository().put(file, ainfo);
						if (bse != null) {
							report(file, bse.getMessage(), bse, true);
						} else {
							return file;
						}
					}
				} catch (BadSyntaxException bex) {
					report(file, bex.getMessage(), bex, true);
				} catch (CoreException | FileNotFoundException e) {
					// TODO - report a problem, but not mark
				}
			}
		}
		
		return null;
	}

	private static void deleteMarkers(IResource file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
			// Log?
		}
	}
	
	private static void report(IFile file, String msg, IHasLocationInfoEx ex, boolean isErrorOrWarning) {
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
		
		JulianProjectVisitor jpv = new JulianProjectVisitor(lvl);
		project.accept(jpv);
		
		JulianPlugin.getASTRepository().removeAllExcept(jpv.filesBuilt);

		// Only explicitly update the folding regions of the active editor.
		if (jpv.filesBuilt.size() > 0) {
			refreshActiveEditor(file -> {
				return jpv.filesBuilt.contains(file);
			});
		}
	}

	private void incrementalBuild(IProject proj, IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		JulianProjectDeltaVisitor visitor = new JulianProjectDeltaVisitor(proj);
		delta.accept(visitor);
		
		List<IFile> filesBuilt = visitor.filesBuilt;
		List<IFile> filesRemoved = visitor.filesRemoved;
		
		if (filesRemoved != null) {
			JulianPlugin.getASTRepository().removeAll(filesRemoved);
		}
		
		// Only explicitly update the folding regions of the active editor.
		if (filesBuilt != null) {
			refreshActiveEditor(file -> {
				return filesBuilt.stream().anyMatch(f -> f.equals(file));
			});
		}
	}
	
	private void refreshActiveEditor(Predicate<IFile> p) {
		Display disp = Display.getDefault();
		disp.asyncExec(() -> {
			// Get the active editor.
			IEditorPart edPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			if (edPart != null && edPart instanceof JulianEditor) {
				IEditorInput input = edPart.getEditorInput();
				if (input instanceof FileEditorInput) {
					FileEditorInput fei = (FileEditorInput)input;
					IFile file = fei.getFile();
					// If it's a Julian script file which just got built,
					if (file.getName().endsWith(EXTNAME) 
						&& p.test(file)) {
						// ... update its folding regions.
						((JulianEditor)edPart).updateFoldingRegions(false);
					}
				}
			}
		});
	}

	// Visitor for incremental build
	private class JulianProjectDeltaVisitor implements IResourceDeltaVisitor {
		private List<IFile> filesBuilt;
		private List<IFile> filesRemoved;
		
		private JulianProjectDeltaVisitor(IProject proj) {
			if (JulianPlugin.getASTRepository().isEnabledFor(proj)) {
				filesRemoved = new ArrayList<IFile>();
			}
		}
		
		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			IProject proj = resource.getProject();
			ParsingLevel lvl = ParsingLevel.loadFromProject(proj, ParsingLevel.SYNTAX);
			switch (delta.getKind()) {
			case IResourceDelta.REMOVED:
				// Resource is removed.
				if (filesRemoved != null && resource instanceof IFile) {
					filesRemoved.add((IFile)resource);
				}
				
				break;
			case IResourceDelta.ADDED:
			case IResourceDelta.CHANGED:
				// Resource is added/changed.
				IFile file = process(resource, lvl);
				if (file != null) {
					if (filesBuilt == null) {
						filesBuilt = new ArrayList<IFile>();
					}
					filesBuilt.add(file);
				}
				
				break;
			}
			
			// Return true to continue visiting children.
			return true;
		}
	}

	// Visitor for full build
	private class JulianProjectVisitor implements IResourceVisitor {
		
		private ParsingLevel lvl;
		private Set<IFile> filesBuilt;
		
		private JulianProjectVisitor(ParsingLevel lvl) {
			this.lvl = lvl;
			this.filesBuilt = new HashSet<>();
		}
		
		public boolean visit(IResource resource) {
			IFile f = process(resource, lvl);
			if (f != null) {
				this.filesBuilt.add(f);
			}
			
			// Return true to continue visiting children.
			return true;
		}
	}
}
