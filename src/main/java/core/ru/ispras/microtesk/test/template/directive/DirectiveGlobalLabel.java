/*
 * Copyright 2016-2019 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.template.directive;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.memory.MemoryAllocator;
import ru.ispras.microtesk.test.template.LabelValue;

import java.math.BigInteger;

public final class DirectiveGlobalLabel implements Directive {
  private final LabelValue label;

  DirectiveGlobalLabel(final LabelValue label) {
    InvariantChecks.checkNotNull(label);
    InvariantChecks.checkNotNull(label.getLabel());

    this.label = label;
  }

  @Override
  public String getText() {
    return String.format(".globl %s", label.getLabel().getUniqueName());
  }

  @Override
  public boolean needsIndent() {
    return true;
  }

  @Override
  public BigInteger apply(final BigInteger currentAddress, final MemoryAllocator allocator) {
    return currentAddress;
  }

  @Override
  public Directive copy() {
    return new DirectiveGlobalLabel(label.sharedCopy());
  }

  @Override
  public String toString() {
    return getText();
  }
}
