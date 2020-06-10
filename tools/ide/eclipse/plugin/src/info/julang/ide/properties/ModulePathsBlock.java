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

package info.julang.ide.properties;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import info.julang.ide.util.GridStyle;
import info.julang.ide.util.ResourceUtil;
import info.julang.ide.util.SWTUtil;

public class ModulePathsBlock {
	
	// Implementation Notes:
	// UI layout inspired by Eclipse Debug UI's environment tab. See:
	// https://github.com/eclipse/eclipse.platform.debug/blob/master/org.eclipse.debug.ui/ui/org/eclipse/debug/ui/EnvironmentTab.java
		
	private Composite parent;
	private IUpdateCallback callback;
	private boolean isRunOrProjectLevel;
	
	// Widgets
	private Button addButton;
	private Button removeButton;
	private TableViewer moduleDirTable; // including data model
	
	private Button appendRadioButton;
	private Button replaceRadioButton;
	
	/**
	 * Create a block that contains widgets for configuring module paths.
	 * 
	 * @param parent The container control.
	 * @param callback A callback to be invoked when the module path table is modified.
	 * @param isRunOrProjectLevel True for configuring module paths for a particular run context; false the project-level property page.
	 */
	public ModulePathsBlock(Composite parent, IUpdateCallback callback, boolean isRunOrProjectLevel) {
		this.parent = parent;
		this.callback = callback;
		this.isRunOrProjectLevel = isRunOrProjectLevel;
		
		createModuleDirsTable(parent);
		createTableButtons(parent);
		
		if (isRunOrProjectLevel) {
			createAppendReplaceButtons(parent);
		}
	}
	
	// Data API

	public void setModulePaths(File[] paths) {
		moduleDirTable.setInput(paths != null ? paths : new File[0]);
	}

	public File[] getModulePaths() {
		List<File> files = new ArrayList<File>();
		TableItem[] items = this.moduleDirTable.getTable().getItems();
		for (TableItem item : items) {
			File file = (File) item.getData();
			if (file != null) {
				files.add(file);
			}
		}
		
		File[] res = new File[files.size()];
		return files.toArray(res);
	}
	
	public boolean isAppendOrReplaceProjectLevelSetting() {
		return isRunOrProjectLevel && appendRadioButton.getSelection();
	}
	
	public void setAppendOrReplaceProjectLevelSetting(boolean shouldAppendOrReplace) {
		if (isRunOrProjectLevel) {
			if (shouldAppendOrReplace) {
				appendRadioButton.setSelection(true);
			} else {
				replaceRadioButton.setSelection(true);
			}
		}
	}

	private void handleTableSelectionChanged(SelectionChangedEvent event) {
		int size = event.getStructuredSelection().size();
		removeButton.setEnabled(size > 0);
	}
	
	// Widgets construction 
	
	private void handleAddButtonSelected() {
		DirectoryDialog dialog = new DirectoryDialog(parent.getShell(), SWT.OPEN);
		IWorkspaceRoot currentContainer = ResourcesPlugin.getWorkspace().getRoot();
		String rootOsPath = ResourceUtil.getAbsoluteFSPath(currentContainer);
		dialog.setFilterPath(rootOsPath);
		String path = dialog.open();

		if (path != null) {
			addModuleDir(new File(path));
		}
	}
	
	private void handleRemoveButtonSelected() {
		IStructuredSelection sel = this.moduleDirTable.getStructuredSelection();
		try {
			moduleDirTable.getControl().setRedraw(false);
			for (Iterator<?> i = sel.iterator(); i.hasNext();) {
				File dir = (File) i.next();
				this.moduleDirTable.remove(dir);
			}
		} finally {
			moduleDirTable.getControl().setRedraw(true);
		}
		
		if (callback != null) {
			callback.onUpdate(this);
		}
	}
	
	private boolean addModuleDir(File dir) {
		TableItem[] items = this.moduleDirTable.getTable().getItems();
		for (TableItem item : items) {
			File file = (File) item.getData();
			if (file.equals(dir)) {
				// Duplicate. Do not add.
				return false;
			}
		}
		
		this.moduleDirTable.add(dir);
		
		if (callback != null) {
			callback.onUpdate(this);
		}
		
		return true;
	}
	
