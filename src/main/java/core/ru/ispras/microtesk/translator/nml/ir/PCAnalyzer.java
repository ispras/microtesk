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

import ru.ispras.microtesk.translator.nml.NmlSymbolKind;
import ru.ispras.microtesk.translator.nml.ir.location.LocationAtom;
import ru.ispras.microtesk.translator.nml.ir.location.LocationFactory;

public final class PCAnalyzer {
  private final LocationFactory locationFactory;
  private final IrInquirer inquirer;

  private final List<LocationAtom> destLocations;
  private List<LocationAtom> srcLocations;

  public PCAnalyzer(LocationFactory locationFactory, IR ir) {
    this.locationFactory = locationFactory;
    this.inquirer = new IrInquirer(ir);

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
      if (location.getSource().getSymbolKind() == NmlSymbolKind.ARGUMENT) {
        return 1;
      }

      if (location.getSource().getSymbolKind() == NmlSymbolKind.MEMORY &&
          !inquirer.isPC(location)) {
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
      if (inquirer.isPC(location)) {
        return true;
      }
    }

    return false;
  }
}
