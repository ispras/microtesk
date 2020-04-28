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
import java.util.Collections;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

import ru.ispras.fortress.expression.Node;
import ru.ispras.microtesk.utils.FortressUtils;

import java.math.BigInteger;

/**
 * {@link Sat4jEncoder} implements an encoder of Fortress nodes to SAT4J CNFs.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class Sat4jEncoder {

  private Sat4jEncoder() {}

  /**
   * Creates a clause of the form {@code OR[j=s..t]{x[j]} = (x[s] | ... | x[t])},
   * i.e. a clause consisting of consecutive boolean variables w/o negations.
   *
   * @param newIndex the index of the first boolean variable.
   * @param size the size of the clause.
   * @return the created clause.
   */
  public static IVecInt newClause(final int newIndex, final int size) {
    // Generate 1 clause OR[i]{x[i]}.
    final int[] literals = new int[size];

    for (int i = 0; i < size; i++) {
      literals[i] = newIndex + i;
    }

    return new VecInt(literals);
  }

  /**
   * Encodes a word-level constraint of the form {@code x == c}.
   *
   * @param lhs the variable ({@code x}).
   * @param lhsIndex the corresponding boolean variable index.
   * @param rhs the constant ({@code c}).
   * @return the CNF.
   */
  public static Collection<IVecInt> encodeVarEqualConst(
      final Node lhs,
      final int lhsIndex,
      final BigInteger rhs) {
    final int size = FortressUtils.getBitSize(lhs);

    // Generate n unit clauses (c[i] ? x[i] : ~x[i]), i.e. for all i: x[i] == c[i].
    final Collection<IVecInt> clauses = new ArrayList<>(size);

    for (int i = 0; i < size; i++) {
      final int index = lhsIndex + FortressUtils.getLowerBit(lhs) + i;
      final int[] literals = new int[] { rhs.testBit(i) ? index : -index };

      clauses.add(new VecInt(literals));
    }

    return clauses;
  }

  /**
   * Encodes a word-level constraint of the form {@code flag <=> (x == c)}.
   * @param flagIndex the flag index.
   * @param lhs the variable ({@code x}).
   * @param lhsIndex the corresponding boolean variable index.
   * @param rhs the constant ({@code c}).
   * @return the CNF.
   */
  public static Collection<IVecInt> encodeVarEqualConst(
      final int flagIndex,
      final Node lhs,
      final int lhsIndex,
      final BigInteger rhs) {
    final int size = FortressUtils.getBitSize(lhs);

    // Generate n+1 clauses (f <=> AND[i]{c[i] ? x[i] : ~x[i]}) ==
    // (OR[i]{c[i] ? ~x[i] : x[i]} | f) & AND[i]{(c[i] ? x[i] : ~x[i]) | ~f}.
    final Collection<IVecInt> clauses = new ArrayList<>(size + 1);
    final int[] literals1 = new int[size + 1];

    for (int i = 0; i < size; i++) {
      final int index = lhsIndex + FortressUtils.getLowerBit(lhs) + i;
      literals1[i] = rhs.testBit(i) ? -index : index;

      final int[] literals2 = new int[] { (rhs.testBit(i) ? index : -index), -flagIndex };
      clauses.add(new VecInt(literals2));
    }

    literals1[size] = flagIndex;
    clauses.add(new VecInt(literals1));

    return clauses;
  }

  /**
   * Encodes a word-level constraint of the form {@code x != c}.
   *
   * @param lhs the variable ({@code x}).
   * @param lhsIndex the corresponding boolean variable index.
   * @param rhs the constant ({@code c}).
   * @return the CNF.
   */
  public static Collection<IVecInt> encodeVarNotEqualConst(
      final Node lhs,
      final int lhsIndex,
      final BigInteger rhs) {
    final int size = FortressUtils.getBitSize(lhs);

    // Generate 1 clause OR[i]{c[i] ? ~x[i] : x[i]}, i.e. exists i: x[i] != c[i].
    final int[] literals = new int[size];

    for (int i = 0; i < size; i++) {
      final int index = lhsIndex + FortressUtils.getLowerBit(lhs) + i;
      literals[i] = rhs.testBit(i) ? -index : index;
    }

    return Collections.singleton(new VecInt(literals));
  }

  /**
   * Encodes a word-level constraint of the form {@code flag <=> (x != c)}.
   *
   * @param flagIndex the flag index.
   * @param lhs the variable ({@code x}).
   * @param lhsIndex the corresponding boolean variable index.
   * @param rhs the constant ({@code c}).
   * @return the CNF.
   */
  public static Collection<IVecInt> encodeVarNotEqualConst(
      final int flagIndex,
      final Node lhs,
      final int lhsIndex,
      final BigInteger rhs) {
    final int size = FortressUtils.getBitSize(lhs);

    // Generate n+1 clauses (f <=> OR[i]{c[i] ? ~x[i] : x[i]}) ==
    // (OR[i]{c[i] ? ~x[i] : x[i]} | ~f) & AND[i]{(c[i] ? x[i] : ~x[i]) | f}.
    final Collection<IVecInt> clauses = new ArrayList<>(size + 1);
    final int[] literals1 = new int[size + 1];

    for (int i = 0; i < size; i++) {
      final int index = lhsIndex + FortressUtils.getLowerBit(lhs) + i;
      literals1[i] = rhs.testBit(i) ? -index : index;

      final int[] literals2 = new int[] { (rhs.testBit(i) ? index : -index), flagIndex };
      clauses.add(new VecInt(literals2));
    }

    literals1[size] = -flagIndex;
    clauses.add(new VecInt(literals1));

    return clauses;
  }

  /**
   * Encodes a word-level constraint of the form {@code x == y}.
   *
   * @param lhs the LHS variable ({@code x}).
   * @param lhsIndex the LHS boolean variable index.
   * @param rhs the RHS variable ({@code y}).
   * @param rhsIndex the RHS boolean variable index.
   * @return the CNF.
   */
  public static Collection<IVecInt> encodeVarEqualVar(
      final Node lhs,
      final int lhsIndex,
      final Node rhs,
      final int rhsIndex) {
    final int size = FortressUtils.getBitSize(lhs);

    // Generate 2*n clauses AND[i]{(x[i] & y[i]) | (~x[i] & ~y[i])} ==
    // AND[i]{(~x[i] | y[i]) & (x[i] | ~y[i])}.
    final Collection<IVecInt> clauses = new ArrayList<>(2 * size);

    for (int i = 0; i < size; i++) {
      final int xIndex = lhsIndex + FortressUtils.getLowerBit(lhs) + i;
      final int yIndex = rhsIndex + FortressUtils.getLowerBit(rhs) + i;

      final int[] literals1 = new int[] { xIndex, -yIndex };
      clauses.add(new VecInt(literals1));

      final int[] literals2 = new int[] { -xIndex, yIndex };
      clauses.add(new VecInt(literals2));
    }

    return clauses;
  }

  /**
   * Encodes a word-level constraint of the form {@code flag <=> (x == y)}.
   *
   * @param flagIndex the flag index.
   * @param lhs the LHS variable ({@code x}).
   * @param lhsIndex the LHS boolean variable index.
   * @param rhs the RHS variable ({@code y}).
   * @param rhsIndex the RHS boolean variable index.
   * @param newIndex the starting index of new boolean variables to be introduced.
   * @return the CNF.
   */
  public static Collection<IVecInt> encodeVarEqualVar(
      final int flagIndex,
      final Node lhs,
      final int lhsIndex,
      final Node rhs,
      final int rhsIndex,
      final int newIndex) {
    final int size = FortressUtils.getBitSize(lhs);

    // Generate 8*n+1 clauses (see below).
    final Collection<IVecInt> clauses = new ArrayList<>(8 * size + 1);

    // Generate 2*n+1 clauses (f <=> AND[i]{(x[i] & y[i]) | (~x[i] & ~y[i])}) ==
    // (f <=> AND[i]{(~x[i] | y[i]) & (x[i] | ~y[i])}) ==
    // (f <=> AND[i]{u[i] & v[i]}) ==
    // (OR[i]{~u[i] | ~v[i]} | f) & AND[i]{u[i] | f} & AND[i]{v[i] | ~f}.
    final int[] literals1 = new int[2 * size + 1];

    for (int i = 0; i < size; i++) {
      final int uIndex = newIndex + i;
      final int vIndex = newIndex + size + i;

      literals1[2 * i]     = -uIndex;
      literals1[2 * i + 1] = -vIndex;

      final int[] literals2 = new int[] { uIndex, -flagIndex };
      clauses.add(new VecInt(literals2));

      final int[] literals3 = new int[] { vIndex, -flagIndex };
      clauses.add(new VecInt(literals3));
    }

    literals1[2 * size] = flagIndex;
    clauses.add(new VecInt(literals1));

    // Generate 3*n clauses AND[i]{u[i] <=> (~x[i] | y[i])} ==
    // AND[i]{(~x[i] | y[i] | ~u[i]) & (x[i] | u[i]) & (~y[i] | u[i])}.
    clauses.addAll(encodeVarEqualBitwiseOr(
        lhs, false, lhsIndex, rhs, true,  rhsIndex, newIndex
    ));

    // Generate 3*n clauses AND[i]{v[i] <=> (x[i] | ~y[i])} ==
    // AND[i]{(x[i] | ~y[i] | ~v[i]) & (~x[i] | v[i]) & (y[i] | v[i])}.
    clauses.addAll(encodeVarEqualBitwiseOr(
        lhs, true, lhsIndex, rhs, false, rhsIndex, newIndex + size
    ));

    return clauses;
  }

  /**
   * Encodes a word-level constraint of the form {@code x != y}.
   *
   * @param lhs the LHS variable ({@code x}).
   * @param lhsIndex the LHS boolean variable index.
   * @param rhs the RHS variable ({@code y}).
   * @param rhsIndex the RHS boolean variable index.
   * @param newIndex the starting index of new boolean variables to be introduced.
   * @return the CNF.
   */
  public static Collection<IVecInt> encodeVarNotEqualVar(
      final Node lhs,
      final int lhsIndex,
      final Node rhs,
      final int rhsIndex,
      final int newIndex) {
    final int size = FortressUtils.getBitSize(lhs);

    // Generate 6*n+1 clauses (see below).
    final Collection<IVecInt> clauses = new ArrayList<IVecInt>(6 * size + 1);

    // Generate 1 clause OR[i]{x[i] & ~y[i] | ~x[i] & y[i]} == OR[i]{u[i] | v[i]}.
    final int[] literals1 = new int[2 * size];

    for (int i = 0; i < size; i++) {
      final int uIndex = newIndex + i;
      final int vIndex = newIndex + size + i;

      literals1[2 * i]     = uIndex;
      literals1[2 * i + 1] = vIndex;
    }

    clauses.add(new VecInt(literals1));

    // Generate 3*n clauses AND[i]{u[i] <=> (x[i] & ~y[i])} ==
    // AND[i]{(~x[i] | y[i] | u[i]) & (x[i] | ~u[i]) & (~y[i] | ~u[i])}.
    clauses.addAll(encodeVarEqualBitwiseAnd(
        lhs, true, lhsIndex, rhs, false, rhsIndex, newIndex
    ));

    // Generate 3*n clauses AND[i]{v[i] <=> (~x[i] & y[i])} ==
    // AND[i]{(x[i] | ~y[i] | v[i]) & (~x[i] | ~v[i]) & (y[i] | ~v[i])}.
    clauses.addAll(encodeVarEqualBitwiseAnd(
        lhs, false, lhsIndex, rhs, true, rhsIndex, newIndex + size
    ));

    return clauses;
  }

  /**
   * Encodes a word-level constraint of the form {@code flag <=> (x != y)}.
   *
   * @param flagIndex the flag index.
   * @param lhs the LHS variable ({@code x}).
   * @param lhsIndex the LHS boolean variable index.
   * @param rhs the RHS variable ({@code y}).
   * @param rhsIndex the RHS boolean variable index.
   * @param newIndex the starting index of new boolean variables to be introduced.
   * @return the CNF.
   */
  public static Collection<IVecInt> encodeVarNotEqualVar(
      final int flagIndex,
      final Node lhs,
      final int lhsIndex,
      final Node rhs,
      final int rhsIndex,
      final int newIndex) {
    final int size = FortressUtils.getBitSize(lhs);

    // Generate 8*n+1 clauses.
    final Collection<IVecInt> clauses = new ArrayList<>(8 * size + 1);

    // Generate 2*n+1 clauses (f <=> OR[i]{(x[i] & ~y[i]) | (~x[i] & y[i])}) ==
    // (f <=> OR[i]{u[i] | v[i]}) ==
    // (OR[i]{u[i] | v[i]} | ~f) & AND[i]{~u[i] | f} & AND[i]{~v[i] | f}.
    final int[] literals1 = new int[2 * size + 1];

    for (int i = 0; i < size; i++) {
      final int uIndex = newIndex + i;
      final int vIndex = newIndex + size + i;

      literals1[2 * i]     = uIndex;
      literals1[2 * i + 1] = vIndex;

      final int[] literals2 = new int[] { -uIndex, flagIndex };
      clauses.add(new VecInt(literals2));

      final int[] literals3 = new int[] { -vIndex, flagIndex };
      clauses.add(new VecInt(literals3));
    }

    literals1[2 * size] = -flagIndex;
    clauses.add(new VecInt(literals1));

    // Generate 3*n clauses AND[i]{u[i] <=> (x[i] & ~y[i])} ==
    // AND[i]{(~x[i] | y[i] | u[i]) & (x[i] | ~u[i]) & (~y[i] | ~u[i])}.
    clauses.addAll(encodeVarEqualBitwiseAnd(
        lhs, true, lhsIndex, rhs, false, rhsIndex, newIndex
    ));

    // Generate 3*n clauses AND[i]{v[i] <=> (~x[i] & y[i])} ==
    // AND[i]{(x[i] | ~y[i] | v[i]) & (~x[i] | ~v[i]) & (y[i] | ~v[i])}.
    clauses.addAll(encodeVarEqualBitwiseAnd(
      lhs, false, lhsIndex, rhs, true, rhsIndex, newIndex
    ));

    return clauses;
  }

  /**
   * Encodes a word-level constraint of the form {@code u == [~]x & [~]y}.
   *
   * @param lhs the LHS variable ({@code x}).
   * @param lhsSign the LHS sign ({@code false} iff {@code x} is negated).
   * @param lhsIndex the LHS boolean variable index.
   * @param rhs the RHS variable ({@code} y).
   * @param rhsSign the RHS sign ({@code false} iff {@code y} is negated).
   * @param rhsIndex the RHS boolean variable index.
   * @param newIndex newIndex the starting index of new boolean variables to be introduced.
   * @return the CNF.
   */
  public static Collection<IVecInt> encodeVarEqualBitwiseAnd(
      final Node lhs,
      final boolean lhsSign,
      final int lhsIndex,
      final Node rhs,
      final boolean rhsSign,
      final int rhsIndex,
      final int newIndex) {
    final int size = FortressUtils.getBitSize(lhs);

    // Generate 3*n clauses AND[i]{u[i] <=> ([~]x[i] & [~]y[i])} ==
    // AND[i]{(~[~]x[i] | ~[~]y[i] | u[i]) & ([~]x[i] | ~u[i]) & ([~]y[i] | ~u[i])}.
    final Collection<IVecInt> clauses = new ArrayList<>(3 * size);

    for (int i = 0; i < size; i++) {
      final int xIndex = lhsIndex + FortressUtils.getLowerBit(lhs) + i;
      final int yIndex = rhsIndex + FortressUtils.getLowerBit(rhs) + i;
      final int uIndex = newIndex + i;

      final int xSignedIndex = lhsSign ? xIndex : -xIndex;
      final int ySignedIndex = rhsSign ? yIndex : -yIndex;

      final int[] literals1 = new int[]{-xSignedIndex, -ySignedIndex, uIndex};
      clauses.add(new VecInt(literals1));

      final int[] literals2 = new int[]{xSignedIndex, -uIndex};
      clauses.add(new VecInt(literals2));

      final int[] literals3 = new int[]{ySignedIndex, -uIndex};
      clauses.add(new VecInt(literals3));
    }

    return clauses;
  }

  /**
   * Encodes a word-level constraint of the form {@code u == [~]x | [~]y}.
   *
   * @param lhs the LHS variable ({@code x}).
   * @param lhsSign the LHS sign ({@code false} iff {@code x} is negated).
   * @param lhsIndex the LHS boolean variable index.
   * @param rhs the RHS variable ({@code} y).
   * @param rhsSign the RHS sign ({@code false} iff {@code y} is negated).
   * @param rhsIndex the RHS boolean variable index.
   * @param newIndex newIndex the starting index of new boolean variables to be introduced.
   * @return the CNF.
   */
  public static Collection<IVecInt> encodeVarEqualBitwiseOr(
      final Node lhs,
      final boolean lhsSign,
      final int lhsIndex,
      final Node rhs,
      final boolean rhsSign,
      final int rhsIndex,
      final int newIndex) {
    final int size = FortressUtils.getBitSize(lhs);

    // Generate 3*n clauses AND[i]{u[i] <=> ([~]x[i] | [~]y[i])} ==
    // AND[i]{([~]x[i] | [~]y[i] | ~u[i]) & (~[~]x[i] | u[i]) & (~[~]y[i] | u[i])}.
    final Collection<IVecInt> clauses = new ArrayList<>(3 * size);

    for (int i = 0; i < size; i++) {
      final int xIndex = lhsIndex + FortressUtils.getLowerBit(lhs) + i;
      final int yIndex = rhsIndex + FortressUtils.getLowerBit(rhs) + i;
      final int uIndex = newIndex + size + i;

      final int xSignedIndex = lhsSign ? xIndex : -xIndex;
      final int ySignedIndex = rhsSign ? yIndex : -yIndex;

      final int[] literals1 = new int[] { xSignedIndex, ySignedIndex, -uIndex };
      clauses.add(new VecInt(literals1));

      final int[] literals2 = new int[] { -xSignedIndex, uIndex };
      clauses.add(new VecInt(literals2));

      final int[] literals3 = new int[] { -ySignedIndex, uIndex };
      clauses.add(new VecInt(literals3));
    }

    return clauses;
  }
}
