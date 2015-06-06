/*
 * Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.solver.constraint.ConstraintBuilder;
import ru.ispras.fortress.solver.constraint.ConstraintKind;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeVariable;

import ru.ispras.fortress.solver.constraint.Constraint;
import ru.ispras.fortress.solver.constraint.Formulas;

import ru.ispras.fortress.transformer.NodeTransformer;
import ru.ispras.fortress.transformer.TransformerRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ru.ispras.microtesk.translator.nml.coverage.Utility.nodeIsOperation;

public final class BlockConverter {
  final Set<Block> visited;
  final List<Constraint> converted;
  final NodeTransformer xform;

  private BlockConverter() {
    this.visited = Collections.newSetFromMap(new IdentityHashMap<Block, Boolean>());
    this.converted = new ArrayList<>();
    this.xform = new NodeTransformer();

    final TransformerRule rule = new TransformerRule() {
      @Override
      public boolean isApplicable(Node node) {
        return Version.hasVersion(node);
      }

      @Override
      public Node apply(Node node) {
        return Version.bakeVersion((NodeVariable) node);
      }
    };
    this.xform.addRule(Node.Kind.VARIABLE, rule);
  }

  public static Collection<Constraint> convert(String name, Block block) {
    final BlockConverter conv = new BlockConverter();
    conv.convertRecursive(name, block);

    return conv.converted;
  }

  private void convertRecursive(String name, Block block) {
    if (!visited.contains(block)) {
      visited.add(block);
      converted.add(convertBlock(name, block, this.xform));
      for (GuardedBlock child : block.getChildren()) {
        convertRecursive(child.name, child.block);
      }
    }
  }

  private static Constraint convertBlock(String name, Block block, NodeTransformer xform) {
    final ConstraintBuilder builder =
        new ConstraintBuilder(ConstraintKind.FORMULA_BASED);
    final Formulas formulas = new Formulas();
    for (NodeOperation node : block.getStatements()) {
      if (nodeIsOperation(node, SsaOperation.THIS_CALL)) {
        formulas.add(node);
      } else if (nodeIsOperation(node, SsaOperation.CALL)) {
        formulas.add(convertCall(node, xform));
      } else {
        formulas.add(Utility.transform(node, xform));
      }
    }
    for (GuardedBlock child : block.getChildren()) {
      final Node guard = Utility.transform(child.guard, xform);
      formulas.add(new NodeOperation(SsaOperation.BLOCK, newNamed(child.name), guard));
    }

    builder.setName(name);
    builder.setInnerRep(formulas);
    builder.addVariables(formulas.getVariables());

    return builder.build();
  }

  static NodeOperation convertCall(final Node node, final NodeTransformer xform) {
    final NodeOperation call = (NodeOperation) node;
    final Node callee = call.getOperand(0);
    if (nodeIsOperation(callee, SsaOperation.CLOSURE)) {
      final Node closure = convertClosure(callee, xform);
      return new NodeOperation(SsaOperation.CALL,
                               closure,
                               call.getOperand(1));
    }
    return call;
  }

  static Node convertClosure(final Node node, final NodeTransformer xform) {
    final Closure closure = new Closure(node);
    final List<Node> arguments =
        new ArrayList<>(closure.getArguments().size() + 1);

    arguments.add(closure.getOriginRef());
    for (final Node arg : closure.getArguments()) {
      if (nodeIsOperation(arg, SsaOperation.CLOSURE)) {
        arguments.add(convertClosure(arg, xform));
      } else if (nodeIsOperation(arg, SsaOperation.ARGUMENT_LINK)) {
        arguments.add(arg);
      } else {
        arguments.add(Utility.transform(arg, xform));
      }
    }
    return new NodeOperation(SsaOperation.CLOSURE, arguments);
  }

  private static NodeVariable newNamed(String name) {
    if (name == null) {
      name = "";
    }
    return new NodeVariable(name, DataType.BOOLEAN);
  }
}

final class SsaConverter {
  final Map<String, SsaForm> ssa;
  final Map<String, Constraint> constraints;
  final Map<String, Block> blocks;
  final List<Block> terminals;
  final NodeTransformer xform;

  SsaConverter(Collection<Constraint> constraints) {
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
    final BlockBuilder builder = new BlockBuilder();
    final Formulas formulas = (Formulas) constraints.get(name).getInnerRep();

    for (Node node : formulas.exprs()) {
      final NodeOperation op = (NodeOperation) node;
      if (nodeIsOperation(op, SsaOperation.THIS_CALL)) {
        block = BlockBuilder.createSingleton(op);
      } else if (nodeIsOperation(op, SsaOperation.CALL)) {
        final NodeOperation call = BlockConverter.convertCall(op, this.xform);
        block = BlockBuilder.createSingleton(call);
      } else if (nodeIsOperation(op, SsaOperation.PHI)) {
        block = BlockBuilder.createPhi();
      } else if (!nodeIsOperation(op, SsaOperation.BLOCK)) {
        builder.add((NodeOperation) Utility.transform(op, this.xform));
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
      if (nodeIsOperation(node, SsaOperation.BLOCK)) {
        final NodeOperation op = (NodeOperation) node;
        final String name = operandName(op, 0);
        final Node guard = Utility.transform(op.getOperand(1), this.xform);
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
