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

package ru.ispras.microtesk.test.sequence;

import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.sequence.rearranger.Rearranger;

public final class GeneratorRearranger<T> implements Generator<T> {
  private final Rearranger<T> rearranger;

  public GeneratorRearranger(final Generator<T> original, final Rearranger<T> rearranger) {
    InvariantChecks.checkNotNull(original);
    InvariantChecks.checkNotNull(rearranger);

    this.rearranger = rearranger;
    this.rearranger.initialize(original);
  }

  @Override
  public void init() {
    rearranger.init();
  }

  @Override
  public boolean hasValue() {
    return rearranger.hasValue();
  }

  @Override
  public List<T> value() {
    return rearranger.value();
  }

  @Override
  public void next() {
    rearranger.next();
  }

  @Override
  public void stop() {
    rearranger.stop();
  }

  @Override
  public Generator<T> clone() {
    throw new UnsupportedOperationException();
  }
}
