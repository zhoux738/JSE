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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;

import info.julang.execution.Argument;
import info.julang.execution.ArgumentUtil;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.CtorNativeExecutor;
import info.julang.hosting.execution.InstanceNativeExecutor;
import info.julang.memory.value.BoolValue;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.StringValue;
import info.julang.memory.value.TempValueFactory;

/**
 * The native implementation of <font color="green">System.IO.File</font>.
 * 
 * @author Ming Zhou
 */
public class JFile extends JItem {
	
	private static final String FullTypeName = "System.IO.File";
	
	//----------------- IRegisteredMethodProvider -----------------//

	public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FullTypeName){

		@Override
		protected void implementProvider(SimpleHostedMethodProvider provider) {
			provider
				.add("ctor", new InitExecutor())
				.add("create", new CreateExecutor())
				.add("delete", new DeleteExecutor())
				.add("exists", new ExistExecutor())
				.add("move", new MoveExecutor())
				.add("rename", new RenameExecutor())
				.add("getName", new GetNameExecutor())
				.add("getPath", new GetPathExecutor())
				.add("getParentPath", new GetParentPathExecutor())
				.add("readAllText", new ReadAllTextExecutor());
		}
		
	};
	
	//----------------- native executors -----------------//
	
	private static class InitExecutor extends CtorNativeExecutor<JFile> {

		@Override
		protected void initialize(ThreadRuntime rt, HostedValue hvalue, JFile jfile, Argument[] args) throws Exception {
			String sv = getString(args, 0);
			jfile.init(sv);
		}
		
	}
	
	private static class GetNameExecutor extends InstanceNativeExecutor<JFile> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JFile jfile, Argument[] args) throws Exception {
			StringValue sv = TempValueFactory.createTempStringValue(jfile.getName());
			return sv;
		}
		
	}
	
	private static class GetPathExecutor extends InstanceNativeExecutor<JFile> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JFile jfile, Argument[] args) throws Exception {
			StringValue sv = TempValueFactory.createTempStringValue(jfile.getPath());
			return sv;
		}
		
	}
	
	private static class GetParentPathExecutor extends InstanceNativeExecutor<JFile> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JFile jfile, Argument[] args) throws Exception {
			String pp = jfile.getParentPath();
			return pp != null ? TempValueFactory.createTempStringValue(pp) : RefValue.NULL;
		}
		
	}
	
	private static class ExistExecutor extends InstanceNativeExecutor<JFile> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JFile jfile, Argument[] args) throws Exception {
			BoolValue sv = TempValueFactory.createTempBoolValue(jfile.exists());
			return sv;
		}
		
	}
	
	private static class CreateExecutor extends IOInstanceNativeExecutor<JFile> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JFile jfile, Argument[] args) throws Exception {
			BoolValue sv = TempValueFactory.createTempBoolValue(jfile.create());
			return sv;
		}
		
	}
	
	private static class DeleteExecutor extends InstanceNativeExecutor<JFile> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JFile jfile, Argument[] args) throws Exception {
			BoolValue sv = TempValueFactory.createTempBoolValue(jfile.delete());
			return sv;
		}
		
	}
	
	private static class MoveExecutor extends IOInstanceNativeExecutor<JFile> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JFile jfile, Argument[] args) throws Exception {
			HostedValue hv = ArgumentUtil.<HostedValue>getArgumentValue(0, args);
			JDirectory dir = (JDirectory)hv.getHostedObject();
			BoolValue sv = TempValueFactory.createTempBoolValue(jfile.move(dir));
			return sv;
		}
		
	}
	
	private static class RenameExecutor extends IOInstanceNativeExecutor<JFile> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JFile jfile, Argument[] args) throws Exception {
			StringValue sv = ArgumentUtil.<StringValue>getArgumentValue(0, args);
			BoolValue bv = TempValueFactory.createTempBoolValue(jfile.rename(sv.getStringValue()));
			return bv;
		}
		
	}
	
	private static class ReadAllTextExecutor extends IOInstanceNativeExecutor<JFile> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JFile jfile, Argument[] args) throws Exception {
			StringValue sv = TempValueFactory.createTempStringValue(jfile.readAll());
			return sv;
		}
		
	}

	//----------------- implementation at native end -----------------//

	@Override
	public void init(String path) throws IOException {
		String cpath = Paths.get(path).toFile().getCanonicalPath();
		this.item = new File(cpath);
		if (item.exists() && item.isDirectory()) {
			throw new JSEIOException("Cannot create a file with a path to a directory.");
		}
	}

	public boolean create() throws IOException {
		return this.item.createNewFile();
	}
	
	public String readAll() throws FileNotFoundException, IOException {
		int length = (int) item.length();
		FileInputStream fos = new FileInputStream(item);
		byte[] bytes = new byte[length];
		try {
			fos.read(bytes, 0, length);
			String str = new String(bytes);
			return str;
		} finally {
			try {
				fos.close();
			} catch (Exception e) {
				// Ignore any exception thrown when closing the file.
			}
		}
	}
	
}
