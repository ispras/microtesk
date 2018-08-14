/*
 * Copyright 2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.memory;

import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.util.InvariantChecks;

import java.math.BigInteger;

/**
 * The job of the {@link LocationManager} class is to save and restore values stored
 * in specific locations.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class LocationManager {
  private final LocationAccessor[] locations;
  private BigInteger[] values;

  public LocationManager(final LocationAccessor... locations) {
    InvariantChecks.checkNotEmpty(locations);

    this.locations = locations;
    this.values = null;
  }

  public void save() {
    InvariantChecks.checkTrue(null == values, "Values have already been saved.");
    values = new BigInteger[locations.length];

    Logger.debug("Saved values:");
    for (int index = 0; index < locations.length; index++) {
      final LocationAccessor location = locations[index];
      final BigInteger value = location.getValue();

      values[index] = value;
      Logger.debug("0x%016X", value);
    }
  }

  public void restore() {
    InvariantChecks.checkNotNull(values, "No values have been saved.");

    Logger.debug("Restored values:");
    for (int index = 0; index < locations.length; index++) {
      final LocationAccessor location = locations[index];
      final BigInteger value = values[index];

      location.setValue(value);
      Logger.debug("0x%016X", location.getValue());
    }

    values = null;
  }
}
