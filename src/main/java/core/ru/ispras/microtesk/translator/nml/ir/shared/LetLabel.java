/*
 * Copyright 2013-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.ir.shared;

import static ru.ispras.fortress.util.InvariantChecks.checkGreaterOrEqZero;
import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

public final class LetLabel {
  private final String name;
  private final String memoryName;
  private final int index;

  LetLabel(String name, String memoryName) {
    this(name, memoryName, 0);
  }

  LetLabel(String name, String memoryName, int index) {
    checkNotNull(name);
    checkNotNull(memoryName);
    checkGreaterOrEqZero(index);

    this.name = name;
    this.memoryName = memoryName;
    this.index = index;
  }

  public String getName() {
    return name;
  }

  public String getMemoryName() {
    return memoryName;
  }

  public int getIndex() {
    return index;
  }

  @Override
  public String toString() {
    return String.format("LetLabel [name=%s, memoryName=%s, index=%d]", name, memoryName, index);
  }
}
