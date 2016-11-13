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

import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.IrVisitorDefault;
import ru.ispras.microtesk.translator.nml.ir.IrWalker;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.Shortcut;
import ru.ispras.microtesk.translator.nml.ir.primitive.Statement;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAttributeCall;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementFormat;

public final class ImageAnalyzer implements TranslatorHandler<Ir> {
  @Override
  public void processIr(final Ir ir) {
    final IrWalker walker = new IrWalker(ir);
    walker.visit(new Visitor(), IrWalker.Direction.LINEAR);
  }

  private static final class Visitor extends IrVisitorDefault {
    private final Map<String, Primitive> visited = new HashMap<>();

    @Override
    public void onPrimitiveBegin(final Primitive item) {
      if (item.isPseudo() || visited.containsKey(item.getName())) {
        setStatus(Status.SKIP);
        return;
      }
      //System.out.println(item.getName());
    }

    @Override
    public void onPrimitiveEnd(final Primitive item) {
      if (getStatus() == Status.SKIP) {
        setStatus(Status.OK);
      } else {
        visited.put(item.getName(), item);
      }
    }

    @Override
    public void onAttributeBegin(final PrimitiveAND andRule, final Attribute attr) {
      if (!attr.getName().equals(Attribute.IMAGE_NAME)) {
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
    public void onShortcutBegin(final PrimitiveAND andRule, final Shortcut shortcut) {
      setStatus(Status.SKIP);
    }

    @Override
    public void onShortcutEnd(final PrimitiveAND andRule, final Shortcut shortcut) {
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
        onStatementAttributeCall(andRule, (StatementAttributeCall) stmt);
      } else {
        InvariantChecks.checkTrue(
            false, "Unsupported statement: " + stmt.getKind());
      }
    }

    private void onStatementFormat(
        final PrimitiveAND primitive, final StatementFormat stmt) {
      
    }

    private void onStatementAttributeCall(
        final PrimitiveAND andRule, final StatementAttributeCall stmt) {
      
    }

    @Override
    public void onAttributeCallBegin(final StatementAttributeCall stmt) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onFormat(final StatementFormat stmt) {
      throw new UnsupportedOperationException();
    }
  }
}
