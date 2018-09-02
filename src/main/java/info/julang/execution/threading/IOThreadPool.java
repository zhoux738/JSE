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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import info.julang.interpretation.internal.FuncCallExecutor;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassType;

/**
 * A pool for IO continuation threads.
 * <p>
 * This class takes a compromised design between Node.js and .NET. It maintains a small pool for
 * IO continuation threads which are shared among all the asynchronous operations. This means 
 * blocking on an IO thread will potentially cause deadlocking for other unrelated threads.
 * <p>
 * The pool is empty at the start and will grow monotonically till the max capacity.
 * 
 * @author Ming Zhou
 */
public class IOThreadPool {

    private List<IOThreadWrapper> pool;
    private Random rand;
    private int capacity;
    private Object lock;

    public IOThreadPool(){
        capacity = Math.max(Runtime.getRuntime().availableProcessors(), 8);
        pool = new ArrayList<IOThreadWrapper>(capacity);
        rand = new Random();
        this.lock = new Object();
    }
    
    /**
     * Fetch an IO continuation thread.
     * <p>
     * An IO-continuation thread is a special worker thread that handles asynchronously posted works 
     * from a platform callback after certain IO operations. For example, Java's async file API has 
     * a CompletionHandler class whose methods get invoked after a chunk of data has been read from 
     * the source. These methods will be running on a JVM worker thread that's likely different from 
     * the one where readAsync() was called. Some OSes, such as Windows, even have a dedicated thread 
     * pool for this purpose. How JVM is interacting with this underlying mechanism is irrelevant to 
     * our discussion here.
     * <p>
     * The main problem we are trying to solve here is that once the callback method is invoked, user's 
     * code will be running on a thread outside Julian's thread manager. Not only will this break 
     * threading API, it may also cause lingering thread after the termination of script engine. To 
     * remedy this, we must move the work from JVM-issued thread to our own thread.
     * <p>
     * IOThreadWrapper creates standard Julian thread that continuously polls a work queue and executes 
     * each work item inline. Exceptions thrown from work items won't break the thread.
     *   
     * @param rt The current thread runtime in which the IO thread is needed. However, a system thread
     * runtime will be created to access to the IO thread.
     * @param outOfCapacity If true, will create a new IO-continuation thread outside the pool. The 
     * caller is responsible for completing this thread after use.
     */
    IOThreadHandle fetch(ThreadRuntime rt, boolean outOfCapacity){
        IOThreadWrapper handle = null;
        if (outOfCapacity) {
            handle = createNew(rt, true); 
        }

        if (handle == null && pool.size() < capacity) {
            synchronized(lock){
                if (pool.size() < capacity) {
                    // We still have room for more
                    handle = createNew(rt, false); 
                    pool.add(handle);
                }
            }
        }
        
        if (handle == null) {
            int size = pool.size();
            int index = rand.nextInt(size);
            handle = pool.get(index);
        }
        
        return handle;
    }
    
    /**
     * Terminate all IO threads in the pool.
     */
    void terminate(){
        for (IOThreadWrapper wrapper : pool) {
            wrapper.abort();
        }
    }
    
    private IOThreadWrapper createNew(ThreadRuntime rt, boolean outOfCapacity){
        if (!(rt instanceof SystemInitiatedThreadRuntime)) {
            rt = new SystemInitiatedThreadRuntime(rt);
        }
        JClassType jct = (JClassType)ThreadRuntimeHelper.loadSystemType(rt, IOThreadWrapper.FullName);
        JClassMethodMember method = jct.getStaticMethodMembersByName(IOThreadWrapper.MethodName_createIOThread).getFirst();
        FuncCallExecutor func = new FuncCallExecutor(rt);
        ObjectValue ov = (ObjectValue)func.invokeMethodInternal(
            method.getMethodType(), 
            IOThreadWrapper.MethodName_createIOThread, 
            new JValue[0], 
            null).deref();
        
        IOThreadWrapper wrapper = new IOThreadWrapper(rt, ov, outOfCapacity);
        wrapper.start();
        return wrapper;
    }
}
