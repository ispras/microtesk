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

package ru.ispras.microtesk.translator.simnml.coverage;

import ru.ispras.fortress.solver.constraint.Constraint;
import ru.ispras.fortress.solver.constraint.ConstraintBuilder;
import ru.ispras.fortress.solver.constraint.Formulas;

import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.transformer.NodeTransformer;
import ru.ispras.fortress.transformer.TransformerRule;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class PathConstraintBuilder {
  final Map<String, NodeVariable> variables;
  final ConstraintBuilder builder;
  final Formulas ssa;
  final Node conditionExpr;

  public PathConstraintBuilder(Node ssa) {
    this.variables = new HashMap<>();
    this.builder = new ConstraintBuilder();
    this.ssa = new Formulas();

    final Node instance = Utility.transform(ssa, setUpTransformer());

    this.conditionExpr = PathFilter.filter(instance);

    if (Utility.nodeIsOperation(instance, StandardOperation.AND)) {
      for (Node node : ((NodeOperation) instance).getOperands()) {
        this.ssa.add(node);
      }
    } else {
      this.ssa.add(instance);
    }
    for (NodeVariable node : variables.values()) {
      this.builder.addVariable(node.getName(), node.getData());
    }
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

  public Constraint build(Node condition) {
    return build(Collections.singleton(condition));
  }

  public Constraint build(Collection<? extends Node> conditions) {
    final Formulas formulas = new Formulas(this.ssa);
    for (Node node : conditions) {
      formulas.add(node);
    }

    this.builder.setInnerRep(formulas);
    return this.builder.build();
  }

  private NodeTransformer setUpTransformer() {
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

    final NodeTransformer xform = new NodeTransformer();
    xform.addRule(Node.Kind.VARIABLE, bake);

    for (Map.Entry<Enum<?>, TransformerRule> entry : IntegerCast.rules().entrySet()) {
      xform.addRule(entry.getKey(), entry.getValue());
    }
    return xform;
  }
}
