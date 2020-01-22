/*
 * Copyright 2018-2020 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.utils;

import ru.ispras.fortress.util.InvariantChecks;

import java.math.BigInteger;
import java.util.EnumMap;
import java.util.Map;

/**
 * The {@link PropertyMap} class is a map that stores values identified by a enumeration.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 *
 * @param <T> Type of the enumeration used to identify the stored properties.
 */
public class PropertyMap<T extends Enum<T> & Property> {
  private final EnumMap<T, Object> properties;

  public PropertyMap(final Class<T> tClass) {
    InvariantChecks.checkNotNull(tClass);
    this.properties = new EnumMap<>(tClass);
  }

  public final void setValue(final T property, final Object value) {
    InvariantChecks.checkNotNull(property);
    InvariantChecks.checkNotNull(value);

    if (property.getValueClass().isAssignableFrom(value.getClass())) {
      properties.put(property, value);
      return;
    }

    if (property.getValueClass().equals(BigInteger.class) && value instanceof Number) {
      properties.put(property, BigInteger.valueOf(((Number) value).longValue()));
      return;
    }

    if (property.getValueClass().equals(Integer.class) && value instanceof Long) {
      properties.put(property, ((Long) value).intValue());
      return;
    }

    throw new IllegalArgumentException(String.format(
        "Illegal value type: %s, expected: %s",
        value.getClass().getSimpleName(),
        property.getValueClass().getSimpleName()
    ));
  }

  public final boolean hasValue(final T property) {
    InvariantChecks.checkNotNull(property);
    return properties.containsKey(property);
  }

  public final Object getValue(final T property) {
    InvariantChecks.checkNotNull(property);
    final Object value = properties.get(property);
    return null != value ? value : property.getDefaultValue();
  }

  public final String getValueAsString(final T property) {
    return getValue(property).toString();
  }

  public final int getValueAsInteger(final T property) {
    final Object value = getValue(property);
    InvariantChecks.checkTrue(value instanceof Integer);
    return (Integer) value;
  }

  public final long getValueAsLong(final T property) {
    final Object value = getValue(property);
    InvariantChecks.checkTrue(value instanceof Long);
    return (Long) value;
  }

  public final BigInteger getValueAsBigInteger(final T property) {
    final Object value = getValue(property);
    InvariantChecks.checkTrue(value instanceof BigInteger);
    return (BigInteger) value;
  }

  public final boolean getValueAsBoolean(final T property) {
    final Object value = getValue(property);
    InvariantChecks.checkTrue(value instanceof Boolean);
    return (Boolean) value;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Properties:");
    for (final Map.Entry<?, Object> entry : properties.entrySet()) {
      sb.append(System.lineSeparator());
      sb.append("    ");
      sb.append(entry.getKey());
      sb.append("=");
      sb.append(entry.getValue());
    }
    return sb.toString();
  }
}
