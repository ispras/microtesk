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

package ru.ispras.microtesk.mmu.test.sequence.filter;

import ru.ispras.microtesk.mmu.translator.coverage.ExecutionPath;
import ru.ispras.microtesk.mmu.translator.coverage.Hazard;
import ru.ispras.microtesk.mmu.translator.coverage.UnitedHazard;
import ru.ispras.microtesk.utils.function.BiPredicate;

/**
 * Filters off test templates with multiple {@code TAG_REPLACED} hazards over the same device for
 * a single execution.
 * 
 * <p>NOTE: Such constraints do not have a well-defined semantics.</p>
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class FilterMultipleTagReplaced implements BiPredicate<ExecutionPath, UnitedHazard> {
  @Override
  public boolean test(final ExecutionPath execution, final UnitedHazard hazard) {
    if (hazard.getRelation(Hazard.Type.TAG_REPLACED).size() > 1) {
      // Filter off.
      return false;
    }

    return true;
  }
}
