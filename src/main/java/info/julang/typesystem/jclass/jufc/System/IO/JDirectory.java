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

import info.julang.execution.Argument;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.CtorNativeExecutor;
import info.julang.hosting.execution.InstanceNativeExecutor;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.BoolValue;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.StringValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.typesystem.jclass.builtin.JStringType;

/**
 * The native implementation of <font color="green">System.IO.Directory</font>.
 * 
 * @author Ming Zhou
 */
public class JDirectory {
	
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
				.add("getName", new GetNameExecutor())
				.add("getPath", new GetPathExecutor())
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
		
		@Override
		protected JValue apply(ThreadRuntime rt, JDirectory jdir, Argument[] args) throws Exception {
			StringValue sv = TempValueFactory.createTempStringValue(jdir.getName());
			return sv;
		}
		
	}
	
	private static class GetPathExecutor extends InstanceNativeExecutor<JDirectory> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JDirectory jdir, Argument[] args) throws Exception {
			StringValue sv = TempValueFactory.createTempStringValue(jdir.getPath());
			return sv;
		}
		
	}
	
	private static class ExistExecutor extends InstanceNativeExecutor<JDirectory> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JDirectory jdir, Argument[] args) throws Exception {
			BoolValue sv = TempValueFactory.createTempBoolValue(jdir.exists());
			return sv;
		}
		
	}
	
	private static class CreateExecutor extends InstanceNativeExecutor<JDirectory> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JDirectory jdir, Argument[] args) throws Exception {
			BoolValue sv = TempValueFactory.createTempBoolValue(jdir.create());
			return sv;
		}
		
	}
	
	private static class DeleteExecutor extends InstanceNativeExecutor<JDirectory> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JDirectory jdir, Argument[] args) throws Exception {
			BoolValue sv = TempValueFactory.createTempBoolValue(jdir.delete());
			return sv;
		}
		
	}
	
	private static class ListAllExecutor extends InstanceNativeExecutor<JDirectory> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JDirectory jdir, Argument[] args) throws Exception {
			File[] files = jdir.listAll();
			ArrayValue av = TempValueFactory.createTemp2DArrayValue(rt.getTypeTable(), JStringType.getInstance(), files.length, 2);
			
			for(int i=0; i<files.length; i++){
				ArrayValue sav = (ArrayValue) RefValue.dereference(av.getValueAt(i));
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
	
	private String path;
	
	private File dir;
	
	public void init(String path){
		this.path = path;
		this.dir = new File(path);
	}
	
	public String getPath(){
		return path;
	}
	
	public String getName(){
		return dir.getName();
	}
	
	public boolean exists(){
		return dir.exists();
	}
	
	public boolean create() {
		return dir.mkdir();
	}
	
	public boolean delete() {
		return dir.delete();
	}
	
	public File[] listAll() {
		File[] files = dir.listFiles();
		if(files == null){
			return new File[0];
		}
		
		return files;
	}
}
