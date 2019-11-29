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
import ru.ispras.microtesk.model.memory.Section;
import ru.ispras.microtesk.test.template.LabelValue;

import java.math.BigInteger;

public final class DirectiveLabel extends Directive {
  private final Section section;
  private final LabelValue label;

  DirectiveLabel(final Section section, final LabelValue label) {
    InvariantChecks.checkNotNull(section);
    InvariantChecks.checkNotNull(label);
    InvariantChecks.checkNotNull(label.getLabel());

    this.section = section;
    this.label = label;
  }

  @Override
  public String getText() {
    return label.getLabel().getUniqueName() + ":";
  }

  @Override
  public BigInteger apply(final BigInteger currentAddress, final MemoryAllocator allocator) {
    final BigInteger virtualAddress = section.physicalToVirtual(currentAddress);
    label.setAddress(virtualAddress);

    return currentAddress;
  }

  @Override
  public Directive copy() {
    return new DirectiveLabel(section, label.sharedCopy());
  }

  @Override
  public String toString() {
    return String.format("%s (%s)", getText(), label);
  }
}
