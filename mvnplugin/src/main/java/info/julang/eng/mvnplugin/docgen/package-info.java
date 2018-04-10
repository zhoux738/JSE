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

/**
 * Utilities to extract documentation from Julian scripts.
 * <p>
 * In Julian scripts, block comments are also used as formal documentation. The rule is
 * for each documentation-worthy element, including top-level types, type members (
 * constructors, methods, fields), the logically immediately preceding block comment 
 * contains documentation for that element.
 * <p>
 * For the following Julian code, <pre><code> /*
 *  * A common object contains ....
 *  *
 *  * To pass a [stream](type: System.IO.Stream) to the processor, ...
 *  * 
 *  * [see: System.IO.Stream]
 *  *&#47;
 * class MyClass {
 * 
 *     /*
 *      * A method to ...
 *      *
 *      * [param: capacity] The total capacity of the container
 *      * [return] a value indicating the success of processing
 *      *&#47;
 *     bool fun(int capacity){
 *     
 *     }
 * }</code></pre>
 * This plugin will generate JSON data file containing
 * <pre><code> {
 *  "name" : "MyClass",
 *  "parent" : "",
 *  "interfaces" : 
 *      [
 *      ],
 *  "type" : "class",
 *  "visibility" : "public",
 *  "summary" : "A common object contains ....\n\nTo pass a [stream](type: System.IO.Stream) to the processor, ...",
 *  "see" : 
 *      [
 *          { "module" : "System.IO", "name" : "Stream" }
 *      ],
 *  "ctors" : 
 *      [
 *      ],
 *  "methods" : 
 *      [
 *          { 
 *              "name" : "fun",
 *              "summary" : "A method to ...",
 *              "visibility" : "public",
 *              "static" : false,
 *              "params" : 
 *                  [
 *                      {
 *                          "name" : "capacity", 
 *                          "type" : { "module" : "", "name" : "int" }, 
 *                          "description" : "The total capacity of the container"
 *                      }
 *                  ],
 *              "return" : 
 *                  {
 *                      "type" : { "module" : "", "name" : "bool" }, 
 *                      "description" : "a value indicating the success of processing"
 *                  },
 *              "throws" :
 *                  [
 *                  ]    
 *          }
 *      ],
 *  "fields" : 
 *      [
 *      ]
 * }
 * </code></pre>
 * For more comprehensive documentation format, see {@link DocModel}.
 * 
 * @author Ming Zhou
 */
package info.julang.eng.mvnplugin.docgen;