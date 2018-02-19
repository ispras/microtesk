/*
 * Copyright 2016-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.options;

import ru.ispras.fortress.util.InvariantChecks;

import java.math.BigInteger;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The {@link Options} stores options.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Options {
  private final Map<Option, Object> options;

  public Options() {
    this.options = new EnumMap<>(Option.class);
  }

  public void setValue(final Option option, final Object value) {
    InvariantChecks.checkNotNull(option);
    InvariantChecks.checkNotNull(value);

    if (option.getValueClass().isAssignableFrom(value.getClass())) {
      options.put(option, value);
      return;
    }

    if (option.getValueClass().equals(BigInteger.class) && value instanceof Number) {
      options.put(option, BigInteger.valueOf(((Number) value).longValue()));
      return;
    }

    if (option.getValueClass().equals(Integer.class) && value instanceof Long) {
      options.put(option, Integer.valueOf(((Long) value).intValue()));
      return;
    }

    throw new IllegalArgumentException(String.format(
        "Illegal value type: %s, expected: %s",
        value.getClass().getSimpleName(),
        option.getValueClass().getSimpleName()
        ));
  }

  public void setValue(final String optionName, final Object value) {
    InvariantChecks.checkNotNull(optionName);
    final Option option = Option.fromName(optionName);
    setValue(option, value);
  }

  public boolean hasValue(final Option option) {
    InvariantChecks.checkNotNull(option);
    return options.containsKey(option);
  }

  public boolean hasValue(final String optionName) {
    InvariantChecks.checkNotNull(optionName);
    final Option option = Option.fromName(optionName);
    return hasValue(option);
  }

  public Object getValue(final Option option) {
    InvariantChecks.checkNotNull(option);
    final Object value = options.get(option);
    return null != value ? value : option.getDefaultValue();
  }

  public Object getValue(final String optionName) {
    InvariantChecks.checkNotNull(optionName);
    final Option option = Option.fromName(optionName);
    return getValue(option);
  }

  public String getValueAsString(final Option option) {
    return getValue(option).toString();
  }

  public String getValueAsString(final String optionName) {
    return getValue(optionName).toString();
  }

  public int getValueAsInteger(final Option option) {
    final Object value = getValue(option);
    InvariantChecks.checkTrue(value instanceof Integer);
    return (Integer) value;
  }

  public int getValueAsInteger(final String optionName) {
    final Object value = getValue(optionName);
    InvariantChecks.checkTrue(value instanceof Integer);
    return (Integer) value;
  }

  public long getValueAsLong(final Option option) {
    final Object value = getValue(option);
    InvariantChecks.checkTrue(value instanceof Long);
    return (Long) value;
  }

  public long getValueAsLong(final String optionName) {
    final Object value = getValue(optionName);
    InvariantChecks.checkTrue(value instanceof Long);
    return (Long) value;
  }

  public BigInteger getValueAsBigInteger(final Option option) {
    final Object value = getValue(option);
    InvariantChecks.checkTrue(value instanceof BigInteger);
    return (BigInteger) value;
  }

  public BigInteger getValueAsBigInteger(final String optionName) {
    final Object value = getValue(optionName);
    InvariantChecks.checkTrue(value instanceof BigInteger);
    return (BigInteger) value;
  }

  public boolean getValueAsBoolean(final Option option) {
    final Object value = getValue(option);
    InvariantChecks.checkTrue(value instanceof Boolean);
    return (Boolean) value;
  }

  public boolean getValueAsBoolean(final String optionName) {
    final Object value = getValue(optionName);
    InvariantChecks.checkTrue(value instanceof Boolean);
    return (Boolean) value;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Options:");
    for (final Entry<Option, Object> entry : options.entrySet()) {
      sb.append(System.lineSeparator());
      sb.append("    ");
      sb.append(entry.getKey());
      sb.append("=");
      sb.append(entry.getValue());
    }
    return sb.toString();
  }
}
