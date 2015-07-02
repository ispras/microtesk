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
import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.translator.nml.NmlSymbolKind;
import ru.ispras.microtesk.translator.nml.ir.expression.Expr;
import ru.ispras.microtesk.translator.nml.ir.location.LocationAtom;
import ru.ispras.microtesk.translator.nml.ir.shared.LetLabel;
import ru.ispras.microtesk.translator.nml.ir.shared.MemoryExpr;

public final class IrInquirer {
  private static final String PC_LABEL = "PC";

  private final Ir ir;

  public IrInquirer(final Ir ir) {
    checkNotNull(ir);

    this.ir = ir;
  }

  public boolean isPC(final LocationAtom location) {
    checkNotNull(location);

    return isRegister(location) && (isExplicitPC(location) || isLabelledAsPC(location));
  }

  public static boolean isRegister(final LocationAtom location) {
    checkNotNull(location);

    final MemoryExpr memory = getMemoryExpr(location);
    return memory != null && memory.getKind() == Memory.Kind.REG;
  }

  public static boolean isMemory(final LocationAtom location) {
    checkNotNull(location);

    final MemoryExpr memory = getMemoryExpr(location);
    return memory != null && memory.getKind() == Memory.Kind.MEM;
  }

  private static MemoryExpr getMemoryExpr(final LocationAtom location) {
    if (location.getSource().getSymbolKind() != NmlSymbolKind.MEMORY) {
      return null;
    }
    return ((LocationAtom.MemorySource) location.getSource()).getMemory();
  }

  private static boolean isExplicitPC(LocationAtom location) {
    return location.getName().equals(PC_LABEL);
  }

  private boolean isLabelledAsPC(LocationAtom location) {
    if (!ir.getLabels().containsKey(PC_LABEL)) {
      return false;
    }

    final LetLabel label = ir.getLabels().get(PC_LABEL);
    if (!label.getMemoryName().equals(location.getName())) {
      return false;
    }

    final int locationIndex;

    final Expr indexExpr = location.getIndex();
    if (null != indexExpr) {
      if (!indexExpr.getValueInfo().isConstant()) {
        return false;
      }
      locationIndex = indexExpr.integerValue();
    } else {
      locationIndex = 0;
    }

    return label.getIndex() == locationIndex;
  }
}
