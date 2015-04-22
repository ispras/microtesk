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

package ru.ispras.microtesk.translator.nml.ir.primitive;

import ru.ispras.microtesk.translator.nml.ir.expression.Expr;
import ru.ispras.microtesk.translator.nml.ir.location.Location;

public final class StatementAssignment extends Statement {
  private final Location left;
  private final Expr right;

  StatementAssignment(Location left, Expr right) {
    super(Kind.ASSIGN);

    if (null == left) {
      throw new NullPointerException();
    }

    if (null == right) {
      throw new NullPointerException();
    }

    this.left = left;
    this.right = right;
  }

  public Location getLeft() {
    return left;
  }

  public Expr getRight() {
    return right;
  }
}
