/*======================================================================================*/
/* README SECTION                                                                       */
/*                                                                                      */
/* TODO:                                                                                */
/* - Brief description of the tree rules' structure and format                          */
/* - Instructions on how to debug and extend the rules                                  */
/* - "TODO" notes                                                                       */     
/*======================================================================================*/

tree grammar SimnMLTreeWalker;

/*======================================================================================*/
/* Options                                                                              */
/*======================================================================================*/

options {
  language=Java;
  tokenVocab=SimnMLParser;
  ASTLabelType=CommonTree;
  superClass=TreeWalkerBase;
}

@rulecatch {
catch(SemanticException se) {
    reportError(se);
    recover(input,se);
}
catch (RecognitionException re) { // Default behavior
    reportError(re);
    recover(input,re);
}
}

/*======================================================================================*/
/* Header for the generated tree walker Java class file (header comments, imports, etc).*/
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
 * SimnMLTreeWalker.java Andrei Tatarnikov
 *
 * WARNING: THIS FILE IS AUTOMATICALLY GENERATED. PLEASE DO NOT MODIFY IT. 
 */

package ru.ispras.microtesk.translator.simnml.grammar;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.antlrex.SemanticException;

import ru.ispras.microtesk.translator.simnml.antlrex.TreeWalkerBase;
import ru.ispras.microtesk.translator.simnml.ESymbolKind;
import ru.ispras.microtesk.model.api.memory.EMemoryKind;

import ru.ispras.microtesk.translator.simnml.ir.PCAnalyzer;
import ru.ispras.microtesk.translator.simnml.ir.expression.*;
import ru.ispras.microtesk.translator.simnml.ir.expression2.*;
import ru.ispras.microtesk.translator.simnml.ir.shared.*;
import ru.ispras.microtesk.translator.simnml.ir.modeop.*;
}

/*======================================================================================*/
/* Members of the generated tree walker class.                                          */
/*======================================================================================*/

@members {
private Map<String, ArgumentTypeExpr> globalArgTypes = null;
}

/*======================================================================================*/
/* Root Rules of Processor Specification                                                */ 
/*======================================================================================*/

// Start rule
startRule 
    :  procSpec*
    ;

procSpec
@init {
System.out.println("Sim-nML:   " + $procSpec.text);
}
    :  letDef
    |  typeDef
    |  memDef
    |  regDef
    |  varDef
    |  modeDef
    |  opDef
    ;

/*======================================================================================*/
/* Let Rules                                                                            */
/*======================================================================================*/

letDef
    :  ^(LET id=ID le=letExpr[$id.text])
{
checkNotNull($id, $le.res, $le.text);
getIR().add($id.text, $le.res);
}
    ;

letExpr [String name] returns [LetExpr res]
    :  ce = staticJavaExpr
{
checkNotNull($ce.start, $ce.res, $ce.text);
$res = getLetFactory().createConstValue(name, $ce.res);
}
    |  sc = STRING_CONST
{
$res = getLetFactory().createConstString(name, $sc.text);

final LetLabel label = getLetFactory().createLabel(name, $sc.text);
if (null != label)
    getIR().add(name, label);
}
//  |  IF^ constNumExpr THEN! letExpr (ELSE! letExpr)? ENDIF! // NOT SUPPORTED IN THIS VERSION
//  |  SWITCH Construction                                    // NOT SUPPORTED IN THIS VERSION
    ;

/*======================================================================================*/
/* Type Rules                                                                           */
/*======================================================================================*/

typeDef
    :  ^(TYPE id=ID te=typeExpr)
{
checkNotNull($id, $te.res, $te.text);
getIR().add($id.text, $te.res);
}
    ;

typeExpr returns [TypeExpr res]
@init {final TypeExprFactory factory = getTypeExprFactory();}
    :   id=ID                      { $res=factory.createAlias($id.text); }
//  |   BOOL                       // TODO: NOT SUPPORTED IN THIS VERSION
    |   ^(t=INT  n=staticJavaExpr) { $res=factory.createIntegerType(where($t), $n.res); }
    |   ^(t=CARD n=staticJavaExpr) { $res=factory.createCardType(where($t), $n.res); }
//  |   ^(t=FIX   n=staticJavaExpr m=staticJavaExpr) // TODO: NOT SUPPORTED IN THIS VERSION
//  |   ^(t=FLOAT n=staticJavaExpr m=staticJavaExpr) // TODO: NOT SUPPORTED IN THIS VERSION
//  |   ^(t=RANGE n=staticJavaExpr m=staticJavaExpr) // TODO: NOT SUPPORTED IN THIS VERSION
    ;

