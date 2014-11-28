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

parser grammar CommonParser;

options {
  language=Java;
  output=AST;
  superClass=ParserBase;
  backtrack=true;
}

tokens {
  // Unary plus operator.
  UPLUS;
  // Unary minus operator.
  UMINUS;

  // Location (used as an expression atom).
  LOCATION;
  // Access to a location by index (e.g. GPR[1]).
  LOCATION_INDEX;
  // Access to a location's bit field (e.g. GPR[0]<0..8>).
  LOCATION_BITFIELD;

  // Reference to a LET constant (used as an expression atom).
  CONST;
}

@members {
/**
This is a global flag needed to indicate that the "bitFieldExpr" rule is being executed.
It is needed to avoid problems related to translation of bit field expressions.
The issue is that the parser incorrectly recognizes constructions like that: "GPR<1..2> + 1".
It recognizes the ">" token as the "greater than" operator instead of bit field expression
termination character. So it recognizes "2> + 1" as a correct expression, but then fails
to continue parsing as it cannot find expected termination character. To overcome the problem,
comparison operators are excluded form expression rules when a bit field expression is being parsed.
This imposes a certain restriction. However, it is not important because bit field expressions
are expected to be evaluated to an integer value, but a boolean. Thus, comparison operators
should not be used in this case from a semantic point of view.
*/
private boolean inBitField = false;

boolean isInBitField() {
  return inBitField;
}

void setInBitField(boolean value) {
  inBitField = value;
}
}

//==================================================================================================
// Type Expression
//==================================================================================================

typeExpr
    :  id=ID  { checkDeclaration($id,ESymbolKind.TYPE); }
//  |  BOOL // TODO: NOT SUPPORTED IN THE CURRENT VERSION 
    |  INT^ LEFT_PARENTH! expr RIGHT_PARENTH!
    |  CARD^ LEFT_PARENTH! expr RIGHT_PARENTH!
    |  FIX^ LEFT_PARENTH! expr COMMA! expr RIGHT_PARENTH!
    |  FLOAT^ LEFT_PARENTH! expr COMMA! expr RIGHT_PARENTH!
//  |  LEFT_HOOK constExpr DOUBLE_DOT constExpr RIGHT_HOOK -> ^(RANGE constExpr constExpr) // TODO: NOT SUPPORTED IN THE CURRENT VERSION  
//  |  ENUM^ LEFT_PARENTH! identifierList RIGHT_PARENTH! // TODO: NOT SUPPORTED IN THE CURRENT VERSION  
    ;

//==================================================================================================
// Expression
//==================================================================================================

expr
    :  nonNumExpr
    |  numExpr
    ;

//==================================================================================================
// Non-Numeric Expression
//==================================================================================================

nonNumExpr
    :  ifExpr
    ;

ifExpr
    :  IF^ expr THEN! expr elseIfExpr* elseExpr? ENDIF!
    ;

elseIfExpr
    :  ELSEIF^ expr THEN! expr
    ;

elseExpr
    :  ELSE^ expr
    ;

//==================================================================================================
// Numeric Expression
//==================================================================================================

numExpr
    :  orLogicExpr
    ;

orLogicExpr
    :  andLogicExpr (OR^ andLogicExpr)*
    ;

andLogicExpr
    :  orBitExpr (AND^ orBitExpr)*
    ;

orBitExpr
    :  xorBitExpr (VERT_BAR^ xorBitExpr)*
    ;

xorBitExpr
    :  andBitExpr (UP_ARROW^ andBitExpr)*
    ;

andBitExpr
    :  relationExpr (AMPER^ relationExpr)*
    ;

relationExpr
    :  comparisionExpr ((EQ^ | NEQ^) comparisionExpr)*
    ;

comparisionExpr
    :  {!isInBitField()}? => shiftExpr ((LEQ^ | GEQ^ | LEFT_BROCKET^ | RIGHT_BROCKET^) shiftExpr)*
    |  shiftExpr
    ;

shiftExpr
    :  plusExpr ((LEFT_SHIFT^ | RIGHT_SHIFT^ | ROTATE_LEFT^ | ROTATE_RIGHT^) plusExpr)*
    ;

plusExpr
    :  mulExpr ((PLUS^ | MINUS^) mulExpr)*
    ;

mulExpr
    :  powExpr ((MUL^ | DIV^ | REM^) powExpr)*
    ;

powExpr
    :  unaryExpr (DOUBLE_STAR^ unaryExpr)*
    ;

unaryExpr
    :  PLUS   unaryExpr -> ^(UPLUS unaryExpr)
    |  MINUS  unaryExpr -> ^(UMINUS unaryExpr)
    |  TILDE^ unaryExpr
    |  NOT^   unaryExpr
    |  atom
    ;

atom
    :  LEFT_PARENTH! expr RIGHT_PARENTH!
    |  letConst
    |  location
    |  CARD_CONST
    |  BINARY_CONST
    |  HEX_CONST
    |  COERCE^ LEFT_PARENTH! typeExpr COMMA! expr RIGHT_PARENTH!
    ;

letConst
    : {isDeclaredAs(input.LT(1), ESymbolKind.LET_CONST)}? ID -> ^(CONST ID)
    ;

//==================================================================================================
// Location
//==================================================================================================

location
    :  locationExpr -> ^(LOCATION locationExpr)
    ;

locationExpr
    :  locationVal (DOUBLE_COLON^ locationExpr)*
    ;

/*  If the bitFieldExpr expression fires, we rewrite the rule as a bitfield expression,
    otherwise we leave it as it is. */
locationVal
    :  locationAtom (bitFieldExpr -> ^(LOCATION_BITFIELD locationAtom bitFieldExpr) | -> locationAtom)
    ;

locationAtom
    :  ID
    |  ID LEFT_HOOK expr RIGHT_HOOK -> ^(LOCATION_INDEX ID expr)
    ;

bitFieldExpr
@init   { setInBitField(true); }
    :  LEFT_BROCKET! expr (DOUBLE_DOT! expr)? RIGHT_BROCKET!
    ;
finally { setInBitField(false); }

//==================================================================================================
// The End
//==================================================================================================
