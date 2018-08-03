/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.model.spec;

import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.basis.MemoryAccessContext;

/**
 * {@link MmuBinding} describes an assignment, i.e. a pair of the kind {@code lhs = rhs},
 * where {@code lhs} is an {@link Node} and {@code rhs} is an {@link Node}.
 *
 * <p>The right-hand side of the assignment is allowed to be {@code null}. It means that the
 * expression can be derived from the context.</p>
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuBinding {
  /** Left-hand side. */
  private final Node lhs;
  /** Right-hand side or {@code null}. */
  private final Node rhs;

  public MmuBinding(final Node lhs, final Node rhs) {
    InvariantChecks.checkNotNull(lhs);
    InvariantChecks.checkNotNull(rhs);

    this.lhs = lhs;
    this.rhs = rhs;
  }

  public MmuBinding(final Node lhs) {
    InvariantChecks.checkNotNull(lhs);

    this.lhs = lhs;
    this.rhs = null;
  }

  public MmuBinding(final Variable lhs, final Node rhs) {
    InvariantChecks.checkNotNull(lhs);
    InvariantChecks.checkNotNull(rhs);

    this.lhs = new NodeVariable(lhs);
    this.rhs = rhs;
  }

  public MmuBinding(final Node lhs, final Variable rhs) {
    InvariantChecks.checkNotNull(lhs);
    InvariantChecks.checkNotNull(rhs);

    this.lhs = lhs;
    this.rhs = new NodeVariable(rhs);
  }

  public MmuBinding(final Variable lhs, final Variable rhs) {
    InvariantChecks.checkNotNull(lhs);
    InvariantChecks.checkNotNull(rhs);

    this.lhs = new NodeVariable(lhs);
    this.rhs = new NodeVariable(rhs);
  }

  public MmuBinding(final Variable lhs, final BitVector rhs) {
    InvariantChecks.checkNotNull(lhs);
    InvariantChecks.checkNotNull(rhs);

    this.lhs = new NodeVariable(lhs);
    this.rhs = NodeValue.newBitVector(rhs);
  }

  public Node getLhs() {
    return lhs;
  }

  public Node getRhs() {
    return rhs;
  }

  public MmuBinding getInstance(
      final String lhsInstanceId,
      final String rhsInstanceId,
      final MemoryAccessContext context) {
    InvariantChecks.checkNotNull(context);

    return new MmuBinding(
        context.getInstance(lhsInstanceId, lhs),
        context.getInstance(rhsInstanceId, rhs));
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder(lhs.toString());

    if (rhs != null) {
      builder.append("=");
      builder.append(rhs);
    }

    return builder.toString();
  }
}
