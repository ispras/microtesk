/*
 * Copyright 2014-2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.coverage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.solver.constraint.Constraint;
import ru.ispras.fortress.solver.constraint.Formulas;
import ru.ispras.fortress.transformer.NodeTransformer;
import ru.ispras.fortress.transformer.Transformer;
import ru.ispras.fortress.transformer.TransformerRule;

final class SsaConverter {
  private final Map<String, SsaForm> ssa;
  private final Map<String, Constraint> constraints;
  private final Map<String, Block> blocks;
  private final List<Block> terminals;
  private final NodeTransformer xform;

  public SsaConverter(final Collection<Constraint> constraints) {
    this.ssa = new HashMap<>();
    this.constraints = new HashMap<>();
    this.blocks = new HashMap<>();
    this.terminals = new ArrayList<>();
    this.xform = new NodeTransformer();

    for (Constraint c : constraints) {
      this.constraints.put(c.getName(), c);
    }

    final TransformerRule rule = new TransformerRule() {
      @Override
      public boolean isApplicable(Node node) {
        return Version.hasBakedVersion(node);
      }

      @Override
      public Node apply(Node node) {
        return Version.undoVersion((NodeVariable) node);
      }
    };
    this.xform.addRule(Node.Kind.VARIABLE, rule);
  }

  public SsaForm convert(String name) {
    if (ssa.containsKey(name)) {
      return ssa.get(name);
    }
    final Block entry = restoreBlock(name);
    final SsaForm ssaForm = new SsaForm(entry, terminals.get(0), blocks.values());

    blocks.clear();
    terminals.clear();
    ssa.put(name, ssaForm);

    return ssaForm;
  }

  private Block restoreBlock(String name) {
    if (blocks.containsKey(name)) {
      return blocks.get(name);
    }
    Block block = null;
    final Block.Builder builder = new Block.Builder();
    final Formulas formulas = (Formulas) constraints.get(name).getInnerRep();

    for (Node node : formulas.exprs()) {
      final NodeOperation op = (NodeOperation) node;
      if (ExprUtils.isOperation(op, SsaOperation.THIS_CALL)) {
        block = Block.newSingleton(op);
      } else if (ExprUtils.isOperation(op, SsaOperation.CALL)) {
        final NodeOperation call = BlockConverter.convertCall(op, this.xform);
        block = Block.newSingleton(call);
      } else if (ExprUtils.isOperation(op, SsaOperation.PHI)) {
        block = Block.newPhi();
      } else if (!ExprUtils.isOperation(op, SsaOperation.BLOCK)) {
        builder.add((NodeOperation) Transformer.transform(op, this.xform));
      }
    }
    if (block == null) {
      block = builder.build();
    }
    blocks.put(name, block);

    final List<GuardedBlock> children = restoreChildren(formulas);
    if (!children.isEmpty()) {
      block.setChildren(children);
    } else {
      terminals.add(block);
    }
    return block;
  }

  private List<GuardedBlock> restoreChildren(Formulas formulas) {
    final List<GuardedBlock> children = new ArrayList<>();
    for (Node node : formulas.exprs()) {
      if (ExprUtils.isOperation(node, SsaOperation.BLOCK)) {
        final NodeOperation op = (NodeOperation) node;
        final String name = operandName(op, 0);
        final Node guard = Transformer.transform(op.getOperand(1), this.xform);
        final GuardedBlock guarded =
            new GuardedBlock(name, guard, restoreBlock(name));
        children.add(guarded);
      }
    }
    return children;
  }

  private static String operandName(NodeOperation node, int i) {
    return ((NodeVariable) node.getOperand(i)).getName();
  }
}
