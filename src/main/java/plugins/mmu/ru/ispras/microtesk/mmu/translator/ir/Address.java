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

package ru.ispras.microtesk.mmu.translator.ir;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import static ru.ispras.fortress.util.InvariantChecks.checkTrue;

import ru.ispras.fortress.data.DataType;

import java.util.Collections;
import java.util.List;

public final class Address {
  private final String id;
  private final Type contentType;
  private final Type addressType;
  private final List<String> accessChain;

  public Address(final String id, final int bitSize) {
    checkNotNull(id);
    checkTrue(bitSize > 0);

    this.id = id;
    this.contentType = new Type(bitSize);
    this.addressType = contentType;
    this.accessChain = Collections.emptyList();
  }

  public Address(final String id, final Type type, final List<String> accessChain) {
    checkNotNull(id);
    checkNotNull(type);
    checkNotNull(accessChain);

    this.id = id;
    this.contentType = type;
    this.addressType = type.accessNested(accessChain);
    this.accessChain = accessChain;
  }

  public String getId() {
    return id;
  }

  public Type getContentType() {
    return contentType;
  }

  public Type getAddressType() {
    return addressType;
  }

  public List<String> getAccessChain() {
    return accessChain;
  }

  @Override
  public String toString() {
    return String.format("address %s[%s]", id, contentType);
  }
}
