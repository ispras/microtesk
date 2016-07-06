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

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.IrVisitorDefault;
import ru.ispras.microtesk.translator.nml.ir.IrWalkerFlow;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.expr.Location;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationAtom;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationConcat;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationSourceMemory;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationSourcePrimitive;
import ru.ispras.microtesk.translator.nml.ir.expr.NodeInfo;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;

public final class MemoryAccessDetector2 {

  private static final class Visitor extends IrVisitorDefault {
    private final Deque<PrimitiveAND> primitives;

    public Visitor() {
      this.primitives = new ArrayDeque<>();
    }

    @Override
    public void onPrimitiveBegin(final Primitive item) {
      if (!item.isOrRule()) {
        final PrimitiveAND primitive = (PrimitiveAND) item;
        this.primitives.push(primitive);

        final boolean isMemoryReference = primitive.getReturnExpr() != null ?
            isMemoryReference(primitive.getReturnExpr()) : false;

        primitive.getInfo().setMemoryReference(isMemoryReference);
      }
    }

    @Override
    public void onPrimitiveEnd(final Primitive item) {
      if (!item.isOrRule()) {
        this.primitives.pop();
      }
    }
  }

  private final Ir ir;

  public MemoryAccessDetector2(final Ir ir) {
    InvariantChecks.checkNotNull(ir);
    this.ir = ir;
  }

  public void start() {
    final IrWalkerFlow walker = new IrWalkerFlow(ir, new Visitor());
    walker.visit();
  }

  private static boolean isMemoryReference(final Expr expr) {
    InvariantChecks.checkNotNull(expr);

    final NodeInfo nodeInfo = expr.getNodeInfo();
    if (!nodeInfo.isLocation()) {
      return false;
    }

    return isMemoryReference((Location) nodeInfo.getSource()); 
  }

  private static boolean isMemoryReference(final Location location) {
    if (location instanceof LocationAtom) {
      return isMemoryReference((LocationAtom) location);
    } else {
      return isMemoryReference((LocationConcat) location);
    }
  }

  private static boolean isMemoryReference(final LocationAtom locationAtom) {
    if ((locationAtom.getSource() instanceof LocationSourceMemory)) {
      final LocationSourceMemory source = (LocationSourceMemory) locationAtom.getSource();
      final BigInteger memorySize = source.getMemory().getSize();

      // MEMs of length 1 are often used as global variables.
      // For this reason, there such MEMs are excluded.
      return source.getMemory().getKind() == Memory.Kind.MEM &&
          memorySize.compareTo(BigInteger.ONE) > 0;
    }

    if ((locationAtom.getSource() instanceof LocationSourcePrimitive)) {
      final LocationSourcePrimitive source =
          (LocationSourcePrimitive) locationAtom.getSource();

      if (source.getPrimitive() instanceof PrimitiveAND) {
        return ((PrimitiveAND) source.getPrimitive()).getInfo().isMemoryReference();
      }
    }

    return false;
  }

  private static boolean isMemoryReference(final LocationConcat locationConcat) { 
    for (final LocationAtom locationAtom : locationConcat.getLocations()) {
      if (isMemoryReference(locationAtom)) {
        return true;
      }
    }
    return false;
  }
}
