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
package info.julang.typesystem.jclass.jufc.System.Reflection;

import info.julang.execution.Argument;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.execution.threading.ThreadRuntimeHelper;
import info.julang.execution.threading.ThreadRuntimeHelper.IObjectPopulater;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.AttrValue;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.TypeValue;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.JClassConstructorMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.MemberKey;
import info.julang.typesystem.jclass.builtin.JAttributeBaseType;

import java.util.List;

/**
 * A mix-in to facilitate code sharing among class member classes.
 * 
 * @author Ming Zhou
 */
public class ScriptMemberBase {

	protected ArrayValue getParams(ThreadRuntime rt, final JParameter[] params) {
		// 1) Load System.Reflection.Parameter
		JClassType sysReflParamTyp = (JClassType)ThreadRuntimeHelper.loadSystemType(rt, ScriptParam.FQCLASSNAME);
		JClassConstructorMember sysReflParamTypCtor = sysReflParamTyp.getClassConstructors()[0];
		
		// 2) Get all ctors for this Type
		ArrayValue av = ThreadRuntimeHelper.createAndPopulateObjectArrayValue(
			rt, params.length - 1, sysReflParamTyp, sysReflParamTypCtor, 
			new IObjectPopulater(){

				@Override
				public Argument[] getArguments(int index) {
					return new Argument[0];
				}

				@Override
				public void postCreation(int index, ObjectValue ov) {
					JParameter param = params[index + 1];
					
					HostedValue hv = (HostedValue)ov;
					ScriptParam sc = (ScriptParam)hv.getHostedObject();
					sc.setParam(param);
				}
			});
		
		return av;
	}
	
	protected ArrayValue getAttributes(ThreadRuntime rt, ICompoundType deftyp, MemberKey mkey) {
		TypeValue tv = rt.getTypeTable().getValue(deftyp.getName());
		List<AttrValue> avs = tv.getMemberAttrValues(mkey);
		if (avs == null || avs.size() == 0){
			return null;
		}

		JValue[] vals = new JValue[avs.size()];
		avs.toArray(vals);

		JClassType typ = (JClassType)ThreadRuntimeHelper.loadSystemType(rt, JAttributeBaseType.Name);
		ArrayValue array = ThreadRuntimeHelper.createAndPopulateArrayValue(rt, typ, vals);
		
		return array;
	}
}
