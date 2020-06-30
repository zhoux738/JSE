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

package info.julang.modulesystem.prescanning.lazy;

import org.antlr.v4.runtime.Token;

import info.julang.interpretation.BadSyntaxException;
import info.julang.interpretation.errorhandling.IHasLocationInfo;
import info.julang.interpretation.syntax.ClassDeclInfo;
import info.julang.interpretation.syntax.ClassSubtype;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.langspec.ast.JulianLexer;
import info.julang.modulesystem.naming.FQName;
import info.julang.modulesystem.prescanning.LazyClassDeclInfo;
import info.julang.modulesystem.prescanning.RawScriptInfo;
import info.julang.scanner.ITokenStream;
import info.julang.scanner.ReadingUtility;
import info.julang.scanner.TokenStream;
import info.julang.typesystem.JType;
import info.julang.typesystem.VoidType;
import info.julang.typesystem.basic.BoolType;
import info.julang.typesystem.basic.ByteType;
import info.julang.typesystem.basic.CharType;
import info.julang.typesystem.basic.FloatType;
import info.julang.typesystem.basic.IntType;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.builtin.JStringType;

/**
 * A fast parser that linearly (instead of tree-based) parses a script off of a stream.
 * 
 * @author Ming Zhou
 */
public final class StreamBasedSyntaxHelper {

	/**
	 * Read the following tokens as class declaration.
	 * <p>
	 * If it is a class declaration, this method stops right before the class body, namely the first '{'. 
	 * Otherwise the PC remains unchanged.
	 * 
	 * @param stream
	 * @throws BadSyntaxException if the declaration is syntactically incorrect.
	 * @return a {@link ClassDeclInfo} instance containing class name and modifiers.
	 */
	public static LazyClassDeclInfo parseClassDeclaration(ITokenStream stream, RawScriptInfo info){
		String moduleName = info.getModuleName();
		LazyClassDeclInfo declInfo = new LazyClassDeclInfo(info);
		declInfo.setLocationInfo(info.getScriptFilePath(), stream);
	
		Token tok = null;
		boolean done = false;
		boolean attrScanned = false;
		while(!done && (tok = stream.next()).getType() != JulianLexer.EOF){
			switch(tok.getType()){
			case JulianLexer.ABSTRACT:
				if(declInfo.isAbstract()){
					resetAndThrow(stream, "Duplicated modifiers: abstract.", declInfo);
					return null;
				}
				declInfo.setAbstract();
				break;
			case JulianLexer.FINAL:
				if(declInfo.isFinal()){
					resetAndThrow(stream, "Duplicated modifiers: final.", declInfo);
					return null;
				}
				declInfo.setFinal();
				break;
			case JulianLexer.STATIC:
				if(declInfo.isStatic()){
					resetAndThrow(stream, "Duplicated modifiers: static.", declInfo);
					return null;
				}
				declInfo.setStatic();
				break;
			case JulianLexer.PUBLIC:
			case JulianLexer.PROTECTED:
			case JulianLexer.PRIVATE:
			case JulianLexer.INTERNAL:
				if(declInfo.isAccessibilitySet()){
					resetAndThrow(stream, "Multiple accessbility modifiers.", declInfo);
					return null;
				}
				declInfo.setAccessibility(Accessibility.parse(tok));
				break;
			case JulianLexer.ENUM:
				ClassSubtype stype = ClassSubtype.ENUM;
				parseClassName(stream, declInfo, moduleName, stype);
				done = true;
				break;
			case JulianLexer.ATTRIBUTE:
				stype = ClassSubtype.ATTRIBUTE;
				parseClassName(stream, declInfo, moduleName, stype);
				done = true;
				break;
			case JulianLexer.CLASS:
				stype = ClassSubtype.CLASS;
				parseClassName(stream, declInfo, moduleName, stype);
				done = true;
				break;
			case JulianLexer.INTERFACE:
				stype = ClassSubtype.INTERFACE;
				parseClassName(stream, declInfo, moduleName, stype);
				done = true;
				break;
			case JulianLexer.LEFT_BRACKET:
				// Attributes. Make sure this happens only once.
				if(!attrScanned){
					stream.backoff();
					// Read each [Attribute(..)]
					while(stream.peek().getType() == JulianLexer.LEFT_BRACKET){
						skipAttributeDeclInfo(stream);
					}
					attrScanned = true;
				} else {
					throw new BadSyntaxException("'[' appears at illegal place in class definition.", declInfo);
				}
				continue;
			default:
				resetAndThrow(stream, "Illegal token scanned for class definition: " + tok.getText() + ".", declInfo);
				break;
			}
		}
		
		if(tok.getType() == JulianLexer.EOF){
			return null;
		}
		
		if(!declInfo.isAccessibilitySet()){
			declInfo.setAccessibility(Accessibility.PUBLIC);
		}
		
		// If it is a class/interface, collect inheritance information.
		ClassSubtype subtyp = declInfo.getSubtype();
		if(subtyp == ClassSubtype.CLASS || subtyp == ClassSubtype.INTERFACE){
			tok = stream.peek();
			if(tok.getType() == JulianLexer.COLON){
				// Inheritance
				stream.next();
				while(true){
					ParsedTypeName parent = parseTypeName(
						stream, 
						false, // allowBasic
						false, // allowVoid
						false, // allowAny
						true, 
						false);
					if(parent == null){
						throw new BadSyntaxException("Illegal type to inherit from.");
					}
					declInfo.addParentTypeName(parent);
					if(stream.peek().getType() == JulianLexer.COMMA){
						stream.next();
						continue;
					} else {
						break;
					}
				}
			}
		}

		tok = stream.peek();
		if(tok.getType() != JulianLexer.LEFT_CURLY){
			throw new BadSyntaxException("Declaration of " + declInfo.getSubtype().name().toLowerCase() + 
				"  must contain a class body enclosed by { and }, but saw '" + tok.getText() + "'.");
		}
		
		return declInfo;
	}
	
