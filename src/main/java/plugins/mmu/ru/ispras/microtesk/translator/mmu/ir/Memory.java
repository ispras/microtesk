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

  private final Var addressArg;
  private final Var dataArg;

  private final Map<String, Var> variables;
  private final Map<String, Attribute> attributes; 

  public Memory(
      String id,
      String addressArgId,
      Address addressArgType,
      String dataArgId,
      int dataArgBitSize,
      Map<String, Var> variables,
      Map<String, Attribute> attributes) {

    checkNotNull(id);
    checkNotNull(addressArgId);
    checkNotNull(addressArgType);
    checkNotNull(dataArgId);
    checkGreaterThanZero(dataArgBitSize);
    checkNotNull(variables);

    this.id = id;
    this.addressArg = new Var(addressArgId, addressArgType.getType());
    this.dataArg = new Var(dataArgId, new Type(dataArgBitSize));
    this.variables = variables;
    this.attributes = attributes;
  }

  public String getId() {
    return id;
  }

  public Var getAddressArg() {
    return addressArg;
  }

  public Var getDataArg() {
    return dataArg;
  }

  @Override
  public String toString() {
    return String.format("mmu %s(%s)=(%s) [vars=%s, attributes=%s]",
        id, addressArg, dataArg, variables, attributes);
  }
}
