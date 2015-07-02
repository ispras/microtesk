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
}

private void pp() {
  if(pp.isHidden()) {
    skip();
  }
}

private void pp(final String text) {
  if(pp.isHidden()) {
    skip();
  } else {
    setText(text);
  }
}}

//==================================================================================================
// Comments, Spaces and Newlines
//==================================================================================================

WHITESPACE     : SPACE+          { skip(); };
NEWLINE        : ('\r'?'\n')+    { skip(); };

SINGLE_COMMENT : '//' .* NEWLINE { skip(); };
MULTI_COMMENT  : '/*' .* '*/'    { skip(); };

fragment
LINE : (~('\n' | '\r'))* ;

fragment
REST : LINE (NEWLINE | EOF) ;

//==================================================================================================
// Preprocessor Directives
//==================================================================================================

PP_DEFINE : '#define' WHITESPACE key=ID val=LINE (NEWLINE | EOF) {
  if (!pp.isHidden()) {
    pp.onDefine($key.getText(), $val.getText());
  }
  skip();
};

PP_UNDEF : '#undef' WHITESPACE key=ID REST {
  if (!pp.isHidden()) {
    pp.onUndef($key.getText());
  }
  skip();
};

PP_IFDEF : '#ifdef' WHITESPACE key=ID {
  // #ifdef is processed even in the hidden scope.
  pp.onIfdef($key.getText());
  skip();
};

PP_IFNDEF : '#ifndef' WHITESPACE key=ID {
  // #ifndef is processed even in the hidden scope.
  pp.onIfndef($key.getText());
  skip();
};

PP_ELSE : '#else' {
  // #else is processed even in the hidden scope.
  pp.onElse();
  skip();
};

PP_ENDIF : '#endif' {
  // #endif is processed even in the hidden scope.
  pp.onEndif();
  skip();
};

PP_INCLUDE : '#include' WHITESPACE '"' filename=PP_FILENAME '"' (WHITESPACE)? (NEWLINE | EOF) {
  if (!pp.isHidden()) {
    pp.includeTokensFromFile($filename.getText());
  }
  skip();
};

PP_EXPAND : '#' key=ID {
  if (!pp.isHidden()) {
    final String substitution = pp.expand($key.getText());
    pp.includeTokensFromString(substitution);
  }
  skip();
};

fragment
PP_FILENAME : (~('"' | '\n'))*;

//==================================================================================================
// Different Symbols
//==================================================================================================

LEFT_PARENTH  : '('           { pp(); };
RIGHT_PARENTH : ')'           { pp(); };

LEFT_BRACE    : '{'           { pp(); };
RIGHT_BRACE   : '}'           { pp(); };

LEFT_HOOK     : '['           { pp(); };
RIGHT_HOOK    : ']'           { pp(); };

COLON         : ':'           { pp(); };
SEMI          : ';'           { pp(); };
COMMA         : ','           { pp(); };
DOT           : '.'           { pp(); };

SHARP         : '#'           { pp(); };

//==================================================================================================
// Operations
//==================================================================================================

ASSIGN        : '='           { pp(); };

PLUS          : '+'           { pp(); };
MINUS         : '-'           { pp(); };
MUL           : '*'           { pp(); };
DIV           : '/'           { pp(); };
REM           : '%'           { pp(); };

DOUBLE_STAR   : '**'          { pp(); };

LEFT_SHIFT    : '<<'          { pp(); };
RIGHT_SHIFT   : '>>'          { pp(); };
ROTATE_LEFT   : '<<<'         { pp(); };
ROTATE_RIGHT  : '>>>'         { pp(); };

LEQ           : '<='          { pp(); };
GEQ           : '>='          { pp(); };
EQ            : '=='          { pp(); };
NEQ           : '!='          { pp(); };

LEFT_BROCKET  : '<'           { pp(); };
RIGHT_BROCKET : '>'           { pp(); };

NOT           : '!'           { pp(); };
AND           : '&&'          { pp(); };
OR            : '||'          { pp(); };

TILDE         : '~'           { pp(); };
AMPER         : '&'           { pp(); };
UP_ARROW      : '^'           { pp(); };
VERT_BAR      : '|'           { pp(); };

DOUBLE_DOT    : '..'          { pp(); };
DOUBLE_COLON  : '::'          { pp(); };

//==================================================================================================
// Control Statements
//==================================================================================================

IF            : 'if'          { pp(); };
THEN          : 'then'        { pp(); };
ELSE          : 'else'        { pp(); };
ELSEIF        : 'elif'        { pp(); };
ENDIF         : 'endif'       { pp(); };

//==================================================================================================
// Special Function Names
//==================================================================================================

COERCE        : 'coerce'      { pp(); };
FORMAT        : 'format'      { pp(); };
TRACE         : 'trace'       { pp(); };
EXCEPTION     : 'exception'   { pp(); };
MARK          : 'mark'        { pp(); };
UNPREDICTED   : 'unpredicted' { pp(); };
UNDEFINED     : 'undefined'   { pp(); };

//==================================================================================================
// Identifier
//==================================================================================================

ID : LETTER (LETTER | DIGIT | '_')* { pp(); };

//==================================================================================================
// Literals
//==================================================================================================

STRING_CONST : '"' NONCONTROL* '"' { String s = $text; pp(s.substring(1, s.length() - 1)); };

BINARY_CONST : '0b' b=BIN_DIG_LST  { pp($b.text); };
HEX_CONST    : '0x' h=HEX_DIG_LST  { pp($h.text); };
CARD_CONST   : DIGIT+              { pp(); };

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
