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

package ru.ispras.microtesk.translator.mmu.spec.builder;

import ru.ispras.fortress.expression.Node;

public class ExpressionConverter {

  public static class Result {
    public final ResultType resultType;
    public final Object object;

    private Result(ResultType resultType, Object object) {
      this.resultType = resultType;
      this.object = object;
    }
  }

  public static enum ResultType {
    VALUE,    // BigInteger
    VARIABLE, // IntegerVariable
    FIELD,    // IntegerField
    CONCAT    // MmuExpression
  }

  private final VariableTracker variables;

  private ExpressionConverter(VariableTracker variables) {
    this.variables = variables;
  }

  public Result convert(Node expr) {
    return null;
  }
 }
