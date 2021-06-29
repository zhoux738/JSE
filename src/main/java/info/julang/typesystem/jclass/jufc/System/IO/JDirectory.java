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
import java.nio.file.Paths;

import info.julang.execution.Argument;
import info.julang.execution.ArgumentUtil;
import info.julang.execution.security.PACON;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.CtorNativeExecutor;
import info.julang.hosting.execution.InstanceNativeExecutor;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.BoolValue;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.IArrayValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.StringValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.typesystem.basic.BoolType;
import info.julang.typesystem.jclass.builtin.JStringType;

/**
 * The native implementation of <code style="color:green">System.IO.Directory</code>.
 * 
 * @author Ming Zhou
 */
public class JDirectory extends JItem {
	
	private static final String FullTypeName = "System.IO.Directory";
	
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
				.add("getChildInfo", new GetChildInfoExecutor())
				.add("listAll", new ListAllExecutor());
		}
		
	};
	
	//----------------- native executors -----------------//
	
	private static class InitExecutor extends CtorNativeExecutor<JDirectory> {

		@Override
		protected void initialize(ThreadRuntime rt, HostedValue hvalue, JDirectory jdir, Argument[] args) throws Exception {
			String sv = getString(args, 0);
			jdir.init(sv);
		}
		
	}
	
	private static class GetNameExecutor extends InstanceNativeExecutor<JDirectory> {
		
		GetNameExecutor() {
			super(PACON.IO.Name, PACON.IO.Op_stat);
		}

		@Override
		protected JValue apply(ThreadRuntime rt, JDirectory jdir, Argument[] args) throws Exception {
			StringValue sv = TempValueFactory.createTempStringValue(jdir.getName());
			return sv;
		}
		
	}
	
	private static class GetPathExecutor extends InstanceNativeExecutor<JDirectory> {
		
		GetPathExecutor() {
			super(PACON.IO.Name, PACON.IO.Op_stat);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, JDirectory jdir, Argument[] args) throws Exception {
			StringValue sv = TempValueFactory.createTempStringValue(jdir.getPath());
			return sv;
		}
		
	}
	
	private static class GetParentPathExecutor extends InstanceNativeExecutor<JDirectory> {
		
		GetParentPathExecutor() {
			super(PACON.IO.Name, PACON.IO.Op_stat);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, JDirectory jdir, Argument[] args) throws Exception {
			String pp = jdir.getParentPath();
			return pp != null ? TempValueFactory.createTempStringValue(pp) : RefValue.NULL;
		}
		
	}
	
	private static class ExistExecutor extends InstanceNativeExecutor<JDirectory> {
		
		ExistExecutor() {
			super(PACON.IO.Name, PACON.IO.Op_stat);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, JDirectory jdir, Argument[] args) throws Exception {
			BoolValue sv = TempValueFactory.createTempBoolValue(jdir.exists());
			return sv;
		}
		
	}
	
	private static class CreateExecutor extends InstanceNativeExecutor<JDirectory> {
		
		CreateExecutor() {
			super(PACON.IO.Name, PACON.IO.Op_write);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, JDirectory jdir, Argument[] args) throws Exception {
			BoolValue sv = TempValueFactory.createTempBoolValue(jdir.create());
			return sv;
		}
		
	}
	
	private static class DeleteExecutor extends InstanceNativeExecutor<JDirectory> {
		
		DeleteExecutor() {
			super(PACON.IO.Name, PACON.IO.Op_write);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, JDirectory jdir, Argument[] args) throws Exception {
			BoolValue sv = TempValueFactory.createTempBoolValue(jdir.delete());
			return sv;
		}
		
	}

	private static class MoveExecutor extends IOInstanceNativeExecutor<JDirectory> {
		
		MoveExecutor() {
			super(PACON.IO.Name, PACON.IO.Op_write);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, JDirectory jdir, Argument[] args) throws Exception {
			HostedValue hv = ArgumentUtil.<HostedValue>getArgumentValue(0, args);
			JDirectory dir = (JDirectory)hv.getHostedObject();
			BoolValue sv = TempValueFactory.createTempBoolValue(jdir.move(dir));
			return sv;
		}
		
	}
	
	private static class RenameExecutor extends IOInstanceNativeExecutor<JDirectory> {
	
		RenameExecutor() {
			super(PACON.IO.Name, PACON.IO.Op_write);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, JDirectory jdir, Argument[] args) throws Exception {
			StringValue sv = ArgumentUtil.<StringValue>getArgumentValue(0, args);
			BoolValue bv = TempValueFactory.createTempBoolValue(jdir.rename(sv.getStringValue()));
			return bv;
		}
		
	}
	private static class GetChildInfoExecutor extends InstanceNativeExecutor<JDirectory> {
		
		GetChildInfoExecutor() {
			super(PACON.IO.Name, PACON.IO.Op_list);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, JDirectory jdir, Argument[] args) throws Exception {
			StringValue sv = ArgumentUtil.<StringValue>getArgumentValue(0, args);
			
			boolean[] res = jdir.getChildInfo(sv.getStringValue());
			
			ArrayValue av = TempValueFactory.createTemp1DArrayValue(rt.getTypeTable(), BoolType.getInstance(), 2);
			for(int i=0; i<res.length; i++){
				TempValueFactory.createTempBoolValue(res[i]).assignTo(av.getValueAt(i));
			}
			
			return av;
		}
		
	}
	
	private static class ListAllExecutor extends InstanceNativeExecutor<JDirectory> {
		
		ListAllExecutor() {
			super(PACON.IO.Name, PACON.IO.Op_list);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, JDirectory jdir, Argument[] args) throws Exception {
			File[] files = jdir.listAll();
			ArrayValue av = TempValueFactory.createTemp2DArrayValue(rt.getTypeTable(), JStringType.getInstance(), files.length, 2);
			
			for(int i=0; i<files.length; i++){
				IArrayValue sav = (IArrayValue) RefValue.dereference(av.getValueAt(i));
				File file = files[i];
				
				JValue value0 = sav.getValueAt(0);
				TempValueFactory.createTempStringValue(file.getPath()).assignTo(value0);
				
				JValue value1 = sav.getValueAt(1);
				TempValueFactory.createTempStringValue(file.isDirectory() ? "D" : "F").assignTo(value1);
			}
			
			return av;
		}
		
	}

	//----------------- implementation at native end -----------------//

	@Override
	public void init(String path) throws IOException {
		String cpath = Paths.get(path).toFile().getCanonicalPath();
		this.item = new File(cpath);
		if (item.exists() && item.isFile()) {
			throw new JSEIOException("Cannot create a directory with a path to a file.");
		}
	}
	
	public boolean create() {
		return this.item.mkdir();
	}
	
	public boolean[] getChildInfo(String name) {
		File ch = new File(item, name);
		boolean[] res = new boolean[] {
			ch.exists(),
			ch.isFile()	
		};
		return res;
	}
	
	public File[] listAll() {
		File[] files = item.listFiles();
		if(files == null){
			return new File[0];
		}
		
		return files;
	}
}
