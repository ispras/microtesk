/*
 * Copyright 2013-2014 ISP RAS (http://www.ispras.ru)
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

import java.util.ArrayList;
import java.util.List;

import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.translator.nml.ESymbolKind;
import ru.ispras.microtesk.translator.nml.ir.expression.Expr;
import ru.ispras.microtesk.translator.nml.ir.location.LocationFactory;
import ru.ispras.microtesk.translator.nml.ir.location.LocationAtom;
import ru.ispras.microtesk.translator.nml.ir.shared.LetLabel;
import ru.ispras.microtesk.translator.nml.ir.shared.MemoryExpr;

public final class PCAnalyzer {
  private final LocationFactory locationFactory;
  private final IR ir;

  private final List<LocationAtom> destLocations;
  private List<LocationAtom> srcLocations;

  public PCAnalyzer(LocationFactory locationFactory, IR ir) {
    this.locationFactory = locationFactory;
    this.ir = ir;

    this.destLocations = new ArrayList<LocationAtom>();
    this.locationFactory.setLog(destLocations);

    this.srcLocations = null;
  }

  public void startTrackingSource() {
    if (!isPCAssignment()) {
      return;
    }

    srcLocations = new ArrayList<LocationAtom>();
    locationFactory.setLog(srcLocations);
  }

  public int getControlTransferIndex() {
    if (null == srcLocations) {
      return -1;
    }

    for (LocationAtom location : srcLocations) {
      if (location.getSource().getSymbolKind() == ESymbolKind.ARGUMENT) {
        return 1;
      }

      if (location.getSource().getSymbolKind() == ESymbolKind.MEMORY && !isPC(location)) {
        return 1;
      }
    }

    return 0;
  }

  public void finalize() {
    locationFactory.resetLog();
  }

  private boolean isPCAssignment() {
    for (LocationAtom location : destLocations) {
      if (isPC(location)) {
        return true;
      }
    }

    return false;
  }

  private boolean isPC(LocationAtom location) {
    if (null == location) {
      throw new NullPointerException(); 
    }

    if (!isRegisterLocation(location)) {
      return false;
    }

    if (isExplicitPCAccess(location)) {
      return true;
    }

    return isLabelledAsPC(location);
  }

  private boolean isRegisterLocation(LocationAtom location) {
    if (location.getSource().getSymbolKind() != ESymbolKind.MEMORY) {
      return false;
    }

    assert location.getSource() instanceof LocationAtom.MemorySource;

    final MemoryExpr memory = ((LocationAtom.MemorySource) location.getSource()).getMemory();
    if (memory.getKind() != Memory.Kind.REG) {
      return false;
    }

    return true;
  }

  private boolean isExplicitPCAccess(LocationAtom location) {
    return location.getName().equals("PC");
  }

  private boolean isLabelledAsPC(LocationAtom location) {
    if (!ir.getLabels().containsKey("PC")) {
      return false;
    }

    final LetLabel label = ir.getLabels().get("PC");
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
