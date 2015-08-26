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

package ru.ispras.microtesk.mmu.settings;

import java.util.HashSet;
import java.util.Set;

import ru.ispras.microtesk.settings.AbstractSettings;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BooleanValuesSettings extends AbstractSettings {
  public static final String TAG = String.format("%s-booleanValues", MmuSettings.TAG_PREFIX);

  public static enum Values {
    TRUE,
    FALSE,
    ALL,
    NONE
  }

  private final String name;
  private final Set<Boolean> values = new HashSet<>();

  public BooleanValuesSettings(final String name, final Values values) {
    super(TAG);

    this.name = name;

    switch (values) {
      case TRUE:
        this.values.add(true);
        break;
      case FALSE:
        this.values.add(false);
        break;
      case ALL:
        this.values.add(true);
        this.values.add(false);
        break;
      default:
        break;
    }
  }

  @Override
  public final String getName() {
    return name;
  }

  public final Set<Boolean> getValues() {
    return values;
  }

  public final void addValue(final boolean value) {
    values.add(value);
  }

  @Override
  public String toString() {
    return String.format("%s=(%s, %s)", getTag(), name, values);
  }
}
