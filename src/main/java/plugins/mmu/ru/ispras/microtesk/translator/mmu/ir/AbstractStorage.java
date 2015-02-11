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

import java.util.Collection;
import java.util.Map;

public abstract class AbstractStorage {
  private final String id;
  private final Var addressArg;
  private final Var dataArg;
  private final Map<String, Attribute> attributes;

  protected AbstractStorage(String id, Var addressArg, Var dataArg, Map<String, Attribute> attributes) {
    checkNotNull(id);
    checkNotNull(addressArg);
    checkNotNull(attributes);

    this.id = id;
    this.addressArg = addressArg;
    this.dataArg = dataArg;
    this.attributes = attributes;
  }

  public final String getId() {
    return id;
  }

  public final Var getAddressArg() {
    return addressArg;
  }

  public final Var getDataArg() {
    return dataArg;
  }

  public int getAttributeCount() {
    return attributes.size();
  }

  public Collection<Attribute> getAttributes() {
    return attributes.values();
  }

  public Attribute getAttribute(String attrId) {
    checkNotNull(attrId);
    return attributes.get(attrId);
  }
}
