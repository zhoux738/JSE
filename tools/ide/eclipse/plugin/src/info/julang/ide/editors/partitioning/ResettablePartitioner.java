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

package info.julang.ide.editors.partitioning;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

import info.julang.util.Pair;

/**
 * A slightly modified partitioner that reacts to certain change pattern with a more cache-wise conservative approach.
 * <p>
 * This combination of FastPartitioner and RuleBasedPartitionScanner has many limits. One of them is parsing code change that involves 
 * multiple lines with mixed rule-satisfying texts. See IMPLEMENTATION NOTES in the source code of this class for more details.
 * 
 * @author Ming Zhou
 */
public class ResettablePartitioner extends FastPartitioner {

	// IMPLEMENTATION NOTES
	//
	// Consider the initial text, '_' representing the caret:
	// 
	// /*
	// string s = "abc";
	// *_
	// 
	// Now the user types a single character: '/'. The common expectation is that because a block comment 
	// is now complete, the whole section should be rendered as commented out. In reality, however, no color
	// change happens. Essentially this is caused by these facts:
	// (1) Before input, RuleBasedPartitionScanner had found only one non-default token: the string literal 
	//     ("abc"), and treated the text before and after as two default tokens.
	// (2) When '/' is typed, a document event is fired that points at the offset 20 (where the caret was 
	//     resting) with the new text == '/'. This triggers re-partitioning. But FastPartitioner on performs
	//     the partitioning with the known non-default token ("abc") as a boundary and scans each text section 
	//     separately. In other words it doesn't re-evaluate the file holistically, therefore failing to see 
	//     the new pattern /* ... */
	// 
	// Production-class plugins, such as JDT, ditches these rudimentary classes and ships with advanced and 
	// intricate partitioning logic. Due to resource constraints JuDE for now can only offer this patch-based
	// alternative. In the future we will consider rolling up with our own parser.
	
	public ResettablePartitioner(IPartitionTokenScanner scanner, String[] legalContentTypes) {
		super(scanner, legalContentTypes);
	}
	
	@Override
	public IRegion documentChanged2(DocumentEvent e) {
		// If the change shows certain pattern, rewrite the event for this occurrence only.
		if (e.getLength() == 0 && "/".equals(e.getText())) {
			try {
				// Narrow down the condition
				Pair<Boolean, IRegion> pair = getBlockCommentRegion(e);
				if (pair.getFirst()) {
					return pair.getSecond();
				}
			} catch (BadLocationException blex) {
				// Ignore
				blex.printStackTrace();
			}
		}
		
		return super.documentChanged2(e);
	}
	
	private Pair<Boolean, IRegion> getBlockCommentRegion(DocumentEvent e) throws BadLocationException {
		IDocument doc = e.getDocument();
		int offset = e.getOffset();
		if (offset >= 1 && doc.getChar(offset - 1) == '*') {
			// (1) the previous char is *
			boolean ready = false;
			boolean found = false;
			int ind = offset - 2;
			for (;ind >= 0; ind--) { // This doesn't handle the case of /* ... */ ... */, but should be fine.
				char c = doc.getChar(ind);
				if (c == '*') {
					ready = true;
				} else if (c == '/' && ready) {
					found = true;
					break;
				} else {
					ready = false;
				}
			}
			
			if (found) {
				// (2) led by "/*"
				
				// Store the event's original args
				String orgText = e.fText;
				int orgOffset = e.fOffset;
				int orgLen = e.fLength;
				
				// Fabricate the new args
				e.fText = e.getDocument().get(ind, offset - ind);
				e.fOffset = ind;
				e.fLength = offset - ind;
				
				try {
					return new Pair<Boolean, IRegion>(
						true,
						super.documentChanged2(e)); // Could be null
				} finally {
					// Restore the event no matter what. Other listeners must see the original event.
					e.fText = orgText;
					e.fLength = orgLen;
					e.fOffset = orgOffset;
				}
			}
		}
		
		return new Pair<Boolean, IRegion>(false, null);
	}

}
