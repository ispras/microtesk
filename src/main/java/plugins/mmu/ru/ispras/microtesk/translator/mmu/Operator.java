/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.mmu;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.expression.StandardOperation;

public enum Operator {
  //------------------------------------------------------------------------------------------------
  OR("||",     rule(DataTypeId.LOGIC_BOOLEAN, StandardOperation.OR)),

  //------------------------------------------------------------------------------------------------
  AND("&&",    rule(DataTypeId.LOGIC_BOOLEAN, StandardOperation.AND)),
  
  //------------------------------------------------------------------------------------------------
  BIT_OR("|",  rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVOR)),
  BIT_XOR("^", rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVXOR)),
  BIT_AND("&", rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVAND)),

  //------------------------------------------------------------------------------------------------
  EQ("==",     rule(DataTypeId.BIT_VECTOR,    StandardOperation.EQ),
               rule(DataTypeId.LOGIC_INTEGER, StandardOperation.EQ)),
  NOT_EQ("!=", rule(DataTypeId.BIT_VECTOR,    StandardOperation.NOTEQ),
               rule(DataTypeId.LOGIC_INTEGER, StandardOperation.NOTEQ)),
  LEQ("<=",    rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVULE),
               rule(DataTypeId.LOGIC_INTEGER, StandardOperation.LESSEQ)),
  GEQ(">=",    rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVUGE),
               rule(DataTypeId.LOGIC_INTEGER, StandardOperation.GREATEREQ)),
  LESS("<",    rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVULT),
               rule(DataTypeId.LOGIC_INTEGER, StandardOperation.LESS)),
  GREATER(">", rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVUGT),
               rule(DataTypeId.LOGIC_INTEGER, StandardOperation.GREATER)),

  //------------------------------------------------------------------------------------------------
  L_SHIFT("<<"),
  R_SHIFT(">>"),
  L_ROTATE("<<<"),
  R_ROTATE(">>>"),

  //------------------------------------------------------------------------------------------------
  PLUS("+",    rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVADD),
               rule(DataTypeId.LOGIC_INTEGER, StandardOperation.ADD)),
  MINUS("-",   rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVSUB),
               rule(DataTypeId.LOGIC_INTEGER, StandardOperation.SUB)),

  //------------------------------------------------------------------------------------------------
  MUL("*",     rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVMUL),
               rule(DataTypeId.LOGIC_INTEGER, StandardOperation.MUL)),
  DIV("/"),
  MOD("%"),

  //------------------------------------------------------------------------------------------------
  POW("**",    rule(DataTypeId.LOGIC_INTEGER, StandardOperation.POWER)),

  //------------------------------------------------------------------------------------------------
  UPLUS("UPLUS"),
  UMINUS("UMINUS"),
  BIT_NOT("~"),
  NOT("!");

  private static final Map<String, Operator> operators;
  static {
    operators = new HashMap<>(); 
    for (Operator op : values()) {
      operators.put(op.getText(), op);
    }
  }
  
  private static class Rule {
    final DataTypeId type;
    final StandardOperation op;

    Rule(DataTypeId type, StandardOperation op) {
      this.type = type;
      this.op = op;
    }
  }

  private static Rule rule(DataTypeId type, StandardOperation op) {
    return new Rule(type, op);
  }

  private final String text;
  private final Map<DataTypeId, StandardOperation> fortressOperators;

  private Operator(String text, Rule... rules) {
    this.text = text;
    this.fortressOperators = new EnumMap<>(DataTypeId.class);

    for (Rule rule : rules) {
      this.fortressOperators.put(rule.type, rule.op);
    }
  }

  public String getText() {
    return text;
  }
  
  public static Operator fromText(String text) {
    return operators.get(text);
  }

  public StandardOperation toFortressFor(DataTypeId typeId) {
    return fortressOperators.get(typeId);
  }
}
