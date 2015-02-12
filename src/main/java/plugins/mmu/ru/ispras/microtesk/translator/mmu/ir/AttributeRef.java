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

import ru.ispras.fortress.expression.Node;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

public final class AttributeRef {
  private final AbstractStorage target;
  private final Attribute attribute;
  private final Node addressArgValue;
  private final Node dataArgValue;

  public AttributeRef(
      AbstractStorage target, Attribute attribute, Node addressArgValue, Node dataArgValue) {

    checkNotNull(target);
    checkNotNull(attribute);
    checkNotNull(addressArgValue);
    checkNotNull(dataArgValue);
   
    this.target = target;
    this.attribute = attribute;
    this.addressArgValue = addressArgValue;
    this.dataArgValue = dataArgValue;
  }

  public AbstractStorage getTarget() {
    return target;
  }

  public Attribute getAttribute() {
    return attribute;
  }

  public Node getAddressArgValue() {
    return addressArgValue;
  }

  public Node getDataArgValue() {
    return dataArgValue;
  }
}
