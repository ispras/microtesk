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
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.nml.NmlSymbolKind;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.IrVisitorDefault;
import ru.ispras.microtesk.translator.nml.ir.IrWalker;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.expr.Location;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationSource;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationSourceMemory;
import ru.ispras.microtesk.translator.nml.ir.expr.NodeInfo;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.Statement;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAttributeCall;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementFormat;

public class RegisterAnalyzer implements TranslatorHandler<Ir> {

  @Override
  public void processIr(Ir ir) {
    final IrWalker walker = new IrWalker(ir);
    walker.visit(new Visitor(), IrWalker.Direction.LINEAR);
  }

  private static final class Visitor extends IrVisitorDefault {

    @Override
    public void onPrimitiveBegin(Primitive item) {
      if (item.isOrRule() || item.getKind() != Primitive.Kind.MODE) {
        setStatus(Status.SKIP);
        return;
      }

      final PrimitiveAND primitive = (PrimitiveAND) item;
      final Expr expr = primitive.getReturnExpr();

      if (null == expr || !isRegisterAccess(expr)) {
        setStatus(Status.SKIP);
        return;
      }

      System.out.println(item.getName());
    }

    @Override
    public void onPrimitiveEnd(Primitive item) {
      if (getStatus() == Status.SKIP) {
        setStatus(Status.OK);
      }
    }

    @Override
    public void onAttributeBegin(final PrimitiveAND andRule, final Attribute attr) {
      if (!attr.getName().equals(Attribute.SYNTAX_NAME)) {
        setStatus(Status.SKIP);
        return;
      }

      InvariantChecks.checkTrue(
          2 >= attr.getStatements().size(), "image cannot have more than 2 statements");
    }

    @Override
    public void onAttributeEnd(final PrimitiveAND andRule, final Attribute attr) {
      setStatus(Status.OK);
    }

    @Override
    public void onStatement(
        final PrimitiveAND andRule,
        final Attribute attr,
        final Statement stmt) {
      InvariantChecks.checkTrue(stmt.getKind() == Statement.Kind.FORMAT ||
                                stmt.getKind() == Statement.Kind.CALL);
      if (stmt instanceof StatementFormat) {
        onStatementFormat(andRule, (StatementFormat) stmt);
      } else if (stmt instanceof StatementAttributeCall) {
        //onStatementAttributeCall(andRule, (StatementAttributeCall) stmt);
      } else {
        InvariantChecks.checkTrue(
            false, "Unsupported statement: " + stmt.getKind());
      }
    }

    private void onStatementFormat(PrimitiveAND andRule, StatementFormat stmt) {
      System.out.println(andRule.getName() + " : " + stmt.getFormat());
      // TODO Auto-generated method stub
    }
  }

  private static boolean isRegisterAccess(final Expr expr) {
    final ExprVisitor visitor = new ExprVisitor();
    final ExprTreeWalker walker = new ExprTreeWalker(visitor);

    walker.visit(expr.getNode());
    return visitor.isRegister();
  }

  private static final class ExprVisitor extends ExprTreeVisitorDefault {
    private boolean register = false;

    public boolean isRegister() {
      return register;
    }

    @Override
    public void onVariable(final NodeVariable variable) {
      if (isRegister(variable)) {
        register = true;
        setStatus(Status.ABORT);
      }
    }

    @Override
    public void onOperationBegin(NodeOperation node) {
      if (node.getOperationId() != StandardOperation.ITE) {
        setStatus(Status.SKIP);
      }
    }

    @Override
    public void onOperationEnd(NodeOperation node) {
      if (getStatus() == Status.SKIP) {
        setStatus(Status.OK);
      }
    }

    private static boolean isRegister(final NodeVariable variable) {
      final NodeInfo nodeInfo = (NodeInfo) variable.getUserData();
      final Location location = (Location) nodeInfo.getSource();
      final LocationSource locationSource = location.getSource();

      if (locationSource.getSymbolKind() != NmlSymbolKind.MEMORY) {
        return false;
      }

      final LocationSourceMemory memory = (LocationSourceMemory) locationSource;
      return memory.getMemory().getKind() == Memory.Kind.REG;
    }
  }
}
