package info.julang.memory.value.operable;

import info.julang.external.interfaces.JValueKind;
import info.julang.memory.value.JValue;

import java.util.Comparator;

public abstract class ValueComparator implements Comparator<JValue>{

	private boolean desc;
	
	public ValueComparator(boolean desc){
		this.desc = desc;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public int compare(JValue v1, JValue v2) {
		v1 = v1.deref();
		v2 = v2.deref();
		
		int res = 0;
		
		// 1) If any one implements Comparable, call it
		if (v1 instanceof Comparable<?>){
			res = ((Comparable<JValue>)v1).compareTo(v2);
		} else if (v2 instanceof Comparable<?>){
			res = -((Comparable<JValue>)v2).compareTo(v1);
		} 

		// 2) If one value is Object, it might implement System.Util.Comparable. So call _compareObjs to give it a try.
		if (res == 0 && (v1.getKind() == JValueKind.OBJECT || v2.getKind() == JValueKind.OBJECT)) {
			res = compareObjectValues(v1, v2);
		}
		
		return desc ? -1 * res : res;
	}
	
	/**
	 * Compare two values. At least one of them is Object.
	 */
	protected abstract int compareObjectValues(JValue v1, JValue v2);
}
