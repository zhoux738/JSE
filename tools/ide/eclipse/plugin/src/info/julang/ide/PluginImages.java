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

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.Bundle;

/**
 * Plugin icons and images.
 * 
 * @author Ming Zhou
 */
public class PluginImages {

	private final static String ICONS_PATH = "$nl$/icons/";
	
	public static final ImageDescriptor IMG_VARIABLE = create("variable.png");
	public static final ImageDescriptor IMG_MODULE = create("module.png");
	public static final ImageDescriptor IMG_SCRIPT_FILE = create("script_file.png");
	public static final ImageDescriptor IMG_MASCOT_64_64 = create("mascot_wiz.png");
	public static final ImageDescriptor IMG_MASCOT = create("mascot.png");
	public static final ImageDescriptor IMG_TERMINATE = create("terminate.png");
	public static final ImageDescriptor IMG_TERMINATE_DISABLED = create("terminate_disabled.png");

	private static ImageDescriptor create(String name) {
		String path = ICONS_PATH + name;
        Bundle bundle = Platform.getBundle(Constants.PLUGIN_ID);
		URL url = FileLocator.find(bundle, new Path(path), null);
		return ImageDescriptor.createFromURL(url);
	}
}
