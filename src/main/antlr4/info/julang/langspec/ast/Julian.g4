///////////////////////////////////// MIT License /////////////////////////////////////////

// Copyright (c) 2017 Ming Zhou
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

grammar Julian;

////////////////////////////////////////// Notes //////////////////////////////////////////

// 1) The file must be placed under a directory corresponding to the desired package 
//    name to emit the following header:
//    package info.julang.langspec.ast;
//
// 2) All the lexer rules are in UPPER CASE; all the parser rules lower case. All the
//    fragments are prefixed by "FRG_".
//
// 3) This file is heavily inspired by the following ANTLR grammar definitions:
//    https://github.com/antlr/grammars-v4/tree/master/csharp (originally by Christian Wulf)
//    https://github.com/antlr/grammars-v4/blob/master/c (originally by Sam Harwell)
//    https://github.com/antlr/grammars-v4/blob/master/java (originally by Terence Parr et al.)
// 
// 4) The expression rules exploit the implicit precedences based on the order of production 
//    rules. It should be noted that the C-like grammar is not an LL one, but all the 
//    ambiguities are either resolvable by applying certain tricks, or can be neglected for 
//    the convenient results.

//////////////////////////////////////// Category /////////////////////////////////////////
    
// I. LEXER
// 1. Comments
// 2. Omittable
// 3. Keywords
// 4. Identifiers
// 5. Numbers
// 6. Text Literals
// 7. Operators

// II. PARSER
// 1. Types
// 2. Argument(s)
// 3. Expression
// 4. Statement
// 5. Executable
// 6. Module
// 7. Type
// 8. Program

////////////////////////////////////////// Rules //////////////////////////////////////////

//////////////////////////////////////// I. Lexer /////////////////////////////////////////

@lexer::members {
public static final int JULDOC = 2;
public static final int SKIPPED = 3;

private Token lastToken = null;

@Override
public Token nextToken() {
    Token next = super.nextToken();

    if (next.getChannel() == Token.DEFAULT_CHANNEL) {
        this.lastToken = next;
    }

    return next;
}

private boolean treatAsRegex() {
    if (this.lastToken == null) {
        return true;
    }
    
    switch (this.lastToken.getType()) {
    	// references
        case JulianLexer.IDENTIFIER:
        case JulianLexer.THIS:
        case JulianLexer.SUPER:
        case JulianLexer.NULL:
        // built-in types
        case JulianLexer.BOOL:
        case JulianLexer.BYTE:
        case JulianLexer.INT:
        case JulianLexer.CHAR:
        case JulianLexer.STRING:
        case JulianLexer.FLOAT:
        case JulianLexer.VOID:
	    // pair closer
        case JulianLexer.RIGHT_BRACKET:
        case JulianLexer.RIGHT_CURLY:
        case JulianLexer.RIGHT_PAREN:
		// values
        case JulianLexer.CHAR_LITERAL:
        case JulianLexer.STRING_LITERAL:
        case JulianLexer.REGEX_LITERAL:
        case JulianLexer.INTEGER_LITERAL:
        case JulianLexer.REAL_LITERAL:
            return false;
        default:
            return true;
	}
}

}

// 1. Comments

BLOCK_COMMENT
  : '/*' .*? '*/' -> channel(JULDOC)
  ;

LINE_COMMENT
  : '//' ~[\r\n]* -> channel(SKIPPED)
  ;
  
// 2. Omittable

WHITESPACE 
  : [ \t]+ -> channel(SKIPPED)
  ;
  
NEW_LINE 
  : ( '\r' '\n'?
    | '\n' ) -> channel(SKIPPED)
  ;
 
// 3. Keywords

