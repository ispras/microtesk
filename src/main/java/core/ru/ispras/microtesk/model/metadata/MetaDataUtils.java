/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * The {@link MetaDataUtils} class contains utility methods to deal with objects
 * implementing the {@link MetaData} interface.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class MetaDataUtils {
  private MetaDataUtils() {}

  /**
   * Takes {@link MetaData} objects from the specified collection and puts them to a map,
   * where names of the objects are used as keys. The order of objects is preserved.
   *
   * @param <T> Exact type of {@code MetaData} objects.
   * @param c Collection of {@code MetaData} objects.
   * @return Map of {@code MetaData} objects.
   */
  public static <T extends MetaData> Map<String, T> toMap(final Collection<T> c) {
    InvariantChecks.checkNotNull(c);

    if (c.isEmpty()) {
      return Collections.emptyMap();
    }

    final Map<String, T> map = new LinkedHashMap<>(c.size());
    for (final T t : c) {
      map.put(t.getName(), t);
    }

    return map;
  }

  /**
   * Takes a collection of {@link MetaData} objects and creates a set containing
   * their names. The order of objects is preserved.
   * 
   * @param c Collection of {@code MetaData} objects.
   * @return Set of {@code MetaData} object names.
   */
  public static Set<String> toNameSet(final Collection<? extends MetaData> c) {
    InvariantChecks.checkNotNull(c);

    if (c.isEmpty()) {
      return Collections.emptySet();
    }

    final Set<String> names = new LinkedHashSet<>(c.size());
    for (final MetaData item : c) {
      names.add(item.getName());
    }

    return names;
  }

  /**
   * Takes a collection of {@link MetaData} objects and creates a set containing
   * names of their elements. If an object in the collection is represented by a group,
   * its elements are added to the set in a recursive manner. The order of objects is preserved.
   * 
   * @param c Collection of {@code MetaData} objects.
   * @return Set of {@code MetaData} object names.
   */
  public static Set<String> toNameSetRecursive(final Collection<? extends MetaData> c) {
    InvariantChecks.checkNotNull(c);

    if (c.isEmpty()) {
      return Collections.emptySet();
    }

    final Set<String> names = new LinkedHashSet<>();
    for (final MetaData item : c) {
      if (item instanceof MetaGroup) {
        names.addAll(toNameSetRecursive(((MetaGroup) item).getItems()));
      } else {
        names.add(item.getName());
      }
    }

    return names;
  }

  /**
   * Takes a collection of {@link MetaData} objects and creates a list containing
   * their names. The order of objects is preserved.
   * 
   * @param c Collection of {@code MetaData} objects.
   * @return List of {@code MetaData} object names.
   */
  public static List<String> toNameList(final Collection<? extends MetaData> c) {
    InvariantChecks.checkNotNull(c);

    if (c.isEmpty()) {
      return Collections.emptyList();
    }

    final List<String> names = new ArrayList<>(c.size());
    for (final MetaData item : c) {
      names.add(item.getName());
    }

    return names;
  }

  /**
   * Takes a collection of {@link MetaData} objects and creates a string
   * that consists of their names separated with the specified separator string.
   * The order of objects is preserved.
   * 
   * @param c Collection of {@code MetaData} objects.
   * @param sep Separator string.
   * @return String containing a list of {@code MetaData} object names or
   *         empty string of the collection is empty.
   */
  public static String toNameListString(
      final Collection<? extends MetaData> c, final String sep) {
    InvariantChecks.checkNotNull(c);

    if (c.isEmpty()) {
      return "";
    }

    final StringBuilder sb = new StringBuilder();
    for (final MetaData item : c) {
      if (sb.length() != 0) {
        sb.append(sep);
      }
      sb.append(item.getName());
    }

    return sb.toString();
  }

  /**
   * Takes a collection of {@link MetaData} objects and creates a string
   * that consists of their names separated with the specified separator string.
   * The order of objects is preserved. If objects in the collection represent
   * groups, they are processed recursively to create a list of their items.
   * 
   * @param c Collection of {@code MetaData} objects.
   * @param sep Separator string.
   * @return String containing a list of {@code MetaData} object names or
   *         empty string of the collection is empty.
   */
  public static String toNameListStringRecursive(
      final Collection<? extends MetaData> c, final String sep) {
    InvariantChecks.checkNotNull(c);

    if (c.isEmpty()) {
      return "";
    }

    final StringBuilder sb = new StringBuilder();
    for (final MetaData item : c) {
      if (sb.length() != 0) {
        sb.append(sep);
      }

      if (item instanceof MetaGroup) {
        sb.append(toNameListStringRecursive(((MetaGroup) item).getItems(), sep));
      } else {
        sb.append(item.getName());
      }
    }

    return sb.toString();
  }
}
