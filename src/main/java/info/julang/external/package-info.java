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
 * Package <b><code>info.julang.external</code></b> defines the interfaces and public-facing classes
 * that can be safely referenced and accessed by the users.
 * <p/>
 * An instance of Julian script engine is supposed to run in an isolated realm within the confines of JVM runtime. 
 * This makes it possible for multiple engine instances running together without causing conflicts with each other
 * with regards to engine-wide shared resources and common facilities. One example of such resource is the statically
 * initialized built-in and foundation classes. The isolation is achieved by {@link EngineFactory}, which loads  
 * engine internals through {@link EngineComponentClassLoader a distinct class loader} that reverts the default 
 * delegation model.
 * <p/>
 * The classes in this package, and its sub-packages, are the only classes (along with some utility classes) under 
 * prefix "info.julang" that can be directly referenced from the caller's world. For example, {@link 
 * EngineFactory} can be safely loaded from the default class loader, or whatever class loader that loads the user's 
 * class. In contrast, any attempt to cast the engine, components, and any of their derivatives created by this 
 * factory to a more concrete form, such as {@link info.julang.execution.simple.SimpleScriptEngine 
 * SimpleScriptEngine}, would fail due to the two classes not being loaded by the same class loader.
 */
package info.julang.external;