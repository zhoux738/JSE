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

package info.julang.interpretation.errorhandling;


public enum KnownJSException {

	// Any enum type defined here must be also defined in a corresponding Julian file.

	Argument("System.ArgumentException"),
	ArrayOutOfRange("System.ArrayOutOfRangeException"),
	ClassLoading("System.ClassLoadingException"),
	DivByZero("System.DivByZeroException"),
	Exception("System.Exception"),
	HostingPlatform("System.HostingPlatformException"),
	IllegalAttributeUsage("System.IllegalAttributeUsageException"),
	IllegalAssignment("System.IllegalAssignmentException"),
	IllegalCasting("System.IllegalCastingException"),
	IllegalMemberAccess("System.IllegalMemberAccessException"),
	IllegalModule("System.IllegalModuleException"),
	IllegalTypeAccess("System.IllegalTypeAccessException"),
	IllegalState("System.IllegalStateException"),
	MissingRequirement("System.MissingRequirementException"),
	NullReference("System.NullReferenceException"),
	OutOfMemory("System.OutOfMemoryException"),
	PlatformClassLoading("System.PlatformClassLoadingException"), // : System.ClassLoadingException
	PlatformOriginal("System.PlatformOriginalException"),
	StackOverflow("System.StackOverflowException"),
	TypeIncompatible("System.TypeIncompatibleException"),
	UndefinedVariableName("System.UndefinedVariableNameException"),
	UnknownMember("System.UnknownMemberException"),
	UnknownType("System.UnknownTypeException"),
	
	BadSyntax("System.Lang.BadSyntaxException"),
	CyclicDependency("System.Lang.CyclicDependencyException"),
	DuplicateSymbol("System.Lang.DuplicateSymbolException"),
	IllegalLiteral("System.Lang.IllegalLiteralException"),
	IllegalOperand("System.Lang.IllegalOperandException"),
	UndefinedSymbol("System.Lang.UndefinedSymbolException"),
	NamespaceConflict("System.Lang.NamespaceConflictException"),
	RuntimeCheck("System.Lang.RuntimeCheckException"),

	IO("System.IO.IOException"),
    
    Socket("System.Network.SocketException"),
    Network("System.Network.NetworkException"),
	
	ConcurrentModification("System.Collection.ConcurrentModificationException"),
	
	ReflectedInvocation("System.Reflection.ReflectedInvocationException"),
	
	UnrecognizedRegex("System.Util.UnrecognizedRegexException"), 
	
	;
	
	private String fullName;
	
	private KnownJSException(String fullName){
		this.fullName = fullName;
	}

	public String getFullName() {
		return fullName;
	}
}