	private void createModuleDirsTable(Composite parent) {
		// Top row label
		SWTUtil.createLabel(
			parent, 
			isRunOrProjectLevel ? "Additional module paths for this run:" : "Module paths:", 
			2, // horizontal span
			false); // do not fill the parent horizontally
		
		// Table container
		Composite tableComposite = SWTUtil.createComposite(parent, 1, 1, GridStyle.FILL_BOTH, gd -> {
			gd.marginWidth = 0;
			gd.marginHeight = 0;
		});
		
		// Table
		moduleDirTable = new TableViewer(
			tableComposite,
			SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		Table table = moduleDirTable.getTable();
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(false);
		table.setLinesVisible(false);
		moduleDirTable.setContentProvider(new ModulePathsContentProvider());
		moduleDirTable.setLabelProvider(new ModulePathLabelProvider());
		moduleDirTable.setColumnProperties(new String[] { "Path" });
		moduleDirTable.addSelectionChangedListener(this::handleTableSelectionChanged);

		// Right-side menu
		Menu menuTable = new Menu(table);
		table.setMenu(menuTable); // Associate with the table

		// Create add module dir menu item
		MenuItem miAdd = new MenuItem(menuTable, SWT.NONE);
		miAdd.setText("Add");
		miAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				handleAddButtonSelected();
			}
		});

		// Create remove module dir menu item
		MenuItem miRemove = new MenuItem(menuTable, SWT.NONE);
		miRemove.setText("Remove");
		miRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				handleRemoveButtonSelected();
			}
		});

		// Button text reacts to selection change
		// (This doesn't seem to work on Mac OS X at least)
		this.moduleDirTable.addSelectionChangedListener(event -> {
			IStructuredSelection selection = moduleDirTable.getStructuredSelection();
			if (selection.size() == 1) {
				miRemove.setText("Remove");
			} else if (selection.size() > 1) {
				miRemove.setText("Remove All");
			}
		});

		table.addListener(SWT.MenuDetect, event -> {
			if (table.getSelectionCount() <= 0) {
				miRemove.setEnabled(false);
			} else {
				miRemove.setEnabled(true);
			}
		});

		// Setup table viewer
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(this.moduleDirTable) {
			@Override
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}

		};

		int feature = 
			ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR 
			| ColumnViewerEditor.TABBING_HORIZONTAL
			| ColumnViewerEditor.KEYBOARD_ACTIVATION;

		TableViewerEditor.create(moduleDirTable, actSupport, feature);

		// Setup environment variable name column
		final TableViewerColumn tcv1 = new TableViewerColumn(moduleDirTable, SWT.NONE, 0);
		tcv1.setLabelProvider(
			new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					return ((File) element).getAbsolutePath();
				}
			});

		TableColumn tc1 = tcv1.getColumn();
		tc1.setText("Path");

		// Create table column layout
		TableColumnLayout tableColumnLayout = new TableColumnLayout(true);
		PixelConverter pixelConverter = new PixelConverter(parent.getFont());
		tableColumnLayout.setColumnData(tc1, new ColumnWeightData(1, pixelConverter.convertWidthInCharsToPixels(20)));
		tableComposite.setLayout(tableColumnLayout);
	}
	
	private void createTableButtons(Composite parent) {
		Composite buttonComposite = SWTUtil.createComposite(
			parent, 1, 1, 
			GridStyle.HORIZONTAL_ALIGN_END, // Some implementations also bitwise-add GridData.VERTICAL_ALIGN_BEGINNING.
			gd -> {
				gd.marginHeight = 0;
				gd.marginWidth = 0;
			});

		addButton = SWTUtil.createPushButton(buttonComposite, "Add", null);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				handleAddButtonSelected();
			}
		});
		
		removeButton = SWTUtil.createPushButton(buttonComposite, "Remove", null);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				handleRemoveButtonSelected();
			}
		});
		removeButton.setEnabled(false);
	}
	
	private void createAppendReplaceButtons(Composite parent) {
		Composite comp = SWTUtil.createComposite(parent, 1, 2, GridStyle.FILL_HORIZONTAL);
		appendRadioButton = SWTUtil.createRadioButton(comp, "Append to the project-level module paths.");
		appendRadioButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//replaceRadioButton.setSelection(false);
				callback.onUpdate(ModulePathsBlock.this);
			}
		});
		replaceRadioButton = SWTUtil.createRadioButton(comp, "Replace the project-level module paths.");
	}
	
	// Content provider for module paths table. 
	// The element type is File. Each element will be fed to ModulePathLabelProvider's API as the 1st arg (in raw Object type).
	private class ModulePathsContentProvider implements IStructuredContentProvider {
		
		// The data input is File[]
		@Override
		public Object[] getElements(Object inputElement) {
			File[] files = (File[]) inputElement;
			if (files != null) {
				return files;
			}
			
			return new Object[0];
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput == null) {
				return;
			}
			
			if (viewer instanceof TableViewer) {
				TableViewer tableViewer = (TableViewer) viewer;
				if (tableViewer.getTable().isDisposed()) {
					return;
				}
				
				tableViewer.setComparator(new ViewerComparator() {
					@Override
					public int compare(Viewer iviewer, Object e1, Object e2) {
						if (e1 == null) {
							return -1;
						} else if (e2 == null) {
							return 1;
						} else {
							return ((File) e1).getAbsoluteFile()
								.compareTo(((File) e2).getAbsoluteFile());
						}
					}
				});
			}
		}
	}

	// Content provider for module paths table. This table contains a single column, which shows the absolute path.
	private class ModulePathLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object element, int columnIndex) {
			String result = null;
			if (element != null) {
				File file = (File) element;
				switch (columnIndex) {
				case 0:
					result = file.getAbsolutePath();
					break;
				default:
					break;
				}
			}
			
			return result;
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}

	@FunctionalInterface
	public static interface IUpdateCallback {
		void onUpdate(ModulePathsBlock block); // May add more args when needed.
	}
}