	/**
	 * Skip an Attribute section in stream, enclosed by [ and ].
	 * @param stream
	 * @return
	 */
	private static void skipAttributeDeclInfo(ITokenStream stream) {
		ReadingUtility.locatePairedToken(stream, JulianLexer.LEFT_BRACKET, JulianLexer.RIGHT_BRACKET, false, false);
	}

	private static void parseClassName(
		ITokenStream stream, ClassDeclInfo declInfo, String moduleName, ClassSubtype stype){
		Token tok = stream.next();
		if(tok.getType() == JulianLexer.IDENTIFIER){
			String simpleName = tok.getText();
			declInfo.setName(simpleName);
			declInfo.setFQName(new FQName(moduleName, simpleName));
			declInfo.setSubtype(stype);
		} else {
			resetAndThrow(stream, "Illegal token scanned for class definition: " + tok.getText() + ".", declInfo);
		}
	}

	/**
	 * Read the following tokens as a type name.
	 * <p/>
	 * The tokens may constitute a type name only if it is in form id(.id)*(\[\])* and is followed 
	 * by another id, such as "<code>A.B x</code>" or "<code>A.B[][] y</code>".
	 * <p/>
	 * If the parse is successful, moves PC past the parsed type name. If not, PC remains unchanged.
	 * 
	 * @param stream
	 * @param fullParse if true, parse array dimension ([][]) and check the following token
	 * if false, only parse the id part (A.B)
	 * @return the parsed type name if it is a type name, null otherwise.
	 */
	public static ParsedTypeName parseTypeName(TokenStream stream, boolean fullParse){
		return parseTypeName(stream, true, false, true, !fullParse, fullParse);
	}
	
