package info.jultest.test.threading;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import info.julang.execution.threading.SequenceNumberTracker;

public class ComponentTests {

    @Test
    public void sequenceNumberTrackerTest() {
        SequenceNumberTracker idSequencer = new SequenceNumberTracker();
        int i = idSequencer.obtain();
        assertEquals(0, i);
        i = idSequencer.obtain();
        assertEquals(1, i);
        i = idSequencer.obtain();
        assertEquals(2, i);
        idSequencer.recycle(1);
        i = idSequencer.obtain();
        assertEquals(1, i);
        i = idSequencer.obtain();
        assertEquals(3, i);
        idSequencer.recycle(0);
        idSequencer.recycle(1);
        idSequencer.recycle(3);
        i = idSequencer.obtain();
        assertEquals(0, i);
        i = idSequencer.obtain();
        assertEquals(1, i);
        i = idSequencer.obtain();
        assertEquals(3, i);
    }
}
