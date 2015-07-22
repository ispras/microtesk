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
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link EnumSetSettings} describes a finite set of items.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class EnumSetSettings<T extends Enum<T>> extends AbstractSettings {
  public static final String TAG = "range";

  private final Set<T> values = new HashSet<>();

  public EnumSetSettings(final Class<T> type) {
    super(TAG);

    values.addAll(EnumSet.allOf(type));
  }

  public Set<T> getValues() {
    return values;
  }

  @Override
  public Collection<AbstractSettings> get(final String tag) {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void add(final AbstractSettings section) {
    if (section instanceof IncludeSettings) {
      final IncludeSettings<T> include = (IncludeSettings<T>) section;
      values.add(include.getItem());
    } else if (section instanceof ExcludeSettings) {
      final ExcludeSettings<T> exclude = (ExcludeSettings<T>) section;
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
