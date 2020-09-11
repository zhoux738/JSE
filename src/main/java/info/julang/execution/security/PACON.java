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

package info.julang.execution.security;

/**
 * Platform Access Category and Operation Names.
 * 
 * @author Ming Zhou
 */
public class PACON {

	public static class Process {
		public final static String Name = "System.Process";
		
		public final static String Op_control = "control";
		public final static String Op_wait = "wait";
		public final static String Op_read = "read";
		public final static String Op_write = "write";
	}
	
	public static class IO {
		public final static String Name = "System.IO";
		
		public final static String Op_list = "list";
		public final static String Op_stat = "stat";
		public final static String Op_read = "read";
		public final static String Op_write = "write";
	}
	
	public static class Socket {
		public final static String Name = "System.Socket";
		
		public final static String Op_listen = "listen";
		public final static String Op_connect = "connect";
		public final static String Op_read = "read";
		public final static String Op_write = "write";
	}
	
	public static class Network {
		public final static String Name = "System.Network";
		
		public final static String Op_resolve = "resolve";
	}
	
	public static class Interop {
		public final static String Name = "System.Interop";
		
		public final static String Op_map = "map";
	}
	
	public static class Environment {
		public final static String Name = "System.Environment";
		
		public final static String Op_read = "read";
	}
	
	public static class Reflection {
		public final static String Name = "System.Reflection";
		
		public final static String Op_load = "load";
	}
	
	public static class Console {
		public final static String Name = "System.Console";
		
		public final static String Op_read = "read";
	}
}
