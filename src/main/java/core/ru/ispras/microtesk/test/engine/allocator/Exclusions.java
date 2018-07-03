/*
 * Copyright 2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.engine.allocator;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.Primitive;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * The {@link Exclusions} class stores indices of currently excluded registers.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class Exclusions {
  private final Map<String, Set<Integer>> excluded = new HashMap<>();

  public void setExcluded(final Primitive primitive, final boolean value) {
    InvariantChecks.checkNotNull(primitive);
    InvariantChecks.checkTrue(AllocatorUtils.isAddressingMode(primitive));

    if (value) {
      exclude(primitive);
    } else {
      include(primitive);
    }
  }

  private void exclude(final Primitive primitive) {
    final String name = primitive.getName();
    final Set<Integer> excludedValues;

    if (excluded.containsKey(name)) {
      excludedValues = excluded.get(name);
    } else {
      excludedValues = new LinkedHashSet<>();
      excluded.put(name, excludedValues);
    }

    InvariantChecks.checkTrue(primitive.getArguments().size() == 1);
    for (final Argument arg : primitive.getArguments().values()) {
      final BigInteger value = arg.getImmediateValue();
      excludedValues.add(value.intValue());
    }
  }

  private void include(final Primitive primitive) {
    final String name = primitive.getName();
    final Set<Integer> excludedValues = excluded.get(name);

    if (null != excludedValues) {
      InvariantChecks.checkTrue(primitive.getArguments().size() == 1);
      for (final Argument arg : primitive.getArguments().values()) {
        final BigInteger value = arg.getImmediateValue();
        excludedValues.remove(value.intValue());
      }
    }
  }

  public Set<Integer> getExcludedIndexes(final String name) {
    return excluded.containsKey(name) ? excluded.get(name) : Collections.<Integer>emptySet();
  }
}
