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

package ru.ispras.microtesk.mmu.test.template;

import java.math.BigInteger;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ru.ispras.fortress.randomizer.Variate;
import ru.ispras.fortress.randomizer.VariateBiased;
import ru.ispras.fortress.randomizer.VariateCollection;
import ru.ispras.fortress.randomizer.VariateComposite;
import ru.ispras.fortress.randomizer.VariateInterval;
import ru.ispras.fortress.randomizer.VariateSingleValue;
import ru.ispras.fortress.util.InvariantChecks;

final class ValueExtractor {
  private final Set<BigInteger> values;

  public ValueExtractor() {
    this.values = new LinkedHashSet<>();
  }

  public Set<BigInteger> getValues() {
    return values;
  }

  public void visit(final Variate<?> variate) {
    InvariantChecks.checkNotNull(variate);

    if (variate instanceof VariateBiased) {
      visit((VariateBiased<?>) variate);
    } else if (variate instanceof VariateCollection) {
      visit((VariateCollection<?>) variate);
    } else if (variate instanceof VariateComposite) {
      visit((VariateComposite<?>) variate);
    } else if (variate instanceof VariateInterval) {
      visit((VariateInterval<?>) variate);
    } else if (variate instanceof VariateSingleValue) {
      visit((VariateSingleValue<?>) variate);
    } else {
      throw new IllegalArgumentException(
          "Unknown Variate subclass: " + variate.getClass().getSimpleName());
    }
  }

  private void visit(final VariateBiased<?> variate) {
    final List<?> values = (List<?>) getField(variate, "values");
    for (final Object value : values) {
      if (value instanceof Variate<?>) {
        visit((Variate<?>) value);
      } else {
        addValue(value);
      }
    }
  }

  private void visit(final VariateCollection<?> variate) {
    final List<?> values = (List<?>) getField(variate, "values");
    for (final Object value : values) {
      if (value instanceof Variate<?>) {
        visit((Variate<?>) value);
      } else {
        addValue(value);
      }
    }
  }

  private void visit(final VariateComposite<?> variate) {
    final Variate<?> composite = (Variate<?>) getField(variate, "composite");
    visit(composite);
  }

  private void visit(final VariateInterval<?> variate) {
    final BigInteger min = toBigInteger(getField(variate, "min"));
    final BigInteger max = toBigInteger(getField(variate, "max"));

    InvariantChecks.checkGreaterOrEq(max, min);

    for (BigInteger value = min;
         value.compareTo(max) <= 0;
         value = value.add(BigInteger.ONE)) {
      this.values.add(value);
    }
  }

  private void visit(final VariateSingleValue<?> variate) {
    final Object value = variate.value();
    addValue(value);
  }

  private void addValue(final Object value) {
    this.values.add(toBigInteger(value));
  }

  private final Object getField(final Object object, final String fieldName) {
    final Class<?> objectClass = object.getClass();

    try {
      final java.lang.reflect.Field field =
          objectClass.getDeclaredField(fieldName);

      field.setAccessible(true);

      final Object fieldValue = field.get(object);
      return fieldValue;
    } catch (final Exception e) {
      throw new IllegalStateException(String.format(
          "Cannot get the value of the %s field of class %s (instance: %s)",
          fieldName, objectClass.getName(), object),
          e
          );
    }
  }

  private BigInteger toBigInteger(final Object value) {
    if (value instanceof BigInteger) {
      return (BigInteger) value;
    }

    if (value instanceof Integer) {
      return BigInteger.valueOf((Integer) value);
    }

    if (value instanceof Long) {
      return BigInteger.valueOf((Long) value);
    }

    throw new ClassCastException(String.format(
        "%s (%s) cannot be cast to BigInteger)",
        value,
        value != null ? value.getClass().getName() : "null"
        ));
  }
}
