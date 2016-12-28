/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.engine;

import java.util.Iterator;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.template.ConcreteCall;

public final class CodeIterator implements Iterator<ConcreteCall> {
  private final List<ConcreteCall> calls;
  private int index;

  public CodeIterator(final List<ConcreteCall> calls, final int startIndex) {
    InvariantChecks.checkNotEmpty(calls);
    InvariantChecks.checkBounds(startIndex, calls.size());

    this.calls = calls;
    this.index = startIndex;
  }

  @Override
  public boolean hasNext() {
    return index < calls.size();
  }

  @Override
  public ConcreteCall next() {
    return calls.get(index++);
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
