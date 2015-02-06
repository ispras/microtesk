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

package ru.ispras.microtesk.translator.mmu.ir;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import static ru.ispras.fortress.util.InvariantChecks.checkGreaterThanZero;

import java.util.Map;

public final class Memory {
  private final String id;

  private final String addressArgId;
  private final Address addressArgType;

  private final String dataArgId;
  private final int dataArgBitSize;

  private final Map<String, MemoryVar> variables;

  public Memory(
      String id,
      String addressArgId,
      Address addressArgType,
      String dataArgId,
      int dataArgBitSize,
      Map<String, MemoryVar> variables) {

    checkNotNull(id);
    checkNotNull(addressArgId);
    checkNotNull(addressArgType);
    checkNotNull(dataArgId);
    checkGreaterThanZero(dataArgBitSize);
    checkNotNull(variables);

    this.id = id;
    this.addressArgId = addressArgId;
    this.addressArgType = addressArgType;
    this.dataArgId = dataArgId;
    this.dataArgBitSize = dataArgBitSize;
    this.variables = variables;
  }

  public String getId() {
    return id;
  }

  public String getAddressArgId() {
    return addressArgId;
  }

  public Address getAddressArgType() {
    return addressArgType;
  }

  public String getDataArgId() {
    return dataArgId;
  }

  public int getDataArgBitSize() {
    return dataArgBitSize;
  }

  @Override
  public String toString() {
    return String.format("mmu %s(%s: %s(%d))=(%s: %d) [vars=%s]",
        id, addressArgId, addressArgType.getId(), addressArgType.getWidth(),
        dataArgId, dataArgBitSize, variables
        );
  }
}
