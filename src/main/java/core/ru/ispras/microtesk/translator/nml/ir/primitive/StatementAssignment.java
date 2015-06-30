/*
 * Copyright 2013-2015 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.nml.ir.expression.Expr;
import ru.ispras.microtesk.translator.nml.ir.location.Location;

public final class StatementAssignment extends Statement {
  private final Location left;
  private final Expr right;

  StatementAssignment(final Location left, final Expr right) {
    super(Kind.ASSIGN);

    InvariantChecks.checkNotNull(left);
    InvariantChecks.checkNotNull(right);

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
