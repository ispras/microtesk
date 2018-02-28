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
import ru.ispras.microtesk.utils.PropertyMap;

import java.math.BigInteger;

/**
 * The {@link Options} stores options.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Options extends PropertyMap<Option> {
  public Options() {
    super(Option.class);
  }

  public void setValue(final String optionName, final Object value) {
    setValue(optionFromName(optionName), value);
  }

  public boolean hasValue(final String optionName) {
    return hasValue(optionFromName(optionName));
  }

  public Object getValue(final String optionName) {
    return getValue(optionFromName(optionName));
  }

  public String getValueAsString(final String optionName) {
    return getValueAsString(optionFromName(optionName));
  }

  public int getValueAsInteger(final String optionName) {
    return getValueAsInteger(optionFromName(optionName));
  }

  public long getValueAsLong(final String optionName) {
    return getValueAsLong(optionFromName(optionName));
  }

  public BigInteger getValueAsBigInteger(final String optionName) {
    return getValueAsBigInteger(optionFromName(optionName));
  }

  public boolean getValueAsBoolean(final String optionName) {
    return getValueAsBoolean(optionFromName(optionName));
  }

  private static Option optionFromName(final String optionName) {
    InvariantChecks.checkNotNull(optionName);
    return Option.fromName(optionName);
  }
}
