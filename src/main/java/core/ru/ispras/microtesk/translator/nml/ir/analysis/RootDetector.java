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

import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.IrVisitorDefault;
import ru.ispras.microtesk.translator.nml.ir.IrWalker;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;

public final class RootDetector implements TranslatorHandler<Ir> {
  @Override
  public void processIr(final Ir ir) {
    final IrWalker walker = new IrWalker(ir);
    walker.visit(new Visitor(ir), IrWalker.Direction.LINEAR);
  }

  private static final class Visitor extends IrVisitorDefault {
    private final Ir ir;

    private Visitor(final Ir ir) {
      this.ir = ir;
    }

    @Override
    public void onResourcesBegin() {
      setStatus(Status.SKIP);
    }

    @Override
    public void onResourcesEnd() {
      setStatus(Status.OK);
    }

    @Override
    public void onPrimitiveBegin(final Primitive item) {
      setStatus(Status.SKIP);
      if (item.isRoot() && Primitive.Kind.OP == item.getKind() && !item.isOrRule()) {
        ir.addRoot(item);
      }
    }

    @Override
    public void onPrimitiveEnd(final Primitive item) {
      setStatus(Status.OK);
    }
  }
}
