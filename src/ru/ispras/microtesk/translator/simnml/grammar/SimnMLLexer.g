/*======================================================================================*/
/* README SECTION                                                                       */
/*                                                                                      */
/* TODO:                                                                                */
/* - Brief description of the lexer rules structure and format                        */
/* - Instructions on how to debug and extend the rules                                  */
/* - "TODO" notes                                                                       */
/*======================================================================================*/

lexer grammar SimnMLLexer;

/*======================================================================================*/
/* Options                                                                              */
/*======================================================================================*/

options {
  language=Java;
}

/*======================================================================================*/
/* Header for the generated lexer Java class file (header comments, imports, etc).      */
/*======================================================================================*/

@header {
/*
 * Copyright (c) 2012 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * SimnMLLexer.java Andrei Tatarnikov
 *
 * WARNING: THIS FILE IS AUTOMATICALLY GENERATED. PLEASE DO NOT MODIFY IT. 
 */
 
  package ru.ispras.microtesk.translator.simnml.grammar;

  import  ru.ispras.microtesk.translator.simnml.SimnMLAnalyzer;
}

@members {
  private SimnMLAnalyzer analyzer;

  public SimnMLLexer(CharStream chars, SimnMLAnalyzer analyzer)
  {
      super(chars);
      this.analyzer = analyzer;
  }
}

/*======================================================================================*/
/* Comments, Spaces and Newlines                                                        */
/*======================================================================================*/

WHITESPACE              :   SPACE+                { $channel = HIDDEN; };
NEWLINE                 :   ('\r'?'\n')+          { $channel = HIDDEN; };

SINGLE_COMMENT          :   '//' .* NEWLINE       { $channel = HIDDEN; };
MULTI_COMMENT           :   '/*' .* '*/' NEWLINE? { $channel = HIDDEN; };

/*======================================================================================*/
/* Preprocessor Directives                                                              */
/*======================================================================================*/
PP_INCLUDE : 'include' WHITESPACE '"' filename=PP_FILENAME '"' (WHITESPACE)? NEWLINE
{
    analyzer.lexInclude($filename.getText());
    skip();
};

fragment
PP_FILENAME : (~('"' | '\n'))*
;

/*======================================================================================*/
/* Lexems for Operations                                                                */
/*======================================================================================*/

ASSIGN                  :    '=';

PLUS                    :    '+';
MINUS                   :    '-';
MUL                     :    '*';
DIV                     :    '/';
REM                     :    '%';

DOUBLE_STAR             :    '**';

LEFT_SHIFT              :    '<<';
RIGHT_SHIFT             :    '>>';
ROTATE_LEFT             :    '<<<';
ROTATE_RIGHT            :    '>>>';

LEQ                     :    '<=';
GEQ                     :    '>=';
EQ                      :    '==';
NEQ                     :    '!=';

LEFT_BROCKET            :    '<';
RIGHT_BROCKET           :    '>';

/*
LEFT_BROCKET_ALT        :    ('<' CARD_CONST '..') => '<' { $type = LEFT_BIT_FIELD; }
                        |    '<' { $type = LEFT_BROCKET; }
                        ;
*/

NOT                     :    '!';
AND                     :    '&&';
OR                      :    '||';

TILDE                   :    '~';
AMPER                   :    '&';
UP_ARROW                :    '^';
VERT_BAR                :    '|';

DOUBLE_DOT              :    '..';

DOUBLE_COLON            :    '::';

/* Different symbols */
LEFT_PARENTH            :    '(';
RIGHT_PARENTH           :    ')';

LEFT_BRACE              :    '{';
RIGHT_BRACE             :    '}';

LEFT_HOOK               :    '[';
RIGHT_HOOK              :    ']';

COLON                   :    ':';
SEMI                    :    ';';
COMMA                   :    ',';
DOT                     :    '.';

SHARP                   :    '#';

/*************************************************************************************
*                              nML Section Keywords                                  *
*************************************************************************************/

/* Declaration Keywords */
LET                     :    'let';
TYPE                    :    'type';
MEM                     :    'mem';
REG                     :    'reg';
VAR                     :    'var';
MODE                    :    'mode';
OP                      :    'op';

/* Standard Attributes (for modes and ops) */
SYNTAX                  :    'syntax';
IMAGE                   :    'image';
ACTION                  :    'action';

/* Data Types */
BOOL                    :    'bool';
CARD                    :    'card';
FIX                     :    'fix';
FLOAT                   :    'float';
INT                     :    'int';
ENUM                    :    'enum';

/* Special function keywords */
COERCE                  :    'coerce';
FORMAT                  :    'format';

/* Conditional keywords */
IF                      :    'if';
THEN                    :    'then';
ELSE                    :    'else';
ENDIF                   :    'endif';

/* Common Lexems */
ID                      :    LETTER (LETTER | DIGIT | '_')* ;

BINARY_CONST            :    '0b' b=BIN_DIG_LST { setText($b.text); };
HEX_CONST               :    '0x' h=HEX_DIG_LST { setText($h.text); };
CARD_CONST              :    DIGIT+ ;

// TODO: fixed numbers are NOT SUPPORTED IN THE CURRENT VERSION.
// NOTE: When they are supported and the rule is enabled it will make bitfield
// constructs (e.g. MEM<1..2>) invalid. They will cause a parser error that can be 
// solved by inserting a space character between the '..' token and number expressions.
// However, this is not elegant. To make both features work together, some more elegant
// solution for this problem will be needed.
//
//FIXED_CONST           :    (DIGIT+ '.' DIGIT+)=> DIGIT+ '.' DIGIT+;

STRING_CONST            :    '"' NONCONTROL* '"' { String s = $text; setText(s.substring(1, s.length()-1)); };

//NUM_CONST               : /*  (DIGIT+ '.' DIGIT)=> DIGIT+ '.' DIGIT+ { $type = FIXED_CONST; }
//                        | */  DIGIT+ { $type = CARD_CONST; }
//                        ;

/*************************************************************************************
*                           "Fragment" lexer rules                                   *
*************************************************************************************/

/*
fragment LEFT_BROCKET   :   ;
fragment LEFT_BIT_FIELD :   ;
*/

fragment BIN_DIG_LST    :   BIN_DIGIT+;
fragment HEX_DIG_LST    :   HEX_DIGIT+;

fragment NONCONTROL     :   LETTER | DIGIT | SYMBOL | SPACE;
fragment LETTER         :   LOWER | UPPER;
fragment LOWER          :   'a'..'z';
fragment UPPER          :   'A'..'Z';
fragment DIGIT          :   '0'..'9';
fragment BIN_DIGIT      :   '0' | '1';
fragment HEX_DIGIT      :   DIGIT | 'a'..'f' | 'A'..'F';
fragment SPACE          :   ' ' | '\t';

// Note: Symbol doesn't include double quote character.
fragment SYMBOL         :   '!' | '#'..'/' | ':'..'@' | '['..'`' | '{'..'~';
