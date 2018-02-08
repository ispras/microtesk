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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.TreeVisitor.Status;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.Shortcut;

import java.util.Collection;
import java.util.Map;

public final class IrWalkerShortcuts {
  private final Ir ir;
  private final IrVisitor visitor;

  public IrWalkerShortcuts(final Ir ir, final IrVisitor visitor) {
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

    if (isStatus(Status.OK)) {
      visitPrimitives(ir.getOps().values());
      if (isStatus(Status.ABORT)) {
        return;
      }
    }

    visitor.onEnd();
  }

  private void visitPrimitives(final Collection<Primitive> primitives) {
    visitor.onPrimitivesBegin();
    if (isStatus(Status.ABORT)) {
      return;
    }

    for (final Primitive item : ir.getOps().values()) {
      if (isStatus(Status.OK) && !item.isOrRule()) {
        visitPrimitive((PrimitiveAND) item);
        if (isStatus(Status.ABORT)) {
          return;
        }
      }
    }

    visitor.onPrimitivesEnd();
  }

  private void visitPrimitive(final PrimitiveAND primitive) {
    visitor.onPrimitiveBegin(primitive);
    if (isStatus(Status.ABORT)) {
      return;
    }

    for (final Shortcut shortcut : primitive.getShortcuts()) {
      if (isStatus(Status.OK)) {
        visitShortcut(primitive, shortcut);
        if (isStatus(Status.ABORT)) {
          return;
        }
      }
    }

    visitor.onPrimitiveEnd(primitive);
  }

  private void visitShortcut(final PrimitiveAND primitive, final Shortcut shortcut) {
    visitor.onShortcutBegin(primitive, shortcut);
    if (isStatus(Status.ABORT)) {
      return;
    }

    final PrimitiveAND entry = shortcut.getEntry();
    final PrimitiveAND target = shortcut.getTarget();

    if (isStatus(Status.OK)) {
      visitPrimitive(entry, target);
      if (isStatus(Status.ABORT)) {
        return;
      }
    }

    visitor.onShortcutEnd(primitive, shortcut);
  }

  private void visitPrimitive(final PrimitiveAND entry, final PrimitiveAND target) {
    visitor.onPrimitiveBegin(entry);
    if (isStatus(Status.ABORT)) {
      return;
    }

    final boolean reachedTarget = entry == target;
    for (final Map.Entry<String, Primitive> item : entry.getArguments().entrySet()) {
      final String argName = item.getKey();
      final Primitive arg = item.getValue();

      if (isStatus(Status.OK) && (arg.getKind() == Primitive.Kind.OP) && !reachedTarget) {
        InvariantChecks.checkFalse(arg.isOrRule());

        visitor.onArgumentBegin(entry, argName, arg);
        if (isStatus(Status.ABORT)) {
          return;
        }

        if (isStatus(Status.OK)) {
          visitPrimitive((PrimitiveAND) arg, target);
          if (isStatus(Status.ABORT)) {
            return;
          }
        }

        visitor.onArgumentEnd(entry, argName, arg);
        if (isStatus(Status.ABORT)) {
          return;
        }
      }
    }

    visitor.onPrimitiveEnd(entry);
  }
}
