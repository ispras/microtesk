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

package ru.ispras.microtesk.mips.test.branch;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.testbase.TestBaseContext;
import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestData;
import ru.ispras.testbase.knowledge.iterator.Iterator;
import ru.ispras.testbase.knowledge.iterator.SingleValueIterator;
import ru.ispras.microtesk.test.engine.branch.BranchDataGenerator;
import ru.ispras.microtesk.test.engine.branch.BranchEngine;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link MipsBranchDataGenerator} is a base class for the MIPS branch instructions' generators.
 */
public abstract class MipsBranchDataGenerator extends BranchDataGenerator {
  protected static int positiveValue() {
    return Randomizer.get().nextIntRange(1, Integer.MAX_VALUE);
  }

  protected static int nonPositiveValue() {
    return Randomizer.get().nextIntRange(Integer.MIN_VALUE, 0);
  }

  protected static int nonNegativeValue() {
    return Randomizer.get().nextIntRange(0, Integer.MAX_VALUE);
  }

  protected static Integer generateEqual(Integer rs, Integer rt) {
    if (rs == null) {
      rs = rt;
    } else if (rt == null) {
      rt = rs;
    } else {
      InvariantChecks.checkTrue(rs.equals(rt), "Incorrect values defined");
    }
    if (rs == null) {
      return Randomizer.get().nextInt();
    }
    return rs;
  }

  protected static Pair<Integer, Integer> generateDistinct(Integer rs, Integer rt) {
    if (rs == null && rt == null) {
      rs = Randomizer.get().nextInt();
      rt = distinctValue(rs);
    } else if (rs == null) {
      rs = distinctValue(rt);
    } else if (rt == null) {
      rt = distinctValue(rs);
    } else {
      InvariantChecks.checkFalse(rs.equals(rt), "Incorrect values defined");
    }

    return new Pair<>(rs, rt);
  }

  private static int distinctValue(final int x) {
    int value = x;
    do {
      value = Randomizer.get().nextInt();
    } while (value == x);
    return value;
  }

  protected static Integer getValue(final String name, final TestBaseQuery query) {
    final String op = getInstructionName(query);
    final Node node = query.getBindings().get(op + "." + name);

    InvariantChecks.checkNotNull(node, name);
    InvariantChecks.checkTrue(ExprUtils.isVariable(node) || ExprUtils.isValue(node), name);

    if (ExprUtils.isValue(node)) {
      final NodeValue value = (NodeValue) node;
      return value.getBitVector().intValue();
    }

    final NodeVariable var = (NodeVariable) node;
    if (var.getData().hasValue()) {
      return var.getData().getValue(BitVector.class).intValue();
    }

    return null;
  }

  protected static Iterator<TestData> generate(final TestBaseQuery query, final int rs) {
    final String op = getInstructionName(query);
    return generate(query, Collections.singletonMap(op + ".rs", rs));
  }

  protected static Iterator<TestData> generate(final TestBaseQuery query, final int rs, final int rt) {
    final String op  = getInstructionName(query);
    final Map<String, Integer> values = new HashMap<>();
    values.put(op + ".rs", rs);
    values.put(op + ".rt", rt);

    return generate(query, values);
  }

  private static String getInstructionName(final TestBaseQuery query) {
    return query.getContext().get(TestBaseContext.INSTRUCTION).toString();
  }

  private static Iterator<TestData> generate(final TestBaseQuery query, final Map<String, Integer> values) {
    InvariantChecks.checkNotNull(query);
    InvariantChecks.checkNotNull(values);

    final Map<String, Node> unknowns = extractUnknown(query);
    final Map<String, Object> bindings = new LinkedHashMap<>();

    for (final Map.Entry<String, Node> entry : unknowns.entrySet()) {
      final String name = entry.getKey();

      if (values.containsKey(name)) {
        final int value = values.get(name);
        final DataType type = entry.getValue().getDataType();
        final BitVector data = BitVector.valueOf(value, type.getSize());

        bindings.put(name, NodeValue.newBitVector(data));
      }
    }

    return new SingleValueIterator<>(new TestData(BranchEngine.ID, bindings));
  }
}
