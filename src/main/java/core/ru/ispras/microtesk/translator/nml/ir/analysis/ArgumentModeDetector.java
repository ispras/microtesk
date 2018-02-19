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

package ru.ispras.microtesk.translator.nml.ir.analysis;

import ru.ispras.fortress.expression.ExprTreeVisitorDefault;
import ru.ispras.fortress.expression.ExprTreeWalker;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.model.ArgumentMode;
import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.IrVisitorDefault;
import ru.ispras.microtesk.translator.nml.ir.IrWalkerFlow;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.expr.Location;
import ru.ispras.microtesk.translator.nml.ir.expr.NodeInfo;
import ru.ispras.microtesk.translator.nml.ir.primitive.Instance;
import ru.ispras.microtesk.translator.nml.ir.primitive.InstanceArgument;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveInfo;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAssignment;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAttributeCall;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;

public final class ArgumentModeDetector implements TranslatorHandler<Ir> {
  @Override
  public void processIr(final Ir ir) {
    final IrWalkerFlow walker = new IrWalkerFlow(ir, new Visitor());
    walker.visit();
  }

  private final class Visitor extends IrVisitorDefault {
    private final Deque<PrimitiveAND> primitives;
    private final Map<PrimitiveAND, PrimitiveAND> visited;

    public Visitor() {
      this.primitives = new ArrayDeque<>();
      this.visited = new IdentityHashMap<>();
    }

    @Override
    public void onPrimitiveBegin(final Primitive item) {
      if (!item.isOrRule()) {
        final PrimitiveAND primitive = (PrimitiveAND) item;
        this.primitives.push(primitive);
        this.visited.put(primitive, primitive);
      }
    }

    @Override
    public void onPrimitiveEnd(final Primitive item) {
      if (!item.isOrRule()) {
        this.primitives.pop();
      }

      if (isStatus(Status.SKIP)) {
        setStatus(Status.OK);
      }
    }

    @Override
    public void onAssignment(final StatementAssignment stmt) {
      markVariables(stmt.getRight(), ArgumentMode.IN);
      markVariables(stmt.getLeft(),  ArgumentMode.OUT);
    }

    @Override
    public void onAttributeCallBegin(final StatementAttributeCall stmt) {
      if (stmt.getCalleeInstance() != null) { // Instance attribute
        markVariables(stmt.getCalleeInstance());
        setStatus(Status.SKIP);
      } else if (stmt.getCalleeName() != null) { // Current primitive's argument attribute
        setStatus(Status.SKIP);
      }
    }

    @Override
    public void onAttributeCallEnd(final StatementAttributeCall stmt) {
      if (stmt.getCalleeName() != null || stmt.getCalleeInstance() != null) {
        setStatus(Status.OK);
      }
    }

    @Override
    public void onConditionBlockBegin(final Node condition) {
      if (null != condition) {
        markVariables(new Expr(condition), ArgumentMode.IN);
      }
    }

    private void markVariable(final String variableName, final ArgumentMode mode) {
      final PrimitiveAND primitive = primitives.peek();
      final PrimitiveInfo info = primitive.getInfo();

      if (primitive.getArguments().containsKey(variableName)) {
        info.setArgUsage(variableName, mode);
      }
    }

    private void markVariables(final Location location, final ArgumentMode mode) {
      final Location atom = (Location) location;
      markVariable(atom.getName(), mode);

      if (null != atom.getIndex()) {
        markVariables(atom.getIndex(), ArgumentMode.IN);
      }

      if (null != atom.getBitfield()) {
        markVariables(atom.getBitfield().getFrom(), ArgumentMode.IN);
        markVariables(atom.getBitfield().getTo(), ArgumentMode.IN);
      }
    }

    private void markVariables(final Expr expr, final ArgumentMode mode) {
      final ExprTreeWalker walker = new ExprTreeWalker(new VariableMarker(mode));
      walker.visit(expr.getNode());
    }

    private final class VariableMarker extends ExprTreeVisitorDefault {
      private final ArgumentMode mode;

      public VariableMarker(final ArgumentMode mode) {
        InvariantChecks.checkNotNull(mode);
        this.mode = mode;
      }

      @Override
      public void onVariable(final NodeVariable variable) {
        final Expr node = new Expr(variable);
        final NodeInfo nodeInfo = node.getNodeInfo();

        InvariantChecks.checkTrue(nodeInfo.isLocation());
        final Location location = (Location) nodeInfo.getSource();

        markVariables(location, mode);
      }
    }

    private void markVariables(final Instance instance) {
      for (int index = 0; index < instance.getArguments().size(); ++index) {
        final InstanceArgument argument = instance.getArguments().get(index);
        switch (argument.getKind()) {
          case EXPR:
            markVariables(argument.getExpr(), ArgumentMode.IN);
            break;

          case INSTANCE:
            markVariables(argument.getInstance());
            break;

          case PRIMITIVE:
            markVariable(argument.getName(), getArgumentMode(instance.getPrimitive(), index));
            break;

          default:
            throw new IllegalStateException("Illegal kind: " + argument.getKind());
        }
      }
    }

    private ArgumentMode getArgumentMode(final PrimitiveAND primitive, final int argumentIndex) {
      InvariantChecks.checkTrue(visited.containsKey(primitive), primitive.getName());
      InvariantChecks.checkBounds(argumentIndex, primitive.getArguments().size());

      int index = 0;
      for (final String name : primitive.getArguments().keySet()) {
        if (argumentIndex == index) {
          return primitive.getInfo().getArgUsage(name);
        }
        index++;
      }

      return null;
    }
  }
}
