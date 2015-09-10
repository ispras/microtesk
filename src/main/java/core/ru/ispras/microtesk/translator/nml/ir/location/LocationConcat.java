/*
 * Copyright 2013-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.ir.location;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ru.ispras.microtesk.translator.nml.ir.shared.Type;

public final class LocationConcat implements Location {
  private final Type type;
  private final List<LocationAtom> locations;

  LocationConcat(Type type, List<LocationAtom> locations) {
    if (null == type) {
      throw new NullPointerException();
    }

    if (null == locations) {
      throw new NullPointerException();
    }

    this.type = type;
    this.locations = Collections.unmodifiableList(locations);
  }

  @Override
  public Type getType() {
    return type;
  }

  public List<LocationAtom> getLocations() {
    return locations;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final LocationConcat other = (LocationConcat) obj;
    if (!getType().equals(other.getType())) {
      return false;
    }

    if (locations.size() != other.locations.size()) {
      return false;
    }

    final Iterator<LocationAtom> thisIterator = locations.iterator();
    final Iterator<LocationAtom> otherIterator = other.getLocations().iterator();

    while (thisIterator.hasNext() && otherIterator.hasNext()) {
      if (!thisIterator.next().equals(otherIterator.next())) {
        return false;
      }
    }

    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for (final Location location : locations) {
      if (sb.length() > 0) {
        sb.append("::");
      }
      sb.append(location.toString());
    }
    return sb.toString();
  }
}
