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

package ru.ispras.microtesk.translator.nml.ir;

import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.Shortcut;

public class IrWalkerShortcuts {
  private final Ir ir;
  private final IrVisitor visitor;

  public IrWalkerShortcuts(final Ir ir, final IrVisitor visitor) {
    InvariantChecks.checkNotNull(ir);
    InvariantChecks.checkNotNull(visitor);

    this.ir = ir;
    this.visitor = visitor;
  }

  public void visit() {
    visitor.onPrimitivesBegin();

    for (final Primitive item : ir.getOps().values()) {
      if (!item.isOrRule()) {
        visitPrimitive((PrimitiveAND) item);
      }
    }

    visitor.onPrimitivesEnd();
  }

  private void visitPrimitive(final PrimitiveAND primitive) {
    visitor.onPrimitiveBegin(primitive);

    for (final Shortcut shortcut : primitive.getShortcuts()) {
      visitShortcut(primitive, shortcut);
    }

    visitor.onPrimitiveEnd(primitive);
  }

  private void visitShortcut(final PrimitiveAND primitive, final Shortcut shortcut) {
    visitor.onShortcutBegin(primitive, shortcut);

    final PrimitiveAND entry = shortcut.getEntry();
    final PrimitiveAND target = shortcut.getTarget();

    visitPrimitive(entry, target);
    visitor.onShortcutEnd(primitive, shortcut);
  }

  private void visitPrimitive(final PrimitiveAND entry, final PrimitiveAND target) {
    visitor.onPrimitiveBegin(entry);

    final boolean reachedTarget = entry == target;
    for (final Map.Entry<String, Primitive> item : entry.getArguments().entrySet()) {
      final String argName = item.getKey();
      final Primitive arg = item.getValue();

      if ((arg.getKind() == Primitive.Kind.OP) && !reachedTarget) {
        InvariantChecks.checkFalse(arg.isOrRule());

        visitor.onArgumentBegin(entry, argName, arg);
        visitPrimitive((PrimitiveAND) arg, target);
        visitor.onArgumentEnd(entry, argName, arg);
      }
    }

    visitor.onPrimitiveEnd(entry);
  }
}
