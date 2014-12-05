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

package ru.ispras.microtesk.model.api.memory;

import static ru.ispras.microtesk.utils.InvariantChecks.checkNotNull;
import static ru.ispras.microtesk.utils.InvariantChecks.checkGreaterThanZero;

import java.math.BigInteger;
import java.util.List;

import ru.ispras.microtesk.model.api.type.Type;

public class MemoryManager {
  private final Memory memory;
  private final int align;
  private final int base;

  private int currentAddress;

  public MemoryManager(Memory memory, int align) {
    checkNotNull(memory);
    
    this.memory = memory;
    this.align = align;
    this.base = 0;
    this.currentAddress = base;
  }

  // TODO:
  // - Allocate Data
  // - Allocate String
  // - Allocate Space

  public int allocateData(Type type, List<BigInteger> data) {
    checkNotNull(type);
    checkNotNull(data);

    return 0;
  }

  public int allocateSpace(Type type, BigInteger fillWith, int count) {
    checkNotNull(type);
    checkNotNull(fillWith);
    checkGreaterThanZero(count);

    return 0;
  }

  public int allocateStrings(Type type, List<String> strings) {
    checkNotNull(type);
    checkNotNull(strings);

    return 0;
  }
  
  private void test() {

  }
}
