package info.jultest.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public final class FileSysHelper {

	/**
	 * Recursively delete everything under a directory.
	 * 
	 * @param dir The directory
	 * @return 
	 */
	public static boolean deleteAll(File dir) {
		boolean succ = true;
	    File[] allContents = dir.listFiles();
	    if (allContents != null) {
	        for (File file : allContents) {
	        	boolean recSucc = deleteAll(file);
	        	succ = succ && recSucc;
	        }
	    }
	    
	    boolean dirSucc = dir.delete();
	    return succ && dirSucc;
	}
	
	/**
	 * Create a temporary folder to be deleted at the end of test run.
	 * 
	 * @return The temporary directory created
	 * @throws IOException
	 */
	public static File createTempDir() throws IOException {
		File dir = File.createTempFile("__jse_test_dir_", ".tmp"); 
		boolean succ = dir.delete() && dir.mkdir();
		if (succ) {
			dir.deleteOnExit();
			return dir;
		} else {
			if (!dir.delete()) {
				dir.deleteOnExit();
			}
			throw new IOException("Cannot create temporary directory.");
		}
	}
	
	/**
	 * Create a temporary file to be deleted at the end of test run.
	 * 
	 * @return The temporary directory created
	 * @throws IOException
	 */
	public static File createTempFile() throws IOException {
		File temp = File.createTempFile("__jse_test_file_", ".tmp"); 
		temp.deleteOnExit();
		temp.createNewFile();
		return temp;
	}
	
	/**
	 * Read all contents from the file and convert to a string with specified charset.
	 * 
	 * @param file The file to read from. 
	 * @param charsetName If null, use ASCII.
	 * @return
	 * @throws IOException
	 */
	public static String readAllString(File file, String charsetName) throws IOException {
		int size = 1024;
		byte[] bytes = new byte[size];
		int offset = 0;
		int count = 0;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try(FileInputStream fis = new FileInputStream(file)) {
			while((count = fis.read(bytes, offset, size - offset)) != -1) {
				offset += count;
				baos.write(bytes, 0, count);
			}
		}
		byte[] all = baos.toByteArray();
		String str = new String(all, charsetName != null ? charsetName : "ascii");
		return str;
	}
	
	/**
	 * Read all contents from the file and convert to a string in ASCII charset.
	 * 
	 * @param file The file to read from.
	 * @return
	 * @throws IOException
	 */
	public static String readAllString(File file) throws IOException {
		return readAllString(file, null);
	}
}
