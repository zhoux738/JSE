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

package info.julang.ide.editors.folding;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;

public class FoldableRegion implements Comparable<FoldableRegion> {

	private Position pos;
	private ProjectionAnnotation anno;
	private RegionType type;
	
	public static FoldableRegion fromList(RegionType type, List<? extends ParserRuleContext> rules) {
		return new FoldableRegion(type, rules.get(0), rules.get(rules.size() - 1));
	}
	
	public static FoldableRegion fromNode(RegionType type, ParserRuleContext cntx) {
		return new FoldableRegion(type, cntx, cntx);
	}
	
	private FoldableRegion(RegionType type, ParserRuleContext start, ParserRuleContext end) {
		int startIndex = start.start.getStartIndex();
		int stopIndex = end.stop.getStopIndex();
		this.pos = new Position(startIndex, stopIndex - startIndex);
		this.type = type;
	}
	
	public Position getPosition() {
		return pos;
	}
	
	public ProjectionAnnotation getAnnotation() { // In future, we may override this in subclasses
		if (anno == null) {
			this.anno = new ProjectionAnnotation();
		}
		
		return anno;
	}
	
	public RegionType getRegionType() {
		return type;
	}

	//-------- Object --------//
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pos == null) ? 0 : pos.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FoldableRegion other = (FoldableRegion) obj;
		if (pos == null) {
			if (other.pos != null)
				return false;
		} else if (!pos.equals(other.pos))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
	
	//-------- Comparable<T> --------//

	@Override
	public int compareTo(FoldableRegion another) {
		if (pos.offset < another.pos.offset) {
			return -1;
		}
		
		if (pos.offset > another.pos.offset) {
			return 1;
		}
		
		if (pos.length < another.pos.length) {
			return -1;
		}
		
		if (pos.length > another.pos.length) {
			return -1;
		}
		
		return 0;
	}
}
