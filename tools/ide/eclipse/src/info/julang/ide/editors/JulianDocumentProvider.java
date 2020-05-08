package info.julang.ide.editors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

/**
 * Provides Julian source file as an partitioned IDocument. 
 * The partition boundaries include comments and string literals.
 * 
 * @author Ming Zhou
 */
public class JulianDocumentProvider extends FileDocumentProvider {

	@Override
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if (document != null) {
			IDocumentPartitioner partitioner = new FastPartitioner(
				new JulianPartitionScanner(),
				new String[] { 
					JulianPartitionScanner.JULIAN_COMMENT, 
					JulianPartitionScanner.JULIAN_STRING_LITERAL,
					JulianPartitionScanner.JULIAN_CHAR_LITERAL,
					JulianPartitionScanner.JULIAN_REGEX_LITERAL
				});
			
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		
		return document;
	}
}