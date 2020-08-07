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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import info.julang.ide.editors.partitioning.JulianPartitionScanner;

/**
 * Toggle source lines with comment prefixes (//).
 * 
 * @author Ming Zhou
 */
public class ToggleCommentHandler extends AbstractHandler {

	// IMPLEMENTATION NOTES:
	// Partly based on JDT's ToggleCommentAction, but refactored into a Command Handler with simplified check logic.
	// https://github.com/eclipse/eclipse.jdt.ui/blob/master/org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/javaeditor/ToggleCommentAction.java
	
	private static final String[] PREFIXES = new String[] {"//"};

	private ITextOperationTarget m_opTarget;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get editor from the event
		IEditorPart epart = HandlerUtil.getActiveEditor(event);
		if (epart == null || !(epart instanceof ITextEditor)) {
			return null;
		}
		
		ITextEditor editor = (ITextEditor)epart;
		m_opTarget = editor.getAdapter(ITextOperationTarget.class);
		if (m_opTarget == null) {
			return null;
		}
		
		// Get the selected text and decide what to do with the toggling
		ISelection selection = editor.getSelectionProvider().getSelection();
		int operationCode = isSelectionCommented(editor, selection)
			? ITextOperationTarget.STRIP_PREFIX
			: ITextOperationTarget.PREFIX;

		// Configure editor to enable operations
		if (!m_opTarget.canDoOperation(operationCode)) {
			setPrefixes();
		}

		Shell shell = editor.getSite().getShell();
		if (shell != null && !shell.isDisposed()) {
			Display display= shell.getDisplay();
			BusyIndicator.showWhile(
				display, 
				new Runnable() {
					@Override
					public void run() {
						m_opTarget.doOperation(operationCode);
					}
				});
		}
		
		// Required
		return null;
	}
	
	// Must set default prefixes to enable text operation.
	private void setPrefixes() {
		ITextViewer vwr = ((ITextViewer)m_opTarget);
		vwr.setDefaultPrefixes(PREFIXES, IDocument.DEFAULT_CONTENT_TYPE);
		vwr.setDefaultPrefixes(PREFIXES, JulianPartitionScanner.JULIAN_STRING_LITERAL);
		vwr.setDefaultPrefixes(PREFIXES, JulianPartitionScanner.JULIAN_CHAR_LITERAL);
		vwr.setDefaultPrefixes(PREFIXES, JulianPartitionScanner.JULIAN_BLOCK_COMMENT);
		vwr.setDefaultPrefixes(PREFIXES, JulianPartitionScanner.JULIAN_COMMENT);
	}
	
	// Check if the lines the selection occupies are ALL commented out. Returns false if there is at least one
	// line that is not commented out.
	private boolean isSelectionCommented(ITextEditor editor, ISelection selection) {
		if (!(selection instanceof ITextSelection)) {
			return false;
		}

		ITextSelection textSelection = (ITextSelection) selection;
		if (textSelection.getStartLine() < 0 || textSelection.getEndLine() < 0) {
			return false;
		}

		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());

		try {
			int startLine = document.getLineOfOffset(textSelection.getOffset());
			int endLine = document.getLineOfOffset(textSelection.getOffset() + textSelection.getLength());

			for (int i = startLine; i <= endLine; i++) {
				IRegion reg = document.getLineInformation(startLine);
				int length = reg.getLength();
				int offset = reg.getOffset();
				int max = offset + length;
				boolean ready = false;
				boolean isCommented = false;
				
				INLINE_SEARCH:
				while(offset < max) {
					switch (document.getChar(offset)) {
					case ' ':
					case '\t':
					case '\n':
					case '\r':
						// Ignore any blank chars
						offset++;
						continue;
					case '/':
						if (!ready) {
							// First '/'
							ready = true;
							offset++;
							continue;
						} else {
							// Two consecutive '/'s. This line is commented.
							isCommented = true;
						}
						// fall thru
					default:
						break INLINE_SEARCH;
					}
				}
				
				if (!isCommented) {
					return false;
				}
			}
		} catch (BadLocationException x) {
			// IGNORE
		}

		return true;
	}
}