	/**
	 * 
	 * @param stream
	 * @param allowBasic if false, return null when seeing a basic type.
	 * @param allowVoid if false, return null when seeing a void type.
	 * @param allowAny if false, return null when seeing a var type.
	 * @param endBeforeArray if true, will end after the type strings are parsed (A.B.C)
	 * @param checkFollowingToken if true, will check if the following token is an identifier, and return null if it isn't.
	 * @return
	 */
	public static ParsedTypeName parseTypeName(
		ITokenStream stream, 
		boolean allowBasic, 
		boolean allowVoid,
		boolean allowAny,
		boolean endBeforeArray, 
		boolean checkFollowingToken){
		
		ParsedTypeName typeName;
		boolean untyped = false;
		
		// A
		Token tok = stream.next();
		JType type = null;
		switch(tok.getType()){
		case JulianLexer.INT: // int ...
			type = IntType.getInstance();
			break;
		case JulianLexer.BOOL: // bool ...
			type = BoolType.getInstance();
			break;		
		case JulianLexer.BYTE: // byte ...
			type = ByteType.getInstance();
			break;
		case JulianLexer.FLOAT: // float ...
			type = FloatType.getInstance();
			break;
		case JulianLexer.CHAR: // char ...
			type = CharType.getInstance();
			break;
		case JulianLexer.STRING: // string ...
			type = JStringType.getInstance();
			break;
		case JulianLexer.VOID:
			type = VoidType.getInstance();
			break;
		case JulianLexer.VAR:
			untyped = true;
			break;
		default:
			break;
		}
		
		if(type != null){
			if((type == VoidType.getInstance() && !allowVoid) || !allowBasic){
				return restoreAndReturn(false, stream, null); // bad syntax
			}
			typeName = new ParsedTypeName(type);
		} else if(untyped){
			typeName = ParsedTypeName.ANY;
		} else {
			if(tok.getType() != JulianLexer.IDENTIFIER){
				return restoreAndReturn(false, stream, null); // bad syntax
			}
			typeName = new ParsedTypeName(tok.getText());
			
			// .B
			while(true){
				tok = stream.peek();
				if(tok.getType() == JulianLexer.DOT){
					stream.next();
					tok = stream.next();
					if(tok.getType() != JulianLexer.IDENTIFIER){
						return restoreAndReturn(false, stream, typeName); // bad syntax
					} else {
						typeName.addSection(tok.getText());
						continue;
					}
				} else {
					break;
				}
			}
		}
		
		if(!endBeforeArray){
			// []
			if(typeName == ParsedTypeName.ANY && stream.peek().getType() == JulianLexer.LEFT_BRACKET){
				throw new BadSyntaxException("Cannot declare an untyped array.");
			}
			boolean succ = parseArrayDimension(stream, typeName);
			if(!succ){
				return null;
			}
		}
		
		tok = stream.peek();
		if (!checkFollowingToken || tok.getType() == JulianLexer.IDENTIFIER){
			return restoreAndReturn(true, stream, typeName); // A type is found since it is followed by an identifier.
		} else {
			return restoreAndReturn(false, stream, typeName);
		}
	}
	
	private static boolean parseArrayDimension(
		ITokenStream stream, ParsedTypeName typeName){
		
		Token tok = null;
		while(true){
			tok = stream.peek();
			if(tok.getType() == JulianLexer.LEFT_BRACKET){
				stream.next();
				tok = stream.next();
				if(tok.getType() != JulianLexer.RIGHT_BRACKET){
					ParsedTypeName result = restoreAndReturn(false, stream, typeName);
					return result != null ? true : false;
				} else {
					typeName.addArrayDimension();
					continue;
				}
			} else {
				break;
			}
		}
		
		return true;
	}

	/**
	 * Reset the stream and throws {@link BadSyntaxException}
	 * @param stream
	 * @param msg
	 */
	private static void resetAndThrow(ITokenStream stream, String msg, IHasLocationInfo linfo) {
		stream.reset();
		throw new BadSyntaxException(msg, linfo);
	}
	
	/**
	 * If <b><code>result</code></b> is true, returns <b><code>toReturn</code></b>. 
	 * If false, resets <b><code>stream</code></b> and returns null. 
	 * 
	 * @param result
	 * @param stream
	 * @param toReturn
	 * @return
	 */
	private static <T> T restoreAndReturn(boolean result, ITokenStream stream, T toReturn) {
		if(result){
			return toReturn;
		} else {
			stream.reset();
			return null;
		}
	}
}