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

import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetConstant;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetLabel;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetString;
import ru.ispras.microtesk.translator.simnml.ir.shared.MemoryExpr;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public final class IRWalker {
  private final IR ir;

  public IRWalker(IR ir) {
    checkNotNull(ir);
    this.ir = ir;
  }

  public void traverse(IRVisitor visitor) {
    checkNotNull(visitor);

    visitor.beginResourceDefinitions();

    traverseConstants(visitor);
    traverseStrings(visitor);
    traverseLabels(visitor);
    traverseTypes(visitor);
    traverseMemory(visitor);

    visitor.endResourceDefinitions();

    traverseModes(visitor);
    traverseOps(visitor);
  }

  private void traverseConstants(IRVisitor visitor) {
    for (Map.Entry<String, LetConstant> e : ir.getConstants().entrySet()) {
      visitor.onLetConstant(e.getKey(), e.getValue());
    }
  }

  private void traverseStrings(IRVisitor visitor) {
    for (Map.Entry<String, LetString> e : ir.getStrings().entrySet()) {
      visitor.onLetString(e.getKey(), e.getValue());
    }
  }

  private void traverseLabels(IRVisitor visitor) {
    for (Map.Entry<String, LetLabel> e : ir.getLabels().entrySet()) {
      visitor.onLetLabel(e.getKey(), e.getValue());
    }
  }

  private void traverseTypes(IRVisitor visitor) {
    for (Map.Entry<String, Type> e : ir.getTypes().entrySet()) {
      visitor.onType(e.getKey(), e.getValue());
    }
  }

  private void traverseMemory(IRVisitor visitor) {
    for (Map.Entry<String, MemoryExpr> e : ir.getMemory().entrySet()) {
      visitor.onMemory(e.getKey(), e.getValue());
    }
  }

  private void traverseModes(IRVisitor visitor) {
    for (Map.Entry<String, Primitive> e : ir.getModes().entrySet()) {
      visitor.onMode(e.getKey(), e.getValue());
    }
  }

  private void traverseOps(IRVisitor visitor) {
    for (Map.Entry<String, Primitive> e : ir.getOps().entrySet()) {
      visitor.onOp(e.getKey(), e.getValue());
    }
  }
}
