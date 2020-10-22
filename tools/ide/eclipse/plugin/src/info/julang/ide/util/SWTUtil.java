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

package info.julang.ide.util;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * A helper to create various widgets.
 * 
 * Most of the code are borrowed from those SWTFactory classes used inside the platform or JDT plugins. 
 * Example: org.eclipse.debug.internal.ui.SWTFactory
 * 
 * @author Ming Zhou
 */
public final class SWTUtil {
	
	@FunctionalInterface
	public static interface ILayoutUpdater {
		void update(GridLayout layout);
	}
	
	@FunctionalInterface
	public static interface IGridDataUpdater {
		void update(GridData data);
	}
	
	/**
	 * Create a composite with pre-defined grid layout.
	 */
	public static Composite createComposite(Composite parent, int columns, int hspan, GridStyle style) {
    	return createComposite(parent, columns, hspan, style, null);
	}
	
	/**
	 * Create a composite with customized grid layout.
	 */
	public static Composite createComposite(Composite parent, int columns, int hspan, GridStyle style, ILayoutUpdater layoutUpdater) {
    	Composite comp = new Composite(parent, SWT.NONE);
    	GridLayout layout = new GridLayout(columns, false);
    	if (layoutUpdater != null) {
    		layoutUpdater.update(layout);
    	}
    	comp.setLayout(layout);
    	GridData gd = new GridData(style.toSWTStyle());
		gd.horizontalSpan = hspan;
		comp.setLayoutData(gd);
		
		return comp;
	}
	
	/**
	 * Create a group with customized grid layout.
	 */
	public static Group createGroup(Composite parent, String text, int columns, int hspan, GridStyle style) {
    	Group g = new Group(parent, SWT.SHADOW_NONE);
    	g.setLayout(new GridLayout(columns, false));
    	g.setText(text);
    	g.setFont(parent.getFont());
    	GridData gd = new GridData(style.toSWTStyle());
		gd.horizontalSpan = hspan;
    	g.setLayoutData(gd);
    	return g;
    }
	
	/**
	 * Create a label.
	 */
	public static Label createLabel(Composite parent, String text, int hspan, boolean hfill, IGridDataUpdater layoutUpdater) {
		Label l = new Label(parent, SWT.NONE);
		l.setText(text);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = hspan;
		gd.grabExcessHorizontalSpace = hfill;
		if (layoutUpdater != null) {
			layoutUpdater.update(gd);
		}
		l.setLayoutData(gd);
		return l;
	}
	
	public static Label createLabel(Composite parent, String text, int hspan, boolean hfill) {
		return createLabel(parent, text, hspan, hfill, null);
	}
	
	/**
	 * Create a separator.
	 */
	public static Label createSeparator(Composite parent, boolean isHorizontalOrVertical) {
		Label separator = new Label(
			parent, 
			SWT.SEPARATOR | (isHorizontalOrVertical ? SWT.HORIZONTAL : SWT.VERTICAL));
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(gridData);
		return separator;
	}
	
	/**
	 * Create a push-style button.
	 */
	public static Button createPushButton(Composite parent, String label, Image image) {
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		if (image != null) {
			button.setImage(image);
		}
		if (label != null) {
			button.setText(label);
		}
		GridData gd = new GridData();
		button.setLayoutData(gd);
		setButtonDimensionHint(button);
		return button;
	}
	
	/**
	 * Create a checkbox-style button.
	 */
	public static Button createCheckBox(Composite parent, String label) {
		Button button = new Button(parent, SWT.CHECK);
		button.setFont(parent.getFont());
		if (label != null) {
			button.setText(label);
		}
		GridData gd = new GridData();
		button.setLayoutData(gd);
		setButtonDimensionHint(button);
		return button;
	}

	/**
	 * Create a radio-style button.
	 */
	public static Button createRadioButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.RADIO);
		button.setFont(parent.getFont());
		if (label != null) {
			button.setText(label);
		}
		GridData gd = new GridData();
		button.setLayoutData(gd);
		setButtonDimensionHint(button);
		return button;
	}
	
	private static void setButtonDimensionHint(Button button) {
		Assert.isNotNull(button);
		Object gd = button.getLayoutData();
		if (gd instanceof GridData) {
			((GridData)gd).widthHint = getButtonWidthHint(button);
			((GridData)gd).horizontalAlignment = GridData.FILL;
		}
	}
	
	private static int getButtonWidthHint(Button button) {
		PixelConverter converter = new PixelConverter(button);
		int widthHint = converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		return Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	}
}
