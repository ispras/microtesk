/*
 * Copyright 2016-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.ir;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.TreeVisitor.Status;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.Shortcut;
import ru.ispras.microtesk.translator.nml.ir.primitive.Statement;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAssignment;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAttributeCall;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementCondition;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementFormat;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementFunctionCall;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class IrWalkerFlow {
  private final Ir ir;
  private final IrVisitor visitor;

  public IrWalkerFlow(final Ir ir, final IrVisitor visitor) {
    InvariantChecks.checkNotNull(ir);
    InvariantChecks.checkNotNull(visitor);

    this.ir = ir;
    this.visitor = visitor;
  }

  private boolean isStatus(final Status status) {
    return visitor.getStatus() == status;
  }

  public void visit() {
    visitor.onBegin();
    if (isStatus(Status.ABORT)) {
      return;
    }

    visitor.onPrimitivesBegin();
    if (isStatus(Status.ABORT)) {
      return;
    }

    if (isStatus(Status.OK)) {
      visitPrimitives(ir.getModes().values());
      if (isStatus(Status.ABORT)) {
        return;
      }

      visitPrimitives(ir.getOps().values());
      if (isStatus(Status.ABORT)) {
        return;
      }
    }

    visitor.onPrimitivesEnd();
    if (isStatus(Status.ABORT)) {
      return;
    }

    visitor.onEnd();
  }

  private void visitPrimitives(final Collection<Primitive> primitives) {
    for (final Primitive item : primitives) {
      if (isStatus(Status.OK) && !item.isOrRule()) {
        visitPrimitive((PrimitiveAND) item);
        if (isStatus(Status.ABORT)) {
          return;
        }
      }
    }
  }

  private void visitPrimitive(final PrimitiveAND primitive) {
    visitor.onPrimitiveBegin(primitive);
    if (isStatus(Status.ABORT)) {
      return;
    }

    if (isStatus(Status.OK)) {
      visitArguments(primitive);
      if (isStatus(Status.ABORT)) {
        return;
      }

      final Attribute action = primitive.getAttributes().get(Attribute.ACTION_NAME);
      if (null != action) {
        visitAttribute(primitive, action);
        if (isStatus(Status.ABORT)) {
          return;
        }
      }
  
      visitShortcuts(primitive);
      if (isStatus(Status.ABORT)) {
        return;
      }
    }

    visitor.onPrimitiveEnd(primitive);
  }

  private void visitArguments(final PrimitiveAND primitive) {
    for (final Map.Entry<String, Primitive> argument : primitive.getArguments().entrySet()) {
      visitor.onArgumentBegin(primitive, argument.getKey(), argument.getValue());
      if (isStatus(Status.ABORT)) {
        return;
      }
      visitor.onArgumentEnd(primitive, argument.getKey(), argument.getValue());
    }
  }

  private void visitShortcuts(final PrimitiveAND primitive) {
    for (final Shortcut shortcut : primitive.getShortcuts()) {
      visitor.onShortcutBegin(primitive, shortcut); 
      if (isStatus(Status.ABORT)) {
        return;
      }

      if (isStatus(Status.OK)) {
        visitPrimitive(shortcut.getEntry());
      }

      visitor.onShortcutEnd(primitive, shortcut);
    }
  }

  private void visitAttribute(
      final PrimitiveAND primitive,
      final Attribute attribute) {
    visitor.onAttributeBegin(primitive, attribute);
    if (isStatus(Status.ABORT)) {
      return;
    }

    visitStatements(primitive, attribute, attribute.getStatements());
    if (isStatus(Status.ABORT)) {
      return;
    }

    visitor.onAttributeEnd(primitive, attribute);
  }

  private void visitStatements(
      final PrimitiveAND primitive,
      final Attribute attribute,
      final List<Statement> stmts) {
    for (final Statement stmt : stmts) {
      if (isStatus(Status.OK)) {
        visitStatement(primitive, attribute, stmt);
        if (isStatus(Status.ABORT)) {
          return;
        }
      }
    }
  }

  private void visitStatement(
      final PrimitiveAND primitive,
      final Attribute attribute,
      final Statement stmt) {
    switch(stmt.getKind()) {
      case ASSIGN:
        visitor.onAssignment((StatementAssignment) stmt);
        break;

      case CALL:
        visitAttributeCall(primitive, (StatementAttributeCall) stmt);
        break;

      case COND:
        visitCondition(primitive, attribute, (StatementCondition) stmt);
        break;

      case FUNCALL:
        visitor.onFunctionCall((StatementFunctionCall) stmt);
        break;

      case FORMAT:
        visitor.onFormat((StatementFormat) stmt);
        break;

      default:
        throw new IllegalArgumentException(
            "Unknown statement kind: " + stmt.getKind());
    }
  }

  private void visitCondition(
      final PrimitiveAND primitive,
      final Attribute attribute,
      final StatementCondition stmt) {
    visitor.onConditionBegin(stmt);
    if (isStatus(Status.ABORT)) {
      return;
    }

    if (isStatus(Status.OK)) {
      for (int index = 0; index < stmt.getBlockCount(); ++index) {
        final StatementCondition.Block block = stmt.getBlock(index);
        final Node condition = block.getCondition() != null ? block.getCondition().getNode() : null;

        visitor.onConditionBlockBegin(condition);
        if (isStatus(Status.ABORT)) {
          return;
        }

        if (isStatus(Status.OK)) {
          visitStatements(primitive, attribute, block.getStatements());
        }

        visitor.onConditionBlockEnd(condition);
        if (isStatus(Status.ABORT)) {
          return;
        }
      }
    }

    visitor.onConditionEnd(stmt);
  }

  private void visitAttributeCall(
      final PrimitiveAND primitive,
      final StatementAttributeCall stmt) {
    visitor.onAttributeCallBegin(stmt);
    if (isStatus(Status.ABORT)) {
      return;
    }

    if (isStatus(Status.OK)) {
      if (stmt.getCalleeInstance() != null) { // Instance attribute
        final PrimitiveAND callee = stmt.getCalleeInstance().getPrimitive();
        final Attribute attribute = callee.getAttributes().get(stmt.getAttributeName());
        visitAttribute(callee, attribute);
      } else if (stmt.getCalleeName() != null) { // Current primitive's argument attribute
        final Primitive callee = primitive.getArguments().get(stmt.getCalleeName());
        if (!callee.isOrRule()) {
          final PrimitiveAND calleePrimitive = (PrimitiveAND) callee;
          final Attribute attribute = calleePrimitive.getAttributes().get(stmt.getAttributeName());
          visitAttribute(calleePrimitive, attribute);
        }
      } else { // Current primitive attribute
        final Attribute attribute = primitive.getAttributes().get(stmt.getAttributeName());
        visitAttribute(primitive, attribute);
      }

      if (isStatus(Status.ABORT)) {
        return;
      }
    }

    visitor.onAttributeCallEnd(stmt);
  }
}
