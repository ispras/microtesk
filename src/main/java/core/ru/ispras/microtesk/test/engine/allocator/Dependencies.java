/*
 * Copyright 2018-2021 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.microtesk.test.template.AbstractCall;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.Primitive;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link Dependencies} tracks read-after-write (RAW) dependencies between instructions.
 *
 * <p>
 * Such tracking is needed to handle situations when an instruction writes a value to be read by
 * subsequent instructions. Those values must not be modified between the write and the reads.
 * That is the corresponding registers must not be selected as output arguments until their values
 * are read by all of the dependent instructions.
 * </p>
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class Dependencies {
  private final Map<Primitive, Integer> dependencies;

  public Dependencies() {
    this.dependencies = new IdentityHashMap<>();
  }

  public void reset() {
    dependencies.clear();
  }

  public void init(final List<AbstractCall> sequence) {
    InvariantChecks.checkNotNull(sequence);

    final Map<Primitive, Integer> referenceMap = new IdentityHashMap<>();
    for (final AbstractCall call : sequence) {
      if (call.isExecutable()) {
        final Primitive primitive = call.getRootOperation();
        countReferences(referenceMap, primitive, false);
      } else if (call.isPreparatorCall()) {
        final Primitive primitive = call.getPreparatorReference().getTarget();
        countReferences(referenceMap, primitive, true);
      }
    }

    reset();
    for (final Map.Entry<Primitive, Integer> entry : referenceMap.entrySet()) {
      if (entry.getValue() > 0) {
        dependencies.put(entry.getKey(), entry.getValue());
      }
    }
  }

  private static void countReferences(
      final Map<Primitive, Integer> referenceMap,
      final Primitive primitive,
      final boolean isWrite) {
    if (AllocatorUtils.isAddressingMode(primitive)) {
      if (referenceMap.containsKey(primitive)) {
        final int count = referenceMap.get(primitive);
        referenceMap.put(primitive, count + 1);
      } else if (isWrite) {
        referenceMap.put(primitive, 0);
      }
    } else {
      for (final Argument arg : primitive.getArguments().values()) {
        if (AllocatorUtils.isPrimitive(arg)) {
          countReferences(referenceMap, (Primitive) arg.getValue(), arg.getMode().isOut());
        }
      }
    }
  }

  public boolean contains(final Primitive primitive) {
    return dependencies.containsKey(primitive);
  }

  public int getReferenceCount(final Primitive primitive) {
    final Integer count = dependencies.get(primitive);
    InvariantChecks.checkNotNull(count);
    return count;
  }

  public void release(final Primitive primitive) {
    final Integer count = dependencies.get(primitive);
    InvariantChecks.checkNotNull(count);

    if (0 == count) {
      dependencies.remove(primitive);
    } else {
      dependencies.put(primitive, count - 1);
    }
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();

    for (final Map.Entry<Primitive, Integer> entry : dependencies.entrySet()) {
      builder.append(entry.getValue());
      builder.append(": ");
      builder.append(entry.getKey());
      builder.append(": ");
      builder.append(entry.getKey().getArguments());
    }

    return builder.toString();
  }
}
