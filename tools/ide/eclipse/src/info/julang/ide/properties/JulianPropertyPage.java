package info.julang.ide.properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import info.julang.ide.Constants;

public class JulianPropertyPage extends PropertyPage {

	private static final String PATH_TITLE = "Path:";
	private static final String ENABLED_TITLE = "&Enabled for parsing:";
	
	private Button checkBox;
	private IResource resource;

	public JulianPropertyPage() {
		
	}
	
	@Override
	public boolean performOk() {
		try {
    		boolean prevEnabled = JulianScriptProperties.isEnabledForParsing(resource);
            boolean currEnabled = checkBox.getSelection();
            if (currEnabled != prevEnabled) {
            	// Changed. Persist the new value.
                resource.setPersistentProperty(
        				new QualifiedName(Constants.PLUGIN_ID, JulianScriptProperties.ENABLED_BOOL_PROPERTY),
        				currEnabled ? JulianScriptProperties.ENABLED_BOOL_VALUE_DEFAULT : JulianScriptProperties.ENABLED_BOOL_VALUE_DISABLED);
            
                // Simply produce a resource change event to trigger the incremental build
                resource.touch(null);
            }
		} catch (CoreException e) {
			return false;
		}
		
		return true;
	}

	@Override
	protected Control createContents(Composite parent) {
		resource = (IResource)getElement();
		
		// The root container
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		// A section that contains read-only info about this script
		addInfoSection(composite);

		// A separator
		Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(gridData);
		
		// A section which the user can edit
		addEditableSection(composite);
		
		return composite;
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		checkBox.setSelection(true);
	}

	private void addInfoSection(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		// Path
		Label pathLabel = new Label(composite, SWT.NONE);
		pathLabel.setText(PATH_TITLE);
		Text pathValueText = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		pathValueText.setText(resource.getFullPath().toString());
	}

	private void addEditableSection(Composite parent) {
		// A checkbox to enable/disable file parsing during build. Default to true. 
		// Mostly useful for JSE development where certain test files are intentionally 
		// written with illegal grammar.
		
		Composite composite = createDefaultComposite(parent);

		Label enabledLabel = new Label(composite, SWT.NONE);
		enabledLabel.setText(ENABLED_TITLE);

		checkBox = new Button(composite, SWT.CHECK);

		boolean enabled = JulianScriptProperties.isEnabledForParsing(resource);
		if (enabled) {
			checkBox.setSelection(true);
		}
	}

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
	}
}