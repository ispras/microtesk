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

import java.util.ArrayDeque;
import java.util.Deque;

import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.IrPass;
import ru.ispras.microtesk.translator.nml.ir.IrVisitor;
import ru.ispras.microtesk.translator.nml.ir.IrVisitorDefault;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementFunctionCall;

public final class ExceptionDetector extends IrPass {
  public ExceptionDetector(final Ir ir) {
    super(ir);
  }

  @Override
  protected IrVisitor getVisitor() {
    return new Visitor();
  }

  private static final class Visitor extends IrVisitorDefault {
    private final Deque<PrimitiveAND> primitives;

    public Visitor() {
      this.primitives = new ArrayDeque<>();
    }

    @Override
    public void onPrimitiveBegin(final Primitive item) {
      if (!item.isOrRule()) {
        this.primitives.push((PrimitiveAND) item);
      }
    }

    @Override
    public void onPrimitiveEnd(final Primitive item) {
      if (!item.isOrRule()) {
        this.primitives.pop();
      }
    }

    @Override
    public void onFunctionCall(final StatementFunctionCall stmt) {
      if (isException(stmt)) {
        final PrimitiveAND primitive = primitives.peek();
        primitive.getInfo().setCanThrowException(true);
        setStatus(Status.SKIP);
        //System.out.printf("[Funcall] %s - exception%n", primitive.getName());
      }
    }

    private static boolean isException(final StatementFunctionCall stmt) {
      return "exception".equals(stmt.getName());
    }
  }
}
