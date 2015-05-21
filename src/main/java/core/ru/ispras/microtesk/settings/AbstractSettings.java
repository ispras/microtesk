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
import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link AbstractSettings} represents abstract settings.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class AbstractSettings {
  private final String tag;

  /**
   * Contains the settings' sections (standard and generator-specific ones).
   * 
   * <p>The key is the section tag; the value is the list of the settings' sections.</p>
   */
  private Map<String, Collection<AbstractSettings>> sections = new HashMap<>();

  public AbstractSettings(final String tag) {
    this.tag = tag;
  }

  public final String getTag() {
    return tag;
  }

  @SuppressWarnings("unchecked")
  public final <T extends AbstractSettings> T getSingle(final String tag) {
    final Collection<AbstractSettings> sections = get(tag);
    for (final AbstractSettings section : sections) {
      return (T) section;
    }

    return null;
  }

  /**
   * Default implementation (can be overloaded in subclasses).
   * 
   * @param tag the tag of the sections to be returned.
   */
  public Collection<AbstractSettings> get(final String tag) {
    return sections.get(tag);
  }

  /**
   * Default implementation (can be overloaded in subclasses).
   * 
   * @param section the settings's section to be added.
   */
  public void add(final AbstractSettings section) {
    InvariantChecks.checkNotNull(section);

    Collection<AbstractSettings> tagSections = sections.get(section.getTag());
    if (tagSections == null) {
      sections.put(section.getTag(), tagSections = new ArrayList<>());
    }

    tagSections.add(section);
  }

  @Override
  public String toString() {
    return String.format("%s", sections.values());
  }
}
