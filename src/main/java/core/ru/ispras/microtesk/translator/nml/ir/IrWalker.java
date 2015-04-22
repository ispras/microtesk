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

package ru.ispras.microtesk.translator.nml.ir;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.Collection;
import java.util.Map;

import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOR;
import ru.ispras.microtesk.translator.nml.ir.primitive.Shortcut;
import ru.ispras.microtesk.translator.nml.ir.primitive.Statement;
import ru.ispras.microtesk.translator.nml.ir.shared.LetConstant;
import ru.ispras.microtesk.translator.nml.ir.shared.LetLabel;
import ru.ispras.microtesk.translator.nml.ir.shared.LetString;
import ru.ispras.microtesk.translator.nml.ir.shared.MemoryExpr;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

/**
 * The IrWalker class performs traversal of an IR using IrVisitor.
 * 
 * @author Andrei Tatarnikov
 */

public final class IrWalker {

  public static enum Direction {
    LINEAR,
    TREE
  }

  private final IR ir;
  private final Direction direction;
  private IrVisitor visitor;

  public IrWalker(IR ir, Direction direction) {
    checkNotNull(ir);
    checkNotNull(direction);

    this.ir = ir;
    this.direction = direction;
    this.visitor = null;
  }

  public void traverse(IrVisitor visitor) {
    checkNotNull(visitor);
    this.visitor = visitor;

    try {
      traverseResources();
      if (Direction.LINEAR == direction) {
        visitor.onPrimitivesBegin();
        traversePrimitives(ir.getModes().values(), false);
        traversePrimitives(ir.getOps().values(), false);
        visitor.onPrimitivesEnd();
      } else if (Direction.TREE == direction) {
        visitor.onPrimitivesBegin();
        traversePrimitives(ir.getRoots(), true);
        visitor.onPrimitivesEnd();
      } else {
        throw new IllegalStateException("Unknown direction: " + direction);
      }
    } finally {
      this.visitor = null;
    }
  }

  private void traverseResources() {
    visitor.onResourcesBegin();

    for (LetConstant let : ir.getConstants().values()) {
      visitor.onLetConstant(let);
    }

    for (LetString let : ir.getStrings().values()) {
      visitor.onLetString(let);
    }

    for (LetLabel let : ir.getLabels().values()) {
      visitor.onLetLabel(let);
    }

    for (Map.Entry<String, Type> e : ir.getTypes().entrySet()) {
      visitor.onType(e.getKey(), e.getValue());
    }

    for (Map.Entry<String, MemoryExpr> e : ir.getMemory().entrySet()) {
      visitor.onMemory(e.getKey(), e.getValue());
    }

    visitor.onResourcesEnd();
  }

  private void traversePrimitives(Collection<Primitive> primitives, boolean isRecursive) {
    for (Primitive primitive : primitives) {
      traversePrimitive(primitive, isRecursive);
    }
  }

  private void traversePrimitive(Primitive primitive, boolean isRecursive) {
    visitor.onPrimitiveBegin(primitive);

    if (primitive instanceof PrimitiveOR) {
      traverseOrRule((PrimitiveOR) primitive, isRecursive);
    }
    else if (primitive instanceof PrimitiveAND) {
      traverseAndRule((PrimitiveAND) primitive, isRecursive);
    }

    visitor.onPrimitiveEnd(primitive);
  }

  private void traverseOrRule(PrimitiveOR orRule, boolean isRecursive) {
    for (Primitive item : orRule.getORs()) {
      visitor.onAlternativeBegin(orRule, item);
      if (isRecursive) {
        traversePrimitive(item, isRecursive);
      }
      visitor.onAlternativeEnd(orRule, item);
    }
  }

  private void traverseAndRule(PrimitiveAND andRule, boolean isRecursive) {
    for (Map.Entry<String, Primitive> e : andRule.getArguments().entrySet()) {
      visitor.onArgumentBegin(andRule, e.getKey(), e.getValue());
      if (isRecursive) {
        traversePrimitive(e.getValue(), isRecursive);
      }
      visitor.onArgumentEnd(andRule, e.getKey(), e.getValue());
    }

    for (Attribute attribute : andRule.getAttributes().values()) {
      visitor.onAttributeBegin(andRule, attribute);
      for (Statement stmt : attribute.getStatements()) {
        visitor.onStatement(andRule, attribute, stmt);
      }
      visitor.onAttributeEnd(andRule, attribute);
    }

    for(Shortcut shortcut : andRule.getShortcuts()) {
      visitor.onShortcutBegin(andRule, shortcut);
      for (Shortcut.Argument argument : shortcut.getArguments()) {
        visitor.onArgumentBegin(argument.getSource(), argument.getUniqueName(), argument.getType());
        visitor.onArgumentEnd(argument.getSource(), argument.getUniqueName(), argument.getType());
      }
      visitor.onShortcutEnd(andRule, shortcut);
    }
  }
}
