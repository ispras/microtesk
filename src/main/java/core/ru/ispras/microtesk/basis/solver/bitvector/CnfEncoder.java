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
      return NEQ_CONST.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code [~]x != c}.
   */
  NEQ_CONST {
    @Override
    public Collection<ArrayList<Integer>> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
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
   * Encodes a word-level constraint of the form {@code [~]x == [~]y}.
   */
  EQ {
    @Override
    public Collection<ArrayList<Integer>> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
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
      return NEQ.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code [~]x != [~]y}.
   */
  NEQ {
    @Override
    public Collection<ArrayList<Integer>> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
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
      return EQ.encodePositive(operands, newIndex);
    }
  },

  /**
   * Encodes a word-level constraint of the form {@code u == [~]x & [~]y}.
   */
  BVAND {
    @Override
    public Collection<ArrayList<Integer>> encodePositive(
        final Operand[] operands, final IntSupplier newIndex) {
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
   * @param operands the operands of the constraint.
   * @param flagIndex the flag index.
   * @param newIndex the supplier of a new boolean variable index.
   * @return the CNF.
   */
  public final Collection<ArrayList<Integer>> encode(
      final Operand[] operands, final int flagIndex, final IntSupplier newIndex) {
    InvariantChecks.checkTrue(flagIndex != 0);

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
}
