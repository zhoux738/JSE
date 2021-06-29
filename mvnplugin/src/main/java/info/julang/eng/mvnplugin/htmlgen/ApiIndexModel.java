/*
MIT License

Copyright (c) 2017 Ming Zhou

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

package info.julang.eng.mvnplugin.htmlgen;

import java.util.Set;
import java.util.TreeSet;

import info.julang.eng.mvnplugin.docgen.DocModel.DocType;
import info.julang.eng.mvnplugin.docgen.DocModel.Documented;
import info.julang.eng.mvnplugin.docgen.DocModel.Script;
import info.julang.eng.mvnplugin.docgen.DocModel.Type;
import info.julang.eng.mvnplugin.mdgen.TutorialInfo.IChapterInfo;

/**
 * The data model used to generate API index page.
 * <p>
 * This model contains two levels: root and modules. There are types under both levels. All the entries are already sorted.
 * 
 * @author Ming Zhou
 */
public class ApiIndexModel {

	protected PModule root;
	
	public ApiIndexModel(){
		root = new PModule("");
	}
	
	public PModule getRoot(){
		return root;
	}
	
	public void addType(Documented doc){
		String modName = doc.getDocFolderName();
		PModule mod = null;
		if (modName == null || "".equals(modName)){
			mod = root;
		} else {
			for(PItem item : root.getItems()){
				if (modName.equals(item.name) && item.isModule()){
					mod = (PModule)item;
					break;
				}
			}
			
			if (mod == null) {
				mod = new PModule(modName);
				root.addItem(mod);
			}
		}
		
		// The more rigorous way to ascertain the Exception type is to check ancestor types, but not worth the efforts for now.
		PType ptyp = new PType(
			doc.name, 
			doc.getDocType() == DocType.TYPE && doc.name.endsWith("Exception") && !"System.Exception".equals(((Type)doc).getFullName()));
		mod.addItem(ptyp);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (PItem item : root.getItems()) {
			sb.append(item.name);
			if (item.isModule()) {
				sb.append( "(M: ");
				sb.append(((PModule)item).size());
				sb.append( ")");
			}

			sb.append(",");
		}
		
		return sb.substring(0, sb.length() - 1);
	}
	
	//----------------------- Model Classes -----------------------//
	
	//---------------------------- API ----------------------------//
	
	public static abstract class PItem implements Comparable<PItem> {
		private String name;
		
		protected PItem (String name){
			this.name = name;
		}
		
		public String getName(){
			return name;
		}
		
		public abstract boolean isModule();
	}
	
	public static class PModule extends PItem {
		private Set<PItem> types = new TreeSet<PItem>();
		
		public PModule(String name){
			super(name);
		}
		
		public boolean isModule(){
			return true;
		}
		
		public void addItem(PItem item){
			types.add(item);
		}
		
		public Iterable<PItem> getItems(){
			return this.types;
		}
		
		public int size() {
			return this.types.size();
		}

		@Override
		public int compareTo(PItem another) {
			// 1. Modules precede types
			if (!another.isModule()) {
				return -1;
			}
			
			// 2. Built-in scripts goes first
			String thisName = this.getName();
			String thatName = another.getName();
			if (Script.DOC_FOLDER_NAME.equals(thisName)) {
				return -1;
			} else if (Script.DOC_FOLDER_NAME.equals(thatName)) {
				return 1;
			} 

			// 3. Sort alphabetically
			return thisName.compareTo(thatName);
		}
	}
	
	public static class PType extends PItem {
		private boolean isException;
		
		public PType(String name, boolean isException){
			super(name);
			this.isException = isException;
		}
		
		public boolean isModule(){
			return false;
		}
		
		public boolean isException(){
			return isException;
		}
		
		@Override
		public int compareTo(PItem another) {
			// 1. Modules precede types
			if (another.isModule()) {
				return 1;
			}
			
			PType anotherType = (PType)another;
			
			// 2. Non-exceptions precede exceptions
			if (!this.isException() && anotherType.isException()) {
				return -1;
			} else if (this.isException() && !anotherType.isException()) {
				return 1;
			}
			
			// 3. Sort alphabetically
			return this.getName().compareTo(another.getName());
		}
	}

	//-------------------------- Tutorial -------------------------//
	
	public static class TutorialIndexModel extends ApiIndexModel {

		private PTutorial pt;
		
		public void addTutorial(IChapterInfo cinfo){
			PTutorial pi = new PTutorial(cinfo.getLinkName(), cinfo.getTitle());
			if (pt != null) {
				pt.setNext(pi);
			}
			pi.setPrev(pt);
			root.addItem(pi);
			pt = pi;
		}
	}
	
	public static class PTutorial extends PItem {
		private String title;
		private PTutorial prev;
		private PTutorial next;
		
		public PTutorial(String name, String title){
			super(name);
			this.title = title;
		}
		
		public void setPrev(PTutorial pt) {
			prev = pt;
		}

		public void setNext(PTutorial pi) {
			next = pi;
		}
		
		public PTutorial getPrev() {
			return prev;
		}

		public PTutorial getNext() {
			return next;
		}

		public boolean isModule(){
			return false;
		}
		
		public String getTitle(){
			return title;
		}
		
		@Override
		public int compareTo(PItem another) {
			return this.getName().compareTo(another.getName());
		}
	}
}
