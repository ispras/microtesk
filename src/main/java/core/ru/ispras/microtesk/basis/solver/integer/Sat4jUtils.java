/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.utils.FortressUtils;

/**
 * {@link Sat4jUtils} contains a number of utilities to deal with SAT4J.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class Sat4jUtils {

  private Sat4jUtils() {}

  public static ISolver getSolver() {
    return SolverFactory.newDefault();
  }

  public static IVecInt createClause(final int newIndex, final int size) {
    InvariantChecks.checkGreaterThanZero(size);

    // Generate 1 clause OR[j](e[j]).
    final int[] literals = new int[size];

    for (int j = 0; j < size; j++) {
      literals[j] = newIndex + j;
    }

    return new VecInt(literals);
  }

  public static IVec<IVecInt> encodeVarEqualConst(
      final Node lhs,
      final int lhsIndex,
      final BigInteger rhs) {
    final int n = FortressUtils.getBitSize(lhs);

    // Generate n clauses (c[i] ? x[i] : ~x[i]).
    final IVecInt[] clauses = new IVecInt[n];

    for (int i = 0; i < n; i++) {
      final int xi = lhsIndex + FortressUtils.getLowerBit(lhs) + i;

      final int literals[] = new int[] { rhs.testBit(i) ? xi : -xi };
      clauses[i] = new VecInt(literals);
    }

    return new Vec<>(clauses);
  }

  public static IVec<IVecInt> encodeVarEqualConst(
      final int linkToIndex,
      final Node lhs,
      final int lhsIndex,
      final BigInteger rhs) {
    final int n = FortressUtils.getBitSize(lhs);

    // Generate n+1 clauses e[j] <=> AND[i](c[i] ? x[i] : ~x[i]) ==
    // (OR[i](c[i] ? ~x[i] : x[i]) | e[j]) & AND[i]((c[i] ? x[i] : ~x[i]) | ~e[j]).
    final IVecInt[] clauses = new IVecInt[n + 1];

    final int[] literals1 = new int[n + 1];

    for (int i = 0; i < n; i++) {
      final int xi = lhsIndex + FortressUtils.getLowerBit(lhs) + i;
      literals1[i] = rhs.testBit(i) ? -xi : xi;

      final int[] literals2 = new int[] { (rhs.testBit(i) ? xi : -xi), -linkToIndex };
      clauses[i] = new VecInt(literals2);
    }

    literals1[n] = linkToIndex;
    clauses[n] = new VecInt(literals1);

    return new Vec<>(clauses);
  }

  public static IVec<IVecInt> encodeVarNotEqualConst(
      final Node lhs,
      final int lhsIndex,
      final BigInteger rhs) {
    final int n = FortressUtils.getBitSize(lhs);

    // Generate 1 clause OR[i](c[i] ? ~x[i] : x[i]).
    final int[] literals = new int[n];

    for (int i = 0; i < n; i++) {
      final int xi = lhsIndex + FortressUtils.getLowerBit(lhs) + i;
      literals[i] = rhs.testBit(i) ? -xi : xi;
    }

    final IVecInt[] clauses = new IVecInt[] { new VecInt(literals) };
    return new Vec<>(clauses);
  }

  public static IVec<IVecInt> encodeVarNotEqualConst(
      final int linkToIndex,
      final Node lhs,
      final int lhsIndex,
      final BigInteger rhs) {
    final int n = FortressUtils.getBitSize(lhs);

    // Generate n+1 clauses e[j] <=> OR[i](c[i] ? ~x[i] : x[i]) ==
    // (OR[i](c[i] ? ~x[i] : x[i]) | ~e[j]) & AND[i]((c[i] ? x[i] : ~x[i]) | e[j]).
    final IVecInt[] clauses = new IVecInt[n + 1];

    final int[] literals1 = new int[n + 1];

    for (int i = 0; i < n; i++) {
      final int xi = lhsIndex + FortressUtils.getLowerBit(lhs) + i;

      literals1[i] = rhs.testBit(i) ? -xi : xi;

      final int[] literals2 = new int[] { (rhs.testBit(i) ? xi : -xi), linkToIndex };
      clauses[i] = new VecInt(literals2);
    }

    literals1[n] = -linkToIndex;
    clauses[n] = new VecInt(literals1);

    return new Vec<>(clauses);
  }

  public static IVec<IVecInt> encodeVarEqualVar(
      final Node lhs,
      final int lhsIndex,
      final Node rhs,
      final int rhsIndex) {
    final int n = FortressUtils.getBitSize(lhs);

    // Generate 2*n clauses (x[i] & y[i]) | (~x[i] & ~y[i]) ==
    // (~x[i] | y[i]) & (x[i] | ~y[i]).
    final IVecInt[] clauses = new IVecInt[2 * n];

    int k = 0;
    for (int i = 0; i < n; i++) {
      final int xi = lhsIndex + FortressUtils.getLowerBit(lhs) + i;
      final int yi = rhsIndex + FortressUtils.getLowerBit(rhs) + i;

      final int[] literals1 = new int[] { xi, -yi };
      clauses[k++] = new VecInt(literals1);

      final int[] literals2 = new int[] { -xi, yi };
      clauses[k++] = new VecInt(literals2);
    }

    return new Vec<>(clauses);
  }

  public static IVec<IVecInt> encodeVarEqualVar(
      final int linkToIndex,
      final Node lhs,
      final int lhsIndex,
      final Node rhs,
      final int rhsIndex,
      final int newIndex) {
    final int n = FortressUtils.getBitSize(lhs);

    // Generate 8*n+1 clauses.
    final IVecInt[] clauses = new IVecInt[8 * n + 1];

    // Generate 2*n+1 clauses e[j] <=> AND[i](x[i] & y[i] | ~x[i] & ~y[i]) ==
    // e[j] <=> AND[i]((~x[i] | y[i]) & (x[i] | ~y[i])) ==
    // e[j] <=> AND[i](u[i] & v[i]) ==
    // (OR[i](~u[i] | ~v[i]) | e[j]) & AND[i](u[i] | ~e[j]) & AND[i](v[i] | ~e[j]).
    final int[] literals11 = new int[2 * n + 1];

    int k = 0;
    for (int i = 0; i < n; i++) {
      final int ui = newIndex + i;
      final int vi = newIndex + n + i;

      literals11[2 * i]     = -ui;
      literals11[2 * i + 1] = -vi;

      final int[] literals12 = new int[] { ui, -linkToIndex };
      clauses[k++] = new VecInt(literals12);

      final int[] literals13 = new int[] { vi, -linkToIndex };
      clauses[k++] = new VecInt(literals13);
    }

    literals11[2 * n] = linkToIndex;
    clauses[k++] = new VecInt(literals11);

    // Generate 3*n clauses u[i] <=> (~x[i] | y[i]) ==
    // (~x[i] | y[i] | ~u[i]) & (x[i] | u[i]) & (~y[i] | u[i]).
    for (int i = 0; i < n; i++) {
      final int xi = lhsIndex + FortressUtils.getLowerBit(lhs) + i;
      final int yi = rhsIndex + FortressUtils.getLowerBit(rhs) + i;
      final int ui = newIndex + i;

      final int[] literals21 = new int[] { -xi, yi, -ui };
      clauses[k++] = new VecInt(literals21);

      final int[] literals22 = new int[] { xi, ui };
      clauses[k++] = new VecInt(literals22);

      final int[] literals23 = new int[] { -yi, ui };
      clauses[k++] = new VecInt(literals23);
    }

    // Generate 3*n clauses v[i] <=> (x[i] | ~y[i]) ==
    // (x[i] | ~y[i] | ~v[i]) & (~x[i] | v[i]) & (y[i] | v[i]).
    for (int i = 0; i < n; i++) {
      final int xi = lhsIndex + FortressUtils.getLowerBit(lhs) + i;
      final int yi = rhsIndex + FortressUtils.getLowerBit(rhs) + i;
      final int vi = newIndex + n + i;

      final int[] literals31 = new int[] { xi, -yi, -vi };
      clauses[k++] = new VecInt(literals31);

      final int[] literals32 = new int[] { -xi, vi };
      clauses[k++] = new VecInt(literals32);

      final int[] literals33 = new int[] { yi, vi };
      clauses[k++] = new VecInt(literals33);
    }

    return new Vec<>(clauses);
  }

  public static IVec<IVecInt> encodeVarNotEqualVar(
      final Node lhs,
      final int lhsIndex,
      final Node rhs,
      final int rhsIndex,
      final int newIndex) {
    final int n = FortressUtils.getBitSize(lhs);

    // Generate 6*n+1 clauses.
    final IVecInt[] clauses = new IVecInt[6 * n + 1];

    // Generate 1 clause OR[i](x[i] & ~y[i] | ~x[i] & y[i]) == OR[i](u[i] | v[i]).
    final int[] literals1 = new int[2 * n];

    for (int i = 0; i < n; i++) {
      final int ui = newIndex + i;
      final int vi = newIndex + n + i;

      literals1[2 * i]     = ui;
      literals1[2 * i + 1] = vi;
    }

    int k = 0;
    clauses[k++] = new VecInt(literals1);

    // Generate 3*n clauses u[i] <=> (x[i] & ~y[i]) ==
    // (~x[i] | y[i] | u[i]) & (x[i] | ~u[i]) & (~y[i] | ~u[i]).
    for (int i = 0; i < n; i++) {
      final int xi = lhsIndex + FortressUtils.getLowerBit(lhs) + i;
      final int yi = rhsIndex + FortressUtils.getLowerBit(rhs) + i;
      final int ui = newIndex + i;

      final int[] literals21 = new int[] { -xi, yi, ui };
      clauses[k++] = new VecInt(literals21);

      final int[] literals22 = new int[] { xi, -ui };
      clauses[k++] = new VecInt(literals22);

      final int[] literals23 = new int[] { -yi, -ui };
      clauses[k++] = new VecInt(literals23);
    }

    // Generate 3*n clauses v[i] <=> (~x[i] & y[i]) ==
    // (x[i] | ~y[i] | v[i]) & (~x[i] | ~v[i]) & (y[i] | ~v[i]).
    for (int i = 0; i < n; i++) {
      final int xi = lhsIndex + FortressUtils.getLowerBit(lhs) + i;
      final int yi = rhsIndex + FortressUtils.getLowerBit(rhs) + i;
      final int vi = newIndex + n + i;

      final int[] literals31 = new int[] { xi, -yi, vi };
      clauses[k++] = new VecInt(literals31);

      final int[] literals32 = new int[] { -xi, -vi };
      clauses[k++] = new VecInt(literals32);

      final int[] literals33 = new int[] { yi, -vi };
      clauses[k++] = new VecInt(literals33);
    }

    return new Vec<>(clauses);
  }

  public static IVec<IVecInt> encodeVarNotEqualVar(
      final int linkToIndex,
      final Node lhs,
      final int lhsIndex,
      final Node rhs,
      final int rhsIndex,
      final int newIndex) {
    final int n = FortressUtils.getBitSize(lhs);

    // Generate 8*n+1 clauses.
    final IVecInt[] clauses = new IVecInt[8 * n + 1];

    // Generate 2*n+1 clauses e[j] <=> OR[i](x[i] & ~y[i] | ~x[i] & y[i]) ==
    // e[j] <=> OR[i](u[i] | v[i]) ==
    // (OR[i](u[i] | v[i]) | ~e[j]) & AND[i](~u[i] | e[j]) & AND(~v[i] | e[j]).
    final int[] literals11 = new int[2 * n + 1];

    int k = 0;
    for (int i = 0; i < n; i++) {
      final int ui = newIndex + i;
      final int vi = newIndex + n + i;

      literals11[2 * i]     = ui;
      literals11[2 * i + 1] = vi;

      final int[] literals12 = new int[] { -ui, linkToIndex };
      clauses[k++] = new VecInt(literals12);

      final int[] literals13 = new int[] { -vi, linkToIndex };
      clauses[k++] = new VecInt(literals13);
    }

    literals11[2 * n] = -linkToIndex;
    clauses[k++] = new VecInt(literals11);

    // Generate 3*n clauses u[i] <=> (x[i] & ~y[i]) ==
    // (~x[i] | y[i] | u[i]) & (x[i] | ~u[i]) & (~y[i] | ~u[i]).
    for (int i = 0; i < n; i++) {
      final int xi = lhsIndex + FortressUtils.getLowerBit(lhs) + i;
      final int yi = rhsIndex + FortressUtils.getLowerBit(rhs) + i;
      final int ui = newIndex + i;

      final int[] literals21 = new int[] { -xi, yi, ui };
      clauses[k++] = new VecInt(literals21);

      final int[] literals22 = new int[] { xi, -ui };
      clauses[k++] = new VecInt(literals22);

      final int[] literals23 = new int[] { -yi, -ui };
      clauses[k++] = new VecInt(literals23);
    }

    // Generate 3*n clauses v[i] <=> (~x[i] & y[i]) ==
    // (x[i] | ~y[i] | v[i]) & (~x[i] | ~v[i]) & (y[i] | ~v[i]).
    for (int i = 0; i < n; i++) {
      final int xi = lhsIndex + FortressUtils.getLowerBit(lhs) + i;
      final int yi = rhsIndex + FortressUtils.getLowerBit(rhs) + i;
      final int vi = newIndex + n + i;

      final int[] literals31 = new int[] { xi, -yi, vi };
      clauses[k++] = new VecInt(literals31);

      final int[] literals32 = new int[] { -xi, -vi };
      clauses[k++] = new VecInt(literals32);

      final int[] literals33 = new int[] { yi, -vi };
      clauses[k++] = new VecInt(literals33);
    }

    return new Vec<>(clauses);
  }

  public static Map<Variable, BigInteger> decodeSolution(
      final IProblem problem,
      final Map<Variable, Integer> indices) {
    final Map<Variable, BigInteger> solution = new LinkedHashMap<>();

    for (final Map.Entry<Variable, Integer> entry : indices.entrySet()) {
      final Variable variable = entry.getKey();
      final int x = entry.getValue();

      BigInteger value = BigInteger.ZERO;
      for (int i = 0; i < variable.getType().getSize(); i++) {
        final int xi = x + i;

        if (problem.model(xi)) {
          value = value.setBit(i);
        }
      }

      solution.put(variable, value);
    }

    return solution;
  }
}