/*======================================================================================*/
/* Location Rules (Memory, Registers, Variables)                                        */
/*======================================================================================*/

memDef
    :  ^(MEM id=ID st=sizeType alias?)
{
// TODO: implement IR for alises
final MemoryExprFactory factory = getMemoryExprFactory();
final MemoryExpr expr = (null != $st.size) ?
   factory.createMemoryExpr(where($id), EMemoryKind.MEM, $st.type, $st.size) :
   factory.createMemoryExpr(EMemoryKind.MEM, $st.type);

getIR().add($id.text, expr);
}
    ;

regDef
    :  ^(REG id=ID st=sizeType)
{
final MemoryExprFactory factory = getMemoryExprFactory();
final MemoryExpr expr = (null != $st.size) ?
   factory.createMemoryExpr(where($id), EMemoryKind.REG, $st.type, $st.size) :
   factory.createMemoryExpr(EMemoryKind.REG, $st.type);

getIR().add($id.text, expr);
}
    ;

varDef
    :  ^(VAR id=ID st=sizeType)
{
final MemoryExprFactory factory = getMemoryExprFactory();
final MemoryExpr expr = (null != $st.size) ?
   factory.createMemoryExpr(where($id), EMemoryKind.VAR, $st.type, $st.size) :
   factory.createMemoryExpr(EMemoryKind.VAR, $st.type);

getIR().add($id.text, expr);
}
    ;

sizeType returns [TypeExpr type, Expr size]
    :   ^(st=SIZE_TYPE s=staticJavaExpr t=typeExpr)
{ 
checkNotNull($st, $s.res, $s.text);
checkNotNull($st, $t.res, $t.text);
$type = $t.res;
$size = $s.res;
}
    |   ^(st=SIZE_TYPE t=typeExpr)
{
checkNotNull($st, $t.res, $t.text);
$type = $t.res;
$size = null;
}
    ;

alias
    :  ^(ALIAS locationAtom)
    ;

/*======================================================================================*/
/* Mode rules                                                                           */
/*======================================================================================*/

modeDef
    :  ^(MODE id=ID {pushSymbolScope(id);} sp=modeSpecPart[where($id), $id.text])
{
checkNotNull($id, $sp.res, $modeDef.text);
getIR().add($id.text, $sp.res);
}
    ;  finally
{
popSymbolScope();
globalArgTypes = null;
}

modeSpecPart [Where w, String name] returns [Mode res]
    :  andRes=andRule
{
checkNotNull(w, $andRes.res, $andRes.text);
globalArgTypes = $andRes.res;
}
       (mr=modeReturn {checkNotNull(w, $mr.res, $mr.text);})?
       attrRes=attrDefList
{
checkNotNull(w, $attrRes.res, $attrRes.text);
$res = getModeOpFactory().createMode($w, $name, $andRes.res, $attrRes.res, $mr.res);
}
    |  orRes=orRule
{
$res = getModeOpFactory().createModeOr($w, $name, $orRes.res);
}
    ;

modeReturn returns [Expr res]
    :  ^(RETURN me=modelExpr {checkNotNull($me.start, $me.res, $me.text);}) {$res = $me.res;}
    ;

/*======================================================================================*/
/* Op rules                                                                             */
/*======================================================================================*/

opDef
    :  ^(OP id=ID {pushSymbolScope(id);} sp=opSpecPart[where($id), $id.text])
{
checkNotNull($id, $sp.res, $opDef.text);
getIR().add($id.text, $sp.res);
}
    ;  finally
{
popSymbolScope();
globalArgTypes = null;
}

opSpecPart [Where w, String name] returns [Op res]
    :  andRes=andRule
{
checkNotNull(w, $andRes.res, $andRes.text);
globalArgTypes = $andRes.res;
}
       attrRes=attrDefList
{
checkNotNull(w, $attrRes.res, $attrRes.text);
$res = getModeOpFactory().createOp($w, $name, $andRes.res, $attrRes.res);
}
    |  orRes=orRule
{
$res = getModeOpFactory().createOpOr($w, $name, $orRes.res);
}
    ;

/*======================================================================================*/
/* Or rules (for modes and ops)                                                         */
/*======================================================================================*/

orRule returns [List<String> res]
@init  {$res = new ArrayList<String>();}
    :  ^(ALTERNATIVES (a=ID {$res.add($a.text);})+)
    ;

/*======================================================================================*/
/* And rules (for modes and ops)                                                        */
/*======================================================================================*/

