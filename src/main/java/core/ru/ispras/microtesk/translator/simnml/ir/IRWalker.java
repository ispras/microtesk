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

package ru.ispras.microtesk.translator.simnml.ir;

import static ru.ispras.microtesk.utils.InvariantChecks.checkNotNull;

import java.util.Map;

import ru.ispras.microtesk.translator.simnml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveOR;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Shortcut;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetConstant;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetLabel;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetString;
import ru.ispras.microtesk.translator.simnml.ir.shared.MemoryExpr;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public final class IRWalker {

  public static enum Direction {
    LINEAR,
    TREE
  }

  private final IR ir;
  private final Direction direction;
  private IRVisitor visitor;

  public IRWalker(IR ir, Direction direction) {
    checkNotNull(ir);
    checkNotNull(direction);

    this.ir = ir;
    this.direction = direction;
    this.visitor = null;
  }

  public void traverse(IRVisitor visitor) {
    checkNotNull(visitor);
    this.visitor = visitor;

    try {
      traverseResources();
      switch(direction) {
        case LINEAR:
          traversePrimitivesLinear();
          break;
        case TREE:
          traversePrimitivesTree();
          break;
        default:
          throw new IllegalStateException(
              "Unknown direction: " + direction);
      }
    } finally {
      this.visitor = null;
    }
  }

  private void traverseResources() {
    visitor.onResourcesBegin();

    // Traverse Constants
    for (Map.Entry<String, LetConstant> e : ir.getConstants().entrySet()) {
      visitor.onLetConstant(e.getKey(), e.getValue());
    }

    // Traverse Strings
    for (Map.Entry<String, LetString> e : ir.getStrings().entrySet()) {
      visitor.onLetString(e.getKey(), e.getValue());
    }

    // Traverse Labels
    for (Map.Entry<String, LetLabel> e : ir.getLabels().entrySet()) {
      visitor.onLetLabel(e.getKey(), e.getValue());
    }

    // Traverse Types
    for (Map.Entry<String, Type> e : ir.getTypes().entrySet()) {
      visitor.onType(e.getKey(), e.getValue());
    }

    // Traverse Memory
    for (Map.Entry<String, MemoryExpr> e : ir.getMemory().entrySet()) {
      visitor.onMemory(e.getKey(), e.getValue());
    }

    visitor.onResourcesEnd();
  }

  private void traversePrimitivesLinear() {
    // Traverse Modes
    for (Map.Entry<String, Primitive> e : ir.getModes().entrySet()) {
      visitPrimitive(e.getKey(), e.getValue(), false);
    }

    // Traverse Ops
    for (Map.Entry<String, Primitive> e : ir.getOps().entrySet()) {
      visitPrimitive(e.getKey(), e.getValue(), false);
    }
  }

  private void traversePrimitivesTree() {
    // Traverse Ops from roots to leaves
    for (Primitive p : ir.getRoots()) {
      visitPrimitive(p.getName(), p, true);
    }
  }

  private void visitPrimitive(String name, Primitive value, boolean isRecursive) {
    visitor.onPrimitiveBegin(name, value);
    if (value instanceof PrimitiveOR) {
      final PrimitiveOR p = (PrimitiveOR) value;
      visitor.onAlternativeBegin(p);
      if (isRecursive) {
        visitPrimitive(p.getName(), p, isRecursive);
      }
      visitor.onAlternativeEnd(p);
    }
    else if (value instanceof PrimitiveAND) {
      final PrimitiveAND p = (PrimitiveAND) value;

      for (Map.Entry<String, Primitive> e : p.getArguments().entrySet()) {
        visitor.onArgumentBegin(e.getKey(), e.getValue());
        if (isRecursive) {
          visitPrimitive(e.getKey(), e.getValue(), isRecursive);
        }
        visitor.onArgumentEnd(e.getKey(), e.getValue());
      }

      for (Attribute attribute : p.getAttributes().values()) {
        visitor.onAttributeBegin(attribute);
        visitor.onAttributeEnd(attribute);
      }

      for(Shortcut shortcut : p.getShortcuts()) {
        visitor.onShortcutBegin(
            shortcut.getName(),
            shortcut.getContextName(),
            shortcut.getEntry(),
            shortcut.getTarget()
            );
       
        for (Shortcut.Argument sa : shortcut.getArguments()) {
          visitor.onArgumentBegin(sa.getUniqueName(), sa.getType());
          visitor.onArgumentEnd(sa.getUniqueName(), sa.getType());
        }

        visitor.onShortcutEnd(
            shortcut.getName(),
            shortcut.getContextName(),
            shortcut.getEntry(),
            shortcut.getTarget()
            );
      }
    }
    visitor.onPrimitiveEnd(name, value);
  }
}
