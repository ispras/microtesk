/*
 * Copyright 2013-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.ir.expression;

import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * Contains descriptions of nML operators.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public enum Operator {
  OR("||", Operands.BINARY, Priority.CURRENT),

  AND("&&", Operands.BINARY, Priority.HIGHER),

  BIT_OR("|", Operands.BINARY, Priority.HIGHER),
  BIT_XOR("^", Operands.BINARY, Priority.HIGHER),
  BIT_AND("&", Operands.BINARY, Priority.HIGHER),

  EQ("==", Operands.BINARY, Priority.HIGHER),
  NOT_EQ("!=", Operands.BINARY, Priority.CURRENT),

  LEQ("<=", Operands.BINARY, Priority.HIGHER),
  GEQ(">=", Operands.BINARY, Priority.CURRENT),
  LESS("<", Operands.BINARY, Priority.CURRENT),
  GREATER(">", Operands.BINARY, Priority.CURRENT),

  L_SHIFT("<<", Operands.BINARY, Priority.HIGHER),
  R_SHIFT(">>", Operands.BINARY, Priority.CURRENT),
  L_ROTATE("<<<", Operands.BINARY, Priority.CURRENT),
  R_ROTATE(">>>", Operands.BINARY, Priority.CURRENT),

  PLUS("+", Operands.BINARY, Priority.HIGHER),
  MINUS("-", Operands.BINARY, Priority.CURRENT),

  MUL("*", Operands.BINARY, Priority.HIGHER),
  DIV("/", Operands.BINARY, Priority.CURRENT),
  MOD("%", Operands.BINARY, Priority.CURRENT),

  POW("**", Operands.BINARY, Priority.HIGHER),

  UPLUS("UPLUS", Operands.UNARY, Priority.HIGHER),
  UMINUS("UMINUS", Operands.UNARY, Priority.CURRENT),
  BIT_NOT("~", Operands.UNARY, Priority.CURRENT),
  NOT("!", Operands.UNARY, Priority.CURRENT),

  // Synthetic operators
  ITE("",  Operands.TERNARY, Priority.HIGHER),
  SQRT("", Operands.UNARY,  Priority.HIGHER);

  private static enum Priority {
    CURRENT {
      @Override
      public int value() {
        return priorityCounter;
      }
    },

    HIGHER {
      @Override
      public int value() {
        return ++priorityCounter;
      }
    };

    abstract int value();
    private static int priorityCounter = 0;
  }

  private static final Map<String, Operator> operators;
  static {
    final Operator[] ops = Operator.values();
    operators = new HashMap<>(ops.length);

    for (final Operator o : ops) {
      operators.put(o.text(), o);
    }
  }

  /**
   * Returns an operator that corresponds to the specified text.
   * 
   * @param text Textual representation of the operator.
   * @return Operator.
   */

  public static Operator forText(final String text) {
    return operators.get(text);
  }

  private final String text;
  private final int operands;
  private final int priority;

  private Operator(final String text, final Operands operands, final Priority priority) {
    InvariantChecks.checkNotNull(text);
    InvariantChecks.checkNotNull(operands);
    InvariantChecks.checkNotNull(priority);

    this.text = text;
    this.operands = operands.count();
    this.priority = priority.value();
  }

  /**
   * Returns textual representation of the operator.
   * 
   * @return Operator text.
   */

  public String text() {
    return text;
  }

  /**
   * Returns relative priority of the operator.
   * 
   * @return Operator priority.
   */

  public int priority() {
    return priority;
  }

  /**
   * Returns operand number.
   * 
   * @return Operand number.
   */

  public int operands() {
    return operands;
  }
}
