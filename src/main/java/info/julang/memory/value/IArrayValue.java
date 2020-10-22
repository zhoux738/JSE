package info.julang.memory.value;

import info.julang.execution.threading.ThreadRuntime;
import info.julang.memory.value.indexable.IIndexable;
import info.julang.memory.value.indexable.JIndexableValue;
import info.julang.memory.value.iterable.IIterator;
import info.julang.typesystem.JType;

/**
 * An umbrella interface that marks the object as possessing array semantics. As long as the object
 * implements this interface it should be considered a Julian array intrinsically.
 * <p>
 * The need of this interface is mainly due to the existence of two kinds of arrays: Julian array and 
 * JVM array. A Julian array is held inside an {@link ArrayValue array value}; a JVM array can be 
 * bound with a variable in the form of {@link HostedArrayValue hosted value}. Array value and hosted 
 * array value are not inherently compatible because their associated types are {@link 
 * info.julang.typesystem.jclass.builtin.JArrayType JArrayType} and {@link 
 * info.julang.typesystem.jclass.builtin.HostedType HostedType}, respectively. So anywhere we need 
 * to operate across these two types of values we must use an interface that encompasses all of array 
 * operations, which is exactly this interface. As a rule of thumb, whenever one is tempted to cast to
 * {@link ArrayValue}, consider casting to this interface instead.
 * 
 * @author Ming Zhou
 */
public interface IArrayValue extends JIndexableValue {

	/**
	 * Get the array type. The return type is intentionally not made 
	 * {@link info.julang.typesystem.jclass.builtin.JArrayType JArrayType} 
	 * since this needs to overlap with {@link JValue}.
	 */
	JType getType();
	
	/**
	 * Converts to an {@link IIndexable indexer}. Overlapping with {@link JValue}.
	 */
	IIndexable asIndexer();
	
	/**
	 * Converts to an {@link IIterator iterator}. Overlapping with {@link JValue}.
	 */
	IIterator asIterator();
	
	/**
	 * Returns true if this is an 1-dimensional array containing primitive Julian type.
	 */
	boolean isBasicArray();
	
	/**
	 * Sort the array in place.
	 * 
	 * @param rt thread runtime
	 * @param desc true to sort in descending order, false ascending.
	 */
	void sort(ThreadRuntime rt, boolean desc);
}
