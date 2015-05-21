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

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link RegionSettings} represents a configuration of a single memory region.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class RegionSettings extends AbstractSettings {
  public static final String TAG = "region";

  public static enum Type {
    TEXT,
    DATA
  }

  private final String name;
  private final Type type;
  private final long startAddress;
  private final long endAddress;
  private final boolean isEnabled;

  private final Collection<AccessSettings> accesses = new ArrayList<>();

  public RegionSettings(final String name, final Type type,
      final long startAddress, final long endAddress, final boolean isEnabled) {
    super(TAG);

    this.name = name;
    this.type = type;
    this.startAddress = startAddress;
    this.endAddress = endAddress;
    this.isEnabled = isEnabled;
  }

  public String getName() {
    return name;
  }

  public Type getType() {
    return type;
  }

  public long getStartAddress() {
    return startAddress;
  }

  public long getEndAddress() {
    return endAddress;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public Collection<AccessSettings> getAccesses() {
    return accesses;
  }

  @Override
  public Collection<AbstractSettings> get(final String tag) {
    InvariantChecks.checkTrue(AccessSettings.TAG.equals(tag));

    final Collection<AbstractSettings> result = new ArrayList<>(accesses.size());
    result.addAll(getAccesses());

    return result;
  }

  @Override
  public void add(final AbstractSettings section) {
    InvariantChecks.checkTrue(section instanceof AccessSettings);

    accesses.add((AccessSettings) section);
  }

  @Override
  public String toString() {
    return String.format("%s={name=%s, type=%s, start=%x, end=%x, enabled=%b}",
        TAG, name, type.name(), startAddress, endAddress, isEnabled);
  }
}
