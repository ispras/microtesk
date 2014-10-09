/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * PreparatorEngine.java, Oct 8, 2014 4:14:15 PM Andrei Tatarnikov
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

import ru.ispras.microtesk.test.template.Primitive;

public final class PreparatorStore {
  private final Map<String, Preparator> preparators;

  PreparatorStore() {
    this.preparators = new HashMap<String, Preparator>();
  }

  public void addPreparator(Preparator preparator) {
    if (null == preparator) {
      throw new NullPointerException();
    }

    preparators.put(preparator.getTargetName(), preparator);
  }

  public Preparator getPreparator(Primitive targetMode) {
    if (null == targetMode) {
      throw new NullPointerException();
    }

    return preparators.get(targetMode.getName());
  }
}
