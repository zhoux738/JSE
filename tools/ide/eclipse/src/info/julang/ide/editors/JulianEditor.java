package info.julang.ide.editors;

import org.eclipse.ui.editors.text.TextEditor;

public class JulianEditor extends TextEditor {

	private ColorManager colorManager;

	public JulianEditor() {
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new JulianConfiguration(colorManager));
		setDocumentProvider(new JulianDocumentProvider());
	}
	
	@Override
	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}

}
