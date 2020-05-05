/*
 * Copyright 2017-2020 ISP RAS (http://www.ispras.ru)
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.IntSupplier;
import java.util.stream.IntStream;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link BitBlaster} implements an encoder of Fortress nodes to SAT4J CNFs.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public enum BitBlaster {
  /**
   * Encodes the true constraint.
   */
  TRUE {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      // The returned collection should be modifiable.
      return new ArrayList<>();
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return FALSE.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes the false constraint.
   */
  FALSE {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      final Collection<IntArray> clauses = new ArrayList<>(1);

      // The returned collection should be modifiable.
      clauses.add(new IntArray(0));
      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return TRUE.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a conjunction of n boolean variables, i.e. {@code x[0] & ... & x[n-1]}.
   */
  AND {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      // Generate n unit clauses: AND[i]{x[i]}.
      final Collection<IntArray> clauses = new ArrayList<>(operands.length);

      for (final Operand operand : operands) {
        final IntArray literals = new IntArray(new int[]{ operand.getSignedIndex() });
        clauses.add(literals);
      }

      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      // De Morgan's law: ~(x[0] & ... & x[n-1]) == (~x[0] | ... | ~x[n-1]).
      for (final Operand operand : operands) {
        operand.sign ^= true;
      }

      final Collection<IntArray> clauses = OR.encodePositive(operands, newIndex);

      for (final Operand operand : operands) {
        operand.sign ^= true;
      }

      return clauses;
    }
  },

  /**
   * Encodes a disjunction of n boolean variables, i.e. {@code x[0] | ... | x[n-1]}.
   */
  OR {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      // Generate 1 clause OR[i]{x[i]}.
      final Collection<IntArray> clauses = new ArrayList<>(1);
      final IntArray literals = new IntArray(operands.length);

      for (final Operand operand : operands) {
        literals.add(operand.getSignedIndex());
      }

      clauses.add(literals);
      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      // De Morgan's law: ~(x[0] | ... | x[n-1]) == (~x[0] & ... & ~x[n-1]).
      for (final Operand operand : operands) {
        operand.sign ^= true;
      }

      final Collection<IntArray> clauses = AND.encodePositive(operands, newIndex);

      for (final Operand operand : operands) {
        operand.sign ^= true;
      }

      return clauses;
    }
  },

  /**
   * Encodes an exclusive disjunction of 2 boolean variables, i.e. {@code x ^ y}.
   */
  XOR {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      InvariantChecks.checkTrue(operands.length == 2);

      // (x ^ y) == (x | y) & (~x | ~y).
      final Collection<IntArray> clauses = new ArrayList<>(2);

      final int xIndex = operands[0].getSignedIndex();
      final int yIndex = operands[1].getSignedIndex();

      final IntArray literals1 = new IntArray(new int[] { +xIndex, +yIndex });
      clauses.add(literals1);

      final IntArray literals2 = new IntArray(new int[] { -xIndex, -yIndex });
      clauses.add(literals2);

      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      InvariantChecks.checkTrue(operands.length == 2);

      // ~(x ^ y) == (~x | y) & (x | ~y).
      final Collection<IntArray> clauses = new ArrayList<>(2);

      final int xIndex = operands[0].getSignedIndex();
      final int yIndex = operands[1].getSignedIndex();

      final IntArray literals1 = new IntArray(new int[] { -xIndex, +yIndex });
      clauses.add(literals1);

      final IntArray literals2 = new IntArray(new int[] { +xIndex, -yIndex });
      clauses.add(literals2);

      return clauses;
    }
  },

  /**
   * Encodes an implication of 2 boolean variables, i.e. {@code x -> y}.
   */
  IMPL {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      // (x -> y) == (~x | y).
      operands[0].sign ^= true;
      final Collection<IntArray> clauses = OR.encodePositive(operands, newIndex);
      operands[0].sign ^= true;

      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      // ~(x -> y) == (x & ~y).
      operands[1].sign ^= true;
      final Collection<IntArray> clauses = AND.encodePositive(operands, newIndex);
      operands[1].sign ^= true;

      return clauses;
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x == c}.
   */
  EQ_CONST {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      InvariantChecks.checkTrue(operands[0].isVariable());
      InvariantChecks.checkTrue(operands[1].isValue());

      final int size = operands[0].size;

      // Generate n unit clauses (c[i] ? x[i] : ~x[i]), i.e. for all i: x[i] == c[i].
      final Collection<IntArray> clauses = new ArrayList<>(size);

      for (int i = 0; i < size; i++) {
        final int index = operands[0].getSignedIndex(i);
        final IntArray literals = new IntArray(
            new int[] { operands[1].value.testBit(i) ? +index : -index });

        clauses.add(literals);
      }

      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return NOTEQ_CONST.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x == y}.
   */
  EQ_VAR {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      InvariantChecks.checkTrue(operands[0].isVariable());
      InvariantChecks.checkTrue(operands[1].isVariable());

      final int size = operands[0].size;

      // Generate 2*n clauses AND[i]{(x[i] & y[i]) | (~x[i] & ~y[i])} ==
      // AND[i]{(~x[i] | y[i]) & (x[i] | ~y[i])}.
      final Collection<IntArray> clauses = new ArrayList<>(2 * size);

      for (int i = 0; i < size; i++) {
        final int xIndex = operands[0].getSignedIndex(i);
        final int yIndex = operands[1].getSignedIndex(i);

        final IntArray literals1 = new IntArray(new int[] { +xIndex, -yIndex });
        clauses.add(literals1);

        final IntArray literals2 = new IntArray(new int[] { -xIndex, +yIndex });
        clauses.add(literals2);
      }

      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return NOTEQ_VAR.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x == c} or {@code x == y}.
   */
  EQ {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      final Operand[] newOperands = new Operand[] {
          operands[0].isVariable() ? operands[0] : operands[1],
          operands[0].isVariable() ? operands[1] : operands[0]
      };

      if (newOperands[1].isValue()) {
        return EQ_CONST.encodePositive(newOperands, newIndex);
      }

      return EQ_VAR.encodePositive(newOperands, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return NOTEQ.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x != c}.
   */
  NOTEQ_CONST {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      InvariantChecks.checkTrue(operands[0].isVariable());
      InvariantChecks.checkTrue(operands[1].isValue());

      final int size = operands[0].size;

      // Generate 1 clause OR[i]{c[i] ? ~x[i] : x[i]}, i.e. exists i: x[i] != c[i].
      final Collection<IntArray> clauses = new ArrayList<>(1);

      final IntArray literals = new IntArray(size);
      for (int i = 0; i < size; i++) {
        final int index = operands[0].getSignedIndex(i);
        literals.add(operands[1].value.testBit(i) ? -index : +index);
      }

      // The returned collection should be modifiable.
      clauses.add(literals);
      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return EQ_CONST.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x != y}.
   */
  NOTEQ_VAR {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      InvariantChecks.checkTrue(operands[0].isVariable());
      InvariantChecks.checkTrue(operands[1].isVariable());

      final int size = operands[0].size;

      // Generate 6*n+1 clauses (see below).
      final Collection<IntArray> clauses = new ArrayList<>(6 * size + 1);

      // Generate 1 clause OR[i]{[~]x[i] & ~[~]y[i] | ~[~]x[i] & [~]y[i]} == OR[i]{u[i] | v[i]}.
      final Operand[] uv = IntStream.range(0, 2 * size).mapToObj(
          i -> new Operand(newIndex.getAsInt(), 1)).toArray(Operand[]::new);
      clauses.addAll(OR.encode(operands, newIndex));

      final int uIndex = uv[0].index;
      final int vIndex = uIndex + size;

      // Generate 3*n clauses AND[i]{u[i] <=> ([~]x[i] & ~[~]y[i])} ==
      // AND[i]{(~[~]x[i] | [~]y[i] | u[i]) & ([~]x[i] | ~u[i]) & (~[~]y[i] | ~u[i])}.
      operands[1].sign ^= true;
      clauses.addAll(BVAND.encode(
          new Operand[] { new Operand(uIndex, size), operands[0], operands[1] }, newIndex
      ));
      operands[1].sign ^= true;

      // Generate 3*n clauses AND[i]{v[i] <=> (~[~]x[i] & [~]y[i])} ==
      // AND[i]{([~]x[i] | ~[~]y[i] | v[i]) & (~[~]x[i] | ~v[i]) & ([~]y[i] | ~v[i])}.
      operands[0].sign ^= true;
      clauses.addAll(BVAND.encode(
          new Operand[] { new Operand(vIndex, size), operands[0], operands[1] }, newIndex
      ));
      operands[0].sign ^= true;

      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return EQ_VAR.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x != c} or {@code x != y}.
   */
  NOTEQ {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      final Operand[] newOperands = new Operand[] {
          operands[0].isVariable() ? operands[0] : operands[1],
          operands[0].isVariable() ? operands[1] : operands[0]
      };

      if (newOperands[1].isValue()) {
        return NOTEQ_CONST.encodePositive(newOperands, newIndex);
      }

      return NOTEQ_VAR.encodePositive(newOperands, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return EQ.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x <= c} (unsigned).
   */
  BVULE_CONST {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      InvariantChecks.checkTrue(operands[0].isVariable());
      InvariantChecks.checkTrue(operands[1].isValue());

      final int size = operands[0].size;

      // Generate at most n clauses (see below).
      final Collection<IntArray> clauses = new ArrayList<>(size);

      // x[n-1]x[x-2]...x[0] <= c[n-1]c[x-2]...c[0].
      for (int i = size - 1; i >= 0; i--) {
        final int index = operands[0].getSignedIndex(i);;

        if (!operands[1].value.testBit(i)) {
          // ...x[i]x[i-1]...x[0] <= ...0c[i-0]...c[0].
          // Add the unit clause ~x[i].
          final IntArray literals = new IntArray(new int[] { -index });
          clauses.add(literals);
        } else {
          // ...x[i]x[i-1]...x[0] <= ...1c[i-0]...c[0].
          // Add the clauses (~x[i] | encode(x[i-1]...x[0], c[i-1]...c[0])).
          operands[0].size = i;
          final Collection<IntArray> tailClauses = encodePositive(operands, newIndex);
          operands[0].size = size;

          for (final IntArray clause : tailClauses) {
            clause.add(-index);
          }

          clauses.addAll(tailClauses);
          break;
        }
      }

      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVUGT_CONST.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x < c} (unsigned).
   */
  BVULT_CONST {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      // Encode x <= c.
      final Collection<IntArray> clauses = BVULE_CONST.encodePositive(operands, newIndex);
      // Encode x != c.
      clauses.addAll(NOTEQ_CONST.encodePositive(operands, newIndex));
      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVUGE_CONST.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x >= c} (unsigned).
   */
  BVUGE_CONST {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      InvariantChecks.checkTrue(operands[0].isVariable());
      InvariantChecks.checkTrue(operands[1].isValue());

      final int size = operands[0].size;

      // Generate at most n clauses (see below).
      final Collection<IntArray> clauses = new ArrayList<>(size);

      // x[n-1]x[x-2]...x[0] >= c[n-1]c[x-2]...c[0].
      for (int i = size - 1; i >= 0; i--) {
        final int index = operands[0].getSignedIndex(i);

        if (operands[1].value.testBit(i)) {
          // ...x[i]x[i-1]...x[0] >= ...1c[i-0]...c[0].
          // Add the unit clause x[i].
          final IntArray literals = new IntArray(new int[] { index });
          literals.add(index);

          clauses.add(literals);
        } else {
          // ...x[i]x[i-1]...x[0] >= ...0c[i-0]...c[0].
          // Add the clauses (x[i] | encode(x[i-1]...x[0], c[i-1]...c[0])).
          operands[0].size = i;
          final Collection<IntArray> tailClauses = encodePositive(operands, newIndex);
          operands[0].size = size;

          for (final IntArray clause : tailClauses) {
            clause.add(index);
          }

          clauses.addAll(tailClauses);
          break;
        }
      }

      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVULT_CONST.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x > c} (unsigned).
   */
  BVUGT_CONST {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      // Encode x >= c.
      final Collection<IntArray> clauses = BVUGE_CONST.encodePositive(operands, newIndex);
      // Encode x != c.
      clauses.addAll(NOTEQ_CONST.encodePositive(operands, newIndex));
      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVULE_CONST.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x <= y} (unsigned).
   */
  BVULE_VAR {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      return encodeUnsignedLessThan(operands, newIndex, false);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVUGT_VAR.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x < y} (unsigned).
   */
  BVULT_VAR {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      return encodeUnsignedLessThan(operands, newIndex, true);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVUGE_VAR.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x >= y} (unsigned).
   */
  BVUGE_VAR {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVULE_VAR.encodePositive(new Operand[] { operands[1], operands[0] }, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVULT_VAR.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x > y} (unsigned).
   */
  BVUGT_VAR {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVULT_VAR.encodePositive(new Operand[] { operands[1], operands[0]}, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVULE_VAR.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x <= c} or {@code x <= y}.
   */
  BVULE {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      final Operand[] newOperands = new Operand[] {
          operands[0].isVariable() ? operands[0] : operands[1],
          operands[0].isVariable() ? operands[1] : operands[0]
      };

      if (newOperands[1].isValue()) {
        return BVULE_CONST.encodePositive(newOperands, newIndex);
      }

      return BVULE_VAR.encodePositive(newOperands, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVUGT.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x < c} or {@code x < y} (unsigned).
   */
  BVULT {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      final Operand[] newOperands = new Operand[] {
          operands[0].isVariable() ? operands[0] : operands[1],
          operands[0].isVariable() ? operands[1] : operands[0]
      };

      if (newOperands[1].isValue()) {
        return BVULT_CONST.encodePositive(newOperands, newIndex);
      }

      return BVULT_VAR.encodePositive(newOperands, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVUGE.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x >= c} or {@code x >= y} (unsigned).
   */
  BVUGE {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      final Operand[] newOperands = new Operand[] {
          operands[0].isVariable() ? operands[0] : operands[1],
          operands[0].isVariable() ? operands[1] : operands[0]
      };

      if (newOperands[1].isValue()) {
        return BVUGE_CONST.encodePositive(newOperands, newIndex);
      }

      return BVUGE_VAR.encodePositive(newOperands, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVULT.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x > c} or {@code x > y} (unsigned).
   */
  BVUGT {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      final Operand[] newOperands = new Operand[] {
          operands[0].isVariable() ? operands[0] : operands[1],
          operands[0].isVariable() ? operands[1] : operands[0]
      };

      if (newOperands[1].isValue()) {
        return BVUGT_CONST.encodePositive(newOperands, newIndex);
      }

      return BVUGT_VAR.encodePositive(newOperands, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVULE.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x <= c} (signed).
   */
  BVSLE_CONST {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      InvariantChecks.checkTrue(operands[0].isVariable());
      InvariantChecks.checkTrue(operands[1].isValue());

      final int size = operands[0].size;
      final int i = size - 1;

      // x[n-1]x[n-2]...x[0] <=(s) c[n-1]c[n-2]...c[0] ==
      // (x[n-1] == c[n-1]) & (x[n-2]...x[0] <=(u) c[n-2]...c[0]) | (~c[n-1] -> x[n-1]).
      final Collection<IntArray> clauses;

      // (x[n-2]...x[0] <=(u) c[n-2]...c[0]).
      if (size == 1) {
        clauses = new ArrayList<>(1);
      } else {
        operands[0].size--;
        clauses = BVULE_CONST.encodePositive(operands, newIndex);
        operands[0].size++;
      }

      final boolean value = operands[1].value.testBit(i);
      final int index = operands[0].getSignedIndex(i);

      // (x[n-1] == c[n-1]) & ...
      final IntArray literals1 = new IntArray(new int[] { value ? +index : -index });
      clauses.add(literals1);

      // ... | (~c[n-1] -> x[n-1]).
      if (!value) {
        clauses.stream().forEach(clause -> clause.add(index));
      }

      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSGT_CONST.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x < c} (signed).
   */
  BVSLT_CONST {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      // Encode x <= c.
      final Collection<IntArray> clauses = BVSLE_CONST.encodePositive(operands, newIndex);
      // Encode x != c.
      clauses.addAll(NOTEQ_CONST.encodePositive(operands, newIndex));
      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSGE_CONST.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x >= c} (signed).
   */
  BVSGE_CONST {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      InvariantChecks.checkTrue(operands[0].isVariable());
      InvariantChecks.checkTrue(operands[1].isValue());

      final int size = operands[0].size;
      final int i = size - 1;

      // x[n-1]x[n-2]...x[0] >=(s) c[n-1]c[n-2]...c[0] ==
      // (x[n-1] == c[n-1]) & (x[n-2]...x[0] >=(u) c[n-2]...c[0]) | (c[n-1] -> ~x[n-1]).
      final Collection<IntArray> clauses;

      // (x[n-2]...x[0] <=(u) c[n-2]...c[0]).
      if (size == 1) {
        clauses = new ArrayList<>(1);
      } else {
        operands[0].size--;
        clauses = BVUGE_CONST.encodePositive(operands, newIndex);
        operands[0].size++;
      }

      final boolean value = operands[1].value.testBit(i);
      final int index = operands[0].getSignedIndex(i);

      // (x[n-1] == c[n-1]) & ...
      final IntArray literals1 = new IntArray(new int[] { value ? +index : -index });
      clauses.add(literals1);

      // ... | (!c[n-1] -> ~x[n-1]).
      if (value) {
        clauses.stream().forEach(clause -> clause.add(-index));
      }

      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSLT_CONST.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x > c} (signed).
   */
  BVSGT_CONST {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      // Encode x >= c.
      final Collection<IntArray> clauses = BVSGE_CONST.encodePositive(operands, newIndex);
      // Encode x != c.
      clauses.addAll(NOTEQ_CONST.encodePositive(operands, newIndex));
      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSLE_CONST.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x <= y} (signed).
   */
  BVSLE_VAR {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      return encodeSignedLessThan(operands, newIndex, false);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSGT_VAR.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x < y} (signed).
   */
  BVSLT_VAR {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      return encodeSignedLessThan(operands, newIndex, true);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSGE_VAR.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x >= y} (signed).
   */
  BVSGE_VAR {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSLE_VAR.encodePositive(new Operand[] { operands[1], operands[0] }, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSLT_VAR.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x > y} (signed).
   */
  BVSGT_VAR {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSLT_VAR.encodePositive(new Operand[] { operands[1], operands[0] }, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSLE_VAR.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x <= c} or {@code x <= y} (signed).
   */
  BVSLE {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      final Operand[] newOperands = new Operand[] {
          operands[0].isVariable() ? operands[0] : operands[1],
          operands[0].isVariable() ? operands[1] : operands[0]
      };

      if (newOperands[1].isValue()) {
        return BVSLE_CONST.encodePositive(newOperands, newIndex);
      }

      return BVSLE_VAR.encodePositive(newOperands, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSGT.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x < c} or {@code x < y} (signed).
   */
  BVSLT {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      final Operand[] newOperands = new Operand[] {
          operands[0].isVariable() ? operands[0] : operands[1],
          operands[0].isVariable() ? operands[1] : operands[0]
      };

      if (newOperands[1].isValue()) {
        return BVSLT_CONST.encodePositive(newOperands, newIndex);
      }

      return BVSLT_VAR.encodePositive(newOperands, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSGE.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x >= c} or {@code x >= y} (signed).
   */
  BVSGE {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      final Operand[] newOperands = new Operand[] {
          operands[0].isVariable() ? operands[0] : operands[1],
          operands[0].isVariable() ? operands[1] : operands[0]
      };

      if (newOperands[1].isValue()) {
        return BVSGE_CONST.encodePositive(newOperands, newIndex);
      }

      return BVSGE_VAR.encodePositive(newOperands, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSLT.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x > c} or {@code x > y} (signed).
   */
  BVSGT {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      final Operand[] newOperands = new Operand[] {
          operands[0].isVariable() ? operands[0] : operands[1],
          operands[0].isVariable() ? operands[1] : operands[0]
      };

      if (newOperands[1].isValue()) {
        return BVSGT_CONST.encodePositive(newOperands, newIndex);
      }

      return BVSGT_VAR.encodePositive(newOperands, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSLE.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code u == x & y}.
   */
  BVAND {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      InvariantChecks.checkTrue(operands[0].isVariable());
      InvariantChecks.checkTrue(operands[1].isVariable());

      final int size = operands[0].size;

      // Generate 3*n clauses AND[i]{u[i] <=> ([~]x[i] & [~]y[i])} ==
      // AND[i]{(~[~]x[i] | ~[~]y[i] | u[i]) & ([~]x[i] | ~u[i]) & ([~]y[i] | ~u[i])}.
      final Collection<IntArray> clauses = new ArrayList<>(3 * size);

      for (int i = 0; i < size; i++) {
        final int uIndex = operands[0].getSignedIndex(i);
        final int xIndex = operands[1].getSignedIndex(i);
        final int yIndex = operands[2].getSignedIndex(i);

        final IntArray literals1 = new IntArray(new int[] { -xIndex, -yIndex, +uIndex });
        clauses.add(literals1);

        final IntArray literals2 = new IntArray(new int[] { +xIndex, -uIndex });
        clauses.add(literals2);

        final IntArray literals3 = new IntArray(new int[] { +yIndex, -uIndex });
        clauses.add(literals3);
      }

      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      throw new UnsupportedOperationException();
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code u == x | y}.
   */
  BVOR {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      InvariantChecks.checkTrue(operands[0].isVariable());
      InvariantChecks.checkTrue(operands[1].isVariable());

      final int size = operands[0].size;

      // Generate 3*n clauses AND[i]{u[i] <=> ([~]x[i] | [~]y[i])} ==
      // AND[i]{([~]x[i] | [~]y[i] | ~u[i]) & (~[~]x[i] | u[i]) & (~[~]y[i] | u[i])}.
      final Collection<IntArray> clauses = new ArrayList<>(3 * size);

      for (int i = 0; i < size; i++) {
        final int uIndex = operands[0].getSignedIndex(i);
        final int xIndex = operands[1].getSignedIndex(i);
        final int yIndex = operands[2].getSignedIndex(i);

        final IntArray literals1 = new IntArray(new int[] { +xIndex, +yIndex, -uIndex });
        clauses.add(literals1);

        final IntArray literals2 = new IntArray(new int[] { -xIndex, +uIndex });
        clauses.add(literals2);

        final IntArray literals3 = new IntArray(new int[] { -yIndex, +uIndex });
        clauses.add(literals3);
      }

      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      throw new UnsupportedOperationException();
    }
  };

  public static final class Operand {
    public int index;
    public boolean sign;
    public int size;
    public BigInteger value;

    public Operand(final BigInteger value) {
      this(0, false, -1, value);
    }

    public Operand(final int index, final boolean sign, final int size) {
      this(index, sign, size, null);
    }

    public Operand(final int index, final int size) {
      this(index, true, size);
    }

    private Operand(final int index, final boolean sign, final int size, final BigInteger value) {
      this.index = index;
      this.sign = sign;
      this.size = size;
      this.value = value;
    }

    public boolean isValue() {
      return value != null;
    }

    public boolean isVariable() {
      return value == null;
    }

    public int getSignedIndex() {
      return sign ? +index : -index;
    }

    public int getSignedIndex(final int i) {
      return sign ? +(index + i) : -(index + i);
    }
  }

  /**
   * Encodes the constraint of the form {@code f <=> C}.
   *
   * @param flagIndex the flag index (@{code f}).
   * @param positiveClauses the positive clauses (@{code C}).
   * @param negativeClauses the negative clauses (@{code ~C}).
   * @return the CNF.
   */
  public static Collection<IntArray> linkToFlag(
      final int flagIndex,
      final Collection<IntArray> positiveClauses,
      final Collection<IntArray> negativeClauses) {
    // Transformation: (f <=> C) == (C | ~f) & (~C | f) ==
    // (encode-positive(C) | ~f) & (encode-negative(C) | f).
    for (final IntArray clause : positiveClauses) {
      clause.add(-flagIndex);
    }

    for (final IntArray clause : negativeClauses) {
      clause.add(+flagIndex);
    }

    final int size = positiveClauses.size() + negativeClauses.size();
    final Collection<IntArray> clauses = new ArrayList<>(size);

    clauses.addAll(positiveClauses);
    clauses.addAll(negativeClauses);

    return clauses;
  }

  /**
   * Encodes the constraint of the form {@code f <=> C}.
   *
   * @param flagIndex the flag index (@{code f}).
   * @param positiveClauses the positive clauses (@{code C}).
   * @param negativeClauses the negative clauses (@{code ~C}).
   * @param negation the negation flag.
   * @return the CNF.
   */
  public static Collection<IntArray> linkToFlag(
      final int flagIndex,
      final Collection<IntArray> positiveClauses,
      final Collection<IntArray> negativeClauses,
      final boolean negation) {
    return linkToFlag(
        flagIndex,
        negation ? negativeClauses : positiveClauses,
        negation ? positiveClauses : negativeClauses);
  }

  /**
   * Encodes a word-level constraint to the bit-level CNF.
   *
   * @param operands the operands of the constraint.
   * @param newIndex the supplier of a new boolean variable index.
   * @return the CNF.
   */
  public abstract Collection<IntArray> encodePositive(Operand[] operands, IntSupplier newIndex);

  /**
   * Encodes the negation of a word-level constraint to the bit-level CNF.
   *
   * @param operands the operands of the constraint.
   * @param newIndex the supplier of a new boolean variable index.
   * @return the CNF.
   */
  public abstract Collection<IntArray> encodeNegative(Operand[] operands, IntSupplier newIndex);

  /**
   * Encodes a word-level constraint to the bit-level CNF.
   *
   * @param operands the operands of the constraint.
   * @param newIndex the supplier of a new boolean variable index.
   * @return the CNF.
   */
  public final Collection<IntArray> encode(final Operand[] operands, final IntSupplier newIndex) {
    return encodePositive(operands, newIndex);
  }

  /**
   * Encodes a word-level constraint to the bit-level CNF.
   *
   * @param operands the operands of the constraint.
   * @param newIndex the supplier of a new boolean variable index.
   * @param negation the negation flag.
   * @return the CNF.
   */
  public final Collection<IntArray> encode(
      final Operand[] operands,
      final IntSupplier newIndex,
      final boolean negation) {
    return negation ? encodeNegative(operands, newIndex) : encodePositive(operands, newIndex);
  }

  /**
   * Encodes a word-level constraint linked to a flag, i.e. {@code f <=> C}.
   *
   * <p>If the flag is zero, encodes the constraints w/o linking it to the flag, i.e. {@code C}.</p>
   *
   * @param operands the operands of the constraint.
   * @param flagIndex the flag index (if zero, .
   * @param newIndex the supplier of a new boolean variable index.
   * @param negation the negation flag.
   * @return the CNF.
   */
  public final Collection<IntArray> encode(
      final Operand[] operands,
      final int flagIndex,
      final IntSupplier newIndex,
      final boolean negation) {
    if (flagIndex == 0) {
      return negation ? encodeNegative(operands, newIndex) : encodePositive(operands, newIndex);
    }

    final Collection<IntArray> positiveClauses = encodePositive(operands, newIndex);
    final Collection<IntArray> negativeClauses = encodeNegative(operands, newIndex);

    return linkToFlag(flagIndex, positiveClauses, negativeClauses, negation);
  }

  /**
   * Encodes a word-level constraint linked to a flag, i.e. {@code f <=> C}.
   *
   * <p>If the flag is zero, encodes the constraints w/o linking it to the flag, i.e. {@code C}.</p>
   *
   * @param operands the operands of the constraint.
   * @param flagIndex the flag index (if zero, .
   * @param newIndex the supplier of a new boolean variable index.
   * @return the CNF.
   */
  public final Collection<IntArray> encode(
      final Operand[] operands,
      final int flagIndex,
      final IntSupplier newIndex) {
    return encode(operands, flagIndex, newIndex, false);
  }

  /**
   * Encodes a word-level constraint of the form {@code x <= y} or {@code x < y} (unsigned).
   *
   * @param operands the operands of the constraint.
   * @param newIndex the supplier of a new boolean variable index.
   * @param strict the flag indicating whether the inequality is strict or not.
   * @return the CNF.
   */
  private static Collection<IntArray> encodeUnsignedLessThan(
      final Operand[] operands, final IntSupplier newIndex, final boolean strict) {
    InvariantChecks.checkTrue(operands[0].isVariable());
    InvariantChecks.checkTrue(operands[1].isVariable());

    final int size = operands[0].size;

    // Generate 5*(n-1)+3 or 5*(n-1)+1 clauses (see below).
    final Collection<IntArray> clauses = new ArrayList<>(5 * size - 2);

    if (size == 1) {
      final int xIndex = operands[0].getSignedIndex();
      final int yIndex = operands[1].getSignedIndex();

      // (x[0] <= y[0]) == (~x[0] | y[0]).
      final IntArray literals1 = new IntArray(new int[] { -xIndex, +yIndex });
      clauses.add(literals1);

      if (strict) {
        // (x[0] != y[0]) == (x[0] | y[0]) & (~x[0] | ~y[0]).
        final IntArray literals2 = new IntArray(new int[] { +xIndex, +yIndex });
        clauses.add(literals2);

        final IntArray literals3 = new IntArray(new int[] { -xIndex, -yIndex });
        clauses.add(literals3);
      }

      return clauses;
    }

    // x[n-1]x[x-2]...x[0] <= y[n-1]y[x-2]...y[0] ==
    // (~x[i] & y[i]) | (x[i] == y[i]) & encode(x[i-1]...x[0], y[i-1]...y[0]) ==
    // (u[i] == (~x[i] & y[i])) & (u[i] | x[i] == y[i]) & (u[i] | encode(...)).
    final int i = size - 1;
    final int uIndex = newIndex.getAsInt();

    // Handle the upper bits.
    operands[0].size = 1;
    operands[1].size = 1;
    operands[0].index += i;
    operands[1].index += i;

    // u[i] == (~x[i] & y[i]).
    operands[0].sign ^= true;
    final Collection<IntArray> clauses1 = BVAND.encode(
        new Operand[] { new Operand(uIndex, 1), operands[0], operands[1] }, newIndex);
    operands[0].sign ^= true;

    // u[i] | (x[i] == y[i])
    final Collection<IntArray> clauses2 = EQ_VAR.encode(operands, newIndex);
    for (final IntArray clause : clauses2) {
      clause.add(uIndex);
    }

    operands[0].index -= i;
    operands[1].index -= i;

    // u[i] | encode(x[i-1]...x[0], y[i-1]...y[0])
    operands[0].size = i;
    operands[1].size = i;
    final Collection<IntArray> clauses3 = encodeUnsignedLessThan(operands, newIndex, strict);
    for (final IntArray clause : clauses3) {
      clause.add(uIndex);
    }
    operands[0].size = size;
    operands[1].size = size;

    clauses.addAll(clauses1);
    clauses.addAll(clauses2);
    clauses.addAll(clauses3);

    return clauses;
  }

  /**
   * Encodes a word-level constraint of the form {@code x <= y} or {@code x < y} (signed).
   *
   * @param operands the operands of the constraint.
   * @param newIndex the supplier of a new boolean variable index.
   * @param strict the flag indicating whether the inequality is strict or not.
   * @return the CNF.
   */
  private static Collection<IntArray> encodeSignedLessThan(
      final Operand[] operands, final IntSupplier newIndex, final boolean strict) {
    InvariantChecks.checkTrue(operands[0].isVariable());
    InvariantChecks.checkTrue(operands[1].isVariable());

    final int size = operands[0].size;
    final int i = size - 1;
    final int uIndex = newIndex.getAsInt();

    // x[n-1]x[n-2]...x[0] <=(s) y[n-1]y[n-2]...y[0] ==
    // (x[n-1] == y[n-1]) & (x[n-2]...x[0] <=(u) y[n-2]...y[0]) | (x[n-1] & ~y[n-1]) ==
    // (u | x[n-1] == y[n-1]) & (u | x[n-2]...x[0] <=(u) y[n-2]...y[0]) & (u == x[n-1] & ~y[n-1]).
    final Collection<IntArray> clauses;

    // x[n-2]...x[0] <=(u) y[n-2]...y[0].
    if (size == 1) {
      // true.
      clauses = new ArrayList<>(5);

      if (strict) {
        // x[0] & ~y[0].
        final int xIndex = operands[0].getSignedIndex();
        final int yIndex = operands[1].getSignedIndex();

        final IntArray literals = new IntArray(new int[] { +xIndex, -yIndex });
        clauses.add(literals);

        return clauses;
      }
    } else {
      // x[n-2]...x[0] <=(u) y[n-2]...y[0].
      operands[0].size = i;
      operands[1].size = i;
      clauses = encodeUnsignedLessThan(operands, newIndex, strict);
    }

    operands[0].size = 1;
    operands[1].size = 1;
    operands[0].index += i;
    operands[1].index += i;

    // (x[n-1] == y[n-1]) & (x[n-2]...x[0] <=(u) y[n-2]...y[0]).
    clauses.addAll(EQ_VAR.encode(operands, newIndex));
    // (u | x[n-1] == y[n-1]) & (u | x[n-2]...x[0] <=(u) y[n-2]...y[0]).
    clauses.stream().forEach(clause -> clause.add(uIndex));

    // (u | x[n-1] == y[n-1]) & (u | x[n-2]...x[0] <=(u) y[n-2]...y[0]) & (u == ~x[n-1] & y[n-1]).
    operands[1].sign ^= true;
    clauses.addAll(BVAND.encode(
        new Operand[] { new Operand(uIndex, 1), operands[0], operands[1] }, newIndex));
    operands[1].sign ^= true;

    operands[0].index -= i;
    operands[1].index -= i;
    operands[0].size = size;
    operands[1].size = size;

    return clauses;
  }
}
