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

package ru.ispras.microtesk.translator.nml.ir.analysis;

import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.IrVisitorDefault;
import ru.ispras.microtesk.translator.nml.ir.IrWalker;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;

public final class ReferenceDetector implements TranslatorHandler<Ir> {
  @Override
  public void processIr(final Ir ir) {
    final IrWalker walker = new IrWalker(ir);
    walker.visit(new Visitor(), IrWalker.Direction.LINEAR);
  }

  private static final class Visitor extends IrVisitorDefault {
    @Override
    public void onResourcesBegin() {
      setStatus(Status.SKIP);
    }

    @Override
    public void onResourcesEnd() {
      setStatus(Status.OK);
    }

    @Override
    public void onArgumentBegin(
        final PrimitiveAND andRule,
        final String argName,
        final Primitive argType) {
      argType.addParentReference(andRule, argName);
    }

    @Override
    public void onAttributeBegin(final PrimitiveAND andRule, final Attribute attr) {
      setStatus(Status.SKIP);
    }

    @Override
    public void onAttributeEnd(final PrimitiveAND andRule, final Attribute attr) {
      setStatus(Status.OK);
    }
  }
}
