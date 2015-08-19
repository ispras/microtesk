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

import java.util.List;

abstract class Nested<T extends Nested<T>> {
  protected abstract T getNested(final String name);

  @SuppressWarnings("unchecked")
  public T searchNested(final List<String> accessChain) {
    checkNotNull(accessChain);

    T container = (T) this;
    for (final String name : accessChain) {
      container = container.getNested(name);
      if (container == null) {
        break;
      }
    }
    return container;
  }

  public T accessNested(final List<String> accessChain) {
    final T nested = searchNested(accessChain);
    checkNotNull(nested);

    return nested;
  }
}
