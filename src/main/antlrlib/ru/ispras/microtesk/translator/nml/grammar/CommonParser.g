/*
 * Copyright 2012-2020 ISP RAS (http://www.ispras.ru)
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
  // Repeat a location N times (e.g. {4}X<0..8>). 
  LOCATION_REPEAT;

  // Reference to a LET constant (used as an expression atom).
  CONST;

  // Sequence of statements.
  SEQUENCE;

  // Statically instantiated MODE or OP
  INSTANCE;
  // Call to the 'action' attribute of a statically instantiated MODE or OP 
  INSTANCE_CALL;

  // Argument of a MODE or OP.
  ARGUMENT;

  // Ternary 'if' expression for string format expressions
  SIF;

  FUNCTION_CALL;
}

//==================================================================================================
// Members
//==================================================================================================

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
// Revision Recognition Rule
//==================================================================================================

revision returns [boolean applicable]
    :  (REVISION LEFT_PARENTH id=ID RIGHT_PARENTH)? {$applicable = isRevisionApplicable($id);} ->
    ;

//==================================================================================================
// Type Expression
//==================================================================================================

typeExpr
    :  id=ID
//  |  BOOL // TODO: NOT SUPPORTED IN THE CURRENT VERSION 
    |  INT^ LEFT_PARENTH! expr RIGHT_PARENTH!
    |  CARD^ LEFT_PARENTH! expr RIGHT_PARENTH!
//  |  FIX^ LEFT_PARENTH! expr COMMA! expr RIGHT_PARENTH!
    |  FLOAT^ LEFT_PARENTH! expr COMMA! expr RIGHT_PARENTH!
//  |  LEFT_HOOK constExpr DOUBLE_DOT constExpr RIGHT_HOOK -> ^(RANGE constExpr constExpr) // TODO: NOT SUPPORTED IN THE CURRENT VERSION  
//  |  ENUM^ LEFT_PARENTH! identifierList RIGHT_PARENTH! // TODO: NOT SUPPORTED IN THE CURRENT VERSION
    |  TYPE_OF^ LEFT_PARENTH! expr RIGHT_PARENTH!
    ;
catch [RecognitionException re] {
    reportError(re);
    recover(input,re);
}

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
    |  functionCall
    ;

functionDecl
    :  FUNCTION^ ID { declare($ID, NmlSymbolKind.FUNCTION, true); }
    ;

functionCall
    :  {isDeclaredAs(input.LT(1), NmlSymbolKind.FUNCTION)}?
       ID LEFT_PARENTH (expr (COMMA expr)*)? RIGHT_PARENTH -> ^(FUNCTION_CALL ID expr*)
    ;

ifExpr
    :  IF^ expr THEN! expr elseIfExpr* elseExpr ENDIF!
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
    |  constant
    |  letConst
    |  location
    |  typeCast
    |  mathFunc
    ;

constant
    :  CARD_CONST
    |  BINARY_CONST
    |  HEX_CONST
    ;

letConst
    :  {isDeclaredAs(input.LT(1), NmlSymbolKind.LET_CONST)}? ID -> ^(CONST ID)
    ;

typeCast
    :  SIGN_EXTEND^    LEFT_PARENTH! typeExpr COMMA! expr RIGHT_PARENTH!
    |  ZERO_EXTEND^    LEFT_PARENTH! typeExpr COMMA! expr RIGHT_PARENTH!
    |  COERCE^         LEFT_PARENTH! typeExpr COMMA! expr RIGHT_PARENTH!
    |  CAST^           LEFT_PARENTH! typeExpr COMMA! expr RIGHT_PARENTH!
    |  INT_TO_FLOAT^   LEFT_PARENTH! typeExpr COMMA! expr RIGHT_PARENTH!
    |  FLOAT_TO_INT^   LEFT_PARENTH! typeExpr COMMA! expr RIGHT_PARENTH!
    |  FLOAT_TO_FLOAT^ LEFT_PARENTH! typeExpr COMMA! expr RIGHT_PARENTH!
    |  IS_TYPE^        LEFT_PARENTH! typeExpr COMMA! expr RIGHT_PARENTH!
    ;

mathFunc
    :  SQRT^        LEFT_PARENTH! expr RIGHT_PARENTH!
    |  ROUND^       LEFT_PARENTH! expr RIGHT_PARENTH!
    |  IS_NAN^      LEFT_PARENTH! expr RIGHT_PARENTH!
    |  IS_SIGN_NAN^ LEFT_PARENTH! expr RIGHT_PARENTH!
    |  SIZE_OF^     LEFT_PARENTH! expr RIGHT_PARENTH!
    ;

//==================================================================================================
// Location
//==================================================================================================

location
    :  locationExpr -> ^(LOCATION locationExpr)
    ;

locationExpr
    :  locationVal (DOUBLE_COLON^ locationExpr)?
    ;

/*  If the bitFieldExpr expression fires, we rewrite the rule as a bitfield expression,
    otherwise we leave it as it is. */
