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
import ru.ispras.microtesk.test.template.UnknownImmediateValue;
import ru.ispras.microtesk.test.template.Value;
import ru.ispras.microtesk.utils.SharedObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

final class AllocatorUtils {
  private AllocatorUtils() {}

  public static List<Value> copyValues(final List<Value> values) {
    if (null == values) {
      return null;
    }

    if (values.isEmpty()) {
      return Collections.emptyList();
    }

    final List<Value> result = new ArrayList<>(values.size());
    for (final Value value : values) {
      if (value instanceof SharedObject) {
        result.add((Value)((SharedObject<?>) value).getCopy());
      } else {
        result.add(value);
      }
    }

    return result;
  }

  public static Set<Integer> toValueSet(final List<Value> values) {
    InvariantChecks.checkNotNull(values);

    if (values.isEmpty()) {
      return Collections.emptySet();
    }

    final Set<Integer> result = new LinkedHashSet<>();
    for (final Value value : values) {
      result.add(value.getValue().intValue());
    }

    return result;
  }

  public static boolean isAddressingMode(final Primitive primitive) {
    return Primitive.Kind.MODE == primitive.getKind();
  }

  public static boolean isPrimitive(final Argument argument) {
    return argument.getValue() instanceof Primitive;
  }

  public static boolean isFixedValue(final Argument argument) {
    if (!argument.isImmediate()) {
      return false;
    }

    if (argument.getValue() instanceof UnknownImmediateValue) {
      return ((UnknownImmediateValue) argument.getValue()).isValueSet();
    }

    return true;
  }

  public static boolean isUnknownValue(final Argument argument) {
    if (!argument.isImmediate()) {
      return false;
    }

    if (argument.getValue() instanceof UnknownImmediateValue) {
      return !((UnknownImmediateValue) argument.getValue()).isValueSet();
    }

    return false;
  }
}
