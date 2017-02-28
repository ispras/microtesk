/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory;

import java.util.ArrayList;
import java.util.Collection;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link BufferDependency} describes a dependency, i.e. a number of buffer access hazards.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class BufferDependency {
  private final Collection<BufferHazard.Instance> hazards = new ArrayList<>();

  public BufferDependency() {}

  public BufferDependency(final BufferDependency dependency) {
    InvariantChecks.checkNotNull(dependency);
    hazards.addAll(dependency.hazards);
  }

  public Collection<BufferHazard.Instance> getHazards() {
    return hazards;
  }

  public void addHazard(final BufferHazard.Instance hazard) {
    InvariantChecks.checkNotNull(hazard);
    hazards.add(hazard);
  }

  @Override
  public String toString() {
    final String separator = ", ";
    final StringBuilder builder = new StringBuilder();

    boolean comma = false;

    builder.append("{");
    for (final BufferHazard.Instance hazard : hazards) {
      builder.append(comma ? separator : "");
      builder.append(hazard);
    }
    builder.append("}");

    return builder.toString();
  }
}
