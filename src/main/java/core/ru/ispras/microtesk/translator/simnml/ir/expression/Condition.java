/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.simnml.ir.expression;

/**
 * Helper class to temporarily represent conditional expressions based on the if-elif-else operator:
 * 
 * <pre>
 * if cond1 then 
 *    expr1
 * elif cond2 then
 *    expr2
 * ...
 * else
 *    exprN
 * endif
 * </pre>
 * 
 * @author Andrei Tatarnikov
 */

public final class Condition {
  private final Expr cond;
  private final Expr expr;

  private Condition(Expr cond, Expr expr) {
    if (null == expr) {
      throw new NullPointerException();
    }

    this.cond = cond;
    this.expr = expr;
  }

  public static Condition newIf(Expr cond, Expr expr) {
    if (null == cond) {
      throw new NullPointerException();
    }

    return new Condition(cond, expr);
  }

  public static Condition newElse(Expr expr) {
    return new Condition(null, expr);
  }

  public Expr getCondition() {
    return cond;
  }

  public boolean isElse() {
    return null == cond;
  }

  public Expr getExpression() {
    return expr;
  }
}
