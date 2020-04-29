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

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.IntSupplier;

import ru.ispras.fortress.util.InvariantChecks;

import java.math.BigInteger;

/**
 * {@link CnfEncoder} implements an encoder of Fortress nodes to SAT4J CNFs.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public enum CnfEncoder {
  /**
   * Encodes a word-level constraint of the form {@code [~]x == c}.
   */
  EQ_CONST {
    @Override
    public Collection<ArrayList<Integer>> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      InvariantChecks.checkTrue(operands[0].isVariable());
      InvariantChecks.checkTrue(operands[1].isValue());

      final int size = operands[0].size;

      // Generate n unit clauses (c[i] ? x[i] : ~x[i]), i.e. for all i: x[i] == c[i].
      final Collection<ArrayList<Integer>> clauses = new ArrayList<>(size);

      for (int i = 0; i < size; i++) {
        final int index = operands[0].sign ? +(operands[0].index + i) : -(operands[0].index + i);

        final ArrayList<Integer> literals = new ArrayList<>(1);
        literals.add(operands[1].value.testBit(i) ? +index : -index);

        clauses.add(literals);
      }

      return clauses;
    }

    @Override
    public Collection<ArrayList<Integer>> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return NOTEQ_CONST.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code [~]x == [~]y}.
   */
  EQ_VAR {
    @Override
    public Collection<ArrayList<Integer>> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      InvariantChecks.checkTrue(operands[0].isVariable());
      InvariantChecks.checkTrue(operands[1].isVariable());

      final int size = operands[0].size;

      // Generate 2*n clauses AND[i]{(x[i] & y[i]) | (~x[i] & ~y[i])} ==
      // AND[i]{(~x[i] | y[i]) & (x[i] | ~y[i])}.
      final Collection<ArrayList<Integer>> clauses = new ArrayList<>(2 * size);

      for (int i = 0; i < size; i++) {
        final int xIndex = operands[0].sign ? +(operands[0].index + i) : -(operands[0].index + i);
        final int yIndex = operands[1].sign ? +(operands[1].index + i) : -(operands[1].index + i);

        final ArrayList<Integer> literals1 = new ArrayList<>(2);
        literals1.add(+xIndex);
        literals1.add(-yIndex);
        clauses.add(literals1);

        final ArrayList<Integer> literals2 = new ArrayList<>(2);
        literals2.add(-xIndex);
        literals2.add(+yIndex);
        clauses.add(literals2);
      }

      return clauses;
    }

    @Override
    public Collection<ArrayList<Integer>> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return NOTEQ_VAR.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code [~]x == c} or {@code [~]x == [~]y}.
   */
  EQ {
    @Override
    public Collection<ArrayList<Integer>> encodePositive(
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
    public Collection<ArrayList<Integer>> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return NOTEQ.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code [~]x != c}.
   */
  NOTEQ_CONST {
    @Override
    public Collection<ArrayList<Integer>> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      InvariantChecks.checkTrue(operands[0].isVariable());
      InvariantChecks.checkTrue(operands[1].isValue());

      final int size = operands[0].size;

      // Generate 1 clause OR[i]{c[i] ? ~x[i] : x[i]}, i.e. exists i: x[i] != c[i].
      final Collection<ArrayList<Integer>> clauses = new ArrayList<>(1);

      final ArrayList<Integer> literals = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        final int index = operands[0].sign ? +(operands[0].index + i) : -(operands[0].index + i);
        literals.add(operands[1].value.testBit(i) ? -index : +index);
      }

      // Do not return an unmodifiable singleton (the clauses are subject to change).
      clauses.add(literals);
      return clauses;
    }

    @Override
    public Collection<ArrayList<Integer>> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return EQ_CONST.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code [~]x != [~]y}.
   */
  NOTEQ_VAR {
    @Override
    public Collection<ArrayList<Integer>> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      InvariantChecks.checkTrue(operands[0].isVariable());
      InvariantChecks.checkTrue(operands[1].isVariable());

      final int size = operands[0].size;

      // Generate 6*n+1 clauses (see below).
      final Collection<ArrayList<Integer>> clauses = new ArrayList<>(6 * size + 1);

      // Generate 1 clause OR[i]{[~]x[i] & ~[~]y[i] | ~[~]x[i] & [~]y[i]} == OR[i]{u[i] | v[i]}.
      final ArrayList<Integer> literals1 = newClause(2 * size, newIndex);
      clauses.add(literals1);

      final int uIndex = literals1.get(0);
      final int vIndex = uIndex + size;

      // Generate 3*n clauses AND[i]{u[i] <=> ([~]x[i] & ~[~]y[i])} ==
      // AND[i]{(~[~]x[i] | [~]y[i] | u[i]) & ([~]x[i] | ~u[i]) & (~[~]y[i] | ~u[i])}.
      operands[1].sign = !operands[1].sign;
      clauses.addAll(BVAND.encode(
          new Operand[] { new Operand(uIndex, size), operands[0], operands[1] }, newIndex
      ));
      operands[1].sign = !operands[1].sign;

      // Generate 3*n clauses AND[i]{v[i] <=> (~[~]x[i] & [~]y[i])} ==
      // AND[i]{([~]x[i] | ~[~]y[i] | v[i]) & (~[~]x[i] | ~v[i]) & ([~]y[i] | ~v[i])}.
      operands[0].sign = !operands[0].sign;
      clauses.addAll(BVAND.encode(
          new Operand[] { new Operand(vIndex, size), operands[0], operands[1] }, newIndex
      ));
      operands[0].sign = !operands[0].sign;

      return clauses;
    }

    @Override
    public Collection<ArrayList<Integer>> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return EQ_VAR.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code [~]x != c} or {@code [~]x != [~]y}.
   */
  NOTEQ {
    @Override
    public Collection<ArrayList<Integer>> encodePositive(
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
    public Collection<ArrayList<Integer>> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return EQ.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code [~]x <= c} (unsigned).
   */
  BVULE_CONST {
    @Override
    public Collection<ArrayList<Integer>> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      InvariantChecks.checkTrue(operands[0].isVariable());
      InvariantChecks.checkTrue(operands[1].isValue());

      final int size = operands[0].size;

      // Generate at most n clauses (see below).
      final Collection<ArrayList<Integer>> clauses = new ArrayList<>(size);

      // x[n-1]x[x-2]...x[0] <= c[n-1]c[x-2]...c[0].
      for (int i = size - 1; i >= 0; i--) {
        final int index = operands[0].sign ? +(operands[0].index + i) : -(operands[0].index + i);

        if (!operands[1].value.testBit(i)) {
          // ...x[i]x[i-1]...x[0] <= ...0c[i-0]...c[0].
          // Add the unit clause ~x[i].
          final ArrayList<Integer> literals = new ArrayList<>(1);
          literals.add(-index);

          clauses.add(literals);
        } else {
          // ...x[i]x[i-1]...x[0] <= ...1c[i-0]...c[0].
          // Add the clauses (~x[i] | encode(x[i-1]...x[0], c[i-1]...c[0])).
          operands[0].size = i;
          final Collection<ArrayList<Integer>> tailClauses = encodePositive(operands, newIndex);
          operands[0].size = size;

          for (final ArrayList<Integer> clause : tailClauses) {
            clause.add(-index);
          }

          clauses.addAll(tailClauses);
          break;
        }
      }

      return clauses;
    }

    @Override
    public Collection<ArrayList<Integer>> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVUGT_CONST.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code [~]x < c} (unsigned).
   */
  BVULT_CONST {
    @Override
    public Collection<ArrayList<Integer>> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      // Encode x <= c.
      final Collection<ArrayList<Integer>> clauses = BVULE_CONST.encodePositive(operands, newIndex);
      // Encode x != c.
      clauses.addAll(NOTEQ_CONST.encodePositive(operands, newIndex));
      return clauses;
    }

    @Override
    public Collection<ArrayList<Integer>> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVUGE_CONST.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code [~]x >= c} (unsigned).
   */
  BVUGE_CONST {
    @Override
    public Collection<ArrayList<Integer>> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      InvariantChecks.checkTrue(operands[0].isVariable());
      InvariantChecks.checkTrue(operands[1].isValue());

      final int size = operands[0].size;

      // Generate at most n clauses (see below).
      final Collection<ArrayList<Integer>> clauses = new ArrayList<>(size);

      // x[n-1]x[x-2]...x[0] >= c[n-1]c[x-2]...c[0].
      for (int i = size - 1; i >= 0; i--) {
        final int index = operands[0].sign ? +(operands[0].index + i) : -(operands[0].index + i);

        if (operands[1].value.testBit(i)) {
          // ...x[i]x[i-1]...x[0] >= ...1c[i-0]...c[0].
          // Add the unit clause x[i].
          final ArrayList<Integer> literals = new ArrayList<>(1);
          literals.add(index);

          clauses.add(literals);
        } else {
          // ...x[i]x[i-1]...x[0] >= ...0c[i-0]...c[0].
          // Add the clauses (x[i] | encode(x[i-1]...x[0], c[i-1]...c[0])).
          operands[0].size = i;
          final Collection<ArrayList<Integer>> tailClauses = encodePositive(operands, newIndex);
          operands[0].size = size;

          for (final ArrayList<Integer> clause : tailClauses) {
            clause.add(index);
          }

          clauses.addAll(tailClauses);
          break;
        }
      }

      return clauses;
    }

    @Override
    public Collection<ArrayList<Integer>> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVULT_CONST.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code [~]x > c} (unsigned).
   */
  BVUGT_CONST {
    @Override
    public Collection<ArrayList<Integer>> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      // Encode x >= c.
      final Collection<ArrayList<Integer>> clauses = BVUGE_CONST.encodePositive(operands, newIndex);
      // Encode x != c.
      clauses.addAll(NOTEQ_CONST.encodePositive(operands, newIndex));
      return clauses;
    }

    @Override
    public Collection<ArrayList<Integer>> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVULE_CONST.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code [~]x <= [~]y} (unsigned).
   */
  BVULE_VAR {
    @Override
    public Collection<ArrayList<Integer>> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      return encodeUnsignedLessThan(operands, newIndex, false);
    }

    @Override
    public Collection<ArrayList<Integer>> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVUGT_VAR.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code [~]x < [~]y} (unsigned).
   */
  BVULT_VAR {
    @Override
    public Collection<ArrayList<Integer>> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      return encodeUnsignedLessThan(operands, newIndex, true);
    }

    @Override
    public Collection<ArrayList<Integer>> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVUGE_VAR.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code [~]x >= [~]y} (unsigned).
   */
  BVUGE_VAR {
    @Override
    public Collection<ArrayList<Integer>> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVULE_VAR.encodePositive(new Operand[] { operands[1], operands[0] }, newIndex);
    }

    @Override
    public Collection<ArrayList<Integer>> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVULT_VAR.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code [~]x > [~]y} (unsigned).
   */
  BVUGT_VAR {
    @Override
    public Collection<ArrayList<Integer>> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVULT_VAR.encodePositive(new Operand[] { operands[1], operands[0]}, newIndex);
    }

    @Override
    public Collection<ArrayList<Integer>> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      return BVULE_VAR.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code u == [~]x & [~]y}.
   */
  BVAND {
    @Override
    public Collection<ArrayList<Integer>> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      InvariantChecks.checkTrue(operands[0].isVariable());
      InvariantChecks.checkTrue(operands[1].isVariable());

      final int size = operands[0].size;

      // Generate 3*n clauses AND[i]{u[i] <=> ([~]x[i] & [~]y[i])} ==
      // AND[i]{(~[~]x[i] | ~[~]y[i] | u[i]) & ([~]x[i] | ~u[i]) & ([~]y[i] | ~u[i])}.
      final Collection<ArrayList<Integer>> clauses = new ArrayList<>(3 * size);

      for (int i = 0; i < size; i++) {
        final int uIndex = operands[0].sign ? +(operands[0].index + i) : -(operands[0].index + i);
        final int xIndex = operands[1].sign ? +(operands[1].index + i) : -(operands[1].index + i);
        final int yIndex = operands[2].sign ? +(operands[2].index + i) : -(operands[2].index + i);

        final ArrayList<Integer> literals1 = new ArrayList<>(3);
        literals1.add(-xIndex);
        literals1.add(-yIndex);
        literals1.add(+uIndex);
        clauses.add(literals1);

        final ArrayList<Integer> literals2 = new ArrayList<>(2);
        literals2.add(+xIndex);
        literals2.add(-uIndex);
        clauses.add(literals2);

        final ArrayList<Integer> literals3 = new ArrayList<>(2);
        literals3.add(+yIndex);
        literals3.add(-uIndex);
        clauses.add(literals3);
      }

      return clauses;
    }

    @Override
    public Collection<ArrayList<Integer>> encodeNegative(
        final Operand[] operands, final IntSupplier newIndex) {
      throw new UnsupportedOperationException();
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code u == [~]x | [~]y}.
   */
  BVOR {
    @Override
    public Collection<ArrayList<Integer>> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
      InvariantChecks.checkTrue(operands[0].isVariable());
      InvariantChecks.checkTrue(operands[1].isVariable());

      final int size = operands[0].size;

      // Generate 3*n clauses AND[i]{u[i] <=> ([~]x[i] | [~]y[i])} ==
      // AND[i]{([~]x[i] | [~]y[i] | ~u[i]) & (~[~]x[i] | u[i]) & (~[~]y[i] | u[i])}.
      final Collection<ArrayList<Integer>> clauses = new ArrayList<>(3 * size);

      for (int i = 0; i < size; i++) {
        final int uIndex = operands[0].sign ? +(operands[0].index + i) : -(operands[0].index + i);
        final int xIndex = operands[1].sign ? +(operands[1].index + i) : -(operands[1].index + i);
        final int yIndex = operands[2].sign ? +(operands[2].index + i) : -(operands[2].index + i);

        final ArrayList<Integer> literals1 = new ArrayList<>(3);
        literals1.add(+xIndex);
        literals1.add(+yIndex);
        literals1.add(-uIndex);
        clauses.add(literals1);

        final ArrayList<Integer> literals2 = new ArrayList<>(2);
        literals2.add(-xIndex);
        literals2.add(+uIndex);
        clauses.add(literals2);

        final ArrayList<Integer> literals3 = new ArrayList<>(2);
        literals3.add(-yIndex);
        literals3.add(+uIndex);
        clauses.add(literals3);
      }

      return clauses;
    }

    @Override
    public Collection<ArrayList<Integer>> encodeNegative(
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
  }

  /**
   * Creates a clause of the form {@code OR[j=s..t]{x[j]} = (x[s] | ... | x[t])},
   * i.e. a clause consisting of consecutive boolean variables w/o negations.
   *
   * @param size the size of the clause.
   * @param newIndex the supplier of a new boolean variable index.
   * @return the created clause.
   */
  public static ArrayList<Integer> newClause(final int size, final IntSupplier newIndex) {
    // Generate 1 clause OR[i]{x[i]}.
    final ArrayList<Integer> literals = new ArrayList<>(size);

    for (int i = 0; i < size; i++) {
      literals.add(newIndex.getAsInt());
    }

    return literals;
  }

  /**
   * Encodes a word-level constraint to the bit-level CNF.
   *
   * @param operands the operands of the constraint.
   * @param newIndex the supplier of a new boolean variable index.
   * @return the CNF.
   */
  public abstract Collection<ArrayList<Integer>> encodePositive(
      Operand[] operands, IntSupplier newIndex);

  /**
   * Encodes the negation of a word-level constraint to the bit-level CNF.
   *
   * @param operands the operands of the constraint.
   * @param newIndex the supplier of a new boolean variable index.
   * @return the CNF.
   */
  public abstract Collection<ArrayList<Integer>> encodeNegative(
      Operand[] operands, IntSupplier newIndex);

  /**
   * Encodes a word-level constraint to the bit-level CNF.
   *
   * @param operands the operands of the constraint.
   * @param newIndex the supplier of a new boolean variable index.
   * @return the CNF.
   */
  public final Collection<ArrayList<Integer>> encode(
      final Operand[] operands, final IntSupplier newIndex) {
    return encodePositive(operands, newIndex);
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
  public final Collection<ArrayList<Integer>> encode(
      final Operand[] operands, final int flagIndex, final IntSupplier newIndex) {
    if (flagIndex == 0) {
      return encodePositive(operands, newIndex);
    }

    // Transformation: (f <=> C) == (C | ~f) & (~C | f) ==
    // (encode-positive(C) | ~f) & (encode-negative(C) | f).
    final Collection<ArrayList<Integer>> clauses1 = encodePositive(operands, newIndex);
    for (final ArrayList<Integer> clause : clauses1) {
      clause.add(-flagIndex);
    }

    final Collection<ArrayList<Integer>> clauses2 = encodeNegative(operands, newIndex);
    for (final ArrayList<Integer> clause : clauses2) {
      clause.add(+flagIndex);
    }

    final int size = clauses1.size() + clauses2.size();
    final Collection<ArrayList<Integer>> clauses = new ArrayList<>(size);

    clauses.addAll(clauses1);
    clauses.addAll(clauses2);

    return clauses;
  }

  /**
   * Encodes a word-level constraint of the form {@code [~]x <= [~]y} or {@code [~]x < [~]y}.
   *
   * @param operands the operands of the constraint.
   * @param newIndex the supplier of a new boolean variable index.
   * @param strict the flag indicating whether the inequality is strict or not.
   * @return the CNF.
   */
  public Collection<ArrayList<Integer>> encodeUnsignedLessThan(
      final Operand[] operands, final IntSupplier newIndex, final boolean strict) {
    InvariantChecks.checkTrue(operands[0].isVariable());
    InvariantChecks.checkTrue(operands[1].isVariable());

    final int size = operands[0].size;

    // Generate 5*(n-1)+3 or 5*(n-1)+1 clauses (see below).
    final Collection<ArrayList<Integer>> clauses = new ArrayList<>(5 * size - 2);

    if (size == 1) {
      final int xIndex = operands[0].sign ? +operands[0].index : -operands[0].index;
      final int yIndex = operands[1].sign ? +operands[1].index : -operands[1].index;

      // (x[0] <= y[0]) == (~x[0] | y[0]).
      final ArrayList<Integer> literals1 = new ArrayList<>(2);
      literals1.add(-xIndex);
      literals1.add(+yIndex);
      clauses.add(literals1);

      if (strict) {
        // (x[0] != y[0]) == (x[0] | y[0]) & (~x[0] | ~y[0]).
        final ArrayList<Integer> literals2 = new ArrayList<>(2);
        literals2.add(+xIndex);
        literals2.add(+yIndex);
        clauses.add(literals2);

        final ArrayList<Integer> literals3 = new ArrayList<>(2);
        literals3.add(+xIndex);
        literals3.add(+yIndex);
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
    operands[0].sign = !operands[0].sign;
    final Collection<ArrayList<Integer>> clauses1 = BVAND.encode(
        new Operand[] { new Operand(uIndex, 1), operands[0], operands[1] }, newIndex);
    operands[0].sign = !operands[0].sign;

    // u[i] | (x[i] == y[i])
    final Collection<ArrayList<Integer>> clauses2 = EQ_VAR.encode(operands, newIndex);
    for (final ArrayList<Integer> clause : clauses2) {
      clause.add(uIndex);
    }

    operands[0].index -= i;
    operands[1].index -= i;

    // u[i] | encode(x[i-1]...x[0], y[i-1]...y[0])
    operands[0].size = i;
    operands[1].size = i;
    final Collection<ArrayList<Integer>> clauses3 = encodePositive(operands, newIndex);
    for (final ArrayList<Integer> clause : clauses3) {
      clause.add(uIndex);
    }
    operands[0].size = size;
    operands[1].size = size;

    clauses.addAll(clauses1);
    clauses.addAll(clauses2);
    clauses.addAll(clauses3);

    return clauses;
  }
}
