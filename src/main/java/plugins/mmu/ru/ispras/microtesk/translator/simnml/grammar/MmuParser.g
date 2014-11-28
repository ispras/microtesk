/*======================================================================================*/
/* README SECTION                                                                       */
/*                                                                                      */
/* TODO:                                                                                */
/* - Brief description of the parser rules' structure and format                        */
/* - Instructions on how to debug and extend the rules                                  */
/* - "TODO" notes                                                                       */
/*======================================================================================*/

parser grammar MmuParser;

/*======================================================================================*/
/* Options                                                                              */
/*======================================================================================*/

options {
  language=Java;
  tokenVocab=MmuLexer;
  output=AST;
  superClass=ParserBase;
}

/*======================================================================================*/
/* Additional tokens. Lists additional tokens to be inserted in the AST by the parser   */
/* to express some syntactic properties.                                                */
/*======================================================================================*/

tokens {
  LOCATION;    // TODO: give info

  RANGE;       // for the range type ([a..b])

  ARGS;        // for AND-rules
  ATTRS;       // for MODE and OP structures 
  RETURN;      // for MODE structures
  
  INDEX;
  BIT_FIELD_OP;

  SEQUENCE;

  UNARY_PLUS;  
  UNARY_MINUS;

  SIZE_TYPE;
}

/*======================================================================================*/
/* Default Exception Handler Code                                                       */
/*======================================================================================*/

@rulecatch {
catch (SemanticException re) { // Default behavior
    reportError(re);
    recover(input,re);
    // We don't insert error nodes in the IR (walker tree). 
    //retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
}
catch (RecognitionException re) { // Default behavior
    reportError(re);
    recover(input,re);
    // We don't insert error nodes in the IR (walker tree). 
    //retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
}
}

/*======================================================================================*/
/* Header for the generated parser Java class file (header comments, imports, etc).     */  
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
 * MMuParser.java 
 *
 * WARNING: THIS FILE IS AUTOMATICALLY GENERATED. PLEASE DO NOT MODIFY IT. 
 */

package ru.ispras.microtesk.translator.simnml.grammar;

import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.simnml.ESymbolKind;
import ru.ispras.microtesk.translator.simnml.antlrex.ParserBase;
}

/*======================================================================================*/
/* Root Rules of Processor Specification                                                */
/*======================================================================================*/

startRule 
	:	bufferoraddress* EOF!
	;
	
bufferoraddress
	:	addressRule
	|	bufferRule
	;
 
/*======================================================================================*/
/* Address     																		    */
/*======================================================================================*/
 
 addressRule
			:	MMU_ADDRESS^ id=ID LEFT_BRACE! WIDTH! ASSIGN! addr=addressExpr SEMI! RIGHT_BRACE! { declare($id, $addr.res, false); }
			;
	
			addressExpr returns [ESymbolKind res]
			:	constExpr     { $res = ESymbolKind.OP;  }
			;

/*======================================================================================*/
/* Buffer     																		    */
/*======================================================================================*/

bufferRule 
    		:  MMU_BUFFER^ id=ID LEFT_BRACE! buf=bufferExpr RIGHT_BRACE! { declare($id, $buf.res, false); } 
    		;

			bufferExpr returns [ESymbolKind res]
			:	(parameter)*
			;

parameter returns [ESymbolKind res]
	:	associativity
	|	sets
	|	line
	|	index
	|	match
	|	policy
	;
	
/*======================================================================================*/
/* Attribute rules (line, associativity)                                                          */
/*======================================================================================*/

associativity
			:	id=MMU_ASSOCIATIVITY^ ASSIGN! ass=associativityExpr SEMI! { declare($id, $ass.res, false); }
			;
			
	associativityExpr returns [ESymbolKind res]
		:	constExpr     { $res = ESymbolKind.OP;  }
		;

sets
			:	id=MMU_SETS^ ASSIGN! sete=setsExpr SEMI! { declare($id, $sete.res, false); } 
			;    	
	
	setsExpr returns [ESymbolKind res]
		:	constExpr     { $res = ESymbolKind.OP;  }
		;
		

/*======================================================================================*/
/* Attribute line                                                                       */
/*======================================================================================*/

line
		:	id=LINE^ LEFT_PARENTH! l=lineExpr RIGHT_PARENTH! SEMI! { declare($id, $l.res, false); } 
		;
		
