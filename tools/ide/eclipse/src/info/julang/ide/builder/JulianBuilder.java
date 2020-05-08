package info.julang.ide.builder;

import java.io.InputStream;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import info.julang.ide.Constants;
import info.julang.ide.properties.JulianScriptProperties;
import info.julang.interpretation.BadSyntaxException;
import info.julang.interpretation.errorhandling.IHasLocationInfoEx;
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

	private static final String MARKER_TYPE = Constants.PLUGIN_ID + ".julianProblem";
	
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
	
	//--------------- Processing a Single File ---------------//

	private void process(IResource resource) {
		if (resource instanceof IFile && resource.getName().endsWith(".jul")) {
			deleteMarkers(resource);

			IFile file = (IFile) resource;
			if (JulianScriptProperties.isEnabledForParsing(file)) {
				try {
					// For now, parse only.
					InputStream text = file.getContents();
					ANTLRParser parser = new ANTLRParser("<unknown>", text, false);
					parser.parse(false, true);
				} catch (BadSyntaxException bex) {
					this.report(file, bex.getMessage(), bex, true);
				} catch (CoreException e) {
					// TODO
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
		getProject().accept(new JulianProjectVisitor());
	}

	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		delta.accept(new JulianProjectDeltaVisitor());
	}

	// Visitor for incremental build
	private class JulianProjectDeltaVisitor implements IResourceDeltaVisitor {
		
		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// Resource is added.
				process(resource);
				break;
			case IResourceDelta.REMOVED:
				// Resource is removed.
				// This doesn't affect syntax validity of the remaining files. So nothing to be done here.
				break;
			case IResourceDelta.CHANGED:
				// Resource is changed.
				process(resource);
				break;
			}
			
			// Return true to continue visiting children.
			return true;
		}
	}

	// Visitor for full build
	private class JulianProjectVisitor implements IResourceVisitor {
		
		public boolean visit(IResource resource) {
			process(resource);
			
			// Return true to continue visiting children.
			return true;
		}
	}
}
