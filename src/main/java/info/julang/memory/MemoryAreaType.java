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

package info.julang.memory;

/**
 * Various types of memory region.
 * 
 * @author Ming Zhou
 *
 */
public enum MemoryAreaType {

	/**
	 * This memory area is for heap storage in scripting engine. 
	 */
	HEAP,
	
	/**
	 * This memory area is for stack storage in scripting engine. 
	 * Note one scripting engine can have more than one stack area.
	 */
	STACK,
	
	/**
	 * This memory area is for static storage in scripting engine. 
	 */
	STATIC,
	
	/**
	 * This memory area is for permanent storage in scripting engine. 
	 */
	PERM,
}
