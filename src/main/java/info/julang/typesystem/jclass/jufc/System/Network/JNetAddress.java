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

package info.julang.typesystem.jclass.jufc.System.Network;

import info.julang.execution.Argument;
import info.julang.execution.security.PACON;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.execution.threading.ThreadRuntimeHelper;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.StaticNativeExecutor;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.typesystem.jclass.JClassType;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class JNetAddress {

    public static final String FQCLASSNAME = "System.Network.NetAddress";
    
    //----------------- IRegisteredMethodProvider -----------------//
    
    public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FQCLASSNAME){

        @Override
        protected void implementProvider(SimpleHostedMethodProvider provider) {
            provider
                .add("resolve", new ResolveExecutor())
                .add("resolveAll", new ResolveAllExecutor())
                .add("getLocal", new GetLocalExecutor());
        }
        
    };
    
	private static class GetLocalExecutor extends StaticNativeExecutor<JNetAddress> {

		@Override
		protected JValue apply(ThreadRuntime rt, Argument[] args) throws Exception {
			ObjectValue res = getLocal(rt);
			return res;
		}
	}
    
	private static class ResolveExecutor extends StaticNativeExecutor<JNetAddress> {

		ResolveExecutor(){
			super(PACON.Network.Name, PACON.Network.Op_resolve);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, Argument[] args) throws Exception {
			String val = this.getString(args, 0);
			ObjectValue res = resolve(rt, val, null);
			return res;
		}
	}
	
	private static class ResolveAllExecutor extends StaticNativeExecutor<JNetAddress> {

		ResolveAllExecutor(){
			super(PACON.Network.Name, PACON.Network.Op_resolve);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, Argument[] args) throws Exception {
			String val = this.getString(args, 0);
			ArrayValue av = resolveAll(rt, val);
			return av;
		}
	}

	private static ObjectValue getLocal(ThreadRuntime rt) {
		InetAddress netAddress;
		String addr = null;
		String name = null;
		int type = -1;
		try {
			netAddress = InetAddress.getLocalHost();
			if (netAddress instanceof Inet4Address) {
				type = AddressType.IPv4.ordinal();
			} else if (netAddress instanceof Inet6Address) {
				type = AddressType.IPv6.ordinal();
			}
				
			addr = netAddress.getHostAddress();
			name = netAddress.getHostName();
		} catch (UnknownHostException e) {
			type = AddressType.Unresolved.ordinal();
		}
		
		// System.out.println(type + ": " + addr + " (" + name + ")")
		
		ObjectValue val = createNetAddressValue(rt, name, addr, type);
		
		return val;
	}
	
	private static ObjectValue resolve(ThreadRuntime rt, String name, String addr) {
		InetAddress netAddress = null;
		int type = -1;
		try {
			netAddress = InetAddress.getByName(addr == null ? name : addr);
			if (netAddress instanceof Inet4Address) {
				type = AddressType.IPv4.ordinal();
			} else if (netAddress instanceof Inet6Address) {
				type = AddressType.IPv6.ordinal();
			}
			
			addr = netAddress.getHostAddress();
		} catch (UnknownHostException e) {
			type = AddressType.Unresolved.ordinal();
		}
		
		// System.out.println(type + ": " + addr + " (" + name + ")")
		
		ObjectValue val = createNetAddressValue(rt, name, addr, type);
		
		return val;
	}
	
	private static ArrayValue resolveAll(ThreadRuntime rt, String name) {
		JClassType eleTyp = (JClassType)ThreadRuntimeHelper.loadSystemType(rt, JNetAddress.FQCLASSNAME);
		ArrayValue av = null;
		try {
			InetAddress[] netAddresses = InetAddress.getAllByName(name);
			ObjectValue[] objs = new ObjectValue[netAddresses.length];
			int i = 0;
			for (InetAddress addr : netAddresses) {
				ObjectValue ov = resolve(rt, name, addr.getHostAddress());
				objs[i] = ov;
				i++;
			}
			av = ThreadRuntimeHelper.createAndPopulateArrayValue(rt, eleTyp, objs);
		} catch (UnknownHostException e) {
			int type = AddressType.Unresolved.ordinal();
			String addr = null;
			ObjectValue val = createNetAddressValue(rt, name, addr, type);
			av = ThreadRuntimeHelper.createAndPopulateArrayValue(rt, eleTyp, new ObjectValue[]{ val });
		}
		
		return av;
	}
	
	private static ObjectValue createNetAddressValue(ThreadRuntime rt, String name, String addr, int type){
		ObjectValue val = ThreadRuntimeHelper.instantiateSystemType(rt, JNetAddress.FQCLASSNAME, new JValue[]{
		    name == null ? TempValueFactory.createTempNullRefValue() : TempValueFactory.createTempStringValue(name),
		    addr == null ? TempValueFactory.createTempNullRefValue() : TempValueFactory.createTempStringValue(addr),
			TempValueFactory.createTempIntValue(type)
		});
		return val;
	}
    
    private enum AddressType {
    	IPv4,
    	IPv6,
    	Unresolved
    }
    
}