lineExpr returns [ESymbolKind res]
		:	
			tag
			COMMA!
			data
		;
		
		tag
			:	id=MMU_TAG^ COLON! lengthExpr { declare($id, $lengthExpr.res, false); }
			;
		
		data
			:	id=MMU_DATA^ COLON! lengthExpr { declare($id, $lengthExpr.res, false); }
			;
		
		lengthExpr returns [ESymbolKind res]
			: 		constExpr { $res = ESymbolKind.OP;  }		
			;
		
/*======================================================================================*/
/* Expression rules (index, match)
/*======================================================================================*/

index
	:	MMU_INDEX^ LEFT_PARENTH! MMU_ADDR! COLON! id=ID RIGHT_PARENTH! ASSIGN! MMU_ADDR! LEFT_BROCKET! ind=indexExpr RIGHT_BROCKET! SEMI! { checkDeclaration($id, $ind.res); }
	;
	
	indexExpr returns [ESymbolKind res]
		:	constDotExpr     { $res = ESymbolKind.OP;  }  
		;
	
match
	:	MMU_MATCH^ LEFT_PARENTH! MMU_ADDR! COLON! id=ID RIGHT_PARENTH! ASSIGN! LINE! DOT! TAG! EQ! MMU_ADDR! LEFT_BROCKET! ma=matchExpr RIGHT_BROCKET! SEMI! { checkDeclaration($id, $ma.res); }
	;
		
	matchExpr returns [ESymbolKind res]
		:	constDotExpr     { $res = ESymbolKind.OP;  }
		;
		
/*======================================================================================*/
/*  Policy Type Rules                                                                   */
/*======================================================================================*/

policy
    	:	id=MMU_POLICY^ ASSIGN! pol=policyExpr  SEMI! { declare($id, $pol.res, false); }
    	;
    	
policyExpr returns [ESymbolKind res]
    : MMU_RANDOM
    | MMU_FIFO
    | MMU_PLRU
    | MMU_LRU
;
catch [RecognitionException re] {
    reportError(re);
    recover(input,re);
}
		
/*======================================================================================*/
/* Constant expression rules (statically calculated)                                    */
/*======================================================================================*/

constExpr
    :  constOrLogicExpr
    ;

constOrLogicExpr
    :  constAndLogicExpr (OR^ constAndLogicExpr)*
    ;

constAndLogicExpr
    :  constOrBitExpr (AND^ constOrBitExpr)*
    ;

constOrBitExpr
    :  constXorBitExpr (VERT_BAR^ constXorBitExpr)*
    ;

constXorBitExpr
    :  constAndBitExpr (UP_ARROW^ constAndBitExpr)*
    ;

constAndBitExpr
    :  constRelationExpr (AMPER^ constRelationExpr)*
    ;

constRelationExpr
    :  constComparisionExpr ((EQ^ | NEQ^) constComparisionExpr)*
    ;

constComparisionExpr
    :  constShiftExpr ((LEQ^ | GEQ^ | LEFT_BROCKET^ | RIGHT_BROCKET^) constShiftExpr)*
    ;

constShiftExpr
    :  constPlusExpr ((LEFT_SHIFT^ | RIGHT_SHIFT^ | ROTATE_LEFT^ | ROTATE_RIGHT^) constPlusExpr)*
    ;

constPlusExpr
    :  constMulExpr ((PLUS^ | MINUS^) constMulExpr)*
    ;

constMulExpr
    :  constPowExpr ((MUL^ | DIV^ | REM^) constPowExpr)*
    ;

constPowExpr
    :  constUnaryExpr (DOUBLE_STAR^ constUnaryExpr)*
    ;

constDotExpr
	:  constExpr DOUBLE_DOT constExpr -> ^(DOUBLE_DOT constExpr constExpr)
	;

constUnaryExpr
    :  PLUS constUnaryExpr -> ^(UNARY_PLUS constUnaryExpr)
    |  MINUS constUnaryExpr -> ^(UNARY_MINUS constUnaryExpr)
    |  TILDE^ constUnaryExpr
    |  NOT^ constUnaryExpr
    |  constAtom
    ;

constAtom
    :  LEFT_PARENTH! constExpr RIGHT_PARENTH!
    |  ID 
    |  CARD_CONST 
    |  BINARY_CONST
    |  HEX_CONST
    |  FIXED_CONST 
    ;
