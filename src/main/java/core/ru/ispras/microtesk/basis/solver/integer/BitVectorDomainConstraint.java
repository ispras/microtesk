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

package ru.ispras.microtesk.basis.solver.integer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link BitVectorDomainConstraint} class represents a domain constraint.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BitVectorDomainConstraint implements BitVectorConstraint {
  /**
   * {@link Kind} contains domain constraint Kinds.
   */
  public static enum Kind {
    RETAIN,
    EXCLUDE
  }

  private final Kind kind;
  private final Node variable;
  private final Set<BitVector> values;

  private final Node formula;

  public BitVectorDomainConstraint(
      final Kind kind,
      final Node variable,
      final Set<BitVector> domain,
      final Set<BitVector> values) {
    InvariantChecks.checkNotNull(kind);
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(values);
    InvariantChecks.checkNotEmpty(values);
    // Parameter {@code domain} can be null.

    this.kind = kind;
    this.variable = variable;
    this.values = values;

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
      final Node equality = new NodeOperation(
          effectiveKind == Kind.RETAIN ? StandardOperation.EQ : StandardOperation.NOTEQ,
          variable,
          new NodeValue(Data.newBitVector(value)));

      operands.add(equality);
    }

    this.formula = new NodeOperation(
        effectiveKind == Kind.RETAIN ? StandardOperation.OR : StandardOperation.AND,
        operands);
  }

  public BitVectorDomainConstraint(
      final Node variable,
      final Set<BitVector> domain,
      final Set<BitVector> values) {
    this(Kind.RETAIN, variable, domain, values);
  }

  public BitVectorDomainConstraint(
      final Node variable,
      final Set<BitVector> values) {
    this(Kind.RETAIN, variable, null, values);
  }

  public BitVectorDomainConstraint(
      final Node variable,
      final BitVector value) {
    this(variable, Collections.singleton(value));
  }

  public Kind getKind() {
    return kind;
  }

  public Node getVariable() {
    return variable;
  }

  public Set<BitVector> getValues() {
    return values;
  }

  @Override
  public Node getFormula() {
    return formula;
  }

  @Override
  public String toString() {
    return formula.toString();
  }
}
