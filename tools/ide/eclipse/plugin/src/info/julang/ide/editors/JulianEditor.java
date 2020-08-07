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

package info.julang.ide.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

import info.julang.ide.JulianPlugin;
import info.julang.ide.builder.JulianBuilder;
import info.julang.ide.builder.ParsingLevel;
import info.julang.ide.editors.folding.FoldingManager;
import info.julang.ide.themes.ColorManagerFactory;
import info.julang.ide.themes.IColorManager;
import info.julang.ide.themes.SharedColorManager;
import info.julang.ide.themes.ThemeChangeListener;
import info.julang.parser.LazyAstInfo;

/**
 * The source editor for Julian.
 * 
 * @author Ming Zhou
 */
public class JulianEditor extends TextEditor {
	
	private static final String CONTEXT_NAME = "#JulianEditorContext";
	
	private static final String[] KEY_BINDINGS = new String[] { "info.julang.editors.binding.context" };
	
	private IColorManager colorManager;
	private JulianConfiguration config;
	private EditorThemeChangeListener listener;
	private FoldingManager foldingMgr;

	private class EditorThemeChangeListener implements IPropertyChangeListener {
		
		@Override
		public void propertyChange(PropertyChangeEvent p) {
			if (IWorkbenchPreferenceConstants.CURRENT_THEME_ID.equals(p.getProperty())){
				Object val = p.getNewValue();
				if (val instanceof String) {
					String sval = (String)val;
					if (sval.startsWith(ThemeChangeListener.JULIAN_THEME_ID_PREFIX)) {
						IThemeManager mgr = PlatformUI.getWorkbench().getThemeManager();
						ITheme theme = mgr.getCurrentTheme();
						SharedColorManager.Instance.setColorRegistry(theme.getColorRegistry());
					}
				}
			}
		}
	}
	
	public JulianEditor() {
		super();

		colorManager = ColorManagerFactory.getColorManager();
		
		setSourceViewerConfiguration(
			config = new JulianConfiguration(colorManager));
		
		setDocumentProvider(new JulianDocumentProvider());
		
		PlatformUI.getPreferenceStore().addPropertyChangeListener(
			listener = new EditorThemeChangeListener());
	}
	
	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		setEditorContextMenuId(CONTEXT_NAME);
	}
	
	@Override
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(KEY_BINDINGS);
	}
	
	@Override
	public void dispose() {
		try {
			config.dispose();
		} catch (Exception e) {
			// Continue to dispose the next resource
			// LOG ("Failed at disposing of config");
		}
		
		try {
			colorManager.dispose();
		} catch (Exception e) {
			// Continue to dispose the next resource
			// LOG ("Failed at disposing of colorManager");
		}
		
		try {
			PlatformUI.getPreferenceStore().removePropertyChangeListener(listener);
		} catch (Exception e) {
			// Continue to dispose the next resource
			// LOG ("Failed at removing listeners from PlatformUI's PreferenceStore()");
		}
		
		super.dispose();
	}
	
	// The following code are for supporting folding.
	
	public void updateFoldingRegions(boolean forceBuild) {
		IFile file = ((FileEditorInput)this.getEditorInput()).getFile();
		LazyAstInfo ainfo = JulianPlugin.getASTRepository().get(file);

		// We can do this only if the file has been built successfully, with its AST stored (ParsingLevel >= ADV_SYNTAX).
		// If the file has not been built, build it now.
		if (ainfo == null
			&& forceBuild
			&& ParsingLevel.loadFromProject(file.getProject(), ParsingLevel.SYNTAX).ordinal() >= ParsingLevel.ADV_SYNTAX.ordinal()) {
			JulianBuilder.buildSingle(file, () -> { 
				LazyAstInfo ainfoNew = JulianPlugin.getASTRepository().get(file);
				if (ainfoNew != null) {
					this.foldingMgr.update(ainfoNew);
				}
			});
		} else {
			if (ainfo != null) {
				this.foldingMgr.update(ainfo);
			}
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();
		ProjectionSupport projectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
		foldingMgr = new FoldingManager(viewer, projectionSupport);
		this.updateFoldingRegions(true);
	}

	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		ISourceViewer viewer = new ProjectionViewer(
			parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);

		getSourceViewerDecorationSupport(viewer);

		return viewer;
	}
}