ABSTRACT :     'abstract';
ANY:           'any';
AS:            'as';             // RESERVED
ATTRIBUTE :    'attribute';
BOOL :         'bool';
BYTE :         'byte';
BREAK :        'break';
CASE :         'case';
CATCH :        'catch';
CHAR :         'char';
CLASS :        'class';
CONST :        'const';
CONTINUE :     'continue';
DEFAULT :      'default';
DO :           'do';
ELSE :         'else';
ENUM :         'enum';
EXPORT:        'export';         // RESERVED
FALSE :        'false';
FINAL :        'final';
FINALLY :      'finally';
FLOAT :        'float';
FOR :          'for';
FOREACH :      'foreach';
HOSTED:        'hosted';
IF :           'if';
IMPORT:        'import';
IN :           'in';
INCLUDE:       'include';        // RESERVED
INT :          'int';
INTERFACE :    'interface';
INTERNAL :     'internal';
IS :           'is';
MODULE:        'module';
NEW :          'new';
NULL :         'null';
PRIVATE :      'private';
PROTECTED :    'protected';
PUBLIC :       'public';
RETURN :       'return';
SEALED:        'sealed';         // RESERVED
STATIC :       'static';
STRING :       'string';
SUPER:         'super';
SWITCH :       'switch';
SYNC:          'sync';
THIS :         'this';
THROW :        'throw';
TRUE :         'true';
TRY :          'try';
TYPEOF :       'typeof';
VAR:           'var';
VOID :         'void';
VOLATILE :     'volatile';       // RESERVED
WHERE :        'where';          // RESERVED
WHILE :        'while';
  
// 4. Identifiers
// (Note identifiers must be defined after keywords)

IDENTIFIER
  : FRG_ID_CHAR_START FRG_ID_CHAR*
  ;

fragment FRG_ID_CHAR_START 
  : FRG_LETTER
  | '_'
  ;

fragment FRG_ID_CHAR 
  : FRG_LETTER
  | FRG_DIGIT
  | '_'
  ;
  
fragment FRG_LETTER
  : [a-zA-Z]
  ;
  
fragment FRG_DIGIT
  : [0-9]
  ; 

fragment FRG_HEXDIGIT
  : [0-9]
  | [a-f]
  | [A-F]
  ; 

fragment FRG_BINDIGIT
  : [0-1]
  ;
    
// 5. Numbers

INTEGER_LITERAL
  : FRG_DEC_NUM_LITERAL    // base-10 
  | FRG_HEX_NUM_LITERAL // base-16 "0x..."
  | FRG_BIN_NUM_LITERAL // base-2  "0b..."
  ;

fragment FRG_DEC_NUM_LITERAL
  : FRG_DIGIT+
  ;
  
fragment FRG_HEX_NUM_LITERAL
  : '0' 'x' FRG_HEXDIGIT+
  ;  

fragment FRG_BIN_NUM_LITERAL
  : '0' 'b' FRG_BINDIGIT+
  ;  
  
REAL_LITERAL // 0.1.0 - support decimal only, and no support for IEEE-754
  : FRG_DEC_NUM_LITERAL '.' FRG_DEC_NUM_LITERAL
  ;

// 6. Text Literals

CHAR_LITERAL 
  : '\'' FRG_CHAR '\''
  ;
  
fragment FRG_CHAR 
  : FRG_SINGLE_CHAR
  | FRG_ESCAPE_CHAR_FROM_CHAR_LITERAL
  ;

