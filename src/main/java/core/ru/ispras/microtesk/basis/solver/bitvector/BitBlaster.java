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
import java.util.Arrays;
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
      Arrays.stream(operands).forEach(operand -> operand.sign ^= true);
      final Collection<IntArray> clauses = OR.encodePositive(operands, newIndex);
      Arrays.stream(operands).forEach(operand -> operand.sign ^= true);

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

      Arrays.stream(operands).forEach(operand -> literals.add(operand.getSignedIndex()));
      clauses.add(literals);

      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      // De Morgan's law: ~(x[0] | ... | x[n-1]) == (~x[0] & ... & ~x[n-1]).
      Arrays.stream(operands).forEach(operand -> operand.sign ^= true);
      final Collection<IntArray> clauses = AND.encodePositive(operands, newIndex);
      Arrays.stream(operands).forEach(operand -> operand.sign ^= true);

      return clauses;
    }
  },

  /**
   * Encodes an exclusive disjunction of n boolean variables, i.e. {@code x[0] ^ ... ^ x[n-1]}.
   */
  XOR {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {

      if (operands.length == 2) {
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

      // (x[0] ^ ... ^ x[n-2]) ^ x[n-1] == (u == (x[0] ^ ... ^ x[n-2])) & (u ^ x[n-1]).
      final Operand[] lhs = new Operand[operands.length - 1];
      IntStream.range(0, lhs.length).forEach(i -> lhs[i] = operands[i]);

      final int uIndex = newIndex.getAsInt();
      final Collection<IntArray> clauses = XOR.encode(lhs, uIndex, newIndex);

      final Operand[] rhs = new Operand[] { new Operand(uIndex, 1), operands[operands.length - 1]};
      clauses.addAll(XOR.encodePositive(rhs, newIndex));

      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {

      // ~(x[0] ^ x[1] ^ ... ^ x[n-1]) == (~x[0] ^ x[1] ^ ... ^ x[n-1]).
      operands[0].sign ^= true;
      final Collection<IntArray> clauses = XOR.encodePositive(operands, newIndex);
      operands[0].sign ^= true;

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
   * Encodes an equivalence of 2 boolean variables, i.e. {@code x <-> y}.
   */
  EQUIV {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      // (x <-> y) == (~x | y) & (x | ~y).
      final Collection<IntArray> clauses = new ArrayList<>(2);

      final int xIndex = operands[0].getSignedIndex();
      final int yIndex = operands[1].getSignedIndex();

      final IntArray literals1 = new IntArray(new int[] { -xIndex, +yIndex });
      clauses.add(literals1);

      final IntArray literals2 = new IntArray(new int[] { +xIndex, -yIndex });
      clauses.add(literals2);

      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      // ~(x <-> y) == (~x <-> y).
      operands[0].sign ^= true;
      final Collection<IntArray> clauses = EQUIV.encodePositive(operands, newIndex);
      operands[0].sign ^= true;

      return clauses;
    }
  },

  /**
   * Encodes a voting functions of 3 boolean variables, i.e. {@code (x & y) | (x & z) | (y & z)}.
   */
  VOTE {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      // (x & y) | (x & z) | (y & z) == (x | y | z) & (x | y) & (x | z) & (y | z).
      final Collection<IntArray> clauses = new ArrayList<>(4);

      final int xIndex = operands[0].getSignedIndex();
      final int yIndex = operands[1].getSignedIndex();
      final int zIndex = operands[2].getSignedIndex();

      final IntArray literals1 = new IntArray(new int[] { xIndex, yIndex, zIndex });
      clauses.add(literals1);

      final IntArray literals2 = new IntArray(new int[] { xIndex, yIndex });
      clauses.add(literals2);

      final IntArray literals3 = new IntArray(new int[] { xIndex, zIndex });
      clauses.add(literals3);

      final IntArray literals4 = new IntArray(new int[] { yIndex, zIndex });
      clauses.add(literals4);

      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      // ~((x & y) | (x & z) | (y & z)) == (~x | ~y) & (~x | ~z) & (~y | ~z).
      final Collection<IntArray> clauses = new ArrayList<>(3);

      final int xIndex = operands[0].getSignedIndex();
      final int yIndex = operands[1].getSignedIndex();
      final int zIndex = operands[2].getSignedIndex();

      final IntArray literals1 = new IntArray(new int[] { -xIndex, -yIndex });
      clauses.add(literals1);

      final IntArray literals2 = new IntArray(new int[] { -xIndex, -zIndex });
      clauses.add(literals2);

      final IntArray literals3 = new IntArray(new int[] { -yIndex, -zIndex });
      clauses.add(literals3);

      return clauses;
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x == const}.
   */
  EQ_CONSTANT {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      InvariantChecks.checkTrue(operands[0].isVariable());
      InvariantChecks.checkTrue(operands[1].isValue());

      final int size = operands[0].size;

      // Generate n unit clauses (c[i] ? x[i] : ~x[i]), i.e. for all i: x[i] == c[i].
      final Collection<IntArray> clauses = new ArrayList<>(size);

      for (int i = 0; i < size; i++) {
        final boolean value = operands[1].value.testBit(i);
        final int index = operands[0].getSignedIndex(i);
        final IntArray literals = new IntArray(new int[] { value ? +index : -index });

        clauses.add(literals);
      }

      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return NOTEQ_CONSTANT.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x == y}.
   */
  EQ_VARIABLE {
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
      return NOTEQ_VARIABLE.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x == const} or {@code x == y}.
   */
  EQ {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      final Operand[] newOperands = reorderOperands(operands);

      return newOperands[1].isValue()
          ? EQ_CONSTANT.encodePositive(newOperands, newIndex)
          : EQ_VARIABLE.encodePositive(newOperands, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return NOTEQ.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x != const}.
   */
  NOTEQ_CONSTANT {
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
        final boolean value = operands[1].value.testBit(i);
        final int index = operands[0].getSignedIndex(i);
        literals.add(value ? -index : +index);
      }

      // The returned collection should be modifiable.
      clauses.add(literals);
      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return EQ_CONSTANT.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x != y}.
   */
  NOTEQ_VARIABLE {
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
      return EQ_VARIABLE.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x != const} or {@code x != y}.
   */
  NOTEQ {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      final Operand[] newOperands = reorderOperands(operands);

      return newOperands[1].isValue()
          ? NOTEQ_CONSTANT.encodePositive(newOperands, newIndex)
          : NOTEQ_VARIABLE.encodePositive(newOperands, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return EQ.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x <= const} (unsigned).
   */
  BVULE_CONSTANT {
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
        final boolean value = operands[1].value.testBit(i);
        final int index = operands[0].getSignedIndex(i);;

        if (!value) {
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
      return BVUGT_CONSTANT.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x < const} (unsigned).
   */
  BVULT_CONSTANT {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      // Encode x <= c.
      final Collection<IntArray> clauses = BVULE_CONSTANT.encodePositive(operands, newIndex);
      // Encode x != c.
      clauses.addAll(NOTEQ_CONSTANT.encodePositive(operands, newIndex));
      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVUGE_CONSTANT.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x >= const} (unsigned).
   */
  BVUGE_CONSTANT {
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
        final boolean value = operands[1].value.testBit(i);
        final int index = operands[0].getSignedIndex(i);

        if (value) {
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
      return BVULT_CONSTANT.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x > const} (unsigned).
   */
  BVUGT_CONSTANT {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      // Encode x >= c.
      final Collection<IntArray> clauses = BVUGE_CONSTANT.encodePositive(operands, newIndex);
      // Encode x != c.
      clauses.addAll(NOTEQ_CONSTANT.encodePositive(operands, newIndex));
      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVULE_CONSTANT.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x <= y} (unsigned).
   */
  BVULE_VARIABLE {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      return encodeUnsignedLessThan(operands, newIndex, false);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVUGT_VARIABLE.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x < y} (unsigned).
   */
  BVULT_VARIABLE {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      return encodeUnsignedLessThan(operands, newIndex, true);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVUGE_VARIABLE.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x >= y} (unsigned).
   */
  BVUGE_VARIABLE {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVULE_VARIABLE.encodePositive(new Operand[] { operands[1], operands[0] }, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVULT_VARIABLE.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x > y} (unsigned).
   */
  BVUGT_VARIABLE {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVULT_VARIABLE.encodePositive(new Operand[] { operands[1], operands[0]}, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVULE_VARIABLE.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x <= const} or {@code x <= y}.
   */
  BVULE {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      final Operand[] newOperands = reorderOperands(operands);

      return newOperands[1].isValue()
          ? BVULE_CONSTANT.encodePositive(newOperands, newIndex)
          : BVULE_VARIABLE.encodePositive(newOperands, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVUGT.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x < const} or {@code x < y} (unsigned).
   */
  BVULT {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      final Operand[] newOperands = reorderOperands(operands);

      return newOperands[1].isValue()
          ? BVULT_CONSTANT.encodePositive(newOperands, newIndex)
          : BVULT_VARIABLE.encodePositive(newOperands, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVUGE.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x >= const} or {@code x >= y} (unsigned).
   */
  BVUGE {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      final Operand[] newOperands = reorderOperands(operands);

      return newOperands[1].isValue()
          ? BVUGE_CONSTANT.encodePositive(newOperands, newIndex)
          : BVUGE_VARIABLE.encodePositive(newOperands, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVULT.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x > const} or {@code x > y} (unsigned).
   */
  BVUGT {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      final Operand[] newOperands = reorderOperands(operands);

      return newOperands[1].isValue()
          ? BVUGT_CONSTANT.encodePositive(newOperands, newIndex)
          : BVUGT_VARIABLE.encodePositive(newOperands, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVULE.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x <= const} (signed).
   */
  BVSLE_CONSTANT {
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
        operands[0].size = i;
        clauses = BVULE_CONSTANT.encodePositive(operands, newIndex);
        operands[0].size = size;
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
      return BVSGT_CONSTANT.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x < const} (signed).
   */
  BVSLT_CONSTANT {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      // Encode x <= c.
      final Collection<IntArray> clauses = BVSLE_CONSTANT.encodePositive(operands, newIndex);
      // Encode x != c.
      clauses.addAll(NOTEQ_CONSTANT.encodePositive(operands, newIndex));
      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSGE_CONSTANT.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x >= const} (signed).
   */
  BVSGE_CONSTANT {
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
        operands[0].size = i;
        clauses = BVUGE_CONSTANT.encodePositive(operands, newIndex);
        operands[0].size = size;
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
      return BVSLT_CONSTANT.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x > const} (signed).
   */
  BVSGT_CONSTANT {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      // Encode x >= c.
      final Collection<IntArray> clauses = BVSGE_CONSTANT.encodePositive(operands, newIndex);
      // Encode x != c.
      clauses.addAll(NOTEQ_CONSTANT.encodePositive(operands, newIndex));
      return clauses;
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSLE_CONSTANT.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x <= y} (signed).
   */
  BVSLE_VARIABLE {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      return encodeSignedLessThan(operands, newIndex, false);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSGT_VARIABLE.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x < y} (signed).
   */
  BVSLT_VARIABLE {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      return encodeSignedLessThan(operands, newIndex, true);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSGE_VARIABLE.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x >= y} (signed).
   */
  BVSGE_VARIABLE {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSLE_VARIABLE.encodePositive(new Operand[] { operands[1], operands[0] }, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSLT_VARIABLE.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x > y} (signed).
   */
  BVSGT_VARIABLE {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSLT_VARIABLE.encodePositive(new Operand[] { operands[1], operands[0] }, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSLE_VARIABLE.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x <= const} or {@code x <= y} (signed).
   */
  BVSLE {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      final Operand[] newOperands = reorderOperands(operands);

      return newOperands[1].isValue()
          ? BVSLE_CONSTANT.encodePositive(newOperands, newIndex)
          : BVSLE_VARIABLE.encodePositive(newOperands, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSGT.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x < const} or {@code x < y} (signed).
   */
  BVSLT {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      final Operand[] newOperands = reorderOperands(operands);

      return newOperands[1].isValue()
          ? BVSLT_CONSTANT.encodePositive(newOperands, newIndex)
          : BVSLT_VARIABLE.encodePositive(newOperands, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSGE.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x >= const} or {@code x >= y} (signed).
   */
  BVSGE {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      final Operand[] newOperands = reorderOperands(operands);

      return newOperands[1].isValue()
          ? BVSGE_CONSTANT.encodePositive(newOperands, newIndex)
          : BVSGE_VARIABLE.encodePositive(newOperands, newIndex);
    }

    @Override
    public Collection<IntArray> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVSLT.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code x > const} or {@code x > y} (signed).
   */
  BVSGT {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      final Operand[] newOperands = reorderOperands(operands);

      return newOperands[1].isValue()
          ? BVSGT_CONSTANT.encodePositive(newOperands, newIndex)
          : BVSGT_VARIABLE.encodePositive(newOperands, newIndex);
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
      InvariantChecks.checkTrue(operands[3].isVariable());

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
      InvariantChecks.checkTrue(operands[2].isVariable());

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
  },

  /**
   * Encodes a word-level constraint of the form {@code u == x + const}.
   */
  BVADD_CONSTANT {
    @Override
    public Collection<IntArray> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      InvariantChecks.checkTrue(operands[0].isVariable());
      InvariantChecks.checkTrue(operands[1].isVariable());
      InvariantChecks.checkTrue(operands[2].isValue());

      final int size = operands[0].size;

      // Generate clauses for the following boolean equations:
      // u[i]   ==  (x[i] ^ c[i]), if const[i] == 0;
      //           ~(x[i] ^ c[i]), if const[1] == 1;
      // c[i+1] ==  (x[i] & c[i]), if const[i] == 0;
      //            (x[i] | c[i]), if const[i] == 1;
      // c[0]   ==  0 (a carry bit).
      final Collection<IntArray> clauses = new ArrayList<>();

      final boolean value0 = operands[2].value.testBit(0);

      // u[0] ==  x[0], if const[0] == 0;
      //         ~x[0], if const[0] == 1.
      final Operand[] ux = new Operand[] { operands[0], operands[1] };
      clauses.addAll(EQUIV.encode(ux, newIndex, value0));

      if (size == 1) {
        return clauses;
      }

      // c[1] == 0,    if const[0] == 0;
      //         x[0], if const[0] == 1.
      final int cIndex1 = newIndex.getAsInt();
      if (!value0) {
        clauses.add(new IntArray(new int[] { -cIndex1 }));
      } else {
        final Operand[] xc = new Operand[] { operands[1], new Operand(cIndex1, 1) };
        clauses.addAll(EQUIV.encode(xc, newIndex));
      }

      int cIndex = cIndex1;
      for (int i = 1; i < size; i++) {
        final boolean value = operands[2].value.testBit(i);
        final int uIndex = operands[0].getSignedIndex(i);
        final Operand[] xc = new Operand[] { operands[1], new Operand(cIndex, 1) };

        operands[1].index += i;
        operands[2].index += i;

        // u[i]   ==  (x[i] ^ c[i]), if const[i] == 0;
        //           ~(x[i] ^ c[i]), if const[1] == 1.
        clauses.addAll(XOR.encode(xc, uIndex, newIndex, value));

        // c[i+1] ==  (x[i] & c[i]), if const[i] == 0;
        //            (x[i] | c[i]), if const[i] == 1.
        if (i != size - 1) {
          cIndex = newIndex.getAsInt();
          clauses.addAll(
              value ? OR.encode(xc, cIndex, newIndex) : AND.encode(xc, cIndex, newIndex)
          );
        }

        operands[1].index -= i;
        operands[2].index -= i;
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
   * Encodes a word-level constraint of the form {@code u == x + y}.
   */
  BVADD_VARIABLE {
    @Override
    public Collection<IntArray> encodePositive(
    final Operand[] operands, final IntSupplier newIndex) {
      InvariantChecks.checkTrue(operands[0].isVariable());
      InvariantChecks.checkTrue(operands[1].isVariable());
      InvariantChecks.checkTrue(operands[2].isVariable());

      final int size = operands[0].size;

      // Generate clauses for the following boolean equations:
      // u[i]   == (x[i] ^ y[i] ^ c[i]);
      // c[i+1] == (x[i] & y[i]) | (x[i] & c[i]) | (y[i] & c[i]);
      // c[0]   == 0 (a carry bit).
      final Collection<IntArray> clauses = new ArrayList<>();
      final Operand[] xy = new Operand[] { operands[1], operands[2] };

      // u[0] == x[0] ^ y[0].
      final int uIndex0 = operands[0].getSignedIndex();
      clauses.addAll(XOR.encode(xy, uIndex0, newIndex));

      if (size == 1) {
        return clauses;
      }

      // c[1] == x[0] & y[0].
      final int cIndex1 = newIndex.getAsInt();
      clauses.addAll(AND.encode(xy, cIndex1, newIndex));

      int cIndex = cIndex1;
      for (int i = 1; i < size; i++) {
        final int uIndex = operands[0].getSignedIndex(i);
        final Operand[] xyc = new Operand[] { operands[1], operands[2], new Operand(cIndex, 1) };

        operands[1].index += i;
        operands[2].index += i;

        // u[i] == (x[i] ^ y[i] ^ c[i]).
        clauses.addAll(XOR.encode(xyc, uIndex, newIndex));

        if (i != size - 1) {
          // c[i+1] == (x[i] & y[i]) | (x[i] & c[i]) | (y[i] & c[i]).
          cIndex = newIndex.getAsInt();
          clauses.addAll(VOTE.encode(xyc, cIndex, newIndex));
        }

        operands[1].index -= i;
        operands[2].index -= i;
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
   * Encodes a word-level constraint of the form {@code u == x + const} or {@code u == x + y}.
   */
  BVADD {
    @Override
    public Collection<IntArray> encodePositive(
    final Operand[] operands, final IntSupplier newIndex) {
      final Operand[] newOperands = reorderOperands(operands);

      return newOperands[1].isValue()
          ? BVADD_CONSTANT.encodePositive(newOperands, newIndex)
          : BVADD_VARIABLE.encodePositive(newOperands, newIndex);
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
    positiveClauses.stream().forEach(clause -> clause.add(-flagIndex));
    negativeClauses.stream().forEach(clause -> clause.add(+flagIndex));

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
   * Reorders two operands to put a variable into the first place.
   *
   * @param operands the operands to be reordered.
   * @return the reordered operands.
   */
  private static Operand[] reorderOperands(final Operand[] operands) {
    return new Operand[] {
        operands[0].isVariable() ? operands[0] : operands[1],
        operands[0].isVariable() ? operands[1] : operands[0]
    };
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
    final Collection<IntArray> clauses2 = EQ_VARIABLE.encode(operands, newIndex);
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
    clauses.addAll(EQ_VARIABLE.encode(operands, newIndex));
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
