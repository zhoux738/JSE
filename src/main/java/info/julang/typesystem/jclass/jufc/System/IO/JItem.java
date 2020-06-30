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

package info.julang.typesystem.jclass.jufc.System.IO;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The parent for both {@link JFile} and {@link JDirectory}.
 * 
 * @author Ming Zhou
 */
public abstract class JItem {

	protected File item;
	
	public abstract void init(String path) throws IOException;
	
	public String getPath() throws IOException {
		return item.getCanonicalPath();
	}
	
	public String getParentPath(){
		return item.getParent();
	}
	
	public String getName(){
		return item.getName();
	}
	
	public boolean exists(){
		return item.exists();
	}
	
	public boolean create() {
		return item.mkdir();
	}
	
	public boolean delete() {
		return item.delete();
	}
	
	public boolean move(JDirectory dir) throws IOException {
        return moveTo(Paths.get(dir.getPath(), item.getName()));
	}
	
	public boolean rename(String name) throws IOException {
		Path newName = Paths.get(item.getParent(), name);
        return moveTo(newName);
	}
	
	private boolean moveTo(Path path) throws IOException {
        Path dstFile = null;
        try {
			dstFile = Files.move(this.item.toPath(), path);
		} catch (FileAlreadyExistsException | NoSuchFileException e) {
        	return false;
		}
        
        if (dstFile != null) {
			this.item = dstFile.toFile();
        	return true;
        } else {
            return false;
        }
	}
}
