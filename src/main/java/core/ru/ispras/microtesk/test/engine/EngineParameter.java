/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.engine;

import ru.ispras.fortress.util.InvariantChecks;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link EngineParameter} defines an interface of engine parameters.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class EngineParameter<T> {
  public static final class Option<T> {
    private final String name;
    private final T value;

    public Option(final String name, final T value) {
      InvariantChecks.checkNotNull(name);
      InvariantChecks.checkNotNull(value);

      this.name = name;
      this.value = value;
    }

    public String getName() {
      return name;
    }

    public T getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.format("%s=%s", name, value);
    }
  }

  private final String name;
  private final Map<Object, T> options;
  private final T defaultOption;

  public EngineParameter(final String name) {
    InvariantChecks.checkNotNull(name);

    this.name = name;
    this.options = null;
    this.defaultOption = null;
  }

  public EngineParameter(final String name, final T defaultOption) {
    InvariantChecks.checkNotNull(name);

    this.name = name;
    this.options = null;
    this.defaultOption = defaultOption;
  }

  @SafeVarargs
  public EngineParameter(final String name, final Option<T> ...options) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkTrue(options.length != 0);

    this.name = name;

    this.options = new HashMap<>();
    for (final Option<T> option : options) {
      this.options.put(option.getName().toLowerCase(), option.getValue());
    }

    this.defaultOption = options[0].getValue();
  }
  
  public final String getName() {
    return name;
  }

  public T getValue(final Object option) {
    InvariantChecks.checkNotNull(options);
    return options.get(option);
  }

  public T getDefaultValue() {
    return defaultOption;
  }

  public final T parse(final Object option) {
    if (option == null) {
      return getDefaultValue();
    }

    final T value = getValue(option);

    if (value != null) {
      return value;
    }

    return getDefaultValue();
  }

  @Override
  public String toString() {
    return name;
  }
}