fragment FRG_SINGLE_CHAR
  : ~['\\\r\n]
  ;

fragment FRG_ESCAPE_CHAR_FROM_CHAR_LITERAL
  : FRG_ESCAPE_CHAR_COMMON
  | '\\\''
  ;
  
fragment FRG_ESCAPE_CHAR_FROM_STRING_LITERAL
  : FRG_ESCAPE_CHAR_COMMON
  | '\\"'
  ;
 
// Note: this will only recognize escape sequences in the script, such as \n, but will NOT
// convert them to the target character. The string it ended with will contain '\' and 'n',
// respectively, but not a '\n'. A further post-processing is required to make the conversion.
// (See https://github.com/antlr/antlr4/issues/211, closed by Sam Harwell)
fragment FRG_ESCAPE_CHAR_COMMON 
  : '\\\\'
  | '\\0'
  | '\\n'
  | '\\r'
  | '\\t' 
  | '\\f'    // \f, \v, \b are also special chars
  | '\\v'
  | '\\b'  
  ; 
 
STRING_LITERAL 
  : '"' FRG_LITERAL_CHAR* '"'
  ;
  
fragment FRG_LITERAL_CHAR 
  : FRG_STR_CHAR
  | FRG_ESCAPE_CHAR_FROM_STRING_LITERAL
  ;
  
fragment FRG_STR_CHAR
  : ~["\\]    // Note that different from other languages, we accept actual line breaker in Julian's string literal
  ;

// 7. Regex Literals

REGEX_LITERAL
  : '/' FRG_REGEX_LITERAL_CHAR+ { treatAsRegex() }? '/'
  ;

fragment FRG_ESCAPE_CHAR_FROM_REGEX_LITERAL
  : '\\' FRG_ESCAPE_CHAR_COMMON
  | '\\' [/.^$\|\[\]\(\)\*\+\?\\-] // Must sync with RG_CHAR in Regex.g4
  ;
   
fragment FRG_REGEX_LITERAL_CHAR 
  : ~[/\r\n\\]
  | FRG_ESCAPE_CHAR_FROM_REGEX_LITERAL
  ;

// 8. Operators

// Scoping
LEFT_CURLY :                  '{';
RIGHT_CURLY :                 '}';
LEFT_BRACKET :                '[';
RIGHT_BRACKET :               ']';
LEFT_PAREN :                  '(';
RIGHT_PAREN :                 ')';

// Delimiting
COMMA :                       ',';
SEMICOLON :                   ';';

// Integer arithmetic
PLUS :                        '+';
MINUS :                       '-';
MULTIPLY :                    '*';
DIVIDE :                      '/';
MODULO :                      '%';

// Comparison
EQUAL :                       '==';
NOT_EQUAL :                   '!=';
LT :                          '<';
GT :                          '>';
LT_EQ :                       '<=';
GT_EQ :                       '>=';

// Self unary op
INCREMENT :                   '++';
DECREMENT :                   '--';

// Logic
AND :                         '&&';
OR :                          '||';
NEGATION :                    '!';

// Bitwise
COMPLEMENT :                  '~';
BITWISE_AND :                 '&';
BITWISE_OR :                  '|';
BITWISE_XOR :                 '^';
BITWISE_LEFT_SHIFT :          '<<';
BITWISE_RIGHT_SHIFT :         '>>';

// Assignment
ASSIGN :                      '=';
PLUS_SELF :                   '+=';
MINUS_SELF :                  '-=';
MULTIPLY_SELF :               '*=';
DIVIDE_SELF :                 '/=';
MODULO_SELF :                 '%=';
BITWISE_AND_SELF :            '&=';
BITWISE_OR_SELF :             '|=';
BITWISE_XOR_SELF :            '^=';
BITWISE_LEFT_SHIFT_SELF :     '<<=';
BITWISE_RIGHT_SHIFT_SELF :    '>>=';

// Quotes
SINGLE_QUOTE :                '\'';
DOUBLE_QUOTE :                '"';

// Special
DOT : '.';
LAMBDA : '=>';
QMARK : '?';
COLON : ':';

/////////////////////////////////////// II. Parser ////////////////////////////////////////

// 1. Types

composite_id
  : IDENTIFIER ( DOT IDENTIFIER )*
  ;
  
// type may contain one or more array ranks
type 
  : base_type rank_specifier*
  ;

rank_specifier 
  : LEFT_BRACKET RIGHT_BRACKET
  ;
    
base_type
  : builtin_type
  | class_type
  ;
  
builtin_type 
    : INT            // int
    | FLOAT          // float
    | BYTE           // byte
    | STRING         // string
    | BOOL           // bool
    | CHAR           // char
    | VAR            // var
    | VOID           // void
    | ANY            // any
    ;
  
class_type 
    : composite_id
    ;
    
// 2. Argument(s)

argument_list 
    : argument ( COMMA argument )*
    ;
argument
    : expression
    ;
    
// 3. Expression

// 3.1. Assignment expression
// See expression rule

// 3.2. Lambda expression
// Also see expression rule

lambda_signature 
    : LEFT_PAREN RIGHT_PAREN
    | LEFT_PAREN lambda_parameter_list RIGHT_PAREN
    | IDENTIFIER
    ;
lambda_parameter_list 
    : lambda_parameter ( COMMA lambda_parameter )*
    ;
lambda_parameter 
    : type? IDENTIFIER
    ;
lambda_body 
    : expression
    | block
    ;

// 3.3 other expressions
// Operator precedence and associativity as defined in info.julang.langspec.Operators.java //

//    FUNCCALL           (10, Associativity.LEFT),
    
//    INDEX              (10, Associativity.LEFT),    // []
//    DOT                (10, Associativity.LEFT),    // .
    
//    NEW                (10, Associativity.RIGHT),
//    TYPEOF             (10, Associativity.LEFT),
    
//    NEGATE             (20, Associativity.RIGHT),   // !
//    INC                (20, Associativity.LEFT),    // ++
//    DEC                (20, Associativity.LEFT),    // --
//    CAST               (20, Associativity.RIGHT),   // (type)
    
//    MULTIPLY           (30, Associativity.LEFT),
//    DIVIDE             (30, Associativity.LEFT),
//    MODULO             (30, Associativity.LEFT),    // %
    
//    ADD                (35, Associativity.LEFT),
//    SUB                (35, Associativity.LEFT),
    
//    BLSHIFT            (40, Associativity.LEFT),    // <<
//    BRSHIFT            (40, Associativity.LEFT),    // >>
    
//    LT                 (45, Associativity.LEFT),
//    LTEQ               (45, Associativity.LEFT),
//    GT                 (45, Associativity.LEFT),
//    GTEQ               (45, Associativity.LEFT),
//    IS                 (45, Associativity.LEFT),    // is
    
//    EQ                 (50, Associativity.LEFT),
//    NEQ                (50, Associativity.LEFT),
    
//    BAND               (55, Associativity.LEFT),    // &
//    BXOR               (56, Associativity.LEFT),    // ^
//    BOR                (57, Associativity.LEFT),    // |
    
//    AND                (60, Associativity.LEFT),    // &&
//    OR                 (61, Associativity.LEFT),    // ||
    
/////////////////////////////////////////////////////////////////////////////////////////////////////////

// The expression rule set uses the first-alternative feature to apply precedence among operators. For 
// example, alternative "expression AND expression" precedes "expression OR expression", so the parse
// tree always builds AND subtree first. Note this only applies when ambiguity arises upon parsing a 
// given token sequence were there no precedence defined, such as "expr AND expr OR expr". Since there 
// will be no ambiguity between "expression function_call", "expression DOT IDENTIFIER" and several 
// others level-10 alternatives, the order of them in the production rules doesn't really matter.

expression
    : primary                                                                # e_primary  
    | expression LEFT_BRACKET expression RIGHT_BRACKET                       # e_indexer       // 10
    | expression function_call                                               # e_function_call
    | expression DOT IDENTIFIER                                              # e_dot
    | expression ( INCREMENT | DECREMENT )                                   # e_increment
    | NEW creator                                                            # e_new
    | TYPEOF LEFT_PAREN type RIGHT_PAREN                                     # e_typeof
    | LEFT_PAREN type RIGHT_PAREN expression                                 # e_cast          // 20
    | ( PLUS | MINUS | NEGATION | COMPLEMENT ) expression                    # e_unary         // 20 (no pre INCREMENT | DECREMENT )
    | expression ( MULTIPLY | DIVIDE | MODULO ) expression                   # e_multiply      // 30
    | expression ( PLUS | MINUS ) expression                                 # e_add           // 35
    | expression ( BITWISE_LEFT_SHIFT | BITWISE_RIGHT_SHIFT ) expression     # e_bitwise_shift // 40
    | expression ( LT | GT | LT_EQ | GT_EQ ) expression                      # e_compare       // 45
    | expression IS type                                                     # e_is            // 45
    | expression ( EQUAL | NOT_EQUAL ) expression                            # e_equal         // 50
    | expression BITWISE_AND expression                                      # e_bitwise_and   // 55
    | expression BITWISE_XOR expression                                      # e_bitwise_xor   // 56
    | expression BITWISE_OR expression                                       # e_bitwise_or    // 57
    | expression AND expression                                              # e_and           // 60
    | expression OR expression                                               # e_or            // 61
    | <assoc=right> expression QMARK expression COLON expression             # e_tertiary      // 70
    | <assoc=right> expression assignment_operator expression                # e_assign        // 70
    | lambda_signature LAMBDA ( ( (RETURN|THROW)? expression ) | block )     # e_lambda        // 70 -- expression is causing ambiguity
    ;

primary
    :   IDENTIFIER
    |   TRUE
    |   FALSE
    |   THIS
    |   SUPER
    |   INTEGER_LITERAL 
    |   REAL_LITERAL 
    |   CHAR_LITERAL 
    |   STRING_LITERAL
    |   REGEX_LITERAL
    |   NULL
    |   '(' expression ')'
    ;
    
function_call
    : LEFT_PAREN argument_list? RIGHT_PAREN
    ;

creator // We cannot reuse type here because of ambiguity it creates with DOT rule, so we replicate it as created_type_name
    : created_type_name ( object_creation_expression | array_creation_expression )
    ;

created_type_name
    : IDENTIFIER ( DOT IDENTIFIER )*
    | builtin_type
    ;
    
assignment_operator 
    : ASSIGN                    // '=';
    | PLUS_SELF                 // '+=';
    | MINUS_SELF                // '-=';
    | MULTIPLY_SELF             // '*=';
    | DIVIDE_SELF               // '/=';
    | MODULO_SELF               // '%=';
    | BITWISE_AND_SELF          // '&=';
    | BITWISE_OR_SELF           // '|=';
    | BITWISE_XOR_SELF          // '^=';
    | BITWISE_RIGHT_SHIFT_SELF  // '>>=';
    | BITWISE_LEFT_SHIFT_SELF   // '<<=';
    ;

object_creation_expression
    : LEFT_PAREN argument_list? RIGHT_PAREN
    ;
    
// For array creation, either leave no length within brackets and use an initializer, 
// or specify dimensions for all the dimensions. For multi-dimensional array, the 
// length at last dimension can be left undefined.
array_creation_expression
    : LEFT_BRACKET RIGHT_BRACKET (LEFT_BRACKET RIGHT_BRACKET)* array_initializer
    | LEFT_BRACKET expression RIGHT_BRACKET (LEFT_BRACKET expression RIGHT_BRACKET)* (LEFT_BRACKET RIGHT_BRACKET)?
    ;

array_initializer
    :   '{' (var_initializer (',' var_initializer)* (',')? )? '}'
    ;

var_initializer
    :   array_initializer
    |   expression
    ;
       
// 4. Statement

block 
    : LEFT_CURLY statement_list? RIGHT_CURLY
    ;

statement_list 
    : statement+
    ;
    
statement 
    : declaration_statement        // 4.1
    | compound_statement
    ;
    
compound_statement 
    : block
    | simple_statement
    ;
    
simple_statement 
    : empty_statement
    | expression_statement         // 4.2
    | if_statement                 // 4.3
    | switch_statement             // 4.4
    | while_statement              // 4.5
    | do_statement                 // 4.6
    | for_statement                // 4.7
    | foreach_statement            // 4.8
    | try_statement                // 4.9
    | throw_statement              // 4.10
    | break_statement              // 4.11
    | continue_statement           // 4.12
    | return_statement             // 4.13
    | sync_statement               // 4.14
    ;
    
empty_statement 
    : SEMICOLON
    ;

// A list of expressions that are separated by ','
statement_expression_list 
    : expression ( COMMA expression )*
    ;
       
// 4.1. Declaration

declaration_statement 
    : type variable_declarators SEMICOLON
    | type function_declarator
    ;
    
variable_declarators 
    : variable_declarator ( COMMA variable_declarator )*
    ;
variable_declarator 
    : IDENTIFIER ( ASSIGN expression )?
    ;
    
variable_declaration // used by for loop
    : type variable_declarators
    ;

// For function declaration when encountered in script, we do not parse the function body.
// Instead, just make sure it is properly enclosed. A parsing will be performed on demand.
function_declarator
    : function_signature LEFT_CURLY executable? RIGHT_CURLY
    ;
function_signature
    : IDENTIFIER function_signature_main
    ;
function_signature_main // (int i, string s, ...)
    : LEFT_PAREN function_parameter_list? RIGHT_PAREN
    ;
function_parameter_list 
    : function_parameter ( COMMA function_parameter )*
    ;
function_parameter // Note this is different form lambda parameter in that the type is required.
    : type IDENTIFIER
    ;
//function_body    // We do not care about the contents of body, but we must make sure it is enclosed properly.
//    : ( function_body_unenclosed | function_body_enclosed ) +
//    ;
//function_body_unenclosed
//    : ~(LEFT_CURLY | RIGHT_CURLY) 
//    ;
//function_body_enclosed
//    : LEFT_CURLY function_body RIGHT_CURLY 
//    | LEFT_CURLY RIGHT_CURLY 
//    ;

// 4.2. Expression
expression_statement
    : expression SEMICOLON
    ;

// 4.3. If
if_statement 
    : IF LEFT_PAREN expression RIGHT_PAREN compound_statement (ELSE compound_statement)? 
    // for "else if", it follows that compound_statement => simple_statement => if_statement
    ;

// 4.4. Switch  
switch_statement 
    : SWITCH LEFT_PAREN expression RIGHT_PAREN switch_block
    ;
switch_block 
    : LEFT_CURLY case_section* default_section? RIGHT_CURLY
    ;
case_section 
    : CASE case_condition COLON statement_list?
    ;
case_condition
    : IDENTIFIER | CHAR_LITERAL | INTEGER_LITERAL | STRING_LITERAL
    ;
default_section 
    : DEFAULT COLON statement_list?
    ;
    
// 4.5. While
while_statement 
    : WHILE LEFT_PAREN expression RIGHT_PAREN compound_statement
    ;

// 4.6. Do
do_statement 
    : DO compound_statement WHILE LEFT_PAREN expression RIGHT_PAREN SEMICOLON
    ;
    
// 4.7 For
for_statement 
    : FOR LEFT_PAREN ( for_statment_head | foreach_statement_head ) RIGHT_PAREN compound_statement
    ;
    
for_statment_head 
    : for_initializer? SEMICOLON for_condition? SEMICOLON for_post_loop?
    ;
for_initializer 
    : variable_declaration
    | statement_expression_list
    ;
for_condition 
    : expression
    ;
for_post_loop 
    : statement_expression_list
    ;
    
// 4.8. Foreach
foreach_statement_head
    : type IDENTIFIER ( IN | COLON ) expression
    ;
    
foreach_statement
    : FOREACH LEFT_PAREN foreach_statement_head RIGHT_PAREN compound_statement
    ;

// 4.9 Try
try_statement 
    : TRY block catch_block* finally_block?
    ;
catch_block 
    : CATCH LEFT_PAREN type IDENTIFIER RIGHT_PAREN block
    ;
finally_block 
    : FINALLY block
    ;

// 4.10 Throw
throw_statement 
    : THROW expression SEMICOLON
    ;
    
// 4.11. Break
break_statement 
    : BREAK SEMICOLON
    ;

// 4.12. Continue
continue_statement 
    : CONTINUE SEMICOLON
    ;

// 4.13. Return
return_statement
    : RETURN expression? SEMICOLON
    ;
    
// 4.14 Sync
sync_statement
    : SYNC '(' expression ')' block
    ;

// 5. Executable
// This is the entrance for 
//   (1) global script, after module, import and class definitions.
//   (2) function/method's body.
executable
    : statement_list
    ;

// 6. Module    
module_definition
    : MODULE composite_id SEMICOLON
    ;

import_statement
    : IMPORT composite_id SEMICOLON
    | IMPORT composite_id AS IDENTIFIER SEMICOLON
    ;

// 7. Type
modifiers
    : ( PUBLIC | PROTECTED | PRIVATE | INTERNAL | FINAL | CONST | ABSTRACT | HOSTED | STATIC )+
    ;

class_extension_definition
    : ':' class_extension_list
    ;
    
class_extension_list
    : composite_id
    | class_extension_list ',' composite_id
    ;

class_body
    : '{' class_member_declaration* '}'
    ;
    
class_member_declaration
    : constructor_declaration
    | method_declaration
    | field_declaration
    ;

constructor_declaration
    : annotations? modifiers? IDENTIFIER function_signature_main constructor_forward_call? ( method_body | ';' ) 
    // name checked deferred to semantic analysis; hosted method doesn't have method body
    ;
    
constructor_forward_call
    : ':' THIS function_call
    | ':' SUPER function_call
    ;
    
method_declaration
    : annotations? modifiers? type IDENTIFIER function_signature_main ( method_body | ';' ) 
    // abstract/hosted method doesn't have method body
    ;

method_body
    : '{' executable? '}'
    ;

field_declaration
    : annotations? modifiers? type IDENTIFIER ( field_initializer | ';' ) 
    ;

field_initializer
    : '=' expression_statement
    ;

annotations
    : annotation+
    ;
    
annotation
    : '[' type attributes_initalization? ']'
    ;
    
attributes_initalization
    : '(' atrribute_initialization_list? ')'
    ;
    
atrribute_initialization_list
    : atrribute_initialization ( ',' atrribute_initialization ) *
    ;
    
atrribute_initialization
    : IDENTIFIER '=' expression
    ;
    
class_definition
    : annotations? modifiers? CLASS IDENTIFIER class_extension_definition? class_body
    ;

interface_definition
    : annotations? modifiers? INTERFACE IDENTIFIER class_extension_definition? interface_body
    ;

interface_body
    : '{' interface_member_declaration* '}'
    ;

interface_member_declaration
    : method_declaration
    | field_declaration
    ;

enum_definition
    : annotations? modifiers? ENUM IDENTIFIER enum_body
    ;
    
enum_body
    : '{' enum_member_declarations? '}'
    ;

enum_member_declarations
    : ordinary_enum_member_declaration* last_enum_member_declaration
    ;
    
ordinary_enum_member_declaration
    : enum_member_declaration_body ','
    ;

last_enum_member_declaration
    : enum_member_declaration_body ','?
    ;
    
enum_member_declaration_body
    : IDENTIFIER enum_member_declaration_initializer?
    ;
    
enum_member_declaration_initializer
    : '=' INTEGER_LITERAL
    ;

attribute_definition
    : annotations? modifiers? ATTRIBUTE IDENTIFIER attribute_body
    ;
    
attribute_body
    : '{' field_declaration* '}'
    ;
    
// 8. Program
preamble
    : module_definition? import_statement*
    ;

declarations
    : type_declaration*
    ;

type_declaration
    : class_definition
    | interface_definition
    | enum_definition 
    | attribute_definition
    ;
    
program
    : preamble declarations executable?
    ;