andRule returns [Map<String,ArgumentTypeExpr> res]
@init  {final Map<String,ArgumentTypeExpr> args = new LinkedHashMap<String,ArgumentTypeExpr>();}
@after {$res = args;}
    :  ^(ARGS (^(id=ID at=argType)
{
declare($id, ESymbolKind.ARGUMENT, false);
args.put($id.text, $at.res);
})*)
    ;

argType returns [ArgumentTypeExpr res]
    :  ^(ARG_MODE id=ID) {$res = new ArgumentTypeExpr(EArgumentKind.MODE, $id.text);}
    |  ^(ARG_OP id=ID)   {$res = new ArgumentTypeExpr(EArgumentKind.OP,   $id.text);}
    |  te=typeExpr       {$res = new ArgumentTypeExpr(EArgumentKind.TYPE, $te.res); }
    ;

/*======================================================================================*/
/* Attribute rules (for modes and ops)                                                  */
/*======================================================================================*/

attrDefList returns [Map<String, Attribute> res]
@init  {final Map<String,Attribute> attrs = new LinkedHashMap<String,Attribute>();}
@after {$res = getAttributeFactory().addDefaultAttributes(attrs);}
    :  ^(ATTRS (attr=attrDef
{
checkNotNull($ATTRS, $attr.res, $attr.text);
attrs.put($attr.res.getName(), $attr.res);
})*)
    ;

attrDef returns [Attribute res]
    :  ^(SYNTAX {declare($SYNTAX, ESymbolKind.ATTRIBUTE, false);} attr=syntaxDef) {$res = $attr.res;}
    |  ^(IMAGE  {declare($IMAGE,  ESymbolKind.ATTRIBUTE, false);} attr=imageDef)  {$res = $attr.res;}
    |  ^(ACTION {declare($ACTION, ESymbolKind.ATTRIBUTE, false);} attr=actionDef[$ACTION.text]) {$res = $attr.res;}
    |  ^(id=ID {declare($ID, ESymbolKind.ATTRIBUTE, false);} attr=actionDef[$id.text]) {$res = $attr.res;}
//  |  USES ASSIGN usesDef     // NOT SUPPORTED IN THE CURRENT VERSION
    ;

syntaxDef returns [Attribute res]
@init  {final AttributeFactory factory = getAttributeFactory();}
    :  ^(DOT id=ID name=SYNTAX)
{
final Statement stmt = factory.createAttributeCallStatement($id.text, $name.text);
$res = factory.createFormatExpression("syntax", stmt);
}
    |  ae=attrExpr
{
if (null != $ae.res)
    $res = factory.createFormatExpression("syntax", $ae.res);
else
    $res = factory.syntax();
}
    ;

imageDef returns [Attribute res]
@init  {final AttributeFactory factory = getAttributeFactory();}
    :  ^(DOT id=ID name=IMAGE)
{
final Statement stmt = factory.createAttributeCallStatement($id.text, $name.text);
$res = factory.createFormatExpression("image", stmt);
}
    |  ae=attrExpr
{
if (null != $ae.res)
    $res = factory.createFormatExpression("image", $ae.res);
else
    $res = factory.image();
}
    ;

actionDef [String actionName] returns [Attribute res]
@init  {final AttributeFactory factory = getAttributeFactory();}
    :  ^(DOT id=ID name=ACTION)
{
final Statement stmt = factory.createAttributeCallStatement($id.text, $name.text);
$res = factory.createAction(actionName, Collections.singletonList(stmt));
}
    |  seq=sequence
{
checkNotNull($seq.start, $seq.res, $seq.text);
$res = factory.createAction(actionName, $seq.res);
}
    ;

/*======================================================================================*/
/* Expresion-like attribute rules(format expressions in the symtax and image attributes)*/
/*======================================================================================*/

attrExpr returns [Statement res]
@init  {final AttributeFactory factory = getAttributeFactory();}
    :  str=STRING_CONST
{
$res = factory.createTextLiteralStatement($str.text);
}
    |  ^(FORMAT fs=STRING_CONST fargs=formatIdList)
{
$res = factory.createFormatStatement($fs.text, $fargs.res);
}
    ;

formatIdList returns [List<FormatArgument> res]
@init  {final List<FormatArgument> args = new ArrayList<FormatArgument>();}
@after {$res = args;}
    :  (fa=formatId
{
args.add($fa.res);
})+
    ;

formatId returns [FormatArgument res]
@init  {final AttributeFactory factory = getAttributeFactory();}
    :  ^(DOT id=ID name=(SYNTAX | IMAGE))
{
$res = factory.createAttrCallFormatArgument($id.text, $name.text);
}
    |  e=modelExpr
{
$res = factory.createExprBasedFormatArgument($e.res);
}
    ;

