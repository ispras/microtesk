/*
 * Copyright 2016-2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.tools.symexec;

import ru.ispras.microtesk.model.Immediate;
import ru.ispras.microtesk.model.IsaPrimitive;
import ru.ispras.microtesk.model.memory.Location;
import ru.ispras.microtesk.translator.nml.coverage.PathConstraintBuilder;
import ru.ispras.microtesk.translator.nml.coverage.SsaAssembler;
import ru.ispras.microtesk.translator.nml.coverage.TestBase;
import ru.ispras.microtesk.utils.NamePath;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.Nodes;
import ru.ispras.fortress.solver.constraint.Constraint;
import ru.ispras.fortress.solver.constraint.Formulas;
import ru.ispras.testbase.TestBaseContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FormulaBuilder {
  public static List<Node> buildFormulas(
    final String model,
    final List<IsaPrimitive> sequence) {
    final SsaAssembler assembler = new SsaAssembler(TestBase.get().getStorage(model));
    final List<Node> formulae = new ArrayList<>(sequence.size());

    int n = 0;
    for (final IsaPrimitive p : sequence) {
      final String prefix = String.format("op_%d", n++, p.getName());
      final String tag = String.format("%s_%s", prefix, p.getName());

      final Map<String, Object> ctx = new HashMap<>();
      final Map<String, BitVector> consts = new LinkedHashMap<>();
      buildContext(ctx, consts, p);

      for (final Map.Entry<String, BitVector> e : consts.entrySet()) {
        final String name = String.format("%s_%s", prefix, e.getKey());
        final BitVector value = e.getValue();

        final Node variable = NodeVariable.newBitVector(name, value.getBitSize());
        variable.setUserData(1);

        formulae.add(Nodes.eq(variable, NodeValue.newBitVector(value)));
      }

      final Node f = assembler.assemble(ctx, p.getName(), tag);
      formulae.add(f);
    }

    final Constraint c = new PathConstraintBuilder(formulae).build();
    return ((Formulas) c.getInnerRep()).exprs();
  }

  private static void buildContext(
      final Map<String, Object> ctx,
      final Map<String, BitVector> consts,
      final IsaPrimitive p) {
    buildContext(ctx, consts, NamePath.get(p.getName()), p);
    ctx.put(TestBaseContext.INSTRUCTION, p.getName());
  }

  private static void buildContext(
    final Map<String, Object> ctx,
    final Map<String, BitVector> consts,
    final NamePath prefix,
    final IsaPrimitive src) {
    for (final Map.Entry<String, IsaPrimitive> entry : src.getArguments().entrySet()) {
      final NamePath path = prefix.resolve(entry.getKey());
      final String key = path.toString();
      final IsaPrimitive arg = entry.getValue();

      ctx.put(key, arg.getName());
      if (arg instanceof Immediate) {
        final Location location = ((Immediate) arg).access();
        consts.put(key, BitVector.valueOf(location.getValue(), location.getBitSize()));
        // override context for immediates
        ctx.put(key, Immediate.TYPE_NAME);
      }

      buildContext(ctx, consts, path, arg);
    }
  }
}
