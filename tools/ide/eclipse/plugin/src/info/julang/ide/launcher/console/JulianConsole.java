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

package info.julang.ide.launcher.console;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

import info.julang.ide.PluginImages;
import info.julang.ide.launcher.JulianRunner;

/**
 * An effectively singleton console. The JulianRunner class has logic to ensure that users cannot launch again when one is already ongoing.
 * 
 * @author Ming Zhou
 */
public class JulianConsole extends IOConsole {

	private static final String CONSOLE_NAME = "JSE Console";

	private JulianRunner runner;
	
	JulianConsole(JulianRunner runner) {
		super(CONSOLE_NAME, PluginImages.IMG_MASCOT);
		this.runner = runner;
	}
	
	public JulianRunner getRunner() {
		return runner;
	}
	
	@Override
	protected void init() {
		super.init();
		
		// stdout
		out = this.newOutputStream();
		
		// stderr
		IOConsoleOutputStream mos = this.newOutputStream();
		mos.setColor(new Color(Display.getCurrent(), 255, 0, 0));
		err = mos;
		
		// stdin
		in = this.getInputStream();
	}

	private OutputStream out;
	private OutputStream err;
	private InputStream in;
	
	public OutputStream getOut() {
		return out;
	}
	
	public OutputStream getErr() {
		return err;
	}
	
	public InputStream getIn() {
		return in;
	}
}