/*======================================================================================*/
/* Sequence statements (for action-like attributes)                                     */
/*======================================================================================*/

sequence returns [List<Statement> res]
@init  {final List<Statement> stmts = new ArrayList<Statement>();}
@after {$res = stmts;}
    :  ^(sq=SEQUENCE (st=statement
{
checkNotNull($sq, $st.res, $st.text);
stmts.addAll($st.res);
})*)
    ;

statement returns [List<Statement> res]
    :  acs=attributeCallStatement
{
checkNotNull($acs.start, $acs.res, $acs.text);
$res = $acs.res;
}
    |  as=assignmentStatement
{
checkNotNull($as.start, $as.res, $as.text);
$res = $as.res;
}
    |  cs=conditionalStatement
{
checkNotNull($cs.start, $cs.res, $cs.text);
$res = $cs.res;
}
//  |  functionCall
//  |  ERROR^ LEFT_PARENTH! STRING_CONST RIGHT_PARENTH!
    ;

attributeCallStatement returns [List<Statement> res]
    :  id=ID
{
$res = Collections.singletonList(
    getAttributeFactory().createAttributeCallStatement($id.text));
}
    |  ^(DOT id=ID name=(ACTION | ID))
{
$res = Collections.singletonList(
    getAttributeFactory().createAttributeCallStatement($id.text, $name.text));
}
    ;

assignmentStatement returns [List<Statement> res]
@init
{
final AttributeFactory factory = getAttributeFactory();
final PCAnalyzer analyzer = new PCAnalyzer(getLocationExprFactory(), getIR());
}
    :  ^(ASSIGN le=location {analyzer.startTrackingSource();} me=modelExpr)
{
final List<Statement> result = new ArrayList<Statement>();
result.add(factory.createAssignmentStatement($le.res, $me.res));

final int ctIndex = analyzer.getControlTransferIndex();
if (ctIndex > 0)
    result.add(factory.createControlTransferStatement(ctIndex));

$res = result;
}
    ;
finally 
{
analyzer.finalize();
}

conditionalStatement returns [List<Statement> res]
    :   ^(IF cond=javaExpr stmts1=sequence stmts2=elseIf?)
{
$res = Collections.singletonList(
    getAttributeFactory().createIfElseStatement($cond.res, $stmts1.res, $stmts2.res));
}
    ;

elseIf returns [List<Statement> res]
    :   ^(ELSE sq=sequence) {$res = $sq.res;}
    ;

/*======================================================================================*/
/* Expression rules (run-time expressions)                                              */
/*======================================================================================*/

javaExpr returns [Expr res]
@init  {final ExprFactory factory = getExprFactory(EExprKind.JAVA);}
    :  e=expr[factory, 0]
{
checkNotNull($e.start, $e.res, $e.text);
$res = factory.evaluate(null, $e.res);
}
    ;

staticJavaExpr returns [Expr res]
@init  {final ExprFactory factory = getExprFactory(EExprKind.JAVA_STATIC);}
    :  e=expr[factory, 0]
{
checkNotNull($e.start, $e.res, $e.text);
$res = factory.evaluate(null, $e.res);
}
    ;

modelExpr returns [Expr res]
@init  {final ExprFactory factory = getExprFactory(EExprKind.MODEL);}
    :  e=expr[factory, 0]
{
checkNotNull($e.start, $e.res, $e.text);
$res = factory.evaluate(null, $e.res);
}
    ;

expr [ExprFactory factory, int depth] returns [Expr res]
@after {$res = $e.res;}
    :  e=binaryExpr[factory, depth]
    |   e=unaryExpr[factory, depth]
    |        e=atom[factory]
    ;

