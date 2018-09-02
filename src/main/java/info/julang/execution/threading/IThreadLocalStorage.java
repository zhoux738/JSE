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

/**
 * A storage interface to be exclusively used by a single thread. 
 * 
 * @author Ming Zhou
 */
public interface IThreadLocalStorage {

    /**
     * Put an object to thread-local storage if it's the specified key 
     * has not been added yet. The object will be provided by the factory.
     * 
     * @param key
     * @param factory
     * @throws IllegalArgumentException factory returns a null value.
     */
    Object putLocal(String key, IThreadLocalObjectFactory factory);
    
    /**
     * Get the object associated with this key. 
     * 
     * @param key
     * @return null if the object is not found.
     */
    Object getLocal(String key);
    
    //----------------- Known keys (used by engine internals) -----------------//
    
    public static String KEY_ASYNC_SOCKET_SESSION = "ASYNC_SOCKET_SESSION";
}
