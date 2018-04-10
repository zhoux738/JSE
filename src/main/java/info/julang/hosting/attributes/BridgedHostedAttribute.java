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

package info.julang.hosting.attributes;

import info.julang.memory.value.AttrValue;
import info.julang.memory.value.StringValue;
import info.julang.typesystem.jclass.JClassMember;

/**
 * <pre><code> // Type I: bridged interface based on registration. Complex adoption with optimized performance.
 * attribute Bridged {
 *   string apiset;
 *   string name;
 * }</code></pre>
 */
public class BridgedHostedAttribute extends HostedAttribute {

	private final static String ex_field_apiset = "apiset";
	
	private final static String ex_field_name = "name";

	private String apiset;
	
	private String name;
	
	public BridgedHostedAttribute(AttrValue av, JClassMember member) {
		super(HostedAttributeType.BRIDGED);
		
		StringValue v1 = StringValue.dereference(av.getMemberValue(ex_field_apiset));
		apiset = v1 != null ? v1.getStringValue() : null;
		
		StringValue v2 = StringValue.dereference(av.getMemberValue(ex_field_name));
		name = v2 != null ? 
				v2.getStringValue() : 
				member != null ? 
					member.getName() : 
					null;
	}

	public String getApiset() {
		return apiset;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public void inheritFrom(HostedAttribute classHa) {
		if(this.apiset != null && !"".equals(this.apiset)){
			return;
		}	
		
		super.inheritFrom(classHa);
		
		BridgedHostedAttribute parent = (BridgedHostedAttribute) classHa;
		this.apiset = parent.apiset;	
	}
	
}
