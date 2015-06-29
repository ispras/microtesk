/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

lexer grammar CommonLexer;

@members {
private ru.ispras.microtesk.translator.antlrex.Preprocessor pp;

public final ru.ispras.microtesk.translator.antlrex.Preprocessor getPreprocessor() {
  return pp;
}
public final void setPreprocessor(final ru.ispras.microtesk.translator.antlrex.Preprocessor pp) {
  this.pp = pp;
}}

//==================================================================================================
// Comments, Spaces and Newlines
//==================================================================================================

WHITESPACE     : SPACE+                { skip(); };
NEWLINE        : ('\r'?'\n')+          { skip(); };

SINGLE_COMMENT : '//' .* NEWLINE       { skip(); };
MULTI_COMMENT  : '/*' .* '*/' NEWLINE? { skip(); };

//==================================================================================================
// Different Symbols
//==================================================================================================

LEFT_PARENTH  : '(';
RIGHT_PARENTH : ')';

LEFT_BRACE    : '{';
RIGHT_BRACE   : '}';

LEFT_HOOK     : '[';
RIGHT_HOOK    : ']';

COLON         : ':';
SEMI          : ';';
COMMA         : ',';
DOT           : '.';

SHARP         : '#';

//==================================================================================================
// Operations
//==================================================================================================

ASSIGN        : '=';

PLUS          : '+';
MINUS         : '-';
MUL           : '*';
DIV           : '/';
REM           : '%';

DOUBLE_STAR   : '**';

LEFT_SHIFT    : '<<';
RIGHT_SHIFT   : '>>';
ROTATE_LEFT   : '<<<';
ROTATE_RIGHT  : '>>>';

LEQ           : '<=';
GEQ           : '>=';
EQ            : '==';
NEQ           : '!=';

LEFT_BROCKET  : '<';
RIGHT_BROCKET : '>';

NOT           : '!';
AND           : '&&';
OR            : '||';

TILDE         : '~';
AMPER         : '&';
UP_ARROW      : '^';
VERT_BAR      : '|';

DOUBLE_DOT    : '..';
DOUBLE_COLON  : '::';

//==================================================================================================
// Control Statements
//==================================================================================================

IF                      :    'if';
THEN                    :    'then';
ELSE                    :    'else';
ELSEIF                  :    'elif';
ENDIF                   :    'endif';

//==================================================================================================
// Special Function Names
//==================================================================================================

COERCE                  :    'coerce';
FORMAT                  :    'format';
TRACE                   :    'trace';
EXCEPTION               :    'exception';
MARK                    :    'mark';
UNPREDICTED             :    'unpredicted';  
UNDEFINED               :    'undefined';

//==================================================================================================
// Identifier
//==================================================================================================

ID : LETTER (LETTER | DIGIT | '_')*;

//==================================================================================================
// Literals
//==================================================================================================

STRING_CONST : '"' NONCONTROL* '"' { String s = $text; setText(s.substring(1, s.length() - 1)); };

BINARY_CONST : '0b' b=BIN_DIG_LST { setText($b.text); };
HEX_CONST    : '0x' h=HEX_DIG_LST { setText($h.text); };
CARD_CONST   : DIGIT+;

// TODO: fixed numbers are NOT SUPPORTED IN THE CURRENT VERSION.
// NOTE: When they are supported and the rule is enabled it will make bitfield
// constructs (e.g. MEM<1..2>) invalid. They will cause a parser error that can be 
// solved by inserting a space character between the '..' token and number expressions.
// However, this is not elegant. To make both features work together, some more elegant
// solution for this problem will be needed.

//FIXED_CONST : (DIGIT+ '.' DIGIT+)=> DIGIT+ '.' DIGIT+;
//NUM_CONST   : /*  (DIGIT+ '.' DIGIT)=> DIGIT+ '.' DIGIT+ { $type = FIXED_CONST; }
//            | */  DIGIT+ { $type = CARD_CONST; };

//==================================================================================================
// Fragments
//==================================================================================================

fragment BIN_DIG_LST : BIN_DIGIT+;
fragment HEX_DIG_LST : HEX_DIGIT+;

fragment NONCONTROL  : LETTER | DIGIT | SYMBOL | SPACE;
fragment LETTER      : LOWER | UPPER;
fragment LOWER       : 'a'..'z';
fragment UPPER       : 'A'..'Z';
fragment DIGIT       : '0'..'9';
fragment BIN_DIGIT   : '0' | '1';
fragment HEX_DIGIT   : DIGIT | 'a'..'f' | 'A'..'F';
fragment SPACE       : ' ' | '\t';

// NOTE: Symbol does not include double quote character.
fragment SYMBOL      : '!' | '#'..'/' | ':'..'@' | '['..'`' | '{'..'~';

//==================================================================================================
// The End
//==================================================================================================

