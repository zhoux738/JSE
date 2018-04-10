package info.jultest.test;

import info.julang.external.EngineFactory;
import info.julang.external.exceptions.JSEError;

import java.net.MalformedURLException;
import java.net.URL;

public class TestCaseEngineFactory extends EngineFactory {

	@Override
	protected URL getEngineBinariesPath() {
		ClassLoader appLoader = EngineFactory.class.getClassLoader();
		URL url = appLoader.getResource(INITIAL_PATH);
		// For unit tests, class files are placed in a hierarchical directory.
		String urlString = url.toString();
		urlString = urlString.substring(0, urlString.length() - INITIAL_PATH_LEN);
		try {
			return new URL(urlString);
		} catch (MalformedURLException e) {
			throw new JSEError("Cannot create engine. Binaries path is invalid: " + urlString);
		}
	}

}
