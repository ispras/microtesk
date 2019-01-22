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
import ru.ispras.microtesk.test.template.FixedValue;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.UnknownImmediateValue;
import ru.ispras.microtesk.test.template.Value;
import ru.ispras.microtesk.utils.SharedObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

final class AllocatorUtils {
  private AllocatorUtils() {}

  public static Collection<Value> copyValues(final Collection<Value> values) {
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

  public static Collection<Value> toValues(final Collection<Integer> values) {
    InvariantChecks.checkNotNull(values);

    if (values.isEmpty()) {
      return Collections.emptySet();
    }

    final Collection<Value> result = new LinkedHashSet<>();
    for (final Integer value : values) {
      result.add(new FixedValue(BigInteger.valueOf(value)));
    }

    return result;
  }

  public static Collection<Integer> toIntegers(final Collection<Value> values) {
    InvariantChecks.checkNotNull(values);

    if (values.isEmpty()) {
      return Collections.emptySet();
    }

    final Collection<Integer> result = new LinkedHashSet<>();
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
