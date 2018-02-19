/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.fortress.util.TreeVisitor;
import ru.ispras.fortress.util.TreeVisitor.Status;

import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOR;
import ru.ispras.microtesk.translator.nml.ir.primitive.Shortcut;
import ru.ispras.microtesk.translator.nml.ir.primitive.Statement;
import ru.ispras.microtesk.translator.nml.ir.shared.LetConstant;
import ru.ispras.microtesk.translator.nml.ir.shared.LetLabel;
import ru.ispras.microtesk.translator.nml.ir.shared.MemoryExpr;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

import java.util.Collection;
import java.util.Map;

/**
 * The {@link IrWalker} class performs traversal of an IR using {@link IrVisitor}.
 * The protocol used for traversal is explained {@linkplain TreeVisitor here}.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class IrWalker {

  public static enum Direction {
    LINEAR,
    TREE
  }

  private final Ir ir;
  private IrVisitor visitor;

  public IrWalker(final Ir ir) {
    InvariantChecks.checkNotNull(ir);
    this.ir = ir;
  }

  private boolean isStatus(final Status status) {
    return visitor.getStatus() == status;
  }

  public void visit(final IrVisitor visitor, final Direction direction) {
    InvariantChecks.checkNotNull(visitor);
    InvariantChecks.checkNotNull(direction);

    this.visitor = visitor;
    try {
      visit(direction);
    } finally {
      this.visitor = null;
    }
  }

  private void visit(final Direction direction) {
    visitor.onBegin();
    if (isStatus(Status.ABORT)) {
      return;
    }

    if (isStatus(Status.OK)) {
      visitResources();
      if (isStatus(Status.ABORT)) {
        return;
      }

      visitPrimitives(direction);
      if (isStatus(Status.ABORT)) {
        return;
      }
    }

    visitor.onEnd();
  }

  private void visitResources() {
    visitor.onResourcesBegin();
    if (isStatus(Status.ABORT)) {
      return;
    }

    if (isStatus(Status.OK)) {
      for (final LetConstant let : ir.getConstants().values()) {
        visitor.onLetConstant(let);
        if (isStatus(Status.ABORT)) {
          return;
        }
      }

      for (final LetLabel let : ir.getLabels().values()) {
        visitor.onLetLabel(let);
        if (isStatus(Status.ABORT)) {
          return;
        }
      }

      for (final Map.Entry<String, Type> e : ir.getTypes().entrySet()) {
        visitor.onType(e.getKey(), e.getValue());
        if (isStatus(Status.ABORT)) {
          return;
        }
      }

      for (final Map.Entry<String, MemoryExpr> e : ir.getMemory().entrySet()) {
        visitor.onMemory(e.getKey(), e.getValue());
        if (isStatus(Status.ABORT)) {
          return;
        }
      }
    }

    visitor.onResourcesEnd();
  }

  private void visitPrimitives(final Direction direction) {
    visitor.onPrimitivesBegin();
    if (isStatus(Status.ABORT)) {
      return;
    }

    if (isStatus(Status.OK)) {
      if (Direction.LINEAR == direction) {
        visitPrimitives(ir.getModes().values(), false);
        if (isStatus(Status.ABORT)) {
          return;
        }

        visitPrimitives(ir.getOps().values(), false);
        if (isStatus(Status.ABORT)) {
          return;
        }
      } else if (Direction.TREE == direction) {
        visitPrimitives(ir.getRoots(), true);
        if (isStatus(Status.ABORT)) {
          return;
        }
      } else {
        InvariantChecks.checkTrue(false, "Unknown direction: " + direction);
      }
    }

    visitor.onPrimitivesEnd();
  }

  private void visitPrimitives(final Collection<Primitive> primitives, final boolean isRecursive) {
    for (final Primitive primitive : primitives) {
      visitPrimitive(primitive, isRecursive);
      if (isStatus(Status.ABORT)) {
        return;
      }
    }
  }

  private void visitPrimitive(final Primitive primitive, final boolean isRecursive) {
    visitor.onPrimitiveBegin(primitive);
    if (isStatus(Status.ABORT)) {
      return;
    }

    if (isStatus(Status.OK)) {
      if (primitive instanceof PrimitiveOR) {
        visitOrRule((PrimitiveOR) primitive, isRecursive);
      } else if (primitive instanceof PrimitiveAND) {
        visitAndRule((PrimitiveAND) primitive, isRecursive);
      }
      if (isStatus(Status.ABORT)) {
        return;
      }
    }

    visitor.onPrimitiveEnd(primitive);
  }

  private void visitOrRule(final PrimitiveOR orRule, final boolean isRecursive) {
    for (final Primitive item : orRule.getOrs()) {
      visitor.onAlternativeBegin(orRule, item);
      if (isStatus(Status.ABORT)) {
        return;
      }

      if (isStatus(Status.OK) && isRecursive) {
        visitPrimitive(item, isRecursive);
      }

      visitor.onAlternativeEnd(orRule, item);
      if (isStatus(Status.ABORT)) {
        return;
      }
    }
  }

  private void visitAndRule(final PrimitiveAND andRule, final boolean isRecursive) {
    for (final Map.Entry<String, Primitive> e : andRule.getArguments().entrySet()) {
      visitor.onArgumentBegin(andRule, e.getKey(), e.getValue());
      if (isStatus(Status.ABORT)) {
        return;
      }

      if (isStatus(Status.OK) && isRecursive) {
        visitPrimitive(e.getValue(), isRecursive);
      }

      visitor.onArgumentEnd(andRule, e.getKey(), e.getValue());
      if (isStatus(Status.ABORT)) {
        return;
      }
    }

    for (final Attribute attribute : andRule.getAttributes().values()) {
      visitAttribute(andRule, attribute);
      if (isStatus(Status.ABORT)) {
        return;
      }
    }

    for (final Shortcut shortcut : andRule.getShortcuts()) {
      visitShortcut(andRule, shortcut);
      if (isStatus(Status.ABORT)) {
        return;
      }
    }
  }

  private void visitAttribute(final PrimitiveAND andRule, final Attribute attribute) {
    visitor.onAttributeBegin(andRule, attribute);
    if (isStatus(Status.ABORT)) {
      return;
    }

    if (isStatus(Status.OK)) {
      for (final Statement stmt : attribute.getStatements()) {
        visitor.onStatementBegin(andRule, attribute, stmt);
        if (isStatus(Status.ABORT)) {
          return;
        }

        visitor.onStatementEnd(andRule, attribute, stmt);
      }
    }

    visitor.onAttributeEnd(andRule, attribute);
  }

  private void visitShortcut(final PrimitiveAND andRule, final Shortcut shortcut) {
    visitor.onShortcutBegin(andRule, shortcut);
    if (isStatus(Status.ABORT)) {
      return;
    }

    if (isStatus(Status.OK)) {
      for (final Shortcut.Argument argument : shortcut.getArguments()) {
        visitor.onArgumentBegin(argument.getSource(), argument.getUniqueName(), argument.getType());
        if (isStatus(Status.ABORT)) {
          return;
        }

        visitor.onArgumentEnd(argument.getSource(), argument.getUniqueName(), argument.getType());
        if (isStatus(Status.ABORT)) {
          return;
        }
      }
    }

    visitor.onShortcutEnd(andRule, shortcut);
  }
}
