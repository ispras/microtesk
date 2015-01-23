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

public final class Memory {
  private final String name;
  private final Address addressType;
  private final String addressName;

  Memory(String name, Address addressType, String addressName) {
    checkNotNull(name);
    checkNotNull(addressType);
    checkNotNull(addressName);

    this.name = name;
    this.addressType = addressType;
    this.addressName = addressName;
  }

  public String getName() {
    return name;
  }

  public Address getAddressType() {
    return addressType;
  }

  public String getAddressName() {
    return addressName;
  }
}