binaryExpr [ExprFactory factory, int depth] returns [Expr res]
@after
{
checkNotNull($e1.start, $e1.res, $e1.text);
checkNotNull($e2.start, $e2.res, $e2.text);
$res = factory.binary(where($op), $op.text, $e1.res, $e2.res);
}
    :  ^(op=OR            e1=expr[factory, depth + 1] e2=expr[factory, depth + 1])
    |  ^(op=AND           e1=expr[factory, depth + 1] e2=expr[factory, depth + 1])
    |  ^(op=VERT_BAR      e1=expr[factory, depth + 1] e2=expr[factory, depth + 1])
    |  ^(op=UP_ARROW      e1=expr[factory, depth + 1] e2=expr[factory, depth + 1])
    |  ^(op=AMPER         e1=expr[factory, depth + 1] e2=expr[factory, depth + 1])
    |  ^(op=EQ            e1=expr[factory, depth + 1] e2=expr[factory, depth + 1])
    |  ^(op=NEQ           e1=expr[factory, depth + 1] e2=expr[factory, depth + 1])
    |  ^(op=LEQ           e1=expr[factory, depth + 1] e2=expr[factory, depth + 1])
    |  ^(op=GEQ           e1=expr[factory, depth + 1] e2=expr[factory, depth + 1])
    |  ^(op=LEFT_BROCKET  e1=expr[factory, depth + 1] e2=expr[factory, depth + 1])
    |  ^(op=RIGHT_BROCKET e1=expr[factory, depth + 1] e2=expr[factory, depth + 1])
    |  ^(op=LEFT_SHIFT    e1=expr[factory, depth + 1] e2=expr[factory, depth + 1])
    |  ^(op=RIGHT_SHIFT   e1=expr[factory, depth + 1] e2=expr[factory, depth + 1])
    |  ^(op=ROTATE_LEFT   e1=expr[factory, depth + 1] e2=expr[factory, depth + 1])
    |  ^(op=ROTATE_RIGHT  e1=expr[factory, depth + 1] e2=expr[factory, depth + 1])
    |  ^(op=PLUS          e1=expr[factory, depth + 1] e2=expr[factory, depth + 1])
    |  ^(op=MINUS         e1=expr[factory, depth + 1] e2=expr[factory, depth + 1])
    |  ^(op=MUL           e1=expr[factory, depth + 1] e2=expr[factory, depth + 1])
    |  ^(op=DIV           e1=expr[factory, depth + 1] e2=expr[factory, depth + 1])
    |  ^(op=REM           e1=expr[factory, depth + 1] e2=expr[factory, depth + 1])
    |  ^(op=DOUBLE_STAR   e1=expr[factory, depth + 1] e2=expr[factory, depth + 1])
    ;

unaryExpr [ExprFactory factory, int depth] returns [Expr res]
@after
{
checkNotNull($e.start, $e.res, $e.text);
$res = factory.unary(where($op), $op.text, $e.res);
}
    :  ^(op=UNARY_PLUS    e=expr[factory, depth + 1])
    |  ^(op=UNARY_MINUS   e=expr[factory, depth + 1])
    |  ^(op=TILDE         e=expr[factory, depth + 1])
    |  ^(op=NOT           e=expr[factory, depth + 1])
    ;

atom [ExprFactory factory] returns [Expr res]
    :  ^(CONST token=ID)  {$res = factory.namedConst(where($token), $token.text);}
    |  ^(token=LOCATION le=locationExpr[0]) {$res = factory.location(where($token), $le.res);}
    |  token=CARD_CONST   {$res = factory.intConst(where($token), $token.text,10);}
    |  token=BINARY_CONST {$res = factory.intConst(where($token), $token.text, 2);}
    |  token=HEX_CONST    {$res = factory.intConst(where($token), $token.text,16);}
    |  ^(token=COERCE te=typeExpr e=modelExpr)
{
checkNotNull($te.start, $te.res, $te.text);
checkNotNull($e.start, $e.res, $e.text);
$res = factory.coerce(where($token), $e.res, $te.res);
}
    ;

/*======================================================================================*/
/* Location rules (rules for accessing model memory)                                    */
/*======================================================================================*/

location returns [LocationExpr res]
    :  ^(LOCATION le=locationExpr[0]) {$res = $le.res;} 
    ;

locationExpr [int depth] returns [LocationExpr res]
@init  {final LocationExprFactory factory = getLocationExprFactory();}
    :  ^(node=DOUBLE_COLON left=locationVal right=locationExpr[depth+1])
{
$res = factory.concat(where($node), $left.res, $right.res);
}
    |  value=locationVal
{
$res = $value.res;
}
    ;

locationVal returns [LocationExpr res]
@init  {final LocationExprFactory factory = getLocationExprFactory();}
    :  ^(node=LOCATION_BITFIELD la=locationAtom je1=staticJavaExpr je2=staticJavaExpr)
{
$res = factory.bitfield(where($node), $la.res, $je1.res, $je2.res);
}
    |  la=locationAtom
{
$res = $la.res;
}
    ;

locationAtom returns [LocationExpr res]
@init
{
final LocationExprFactory factory = getLocationExprFactory();
LocationExpr atom = null;
}
@after
{
$res = atom;
}
    :  ^(LOCATION_INDEX id=ID e=javaExpr)
{
atom = factory.location(where($id), $id.text, $e.res);
}
    |  id=ID
{
atom = factory.location(where($id), $id.text, globalArgTypes);
}
    ;

/*======================================================================================*/
