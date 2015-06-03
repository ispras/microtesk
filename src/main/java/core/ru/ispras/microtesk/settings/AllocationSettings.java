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
import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link AllocationSettings} represents an allocation table for addressing modes.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class AllocationSettings extends AbstractSettings {
  public static final String TAG = "allocation";

  private final Map<String, ModeSettings> modes = new LinkedHashMap<>();

  public AllocationSettings() {
    super(TAG);
  }

  public Collection<ModeSettings> getModes() {
    return modes.values();
  }

  public ModeSettings getRegion(final String name) {
    return modes.get(name);
  }

  @Override
  public void add(final AbstractSettings section) {
    InvariantChecks.checkTrue(section instanceof ModeSettings);

    final ModeSettings mode = (ModeSettings) section;
    modes.put(mode.getName(), mode);
  }

  @Override
  public String toString() {
    return String.format("%s=%s", TAG, getModes());
  }
}
