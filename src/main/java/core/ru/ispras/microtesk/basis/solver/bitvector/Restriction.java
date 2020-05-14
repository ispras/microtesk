/*
 * Copyright 2015-2020 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.basis.solver.bitvector;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.Nodes;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link Restriction} is generic interface of a bit-vector constraint.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Restriction {
  private Restriction() {}

  public enum Kind {
    RETAIN,
    EXCLUDE
  }

  public static Node domain(
      final Kind kind,
      final Node variable,
      final Set<BitVector> domain,
      final Set<BitVector> values) {
    InvariantChecks.checkNotNull(kind);
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(values);
    InvariantChecks.checkNotEmpty(values);
    // Parameter {@code domain} can be null.

    final boolean inverse = (domain != null) && (domain.size() < 2 * values.size());

    final Kind effectiveKind;
    final Set<BitVector> effectiveValues;

    if (inverse) {
      effectiveKind = kind == Kind.RETAIN ? Kind.EXCLUDE : Kind.RETAIN;
      effectiveValues = new LinkedHashSet<>(domain);
      effectiveValues.removeAll(values);
    } else {
      effectiveKind = kind;
      effectiveValues = values;
    }

    // Construct the constraint formula.
    final List<Node> operands = new ArrayList<>(effectiveValues.size());
    for (final BitVector value : effectiveValues) {
      final Node equality = effectiveKind == Kind.RETAIN
          ? Nodes.eq(variable, NodeValue.newBitVector(value))
          : Nodes.noteq(variable, NodeValue.newBitVector(value));

      operands.add(equality);
    }

    return effectiveKind == Kind.RETAIN ? Nodes.or(operands) : Nodes.and(operands);
  }

  public static Node domain(
      final Node variable,
      final Set<BitVector> values) {
    return domain(Kind.RETAIN, variable, null, values);
  }

  public static Node range(final Variable variable, final BitVector min, final BitVector max) {
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(min);
    InvariantChecks.checkNotNull(max);

    final Node lowerBound = Nodes.bvuge(new NodeVariable(variable), NodeValue.newBitVector(min));
    final Node upperBound = Nodes.bvule(new NodeVariable(variable), NodeValue.newBitVector(max));

    return Nodes.and(lowerBound, upperBound);
  }
}
