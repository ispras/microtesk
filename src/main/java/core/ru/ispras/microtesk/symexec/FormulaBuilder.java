/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.symexec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.solver.constraint.Constraint;
import ru.ispras.fortress.solver.constraint.Formulas;
import ru.ispras.microtesk.model.api.Immediate;
import ru.ispras.microtesk.model.api.IsaPrimitive;
import ru.ispras.microtesk.translator.nml.coverage.PathConstraintBuilder;
import ru.ispras.microtesk.translator.nml.coverage.SsaAssembler;
import ru.ispras.microtesk.translator.nml.coverage.TestBase;
import ru.ispras.testbase.TestBaseContext;

public final class FormulaBuilder {
  public static List<Node> buildFormulas(
    final String model,
    final List<IsaPrimitive> sequence) {
    final SsaAssembler assembler =
      new SsaAssembler(TestBase.get().getStorage(model));

    final List<Node> formulae = new ArrayList<>(sequence.size());
    int n = 0;
    for (final IsaPrimitive p : sequence) {
      final String tag = String.format("%s:%d", p.getName(), n++);
      final Node f = assembler.assemble(buildContext(p), p.getName(), tag);
      formulae.add(f);
    }
    final Constraint c = new PathConstraintBuilder(formulae).build();
    return ((Formulas) c.getInnerRep()).exprs();
  }

  private static Map<String, String> buildContext(final IsaPrimitive p) {
    final Map<String, String> ctx = new HashMap<>();
    buildContext(ctx, p.getName() + ".", p);
    ctx.put(TestBaseContext.INSTRUCTION, p.getName());
    return ctx;
  }

  private static void buildContext(
    final Map<String, String> ctx,
    final String prefix,
    final IsaPrimitive src) {
    for (final Map.Entry<String, IsaPrimitive> entry : src.getArguments().entrySet()) {
      final String key = prefix + entry.getKey();
      final IsaPrimitive arg = entry.getValue();

      ctx.put(key, arg.getName());
      buildContext(ctx, key + ".", arg);
    }
  }
}
