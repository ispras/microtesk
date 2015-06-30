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

package ru.ispras.microtesk.test.mmu.sequence.filter;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.microtesk.translator.mmu.coverage.ExecutionPath;
import ru.ispras.microtesk.translator.mmu.coverage.Hazard;
import ru.ispras.microtesk.translator.mmu.ir.spec.MmuDevice;
import ru.ispras.microtesk.utils.function.TriPredicate;

/**
 * Filters off test templates, where {@code TAG_EQUAL} is specified for a non-replaceable device
 * {@code D} and {@code execution1.getEvent(D) != execution2.getEvent(D)}.
 * 
 * <p>NOTE: Such test templates are unsatisfiable.</p>
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class FilterNonReplaceableTagEqual implements TriPredicate<ExecutionPath, ExecutionPath, Hazard> {
  @Override
  public boolean test(final ExecutionPath execution1, final ExecutionPath execution2,
      final Hazard hazard) {

    if (hazard.getType() == Hazard.Type.TAG_EQUAL) {
      final MmuDevice hazardDevice = hazard.getDevice();
      final List<MmuDevice> devices = new ArrayList<>();

      if (hazardDevice != null) {
        devices.add(hazardDevice);
        if (hazardDevice.isView()) {
          devices.add(hazardDevice.getParent());
        }
      }

      for (final MmuDevice device : devices) {
        if (!device.isReplaceable()) {
          if (execution1.getEvent(device) != execution2.getEvent(device)) {
            // Filter off.
            return false;
          }
        }
      }
    }

    return true;
  }
}
