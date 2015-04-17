/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.template;

import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.template.Primitive;

public final class PreparatorStore {
  private final Map<String, Preparator> preparators;

  public PreparatorStore() {
    this.preparators = new HashMap<String, Preparator>();
  }

  public void addPreparator(Preparator preparator) {
    InvariantChecks.checkNotNull(preparator);
    preparators.put(preparator.getTargetName(), preparator);
  }

  public Preparator getPreparator(Primitive targetMode) {
    InvariantChecks.checkNotNull(targetMode);
    return preparators.get(targetMode.getName());
  }
}
