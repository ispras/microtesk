/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.solver.constraint.Constraint;
import ru.ispras.fortress.solver.constraint.ConstraintBuilder;
import ru.ispras.fortress.solver.constraint.Formulas;
import ru.ispras.fortress.transformer.NodeTransformer;
import ru.ispras.fortress.transformer.TransformerRule;
import ru.ispras.fortress.util.InvariantChecks;

public final class PathConstraintBuilder {
  final Map<String, NodeVariable> variables = new HashMap<>();
  final ConstraintBuilder builder = new ConstraintBuilder();
  final List<Node> ssa;
  final List<NodeVariable> specialMarks = new ArrayList<>();
  final Node conditionExpr;

  public PathConstraintBuilder(final Node node) {
    this(ExprUtils.isOperation(node, StandardOperation.AND)
      ? ((NodeOperation) node).getOperands()
      : Collections.singleton(node));
  }

  public PathConstraintBuilder(final Collection<? extends Node> formulas) {
    InvariantChecks.checkNotNull(formulas);
    this.ssa = Utility.transform(formulas, setUpTransformer());
    for (NodeVariable node : variables.values()) {
      this.builder.addVariable(node.getName(), node.getData());
    }
    this.conditionExpr = PathFilter.filter(ExprUtils.AND(this.ssa));
  }

  public Map<String, NodeVariable> getVariables() {
    return this.variables;
  }

  public ConstraintBuilder getConstraintBuilder() {
    return this.builder;
  }

  public Paths getPaths() {
    return new Paths(this, Collections.singletonList(conditionExpr));
  }

  public List<NodeVariable> getSpecialMarks() {
    return Collections.unmodifiableList(specialMarks);
  }

  public Constraint build() {
    return build(Collections.<Node>emptyList());
  }

  public Constraint build(Node condition) {
    return build(Collections.singleton(condition));
  }

  public Constraint build(Collection<? extends Node> conditions) {
    final Formulas formulas = new Formulas();
    formulas.addAll(this.ssa);
    formulas.addAll(conditions);

    this.builder.setInnerRep(formulas);
    return this.builder.build();
  }

  private NodeTransformer setUpTransformer() {
    final Map<Enum<?>, TransformerRule> rules = IntegerCast.rules();

    final TransformerRule bake = new TransformerRule() {
      @Override
      public boolean isApplicable(Node node) {
        return node.getKind() == Node.Kind.VARIABLE;
      }

      @Override
      public Node apply(Node node) {
        final NodeVariable var = (NodeVariable) node;
        final String name =
            String.format("%s!%d", var.getName(), var.getUserData());
        if (variables.containsKey(name)) {
          return variables.get(name);
        }
        final NodeVariable baked =
            new NodeVariable(new Variable(name, var.getData()));
        variables.put(name, baked);

        return baked;
      }
    };

    final TransformerRule collectMarks = new TransformerRule() {
      @Override
      public boolean isApplicable(final Node node) {
        return ExprUtils.isOperation(node, SsaOperation.MARK);
      }

      @Override
      public Node apply(final Node node) {
        for (final Node mark : ((NodeOperation) node).getOperands()) {
          specialMarks.add((NodeVariable) mark);
        }
        return Expression.TRUE;
      }
    };

    rules.put(Node.Kind.VARIABLE, bake);
    rules.put(SsaOperation.MARK, collectMarks);

    return new NodeTransformer(rules);
  }
}
