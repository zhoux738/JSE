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

package info.julang.ide;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;
import org.osgi.framework.BundleContext;

import info.julang.ide.ast.ASTRepository;
import info.julang.ide.themes.SharedColorManager;

/**
 * The activator class controls the plug-in life cycle.
 * 
 * @author Ming Zhou
 */
public class JulianPlugin extends AbstractUIPlugin {

	private static JulianPlugin plugin;
	
	private ASTRepository repo;
	
	/**
	 * The constructor
	 */
	public JulianPlugin() { 
		repo = new ASTRepository();
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		// This is how to get e4's theme change event. This is not public API, so let's not use it.
		/*
		IEventBroker eventBroker = PlatformUI.getWorkbench().getService(IEventBroker.class);
		eventBroker.subscribe(IThemeEngine.Events.THEME_CHANGED, e -> {
			String topic = e.getTopic();
			String[] propNames = e.getPropertyNames();
			System.out.println("IThemeEngine Event (TOPIC=" + topic + "): " + String.join(", ", propNames));
		});
		*/

		IThemeManager mgr = PlatformUI.getWorkbench().getThemeManager();
		ITheme theme = mgr.getCurrentTheme();
		SharedColorManager.Instance.setColorRegistry(theme.getColorRegistry());
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static JulianPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(Constants.PLUGIN_ID, path);
	}
	
	public static ASTRepository getASTRepository() {
		return plugin.repo;
	}
}
