package info.jultest.test.values;

import info.julang.memory.JSEOutOfMemoryException;
import info.julang.memory.IStored;
import info.julang.memory.MemoryArea;
import info.julang.memory.MemoryAreaType;
import info.julang.memory.MemoryOpreationException;

public class DummyMemoryArea implements MemoryArea {

	@Override
	public MemoryAreaType getKind() {
		return MemoryAreaType.STATIC;
	}

	@Override
	public boolean allocate(IStored value) throws JSEOutOfMemoryException {
		return true;
	}

	@Override
	public boolean reallocate(IStored value)
		throws JSEOutOfMemoryException, MemoryOpreationException {
		return true;
	}

	@Override
	public boolean deallocate(IStored value)
		throws MemoryOpreationException {
		return true;
	}

	@Override
	public boolean isRecycled() {
		return false;
	}

}
