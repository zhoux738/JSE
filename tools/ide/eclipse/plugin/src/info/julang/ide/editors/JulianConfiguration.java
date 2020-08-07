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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

import info.julang.ide.editors.partitioning.JulianPartitionScanner;
import info.julang.ide.editors.scanning.FontStyle;
import info.julang.ide.editors.scanning.JulianSourceScanner;
import info.julang.ide.editors.scanning.SingleTokenScanner;
import info.julang.ide.themes.IColorManager;

public class JulianConfiguration extends SourceViewerConfiguration {
	
	private JulianSourceScanner scanner;
	private SingleTokenScanner commentScanner;
	private SingleTokenScanner blockCommentScanner;
	private SingleTokenScanner charScanner;
	private SingleTokenScanner stringScanner;
	private IColorManager colorManager;
	
	private final static String[] ContentTypes = new String[] {
		IDocument.DEFAULT_CONTENT_TYPE,
		JulianPartitionScanner.JULIAN_BLOCK_COMMENT,
		JulianPartitionScanner.JULIAN_COMMENT,
		JulianPartitionScanner.JULIAN_STRING_LITERAL,
		JulianPartitionScanner.JULIAN_CHAR_LITERAL,
		//JulianPartitionScanner.JULIAN_REGEX_LITERAL
	};

	public JulianConfiguration(IColorManager colorManager) {
		this.colorManager = colorManager;
	}

	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return ContentTypes.clone();
	}

	protected JulianSourceScanner getJulianScanner(ISourceViewer srcViewer) {
		if (scanner == null) {
			scanner = new JulianSourceScanner(colorManager, srcViewer);
			scanner.initialze();
		}
		
		return scanner;
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();
		
		if (commentScanner == null) {
			commentScanner = new SingleTokenScanner(colorManager, sourceViewer, FontStyle.COMMENT);
			commentScanner.initialze();
			blockCommentScanner = new SingleTokenScanner(colorManager, sourceViewer, FontStyle.COMMENT);
			blockCommentScanner.initialze();
			stringScanner = new SingleTokenScanner(colorManager, sourceViewer, FontStyle.LITERAL);
			stringScanner.initialze();
			charScanner = new SingleTokenScanner(colorManager, sourceViewer, FontStyle.LITERAL);
			charScanner.initialze();
		}
		
		addPartitionScanner(reconciler, IDocument.DEFAULT_CONTENT_TYPE, getJulianScanner(sourceViewer));
		addPartitionScanner(reconciler, JulianPartitionScanner.JULIAN_BLOCK_COMMENT, blockCommentScanner);
		addPartitionScanner(reconciler, JulianPartitionScanner.JULIAN_COMMENT, commentScanner);
		addPartitionScanner(reconciler, JulianPartitionScanner.JULIAN_STRING_LITERAL, stringScanner);
		addPartitionScanner(reconciler, JulianPartitionScanner.JULIAN_CHAR_LITERAL, charScanner);

		return reconciler;
	}
	
	private void addPartitionScanner(PresentationReconciler reconciler, String contentType, ITokenScanner scanner) {
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(scanner);
		reconciler.setDamager(dr, contentType);
		reconciler.setRepairer(dr, contentType);
	}

	// This is to allow the markers at the left margin of the editor to show the message when being hovered over by mouse cursor.
	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
	    return new DefaultAnnotationHover();
	}
	
	public void dispose() {
		if (scanner == null) {
			scanner.dispose();
		}
	}
}