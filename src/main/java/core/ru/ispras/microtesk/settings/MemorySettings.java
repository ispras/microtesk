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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link MemorySettings} represents memory regions being accessed by test programs.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemorySettings extends AbstractSettings {
  public static final String TAG = "memory";

  private final Map<String, RegionSettings> regions = new LinkedHashMap<>();

  public MemorySettings() {
    super(TAG);
  }

  public Collection<RegionSettings> getRegions() {
    return regions.values();
  }

  public RegionSettings getRegion(final String name) {
    return regions.get(name);
  }

  public boolean isEnabled(final String name) {
    final RegionSettings region = getRegion(name);
    return region.isEnabled();
  }

  public boolean checkTextAddress(final long address) {
    return checkAddress(RegionSettings.Type.TEXT, address);
  }

  public boolean checkDataAddress(final long address) {
    return checkAddress(RegionSettings.Type.DATA, address);
  }

  private boolean checkAddress(final RegionSettings.Type type, final long address) {
    for (final RegionSettings region : getRegions()) {
      if (region.getType() == type && region.checkAddress(address)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Collection<AbstractSettings> get(final String tag) {
    InvariantChecks.checkTrue(RegionSettings.TAG.equals(tag));

    final Collection<AbstractSettings> result = new ArrayList<>(regions.size());
    result.addAll(getRegions());

    return result;
  }

  @Override
  public void add(final AbstractSettings section) {
    InvariantChecks.checkTrue(section instanceof RegionSettings);

    final RegionSettings region = (RegionSettings) section;
    regions.put(region.getName(), region);
  }

  @Override
  public String toString() {
    return String.format("%s=%s", TAG, getRegions());
  }
}
