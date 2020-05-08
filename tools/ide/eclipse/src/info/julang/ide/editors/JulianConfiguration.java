package info.julang.ide.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class JulianConfiguration extends SourceViewerConfiguration {
	
	private JulianScanner scanner;
	private ColorManager colorManager;
	
	private final static String[] ContentTypes = new String[] {
		IDocument.DEFAULT_CONTENT_TYPE,
		JulianPartitionScanner.JULIAN_COMMENT,
		JulianPartitionScanner.JULIAN_STRING_LITERAL,
		JulianPartitionScanner.JULIAN_CHAR_LITERAL,
		JulianPartitionScanner.JULIAN_REGEX_LITERAL
	};

	public JulianConfiguration(ColorManager colorManager) {
		this.colorManager = colorManager;
	}

	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return ContentTypes.clone();
	}

	protected JulianScanner getJulianScanner() {
		if (scanner == null) {
			scanner = new JulianScanner(colorManager);
		}
		
		return scanner;
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getJulianScanner());
		
		for (String ct : ContentTypes) {
			reconciler.setDamager(dr, ct);
			reconciler.setRepairer(dr, ct);
		}

		return reconciler;
	}

}