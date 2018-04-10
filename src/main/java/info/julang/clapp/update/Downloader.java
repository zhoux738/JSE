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

package info.julang.clapp.update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;

import info.julang.VersionUtility;

/**
 * Download the latest version of JSE release to the same directory as the currently running jar.
 * A wrapper script will handle replacement the next time it's invoked.
 * <p>
 * App layout
 * <pre>
 * +-jse.cmd
 * +-jse.sh
 * +-JSE.properties
 * +-lib\
 *      | (Always existing files)
 *      +-JSE-VER.jar
 *      |
 *      | (Initially non-exist, generated after first update)
 *      +-JSE-VER.jar.BAK
 *      +-JSE.cmd.BAK
 *      |
 *      | (Transient files, existing only during update)
 *      +-JSE-VER.jar.TEMP
 *      +-JSE.cmd.TEMP
 * </pre>
 * @author Ming Zhou
 */
public class Downloader {

	private final static String VERSION_HEADER = "X-JSE-version";
	private final static String JAR_PREFIX = "JSE-";
	private final static String JAR_POSTFIX = ".jar";
	private String versionCheckUrl;
	private String downloadUrlBase;
	
	public Downloader(String baseUrl){
		this.versionCheckUrl = assembleURL(baseUrl, "/version");
		this.downloadUrlBase = assembleURL(baseUrl, "/releases/");
	}
	
	/**
	 * Try download the latest version of JSE, if necessary, to the same directory as the current jar.
	 * 
	 * @return true successfully downloaded the latest version; false either downloading is not necessary or some error occurred.
	 */
	public boolean download(){
		try {
			URL url = new URL(versionCheckUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				String latest = con.getHeaderField(VERSION_HEADER);
				String current = VersionUtility.getVersion();
				if(isUpdateNeeded(current, latest)){
					return download(latest); 
				} else {
					return false;
				}
			} else {
				System.err.println("Couldn't perform version check against update server. The server returned a non-successful code: " + responseCode);
				return false;
			}
		} catch (MalformedURLException e) {
			System.err.println("The update server's URL is malformed: " + e.getMessage());
		} catch (ProtocolException e) {
			System.err.println("Couldn't perform version check against update server.");
		} catch (IOException e) {
			System.err.println("Couldn't perform version check against update server. A network error ocurred: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Encountered an unexpected error. " + e.getMessage());
		}

		return false;
	}

	private boolean download(String latest) throws IOException, URISyntaxException {
		URL url = assembleDownloadURL(latest);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		int responseCode = con.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK) {
			String dirPath = JarUtil.getCurrentJarDir();
			String npath = dirPath + "/" + JAR_PREFIX + latest + ".TEMP";
			File target = new File(npath);
			target.createNewFile();
			byte[] buffer = new byte[8192];
			try (FileOutputStream fos = new FileOutputStream(target);
				InputStream is = con.getInputStream()){
				int read = 0;
				while((read = is.read(buffer, 0, buffer.length)) >= 0){
					fos.write(buffer, 0, read);
				}
			}
			
			System.out.println("Latest binaries downloaded to " + target.getAbsolutePath());
			
			// Generate new script file from the new jar
			int exitCode = generateBatchFile(target);
			return exitCode == 0;
		}
		
		return false;
	}

	private int generateBatchFile(File jarFile) {
		int exitCode = 1;
		try {
			// The new jar file has suffix TEMP, but that doesn't prevent it from being executed by JVM
			ProcessBuilder pb = new ProcessBuilder("java", "-jar", jarFile.getAbsolutePath(), "-so-2").inheritIO();
			Process p = pb.start();
			exitCode = p.waitFor();	
			if (exitCode != 0) {
				System.err.println("Failed to generate new script file.");
			}
		} catch (IOException | InterruptedException e) {
			System.err.println("Failed to generate new script file. " + e.getMessage());
		}
		
		return exitCode;
	}

	private String assembleURL(String base, String relative) {
		while(base.endsWith("/") || base.endsWith("\\")){
			base = base.substring(0, base.length() - 1);
		}

		return base + relative;
	}
	
	private URL assembleDownloadURL(String latest) throws MalformedURLException {
		String url = downloadUrlBase + JAR_PREFIX + latest + JAR_POSTFIX;
		return new URL(url);
	}

	// Return true if a < b
	private boolean isUpdateNeeded(String a, String b) {
		String[] as = a.split("\\.");
		String[] bs = b.split("\\.");
		
		return compare(as, bs, 0);
	}

	private boolean compare(String[] as, String[] bs, int i) {
		if (i == as.length || i == bs.length){
			return as.length < bs.length;
		}
		
		int ia = Integer.parseInt(as[i]);
		int ib = Integer.parseInt(bs[i]);
		if (ia == ib) {
			return compare(as, bs, i + 1);
		} else {
			return ia < ib;
		}
	}
	
}