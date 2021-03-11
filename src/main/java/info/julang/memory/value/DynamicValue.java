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

package info.julang.memory.value;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import info.julang.external.exceptions.JSEError;
import info.julang.external.interfaces.JValueKind;
import info.julang.memory.MemoryArea;
import info.julang.typesystem.IllegalMemberAccessException;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.builtin.JDynamicType;

/**
 * DynamicValue represents a dynamic object in Julian scripts. 
 * <p>
 * A special case of {@link ObjectValue}, member access to DynamicValue is handled without type check. Retrieving 
 * a non-existent member returns <code>null</code>; setting a non-existent member or overwriting an existing member
 * with different type is always OK.
 * 
 * @author Ming Zhou
 */
public class DynamicValue extends ObjectValue implements Iterable<Entry<String, JValue>> {
	
	private Map<String, JValue> map;
	private boolean canOverwite;
	
	// Read from config object
	private boolean throwOnUndefined;
	private boolean autobind;
	private boolean sealAfterInit;
	
	/**
	 * Create a new empty dynamic value.
	 * 
	 * @param memory
	 * @param typ
	 */
	public DynamicValue(MemoryArea memory, JType typ) {
		super(memory, typ, false);
		this.map = new HashMap<String, JValue>();
		this.canOverwite = true;
	}
	
	/**
	 * Initialize with a config object.
	 * 
	 * @param config A dynamic object that may contain certain flags.
	 */
	public void init(DynamicValue config) {
		this.throwOnUndefined = tryGetBool(config, "throwOnUndefined");
		this.autobind = tryGetBool(config, "autobind");
		this.sealAfterInit = tryGetBool(config, "sealed");
	}
	
	private static boolean tryGetBool(DynamicValue config, String configName) {
		JValue val = config.get(configName);
		if (val != null) {
			val = val.deref();
			if (val.getKind() == JValueKind.BOOLEAN) {
				return ((BoolValue)val).getBoolValue();
			}
		}
		
		return false;
	}

	@Override
	public JValueKind getKind() {
		return JValueKind.OBJECT;
	}

//	@Override
//	public JType getType() {
//		return JDynamicType.getInstance();
//	}
	
	@Override
	protected void initialize(JType type, MemoryArea memory) {
		super.initialize(type, memory);
	}
	
	@Override
	public JValueKind getBuiltInValueKind(){
		return JValueKind.DYNAMIC;
	}
	
	public boolean shouldThrowIfNotExist() {
		return this.throwOnUndefined;
	}

	public boolean shouldBindToAnyFunction() {
		return autobind;
	}
	
	/**
	 * Get JValue by key. Returns null if not set.
	 * @param key
	 * @return
	 */
	public JValue get(String key) {
		return map.get(key);
	}
	
	public void set(String key, JValue val) {
		if (val == null) {
			throw new JSEError("Tried to set null to member '" + key + "'.", DynamicValue.class);
		}
		
		if (canOverwite) {
			map.put(key, val);
		} else {
			throw IllegalMemberAccessException.overwriteSealedDynamicEx(key);
		}
	}

	public int count() {
		return map.size();
	}

	@Override
	public Iterator<Entry<String, JValue>> iterator() {
		return this.map.entrySet().iterator();
	}
	
	//------ Information used to decide whether to bind a Function property added through initializer ------//

	private Set<Integer> bindingIndices;
	
	public void addIndexToBind(int i) {
		if (bindingIndices == null) {
			bindingIndices = new HashSet<>();
		}
		
		bindingIndices.add(i);
	}
	
	public boolean shouldBindByIndex(int index) {
		if (this.autobind) {
			return true;
		}
		
		if (bindingIndices != null) {
			return bindingIndices.contains(index);
		}
		
		return false;
	}

	public void completeInit() {
		if (sealAfterInit) {
			canOverwite = false;
		}
		
		bindingIndices = null;
	}
}
