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

import ru.ispras.microtesk.model.memory.MemoryAllocator;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.test.template.LabelValue;

import java.math.BigInteger;

public final class DirectiveLabelWeak extends DirectiveLabel {
  DirectiveLabelWeak(final Options options, final LabelValue label) {
    super(options, label);
  }

  @Override
  public String getText() {
    return String.format(options.getValueAsString(Option.WEAK_FORMAT),
        label.getLabel().getUniqueName());
  }

  @Override
  public BigInteger apply(final BigInteger currentAddress, final MemoryAllocator allocator) {
    return currentAddress;
  }

  @Override
  public Directive copy() {
    return new DirectiveLabelWeak(options, label.newCopy());
  }
}
