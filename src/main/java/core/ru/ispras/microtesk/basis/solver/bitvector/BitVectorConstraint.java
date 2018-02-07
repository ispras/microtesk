/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.Nodes;
import ru.ispras.fortress.util.BitUtils;
import ru.ispras.fortress.util.InvariantChecks;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link BitVectorConstraint} is generic interface of a bit-vector constraint.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class BitVectorConstraint {
  private BitVectorConstraint() {}

  private static final String NEW_VARIABLE_PREFIX = "new$";
  private static int newVariableId = 0;

  public static enum Kind {
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

    final List<Node> operands = new ArrayList<>();
    encodeGreaterThanOrEqualTo(operands, variable, min);
    encodeLessThanOrEqualTo(operands, variable, max);

    return Nodes.and(operands);
  }

  private static void encodeGreaterThanOrEqualTo(
      final List<Node> operands,
      final Variable x,
      final BitVector a) {
    // Represent x >= a.
    encodeInequality(operands, x, a, true);
  }

  private static void encodeLessThanOrEqualTo(
      final List<Node> operands,
      final Variable x,
      final BitVector b) {
    // Represent x <= b.
    encodeInequality(operands, x, b, false);
  }

  private static void encodeInequality(
      final List<Node> operands,
      final Variable x,
      final BitVector a,
      final boolean greaterThanOrEqualTo) {

    int lowerBit = 0;
    while (lowerBit < x.getType().getSize() && greaterThanOrEqualTo != a.getBit(lowerBit)) {
      lowerBit++;
    }

    int upperBit = x.getType().getSize() - 1;
    while (upperBit >= 0 && greaterThanOrEqualTo == a.getBit(upperBit)) {
      upperBit--;
    }

    if (upperBit + 1 < x.getType().getSize()) {
      final int sizeInBits = (x.getType().getSize() - upperBit) - 1;

      final BigInteger value = greaterThanOrEqualTo
          ? BitUtils.getBigIntegerMask(sizeInBits)
          : BigInteger.ZERO;

      operands.add(
          Nodes.eq(
              Nodes.bvextract(x.getType().getSize() - 1, upperBit + 1, x),
              NodeValue.newBitVector(BitVector.valueOf(value, sizeInBits))
          )
      );
    }

    if (upperBit <= lowerBit) {
      return;
    }

    int numberOfBits = 0;
    for (int i = lowerBit; i <= upperBit; i++) {
      if (greaterThanOrEqualTo != a.getBit(i)) {
        numberOfBits++;
      }
    }

    // Introduce a new variable to encode OR.
    final Variable e = new Variable(
        String.format("%s%d", NEW_VARIABLE_PREFIX, newVariableId++),
        DataType.bitVector(numberOfBits + 1));

    // (e[0] | ... | e[n-1]) == (e != 0).
    operands.add(
        Nodes.noteq(
            Nodes.bvextract(e),
            NodeValue.newBitVector(BitVector.FALSE)
        )
    );

    // u[0] == (x[upper] = a[upper]).
    // e[0] <=> u[0] == (~u[0] | e[0]) & (u[0] | ~e[0]).
    //                     clause 1         clause 2
    final List<Node> clauseBuilder1 = new ArrayList<>();

    clauseBuilder1.add(
        Nodes.noteq(
            Nodes.bvextract(upperBit, lowerBit, x),
            NodeValue.newBitVector(a.field(lowerBit, upperBit))
        )
    );

    clauseBuilder1.add(
        Nodes.eq(
            Nodes.bvextract(0, e),
            NodeValue.newBitVector(BitVector.TRUE)
        )
    );

    operands.add(Nodes.or(clauseBuilder1));

    final List<Node> clauseBuilder2 = new ArrayList<>();

    clauseBuilder2.add(
        Nodes.eq(
            Nodes.bvextract(upperBit, lowerBit, x),
            NodeValue.newBitVector(a.field(lowerBit, upperBit))
        )
    );

    clauseBuilder2.add(
        Nodes.eq(
            Nodes.bvextract(0, e),
            NodeValue.newBitVector(BitVector.FALSE)
        )
    );

    operands.add(Nodes.or(clauseBuilder2));

    int k = 1;
    for (int i = upperBit; i >= lowerBit; i--) {
      if (greaterThanOrEqualTo == a.getBit(i)) {
        continue;
      }

      // u[k] == (x[upper] = a[upper]).
      // v[k] == (x[next] = 1 (for >=) or 0 (for <=)).

      // e[k] <=> u[k] & v[k] == (~u[k] | ~v[k] | e[k]) & (u[k] | ~e[k]) & (v[k] | ~e[k]).
      //                                 clause 3            clause 4         clause 5
      final List<Node> clauseBuilder3 = new ArrayList<>();

      if (i < upperBit) {
        final int j = i + 1;

        clauseBuilder3.add(
            Nodes.noteq(
                Nodes.bvextract(upperBit, j, x),
                NodeValue.newBitVector(a.field(j, upperBit))
            )
        );

        final List<Node> clauseBuilder4 = new ArrayList<>();

        clauseBuilder4.add(
            Nodes.eq(
                Nodes.bvextract(upperBit, j, x),
                NodeValue.newBitVector(a.field(j, upperBit))
            )
        );

        clauseBuilder4.add(
            Nodes.eq(
                Nodes.bvextract(k, e),
                NodeValue.newBitVector(BitVector.FALSE)
            )
        );

        operands.add(Nodes.or(clauseBuilder4));
      }

      clauseBuilder3.add(
          Nodes.eq(
              Nodes.bvextract(i, x),
              NodeValue.newBitVector(greaterThanOrEqualTo ? BitVector.FALSE : BitVector.TRUE)
          )
      );

      clauseBuilder3.add(
          Nodes.eq(
              Nodes.bvextract(k, e),
              NodeValue.newBitVector(BitVector.TRUE)
          )
       );

      operands.add(Nodes.or(clauseBuilder3));

      final List<Node> clauseBuilder5 = new ArrayList<>();

      clauseBuilder5.add(
          Nodes.eq(
              Nodes.bvextract(i, x),
              NodeValue.newBitVector(greaterThanOrEqualTo ? BitVector.TRUE : BitVector.FALSE)
          )
      );

      clauseBuilder5.add(
          Nodes.eq(
              Nodes.bvextract(k, e),
              NodeValue.newBitVector(BitVector.FALSE)
          )
      );

      operands.add(Nodes.or(clauseBuilder5));

      k++;
    }
  }

}
