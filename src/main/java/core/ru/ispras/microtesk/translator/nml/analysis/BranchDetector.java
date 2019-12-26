/*
 * Copyright 2015-2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.analysis;

import ru.ispras.fortress.expression.ExprTreeVisitorDefault;
import ru.ispras.fortress.expression.ExprTreeWalker;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.IrVisitorDefault;
import ru.ispras.microtesk.translator.nml.ir.IrWalkerFlow;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.expr.Location;
import ru.ispras.microtesk.translator.nml.ir.expr.NodeInfo;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAnd;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveReference;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAssignment;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Map;

public final class BranchDetector implements TranslatorHandler<Ir> {
  private IrInquirer inquirer;

  @Override
  public void processIr(final Ir ir) {
    this.inquirer = new IrInquirer(ir);

    //System.out.println(ir.getOps()); // TODO:

    final IrWalkerFlow walker = new IrWalkerFlow(ir, new Visitor());
    walker.visit();
  }

  private final class Visitor extends IrVisitorDefault {
    private final Deque<PrimitiveAnd> primitives;
    private final Deque<Node> conditions;

    public Visitor() {
      this.primitives = new ArrayDeque<>();
      this.conditions = new ArrayDeque<>();
    }

    @Override
    public void onPrimitiveBegin(final Primitive item) {
      //System.out.println("onPrimitiveBegin  " + item.getName());
      if (!item.isOrRule()) {
        this.primitives.push((PrimitiveAnd) item);
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
    public void onConditionBlockBegin(final Node condition) {
      this.conditions.push(condition != null ? condition : NodeValue.newBoolean(true));
    }

    @Override
    public void onConditionBlockEnd(final Node condition) {
      this.conditions.pop();
    }

    @Override
    public void onAssignment(final StatementAssignment stmt) {

      //Collection<PrimitiveReference> mapParents = primitives.getLast().getParents();
      //for (final PrimitiveReference i : mapParents) {
        //System.out.println("getParents: " + i.getName() + "  ");
      //}
      //System.out.printf("[onAssignment] %s %s %n", primitives.getLast().getName(), primitives.getLast().getArguments().size());
      //Map<String, Primitive> temp = primitives.getLast().getArguments();
      //for (Map.Entry<String, Primitive> entry : temp.entrySet())
      //{System.out.println(entry.getKey() + "/" + entry.getValue().getName());      }

      if (isPCAssignment(stmt)) {
        final boolean isConditional = !conditions.isEmpty();
        final PrimitiveAnd primitive =  primitives.getLast();

        //for (Node condition : conditions) {
        //  System.out.println("condition: " + condition.toString());
        //}

        //NodeOperation temp2 = (NodeOperation) conditions.getFirst();
        //System.out.println("condition operans: " + temp2.getOperands());
        //System.out.println("condition operation: " + temp2.getOperationId());

        if (isConditional) {
          primitive.getInfo().setConditionalBranch(true);
          primitive.getInfo().setConditionForBranch(conditions.getLast());
        } else {
          primitive.getInfo().setBranch(true);
        }

        //System.out.printf("[B] %s - %sbranch%s%n",
        //    primitive.getName(), isConditional ? "conditional " : "", ", condition:" + primitive.getInfo().getConditionForBranch());
      }
    }

    private boolean isPCAssignment(final StatementAssignment stmt) {
      final Expr left = stmt.getLeft();
      final Expr right = stmt.getRight();

      if (!inquirer.isPC(left)) {
        return false;
      }

      //System.out.println("condition: " + conditions.toString());
     // for (Node condition : conditions) {
        //System.out.println("condition: " + condition.toString());
        //System.out.println("condition: " + condition.getDataType());
        //System.out.println("condition: " + condition.getDataTypeId());
        //System.out.println("condition: " + condition.getKind());
     // }

      final PCSourceExplorer visitor = new PCSourceExplorer();
      final ExprTreeWalker walker = new ExprTreeWalker(visitor);

      walker.visit(right.getNode());
      return visitor.isBasedOnExternalParameters();
    }
  }

  private final class PCSourceExplorer extends ExprTreeVisitorDefault {
    private boolean basedOnExternalParameters = false;

    public boolean isBasedOnExternalParameters() {
      return basedOnExternalParameters;
    }

    @Override
    public void onVariable(final NodeVariable variable) {
      final Expr node = new Expr(variable);
      final NodeInfo nodeInfo = node.getNodeInfo();

      InvariantChecks.checkTrue(nodeInfo.isLocation());
      final Location location = (Location) nodeInfo.getSource();

      if (!inquirer.isPC(location)) {
        basedOnExternalParameters = true;
        setStatus(Status.ABORT);
      }
    }
  }
}