locationVal
    :  locationAtom (bitFieldExpr -> ^(LOCATION_BITFIELD locationAtom bitFieldExpr) | -> locationAtom)
    |  LEFT_BRACE expr RIGHT_BRACE locationVal -> ^(LOCATION_REPEAT expr locationVal)
    ;

locationAtom
    :  ID
    |  ID LEFT_HOOK expr RIGHT_HOOK (DOT ID)* -> ^(LOCATION_INDEX ID expr ID*)
    |  ID (DOT ID)+ -> ^(DOT ID ID+)
    |  instance (DOT ID)? -> ^(INSTANCE_CALL instance ID?)
    ;

bitFieldExpr
@init  { setInBitField(true); }
    :  LEFT_BROCKET! expr (DOUBLE_DOT! expr)? RIGHT_BROCKET!
    ;
finally { setInBitField(false); }

//==================================================================================================
// Expression-like attribute rules (format expressions in the syntax and image attributes)
//==================================================================================================

attrExpr
    :  STRING_CONST
    |  FORMAT^ LEFT_PARENTH! STRING_CONST (COMMA! formatIdList)? RIGHT_PARENTH!
    ;

formatIdList
    :  formatId (COMMA! formatId)*
    ;

formatId
    :  STRING_CONST
    |  IF expr THEN formatId formatIdElseIf* ELSE formatId ENDIF ->
         ^(SIF expr formatId formatIdElseIf* ELSE formatId)
    |  expr
    |  ID DOT^ attributeFormatCall
    |  instance DOT attributeFormatCall -> ^(INSTANCE_CALL instance attributeFormatCall)
    ;

formatIdElseIf
    :  ELSEIF^ expr THEN! formatId
    ;

attributeFormatCall
    :  SYNTAX | IMAGE
    ;

//==================================================================================================
// Sequence statements (for action-like attributes)
//==================================================================================================

sequence
    :  (statement SEMI)* -> ^(SEQUENCE statement*) 
    ;

statement
    :  attributeCallStatement
    |  functionCall
    |  location ASSIGN^ expr
    |  conditionalStatement
    |  functionCallStatement
    |  RETURN^ expr
    ;

attributeCallStatement
    :  ID
    |  ID DOT^ attributeCall
    |  instance DOT attributeCall -> ^(INSTANCE_CALL instance attributeCall)
       // TODO: This is a hack to make it work somehow (calling 'init' externally like 'action')
    |  {isDeclaredAs(input.LT(1), NmlSymbolKind.ARGUMENT)}? ID DOT^ ID
    |  {isDeclaredAs(input.LT(1), NmlSymbolKind.MODE) || isDeclaredAs(input.LT(1), NmlSymbolKind.OP)}?
       ID LEFT_PARENTH (instance_arg2 (COMMA instance_arg2)*)? RIGHT_PARENTH DOT ID ->
       ^(INSTANCE_CALL ^(INSTANCE ID instance_arg2*) ID)
    ;

// TODO: Hack to overcome rule conflict at the price of limitations and ugliness
instance_arg2 : argument | constant;

attributeCall
    :  ACTION
    ;

instance
    :  {!isDeclaredAs(input.LT(1), NmlSymbolKind.FUNCTION)}? ID LEFT_PARENTH (instance_arg? (COMMA instance_arg)*)? RIGHT_PARENTH -> ^(INSTANCE ID instance_arg*)
    ;

instance_arg
    :  instance
    |  argument
    |  expr
    ;

argument
    :  {isDeclaredAs(input.LT(1), NmlSymbolKind.ARGUMENT) && (input.LA(2) == COMMA || input.LA(2) == RIGHT_PARENTH) }? ID -> ^(ARGUMENT ID)
    ;

conditionalStatement
    :  ifStmt
    ;

ifStmt
    :  IF^ expr THEN! sequence elseIfStmt* elseStmt? ENDIF!
    ;

elseIfStmt
    :  ELSEIF^ expr THEN! sequence
    ;

elseStmt
    :  ELSE^ sequence
    ;

functionCallStatement
    :  TRACE^ LEFT_PARENTH! STRING_CONST (COMMA! formatIdList)? RIGHT_PARENTH!
    |  EXCEPTION^ LEFT_PARENTH! STRING_CONST RIGHT_PARENTH!
    |  MARK^ LEFT_PARENTH! STRING_CONST RIGHT_PARENTH!
    |  UNPREDICTED (LEFT_PARENTH! RIGHT_PARENTH!)?
    |  UNDEFINED (LEFT_PARENTH! RIGHT_PARENTH!)?
    |  ASSERT^ LEFT_PARENTH! expr (COMMA! STRING_CONST)? RIGHT_PARENTH!
//  |  ERROR^ LEFT_PARENTH! STRING_CONST RIGHT_PARENTH!
    ;

//==================================================================================================
// The End
//==================================================================================================
