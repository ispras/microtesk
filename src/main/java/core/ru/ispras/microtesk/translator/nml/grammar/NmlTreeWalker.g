/*
 * Copyright 2012-2015 ISP RAS (http://www.ispras.ru)
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

/*======================================================================================*/
/* README SECTION                                                                       */
/*                                                                                      */
/* TODO:                                                                                */
/* - Brief description of the tree rules' structure and format                          */
/* - Instructions on how to debug and extend the rules                                  */
/* - "TODO" notes                                                                       */     
/*======================================================================================*/

tree grammar NmlTreeWalker;

/*======================================================================================*/
/* Options                                                                              */
/*======================================================================================*/

options {
  language=Java;
  tokenVocab=NmlParser;
  ASTLabelType=CommonTree;
  superClass=NmlTreeWalkerBase;
}

@rulecatch {
catch (final RecognitionException re) { // Default behavior
  reportError(re);
  recover(input,re);
}
}

/*======================================================================================*/
/* Header for the generated tree walker Java class file (header comments, imports, etc).*/
/*======================================================================================*/

@header {
/*
 * Copyright 2012-2015 ISP RAS (http://www.ispras.ru)
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
 *
 * WARNING: THIS FILE IS AUTOMATICALLY GENERATED. PLEASE DO NOT MODIFY IT. 
 */

package ru.ispras.microtesk.translator.nml.grammar;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

import ru.ispras.fortress.util.Pair;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;

import ru.ispras.microtesk.translator.antlrex.symbols.Where;
import ru.ispras.microtesk.translator.nml.antlrex.NmlTreeWalkerBase;
import ru.ispras.microtesk.translator.nml.NmlSymbolKind;
import ru.ispras.microtesk.model.api.memory.Memory;

import ru.ispras.microtesk.translator.nml.ir.expr.*;
import ru.ispras.microtesk.translator.nml.ir.shared.*;
import ru.ispras.microtesk.translator.nml.ir.primitive.*;
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
// System.out.println("nML:   " + $procSpec.text);
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
    ;

letExpr [String name]
    :  ce = constExpr
{
checkNotNull($ce.start, $ce.res, $ce.text);
getLetFactory().createConstant(name, $ce.res);
}
    |  sc = STRING_CONST
{
getLetFactory().createString(name, $sc.text);
}
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

typeExpr returns [Type res]
    :   id=ID { $res=getTypeFactory().newAlias(where($id), $id.text); }
//  |   BOOL                       // TODO: NOT SUPPORTED IN THIS VERSION
    |   ^(t=INT   n=sizeExpr) { $res=getTypeFactory().newInt(where($t), $n.res); }
    |   ^(t=CARD  n=sizeExpr) { $res=getTypeFactory().newCard(where($t), $n.res); }
//  |   ^(t=FIX   n=sizeExpr m=sizeExpr)
//           { $res=getTypeFactory().newFix(where($t), $n.res, $m.res); }
    |   ^(t=FLOAT n=sizeExpr m=sizeExpr)
            { $res=getTypeFactory().newFloat(where($t), $n.res, $m.res); }
//  |   ^(t=RANGE n=staticJavaExpr m=staticJavaExpr) // TODO: NOT SUPPORTED IN THIS VERSION
    ;

/*======================================================================================*/
/* Location Rules (Memory, Registers, Variables)                                        */
/*======================================================================================*/

memDef
    :  ^(MEM id=ID st=sizeType al=alias?)
{
checkNotNull($id, $st.type, $st.text);
final MemoryExprFactory factory = getMemoryExprFactory();
final MemoryExpr expr = 
   factory.createMemory(where($id), Memory.Kind.MEM, $st.type, $st.size, $al.res);

getIR().add($id.text, expr);
}
    ;

regDef
    :  ^(REG id=ID st=sizeType al=alias?)
{
checkNotNull($id, $st.type, $st.text);
final MemoryExprFactory factory = getMemoryExprFactory();
final MemoryExpr expr =
   factory.createMemory(where($id), Memory.Kind.REG, $st.type, $st.size, $al.res);

getIR().add($id.text, expr);
}
    ;

varDef
    :  ^(VAR id=ID st=sizeType al=alias?)
{
checkNotNull($id, $st.type, $st.text);
final MemoryExprFactory factory = getMemoryExprFactory();
final MemoryExpr expr =
   factory.createMemory(where($id), Memory.Kind.VAR, $st.type, $st.size, $al.res);

getIR().add($id.text, expr);
}
    ;

sizeType returns [Type type, Expr size]
    :  ^(st=SIZE_TYPE s=sizeExpr t=typeExpr)
{ 
checkNotNull($st, $s.res, $s.text);
checkNotNull($st, $t.res, $t.text);
$type = $t.res;
$size = $s.res;
}
    |  ^(st=SIZE_TYPE t=typeExpr)
{
checkNotNull($st, $t.res, $t.text);
$type = $t.res;
$size = null;
}
    ;

alias returns [Alias res]
@init {final MemoryExprFactory factory = getMemoryExprFactory();}
    :  ^(ALIAS le=location)
{
checkNotNull($le.start, $le.res, $le.text);
$res = Alias.forLocation($le.res);
}
    |  ^(ALIAS ^(DOUBLE_DOT id=ID min=indexExpr max=indexExpr))
{
checkNotNull($min.start, $min.res, $min.text);
checkNotNull($max.start, $max.res, $max.text);
$res = factory.createAlias(where($id), $id.text, $min.res, $max.res);
}
    ;

/*======================================================================================*/
/* Mode rules                                                                           */
/*======================================================================================*/

modeDef 
@init {reserveThis();}
    :  ^(MODE id=ID {pushSymbolScope(id);} sp=modeSpecPart[where($id), $id.text]
{
checkNotNull($id, $sp.res, $modeDef.text);
getIR().add($id.text, $sp.res);
})
    ;  finally
{
popSymbolScope();

resetThisArgs();
finalizeThis($sp.res);
}

modeSpecPart [Where w, String name] returns [Primitive res]
    :  andRes=andRule
{
checkNotNull(w, $andRes.res, $andRes.text);
setThisArgs($andRes.res);
}
       (mr=modeReturn {checkNotNull(w, $mr.res, $mr.text);})?
       attrRes=attrDefList
{
checkNotNull($attrRes.start, $attrRes.res, $attrRes.text);
$res = getPrimitiveFactory().createMode($w, $name, $andRes.res, $attrRes.res, $mr.res);
}
    |  orRes=orRule
{
$res = getPrimitiveFactory().createModeOR($w, $name, $orRes.res);
}
    ;

modeReturn returns [Expr res]
    :  ^(RETURN me=dataExpr {checkNotNull($me.start, $me.res, $me.text);}) {$res = $me.res;}
    ;

/*======================================================================================*/
/* Op rules                                                                             */
/*======================================================================================*/

opDef
@init {reserveThis();}
    :  ^(OP id=ID {pushSymbolScope(id);} sp=opSpecPart[where($id), $id.text]
{
checkNotNull($id, $sp.res, $opDef.text);
getIR().add($id.text, $sp.res);
})
    ;  finally
{
popSymbolScope();

resetThisArgs();
finalizeThis($sp.res);
}

opSpecPart [Where w, String name] returns [Primitive res]
    :  andRes=andRule
{
checkNotNull(w, $andRes.res, $andRes.text);
setThisArgs($andRes.res);
}
       attrRes=attrDefList
{
checkNotNull(w, $attrRes.res, $attrRes.text);
$res = getPrimitiveFactory().createOp($w, $name, $andRes.res, $attrRes.res);
}
    |  orRes=orRule
{
$res = getPrimitiveFactory().createOpOR($w, $name, $orRes.res);
}
    ;

/*======================================================================================*/
/* Or rules (for modes and ops)                                                         */
/*======================================================================================*/

orRule returns [List<String> res]
@init  {$res = new ArrayList<>();}
    :  ^(ALTERNATIVES (a=ID {$res.add($a.text);})+)
    ;

/*======================================================================================*/
/* And rules (for modes and ops)                                                        */
/*======================================================================================*/

andRule returns [Map<String,Primitive> res]
@init  {final Map<String,Primitive> args = new LinkedHashMap<>();}
@after {$res = args;}
    :  ^(ARGS (^(id=ID at=argType)
{
checkNotNull($id, $at.res, $at.text);
args.put($id.text, $at.res);
})*)
    ;

argType returns [Primitive res]
@init  {final PrimitiveFactory factory = getPrimitiveFactory();}
    :  ^(ARG_MODE id=ID) {$res = factory.getMode(where($id), $id.text);}
    |  ^(ARG_OP id=ID)   {$res = factory.getOp(where($id), $id.text);}
    |  te=typeExpr       {checkNotNull($te.start, $te.res, $te.text); $res = factory.createImm($te.res);}
    ;

/*======================================================================================*/
/* Attribute rules (for modes and ops)                                                  */
/*======================================================================================*/

attrDefList returns [Map<String, Attribute> res]
@init  {final Map<String,Attribute> attrs = new LinkedHashMap<>();}
@after {$res = attrs;}
    :  ^(ATTRS (attr=attrDef
{
checkNotNull($attr.start, $attr.res, $attr.text);
attrs.put($attr.res.getName(), $attr.res);
})*)
    ;

attrDef returns [Attribute res]
    :  ^(id=SYNTAX {checkMemberDeclared($SYNTAX, NmlSymbolKind.ATTRIBUTE);} attr1=syntaxDef
{
checkNotNull($attr1.start, $attr1.res, $attr1.text);
$res = $attr1.res;
})
    |  ^(id=IMAGE  {checkMemberDeclared($IMAGE,  NmlSymbolKind.ATTRIBUTE);} attr2=imageDef
{
checkNotNull($attr2.start, $attr2.res, $attr2.text);
$res = $attr2.res;
})
    |  ^(id=ACTION {checkMemberDeclared($ACTION, NmlSymbolKind.ATTRIBUTE);} attr3=actionDef[$ACTION.text]
{
checkNotNull($attr3.start, $attr3.res, $attr3.text);
$res = $attr3.res;
})
    |  ^(id=ID  {checkMemberDeclared($ID, NmlSymbolKind.ATTRIBUTE);} attr4=actionDef[$id.text]
{
checkNotNull($attr4.start, $attr4.res, $attr4.text);
$res = $attr4.res;
})
//  |  USES ASSIGN usesDef     // NOT SUPPORTED IN THE CURRENT VERSION
    ;

syntaxDef returns [Attribute res]
    :  ^(DOT id=ID name=SYNTAX)
{
final Statement stmt = getStatementFactory().createAttributeCall(where($id), $id.text, $name.text);
$res = getAttributeFactory().createExpression("syntax", stmt);
}
    |  ae=attrExpr
{
$res = getAttributeFactory().createExpression("syntax", $ae.res);
}
    ;

imageDef returns [Attribute res]
    :  ^(DOT id=ID name=IMAGE)
{
final Statement stmt = getStatementFactory().createAttributeCall(where($id), $id.text, $name.text);
$res = getAttributeFactory().createExpression("image", stmt);
}
    |  ae=attrExpr
{
$res = getAttributeFactory().createExpression("image", $ae.res);
}
    ;

actionDef [String actionName] returns [Attribute res]
@init  {final AttributeFactory factory = getAttributeFactory();}
    :  ^(DOT id=ID name=ACTION)
{
final Statement stmt = getStatementFactory().createAttributeCall(where($id), $id.text, $name.text);
$res = factory.createAction(actionName, Collections.singletonList(stmt));
}
    |  seq=sequence
{
checkNotNull($seq.start, $seq.res, $seq.text);
$res = factory.createAction(actionName, $seq.res);
}
    ;

/*======================================================================================*/
/* Expression-like attribute rules(format expressions in syntax and image attributes)   */
/*======================================================================================*/

attrExpr returns [Statement res]
    :  str=STRING_CONST
{
$res = getStatementFactory().createFormat(where($str), $str.text, null);
}
    |  ^(FORMAT fs=STRING_CONST (fargs=formatIdList)?)
{
$res = getStatementFactory().createFormat(where($fs), $fs.text, $fargs.res);
}
    ;

formatIdList returns [List<Format.Argument> res]
@init  {final List<Format.Argument> args = new ArrayList<>();}
@after {$res = args;}
    :  (fa=formatId {args.add($fa.res);})+
    ;

formatId returns [Format.Argument res]
    : str=STRING_CONST
{
$res = Format.createArgument($str.text);
}
    | ^(SIF  {final Format.ConditionBuilder builder = new Format.ConditionBuilder();}
       c1=logicExpr e1=formatId            {builder.addCondition($c1.res, $e1.res);}
       (^(ELSEIF c2=logicExpr e2=formatId) {builder.addCondition($c2.res, $e2.res);})*
       ELSE e3=formatId)                   {builder.addCondition(null,    $e3.res);}
{
$res = builder.build();
}
    | e=dataExpr
{
$res = Format.createArgument($e.res);
}
    |  ^(DOT id=ID name=(SYNTAX | IMAGE))
{
$res = Format.createArgument((StatementAttributeCall)
    getStatementFactory().createAttributeCall(where($id), $id.text, $name.text));
}
    |  ^(INSTANCE_CALL i=instance name=(SYNTAX | IMAGE))
{
checkNotNull($i.start, $i.res, $i.text);
$res = Format.createArgument((StatementAttributeCall)
    getStatementFactory().createAttributeCall(where($i.start), $i.res, $name.text));
}
    ;

/*======================================================================================*/
/* Sequence statements (for action-like attributes)                                     */
/*======================================================================================*/

sequence returns [List<Statement> res]
@init  {final List<Statement> stmts = new ArrayList<>();}
@after {$res = stmts;}
    :  ^(sq=SEQUENCE (st=statement
{
checkNotNull($st.start, $st.res, $st.text);
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
    |  fcs=functionCallStatement
{
checkNotNull($fcs.start, $fcs.res, $fcs.text);
$res = $fcs.res;
}
    ;

attributeCallStatement returns [List<Statement> res]
    :  id=ID
{
$res = Collections.singletonList(
    getStatementFactory().createAttributeCall(where($id), $id.text));
}
    |  ^(DOT id=ID name=(ACTION | ID))
{
$res = Collections.singletonList(
    getStatementFactory().createAttributeCall(where($id), $id.text, $name.text));
}
    |  ^(INSTANCE_CALL i=instance name=(ACTION | ID))
{
checkNotNull($i.start, $i.res, $i.text);
$res = Collections.singletonList(
   getStatementFactory().createAttributeCall(where($i.start), $i.res, $name.text)); 
}
    ;

instance returns [Instance res]
@init  {final List<InstanceArgument> args = new ArrayList<>();}
    :  ^(INSTANCE id=ID (arg=instance_arg {args.add($arg.res);})*)
{
$res = getPrimitiveFactory().newInstance(where($id), $id.text, args);
}
    ;

instance_arg returns [InstanceArgument res]
@init
{
getLocationFactory().beginCollectingArgs();
}
    :  i=instance
{
$res = InstanceArgument.newInstance($i.res);
}
    |  e=dataExpr
{
$res = InstanceArgument.newExpr($e.res, getLocationFactory().getInvolvedArgs());
}
    |  ^(ARGUMENT id=ID)
{
$res = InstanceArgument.newPrimitive(
    $id.text, getPrimitiveFactory().getArgument(where($id), $id.text));
}
    ;
finally
{
getLocationFactory().endCollectingArgs();
}

assignmentStatement returns [List<Statement> res]
@init
{
getLocationFactory().beginLhs();
}
    :  ^(ASSIGN le=location
{
checkNotNull($le.start, $le.res, $le.text);
getLocationFactory().beginRhs();
}
    me=dataExpr)
{
checkNotNull($me.start, $me.res, $me.text);
final List<Statement> result = new ArrayList<>();
result.add(getStatementFactory().createAssignment(where($le.start), $le.res, $me.res));
$res = result;
}
    ;
finally
{
getLocationFactory().endAssignment();
}

conditionalStatement returns [List<Statement> res]
    :  ifs = ifStmt { $res = $ifs.res; }
    ;

ifStmt returns [List<Statement> res]
@init  {final List<StatementCondition.Block> blocks = new ArrayList<>();}
    :  ^(IF {getLocationFactory().beginRhs();} cond=logicExpr {getLocationFactory().endAssignment();}
         stmts=sequence
{
checkNotNull($stmts.start, $stmts.res, $stmts.text);
blocks.add(StatementCondition.Block.newIfBlock($cond.res, $stmts.res));
}
        (elifb=elseIfStmt
{
checkNotNull($elifb.start, $elifb.res, $elifb.text);
blocks.add($elifb.res);
})*
        (eb=elseStmt
{
checkNotNull($eb.start, $eb.res, $eb.text);
blocks.add($eb.res);
})?)
{
$res = Collections.singletonList(getStatementFactory().createCondition(blocks));
}
    ;

elseIfStmt returns [StatementCondition.Block res]
    :  ^(ELSEIF {getLocationFactory().beginRhs();}cond=logicExpr{getLocationFactory().endAssignment();} stmts=sequence)
{
checkNotNull($stmts.start, $stmts.res, $stmts.text);
$res = StatementCondition.Block.newIfBlock($cond.res, $stmts.res);
}
    ;

elseStmt returns [StatementCondition.Block res]
    :  ^(ELSE stmts=sequence)
{
checkNotNull($stmts.start, $stmts.res, $stmts.text);
$res = StatementCondition.Block.newElseBlock($stmts.res);
}
    ;
    
functionCallStatement returns [List<Statement> res]
    :  ^(id=EXCEPTION str=STRING_CONST)
{
$res = Arrays.asList(
    getStatementFactory().createExceptionCall(where($id), $str.text)
    );
}
    |  ^(id=TRACE fs=STRING_CONST (fargs=formatIdList)?)
{
$res = Collections.singletonList(
    getStatementFactory().createTrace(where($id), $fs.text, $fargs.res));
}
    |  ^(id=MARK str=STRING_CONST)
{
$res = Collections.singletonList(
    getStatementFactory().createMark(where($id), $str.text));
}
    |  UNPREDICTED
{
$res = Collections.singletonList(getStatementFactory().createUnpredicted());
}
    |  UNDEFINED 
{
$res = Collections.singletonList(getStatementFactory().createUndefined());
}
    ;

/*======================================================================================*/
/* Extended Expression Rules                                                            */
/*                                                                                      */
/* There are several use cases for expressions that impose certain restrictions on them:*/
/*                                                                                      */
/* 1. Constant expressions. These expressions are statically calculated at translation  */
/*    time and evaluated to constant Java values (currently, "int" or "long"). Constant */
/*    expressions are used in Let constructions.                                        */
/*                                                                                      */
/* 2. Size expressions. These expressions are constant expressions evaluated to         */
/*    constant integer values. Size has the Java "int" type. Size expressions are used  */
/*    to describe types (e.g. card(32)) and memory locations (reg, mem and var          */
/*    definitions).                                                                     */
/*                                                                                      */
/* 3. Index expressions. These expressions should be evaluated to Java integer values.  */
/*    Index is represented by then Java "int" type. Index expressions are used to       */
/*    access locations by their index in a memory line (e.g. GPR[index + 1]) and to     */
/*    address bitfields of locations (e.g. temp<x+1 .. x+5>).                           */
/*                                                                                      */
/* 4. Logic expressions. There expressions are evaluated to boolean values. Logic       */
/*    expressions are used in condition statements.                                     */
/*                                                                                      */
/* 5. Data expressions. Data expressions are described in terms of locations. All       */
/*    manipulations with locations are described by data expressions. These expressions */
/*    are used in assignment statements, etc.                                           */
/*======================================================================================*/

constExpr returns [Expr res]
    :  e=expr[0]
{
checkNotNull($e.start, $e.res, $e.text);
$res = getExprFactory().evaluateConst(where($e.start), $e.res);
}
    ;

sizeExpr returns [Expr res]
    :  e=expr[0]
{
checkNotNull($e.start, $e.res, $e.text);
$res = getExprFactory().evaluateSize(where($e.start), $e.res);
}
    ;

indexExpr returns [Expr res]
    :  e=expr[0]
{
checkNotNull($e.start, $e.res, $e.text);
$res = getExprFactory().evaluateIndex(where($e.start), $e.res);
}
    ;

logicExpr returns [Expr res]
    :  e=expr[0]
{
checkNotNull($e.start, $e.res, $e.text);
$res = getExprFactory().evaluateLogic(where($e.start), $e.res);
}
    ;

dataExpr returns [Expr res]
    :  e=expr[0]
{
checkNotNull($e.start, $e.res, $e.text);
$res = getExprFactory().evaluateData(where($e.start), $e.res);
}
    ;   

/*======================================================================================*/
/* Expression rules                                                                     */
/*======================================================================================*/

expr [int depth] returns [Expr res]
@after {$res = $e.res;}
    : e=nonNumExpr[depth]
    | e=numExpr[depth]
    ;

/*======================================================================================*/
/* Non-numeric expressions (TODO: temporary implementation)                             */
/*======================================================================================*/

nonNumExpr [int depth] returns [Expr res]
@after {$res = $e.res;}
    : e=ifExpr[depth]
    ;

ifExpr [int depth] returns [Expr res]
@init  {final List<Pair<Expr, Expr>> conds = new ArrayList<>();}
    :  ^(op=IF cond=logicExpr e=expr[depth]
{
checkNotNull($cond.start, $cond.res, $cond.text);
checkNotNull($e.start, $e.res, $e.text);
conds.add(new Pair<>($cond.res, $e.res));
}
       (eifc=elseIfExpr[depth]
{
checkNotNull($eifc.start, $eifc.res, $eifc.text);
conds.add($eifc.res);
})*
       (elsc=elseExpr[depth]
{
checkNotNull($elsc.start, $elsc.res, $elsc.text);
conds.add($elsc.res);
}))
{
$res = getExprFactory().condition(where($op), conds);
}
    ;

elseIfExpr [int depth] returns [Pair<Expr, Expr> res]
    :  ^(ELSEIF cond=logicExpr e=expr[depth])
{
checkNotNull($cond.start, $cond.res);
checkNotNull($e.start, $e.res);
$res = new Pair<>($cond.res, $e.res);
}
    ;

elseExpr [int depth] returns [Pair<Expr, Expr> res]
    :  ^(ELSE e=expr[depth]) {
$res = new Pair<>(new Expr(NodeValue.newBoolean(true)), $e.res);
}
    ;

/*======================================================================================*/
/* Numeric expressions                                                                  */
/*======================================================================================*/
    
numExpr [int depth] returns [Expr res]
@after {$res = $e.res;}
    :  e=binaryExpr[depth]
    |   e=unaryExpr[depth]
    |        e=atom
    ;

binaryExpr [int depth] returns [Expr res]
@after
{
checkNotNull($e1.start, $e1.res, $e1.text);
checkNotNull($e2.start, $e2.res, $e2.text);
$res = getExprFactory().operator(where($op), $op.text, $e1.res, $e2.res);
}
    :  ^(op=OR            e1=expr[depth + 1] e2=expr[depth + 1])
    |  ^(op=AND           e1=expr[depth + 1] e2=expr[depth + 1])
    |  ^(op=VERT_BAR      e1=expr[depth + 1] e2=expr[depth + 1])
    |  ^(op=UP_ARROW      e1=expr[depth + 1] e2=expr[depth + 1])
    |  ^(op=AMPER         e1=expr[depth + 1] e2=expr[depth + 1])
    |  ^(op=EQ            e1=expr[depth + 1] e2=expr[depth + 1])
    |  ^(op=NEQ           e1=expr[depth + 1] e2=expr[depth + 1])
    |  ^(op=LEQ           e1=expr[depth + 1] e2=expr[depth + 1])
    |  ^(op=GEQ           e1=expr[depth + 1] e2=expr[depth + 1])
    |  ^(op=LEFT_BROCKET  e1=expr[depth + 1] e2=expr[depth + 1])
    |  ^(op=RIGHT_BROCKET e1=expr[depth + 1] e2=expr[depth + 1])
    |  ^(op=LEFT_SHIFT    e1=expr[depth + 1] e2=expr[depth + 1])
    |  ^(op=RIGHT_SHIFT   e1=expr[depth + 1] e2=expr[depth + 1])
    |  ^(op=ROTATE_LEFT   e1=expr[depth + 1] e2=expr[depth + 1])
    |  ^(op=ROTATE_RIGHT  e1=expr[depth + 1] e2=expr[depth + 1])
    |  ^(op=PLUS          e1=expr[depth + 1] e2=expr[depth + 1])
    |  ^(op=MINUS         e1=expr[depth + 1] e2=expr[depth + 1])
    |  ^(op=MUL           e1=expr[depth + 1] e2=expr[depth + 1])
    |  ^(op=DIV           e1=expr[depth + 1] e2=expr[depth + 1])
    |  ^(op=REM           e1=expr[depth + 1] e2=expr[depth + 1])
    |  ^(op=DOUBLE_STAR   e1=expr[depth + 1] e2=expr[depth + 1])
    ;

unaryExpr [int depth] returns [Expr res]
@after
{
checkNotNull($e.start, $e.res, $e.text);
$res = getExprFactory().operator(where($op), $op.text, $e.res);
}
    :  ^(op=UPLUS   e=expr[depth + 1])
    |  ^(op=UMINUS  e=expr[depth + 1])
    |  ^(op=TILDE   e=expr[depth + 1])
    |  ^(op=NOT     e=expr[depth + 1])
    ;

atom returns [Expr res]
    :  ^(CONST token=ID)  {$res = getExprFactory().namedConstant(where($token), $token.text);}
    |  ^(token=LOCATION le=locationExpr[0]
{
checkNotNull($le.start, $le.res, $le.text);
$res = $le.res;
})
    |  token=CARD_CONST   {$res = getExprFactory().constant(where($token), $token.text,10);}
    |  token=BINARY_CONST {$res = getExprFactory().constant(where($token), $token.text, 2);}
    |  token=HEX_CONST    {$res = getExprFactory().constant(where($token), $token.text,16);}
    |  tc=typeCast
{
checkNotNull($tc.start, $tc.res, $tc.text);
$res = $tc.res;
}
    |  ^(token=SQRT e=dataExpr
{
checkNotNull($e.start, $e.res, $e.text);
$res = getExprFactory().sqrt(where($token), $e.res);
})
    |  ^(token=IS_NAN e=dataExpr
{
checkNotNull($e.start, $e.res, $e.text);
$res = getExprFactory().isNan(where($token), $e.res);
})
    |  ^(token=IS_SIGN_NAN e=dataExpr
{
checkNotNull($e.start, $e.res, $e.text);
$res = getExprFactory().isSignalingNan(where($token), $e.res);
})
    ;

/*======================================================================================*/
/* Type conversion resules                                                              */
/*======================================================================================*/

typeCast returns [Expr res]
    :  ^(token=SIGN_EXTEND te=typeExpr e=dataExpr
{
checkNotNull($te.start, $te.res, $te.text);
checkNotNull($e.start,   $e.res,  $e.text);
$res = getExprFactory().signExtend(where($token), $e.res, $te.res);
})
    |  ^(token=ZERO_EXTEND te=typeExpr e=dataExpr
{
checkNotNull($te.start, $te.res, $te.text);
checkNotNull($e.start,   $e.res,  $e.text);
$res = getExprFactory().zeroExtend(where($token), $e.res, $te.res);
})
    |  ^(token=COERCE te=typeExpr e=dataExpr
{
checkNotNull($te.start, $te.res, $te.text);
checkNotNull($e.start,   $e.res,  $e.text);
$res = getExprFactory().coerce(where($token), $e.res, $te.res);
})
    |  ^(token=CAST te=typeExpr e=dataExpr
{
checkNotNull($te.start, $te.res, $te.text);
checkNotNull($e.start,   $e.res,  $e.text);
$res = getExprFactory().cast(where($token), $e.res, $te.res);
})
    |  ^(token=INT_TO_FLOAT te=typeExpr e=dataExpr
{
checkNotNull($te.start, $te.res, $te.text);
checkNotNull($e.start,   $e.res,  $e.text);
$res = getExprFactory().int_to_float(where($token), $e.res, $te.res);
})
    |  ^(token=FLOAT_TO_INT te=typeExpr e=dataExpr
{
checkNotNull($te.start, $te.res, $te.text);
checkNotNull($e.start,   $e.res,  $e.text);
$res = getExprFactory().float_to_int(where($token), $e.res, $te.res);
})
    |  ^(token=FLOAT_TO_FLOAT te=typeExpr e=dataExpr
{
checkNotNull($te.start, $te.res, $te.text);
checkNotNull($e.start,   $e.res,  $e.text);
$res = getExprFactory().float_to_float(where($token), $e.res, $te.res);
})
    ;

/*======================================================================================*/
/* Location rules (rules for accessing model memory)                                    */
/*======================================================================================*/

location returns [Expr res]
    :  ^(LOCATION le=locationExpr[0] {checkNotNull($le.start, $le.res, $le.text);})
{
$res = $le.res;
}
    ;

locationExpr [int depth] returns [Expr res]
    :  ^(node=DOUBLE_COLON left=locationVal right=locationExpr[depth+1]
{
checkNotNull($left.start,  $left.res,  $left.text);
checkNotNull($right.start, $right.res, $right.text);

$res = getLocationFactory().concat(where($node), $left.res, $right.res);
})
    |  value=locationVal
{
$res = $value.res;
}
    ;

locationVal returns [Expr res]
    :  ^(node=LOCATION_BITFIELD la=locationAtom je1=indexExpr (je2=indexExpr)?)
{
checkNotNull($la.start, $la.res, $la.text);
checkNotNull($je1.start, $je1.res, $je1.text);

if (null == $je2.res)
    $res = getLocationFactory().bitfield(where($node), $la.res, $je1.res);
else
    $res = getLocationFactory().bitfield(where($node), $la.res, $je1.res, $je2.res);
}
    |  la=locationAtom
{
$res = $la.res;
}
    |  ^(LOCATION_REPEAT count=constExpr value=locationVal)
{
$res = getExprFactory().repeat(where($count.start), $count.res, $value.res);
}
    ;

locationAtom returns [Expr res]
    :  ^(LOCATION_INDEX id=ID e=indexExpr)
{
checkNotNull($e.start, $e.res, $e.text);
$res = getLocationFactory().location(where($id), $id.text, $e.res);
}
    |  id=ID
{
$res = getLocationFactory().location(where($id), $id.text);
}
    | ^(DOT id=ID ID+) {raiseError(where($id), "Unsupported construct.");}
    | ^(INSTANCE_CALL w=.) {raiseError(where($w), "Unsupported construct.");}
    ;

/*======================================================================================*/
