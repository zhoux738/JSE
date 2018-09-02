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

package info.julang.execution.threading;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A thread-safe utility class to generate reusable sequence number.
 * <p>
 * This class can either generate a new sequence number or re-issue a recycled one. It will always try to
 * generate a number as small as possible. The caller should release the number back this tracker after use. 
 * 
 * @author Ming Zhou
 */
public class SequenceNumberTracker {

    private final AtomicInteger idSequencer;
    private final PriorityBlockingQueue<Integer> queue;
    
    public SequenceNumberTracker(){
        idSequencer = new AtomicInteger(-1);
        queue = new PriorityBlockingQueue<Integer>();
    }
    
    public int obtain(){
        Integer res = queue.poll();
        if (res == null){
            res = idSequencer.incrementAndGet();
        }
        
        return res;
    }
    
    public void recycle(int val){
        queue.offer(val);
    }
}
