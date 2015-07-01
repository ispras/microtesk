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
import ru.ispras.fortress.expression.Node;

public final class AttributeRef {
  private final AbstractStorage target;
  private final Attribute attribute;
  private final Node addressArgValue;

  public AttributeRef(
      final AbstractStorage target,
      final Attribute attribute,
      final Node addressArgValue) {

    checkNotNull(target);
    checkNotNull(attribute);
    checkNotNull(addressArgValue);

    this.target = target;
    this.attribute = attribute;
    this.addressArgValue = addressArgValue;
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
  
  public String getText() {
    return String.format("%s(%s).%s",
        target.getId(), addressArgValue.toString(), attribute.getId());
  }

  @Override
  public String toString() {
    return getText();
  }
}
