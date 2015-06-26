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

package ru.ispras.microtesk.settings;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link RangeSettings} describes a finite set of integers.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class RangeSettings extends AbstractSettings {
  public static final String TAG = "range";

  private final Set<Integer> values = new HashSet<>();

  public RangeSettings(final int min, final int max) {
    super(TAG);

    for (int i = min; i <= max; i++) {
      values.add(i);
    }
  }

  public Set<Integer> getValues() {
    return values;
  }

  @Override
  public Collection<AbstractSettings> get(final String tag) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(final AbstractSettings section) {
    if (section instanceof IncludeSettings) {
      final IncludeSettings include = (IncludeSettings) section;
      values.add(include.getItem());
    } else if (section instanceof ExcludeSettings) {
      final ExcludeSettings exclude = (ExcludeSettings) section;
      values.remove(exclude.getItem());
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public String toString() {
    return String.format("%s=%s", TAG, values);
  }
}
