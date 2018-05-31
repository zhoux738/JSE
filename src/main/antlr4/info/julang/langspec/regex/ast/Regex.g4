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

grammar Regex;

////////////////////////////////////////// Notes //////////////////////////////////////////

// 1) The file must be placed under a directory corresponding to the desired package 
//    name to emit the following header:
//    package info.julang.langspec.regex.ast;
//
// 2) All the lexer rules are in UPPER CASE; all the parser rules lower case. All the
//    fragments are prefixed by "FRG_".
//
// 3) This file is heavily inspired by this BNF, which itself is deduced from Perl's regex.
//    http://www.cs.sfu.ca/~cameron/Teaching/384/99-3/regexp-plg.html (by Robert D. Cameron)

//////////////////////////////////////// I. Lexer /////////////////////////////////////////

// metachars
RG_LEFT_BRACKET :  '[';
RG_RIGHT_BRACKET : ']';
RG_LEFT_PAREN :    '(';
RG_RIGHT_PAREN :   ')';
RG_ONE_OR_MORE :   '+';
RG_ZERO_OR_MORE :  '*';
RG_ZERO_OR_ONE :   '?';
RG_RANGE :         '-';
RG_UNION :         '|';
RG_BOS :           '^';
RG_EOS :           '$';
RG_ANY :           '.';
RG_ESCAPE :        '\\';

// regular chars
RG_CHAR
  : ~[.^$\|\[\]\(\)\*\+\?\n\r\t\f\v\b\\-]
  ;

RG_METACHAR_ESCAPE
  : FRG_ESC_LEFT_BRACKET
  | FRG_ESC_RIGHT_BRACKET
  | FRG_ESC_LEFT_PAREN
  | FRG_ESC_RIGHT_PAREN
  | FRG_ESC_ONE_OR_MORE
  | FRG_ESC_RANGE
  | FRG_ESC_ZERO_OR_MORE
  | FRG_ESC_UNION
  | FRG_ESC_ZERO_OR_ONE
  | FRG_ESC_BOS
  | FRG_ESC_EOS
  | FRG_ESC_ANY
  | FRG_ESC_ESCAPE
  | FRG_ESC_N
  | FRG_ESC_R
  | FRG_ESC_T
  | FRG_ESC_F
  | FRG_ESC_V
  | FRG_ESC_B
  ;
  
fragment FRG_ESC_LEFT_BRACKET :  '\\[';
fragment FRG_ESC_RIGHT_BRACKET : '\\]';
fragment FRG_ESC_LEFT_PAREN :    '\\(';
fragment FRG_ESC_RIGHT_PAREN :   '\\)';
fragment FRG_ESC_ZERO_OR_MORE :  '\\*';
fragment FRG_ESC_ONE_OR_MORE :   '\\+';
fragment FRG_ESC_ZERO_OR_ONE :   '\\?';
fragment FRG_ESC_RANGE :         '\\-';
fragment FRG_ESC_UNION :         '\\|';
fragment FRG_ESC_BOS :           '\\^';
fragment FRG_ESC_EOS :           '\\$';
fragment FRG_ESC_ANY :           '\\.';
fragment FRG_ESC_ESCAPE :       '\\\\';
fragment FRG_ESC_N :             '\\n';
fragment FRG_ESC_R :             '\\r';
fragment FRG_ESC_T :             '\\t'; 
fragment FRG_ESC_F :             '\\f';
fragment FRG_ESC_V :             '\\v';
fragment FRG_ESC_B :             '\\b';

/////////////////////////////////////// II. Parser ////////////////////////////////////////

// union of two regexes
regex
  : regex '|' sub_regex
  | sub_regex
  ;
  
// concatenation of two regexes
sub_regex
  : sub_regex quantified_regex
  | quantified_regex
  ;

quantified_regex
  : unit_regex quantifier?
  ;
 
quantifier
  : '+'
  | '*'
  | '?'
  ;
   
unit_regex
  : '(' regex ')'
  | '.'
  | '^'
  | '$'
  | schar
  | charset
  ;

schar
  : RG_CHAR
  | RG_METACHAR_ESCAPE
  ;

charset
  : '[' '^'? set_item+ ']'
  ;
  
set_item
  : range
  | schar
  ;
  
range
  : schar '-' schar
  ;