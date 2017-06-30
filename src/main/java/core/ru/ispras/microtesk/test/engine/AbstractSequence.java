/*
 * Copyright 2014-2017 ISP RAS (http://www.ispras.ru)
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

import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.memory.Section;
import ru.ispras.microtesk.test.template.AbstractCall;

public final class AbstractSequence {
  private final Section section;
  private final List<AbstractCall> sequence;

  public AbstractSequence(final Section section, final List<AbstractCall> sequence) {
    InvariantChecks.checkNotNull(section);
    InvariantChecks.checkNotNull(sequence);

    this.section = section;
    this.sequence = sequence;
  }

  public Section getSection() {
    return section;
  }

  public boolean isEmpty() {
    return sequence.isEmpty();
  }

  public List<AbstractCall> getSequence() {
    return sequence;
  }

  @Override
  public String toString() {
    return sequence.toString();
  }
}
