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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

import info.julang.langspec.ast.JulianParser;
import info.julang.langspec.ast.JulianParser.DeclarationsContext;
import info.julang.langspec.ast.JulianParser.ExecutableContext;
import info.julang.langspec.ast.JulianParser.Import_statementContext;
import info.julang.langspec.ast.JulianParser.PreambleContext;
import info.julang.langspec.ast.JulianParser.ProgramContext;
import info.julang.langspec.ast.JulianParser.StatementContext;
import info.julang.langspec.ast.JulianParser.Statement_listContext;
import info.julang.langspec.ast.JulianParser.Type_declarationContext;
import info.julang.parser.LazyAstInfo;

/**
 * Track all of the foldable regions on a projection viewer.
 * 
 * @author Ming Zhou
 */
public class FoldingManager {

	private ProjectionAnnotationModel model;
	
	private TreeSet<FoldableRegion> oldRegions;
	
	private TreeSet<FoldableRegion> newRegions;
	
	public FoldingManager(ProjectionViewer viewer, ProjectionSupport support) {
		support.install();

		viewer.doOperation(ProjectionViewer.TOGGLE);

		model = viewer.getProjectionAnnotationModel();
	}
	
	/**
	 * Perform the updates to the underlying annotation model associated with this editor. All regions with same
	 * range and type to anyone that has been added via {@link #add(FoldableRegion)} will be preserved, while others 
	 * will be removed. A region that does not find a match from the previous region collection will be added.
	 */
	public void update(LazyAstInfo ainfo) {
		ProgramContext pc = ainfo.getAST();
		if (pc == null) {
			// The parsing failed. Do not update.
			return;
		}
		
		newRegions = new TreeSet<>();
		updateFoldingRegionsInternal(pc);
		
		@SuppressWarnings("unchecked")
		TreeSet<FoldableRegion> toDelete = oldRegions != null 
			? (TreeSet<FoldableRegion>) oldRegions.clone()
			: new TreeSet<FoldableRegion>();
			
		List<FoldableRegion> toAdd = new ArrayList<FoldableRegion>();
		
		for(FoldableRegion reg : newRegions) {
			if (toDelete.remove(reg)) {
				// If a previous region exists with the exactly same range and type, consider it to be unchanged.
				// We may revisit this logic in the future if we also want to differentiate collapsed regions 
				// based on the contents.
				continue;
			} else {
				toAdd.add(reg);
			}
		}
		
		int size = toDelete.size();
		Annotation[] annosToDelete = new Annotation[size];
		int index = 0;
		for (FoldableRegion reg : toDelete) {
			annosToDelete[index] = reg.getAnnotation();
			index++;
		}
		
		index = 0;
		size = toAdd.size();
		HashMap<Annotation, Position> annosToAdd = new HashMap<>(size);
		for (FoldableRegion reg : toAdd) {
			annosToAdd.put(reg.getAnnotation(), reg.getPosition());
			index++;
		}

		model.modifyAnnotations(annosToDelete, annosToAdd, null);

		oldRegions = newRegions;
		newRegions = null;
	}

	private void updateFoldingRegionsInternal(ProgramContext pc) {
		// TODO - block comment is not supported yet. To support it, must
		//        also store the "trivia" tokens while parsing.
		
		// import A.B.C;
		// import D.E;
		// ...
		PreambleContext preamCntx = pc.preamble();
		if (preamCntx != null) {
			List<Import_statementContext> list = preamCntx.import_statement();
			if (list != null && list.size() > 3) { 
				// For now, no need to collapse if there are less than 3 import statements. 
				// We may improve this in future by customizing the projected text
				newRegions.add(
					FoldableRegion.fromList(RegionType.IMPORT_SECTION, list));
			}
		}
		
		// class MyClass {
		//   ...
		// }
		// enum MyEnum {
		//   ...
		// }
		DeclarationsContext declsCntx = pc.declarations();
		if (declsCntx != null) {
			List<Type_declarationContext> list = declsCntx.type_declaration();
			if (list != null) {
				for (Type_declarationContext td : list) {
					if (td.start.getLine() != td.stop.getLine()) { // No need to collapse if the declaration is a one-liner.
						newRegions.add(
							FoldableRegion.fromNode(RegionType.TYPE_DEF, td));
					
						// TODO - add foldable region for each ctor/method member.
					}
				}
			}
		}
		
		// void fun1(int arg) {
		//   ...
		// }
		ExecutableContext execCntx = pc.executable();
		if (execCntx != null) {
			Statement_listContext stmtListCntx = execCntx.statement_list();
			if (stmtListCntx != null) {
				List<StatementContext> list = stmtListCntx.statement();
				if (list != null) {
					for (StatementContext sc : list) {
						ParserRuleContext prt0 = (ParserRuleContext) sc.children.get(0);
						if (prt0.getRuleIndex() == JulianParser.RULE_declaration_statement) {
							ParserRuleContext prt01 = (ParserRuleContext) prt0.children.get(1);
							if (prt01.getRuleIndex() == JulianParser.RULE_function_declarator) {
								if (sc.start.getLine() != sc.stop.getLine()) { // No need to collapse if the function is a one-liner.
									newRegions.add(
										FoldableRegion.fromNode(RegionType.FUNCTION_DEF, sc));
								}
							}
						}
					}
				}
			}
		}
	}
}
