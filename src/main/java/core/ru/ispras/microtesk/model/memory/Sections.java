/*
 * Copyright 2017-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.util.InvariantChecks;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public final class Sections {
  private static Sections instance = null;

  private final HashMap<String, Section> sections;
  private final TreeMap<BigInteger, Section> sectionAddresses;

  private Section textSection;
  private Section dataSection;

  private Sections() {
    this.sections = new HashMap<>();
    this.sectionAddresses = new TreeMap<>();

    this.textSection = null;
    this.dataSection = null;
  }

  public static Sections get() {
    if (null == instance) {
      instance = new Sections();
    }

    return instance;
  }

  public void setTextSection(final Section section) {
    addSection(section);
    this.textSection = section;
  }

  public Section getTextSection() {
    return textSection;
  }

  public void setDataSection(final Section section) {
    addSection(section);
    this.dataSection = section;
  }

  public Section getDataSection() {
    return dataSection;
  }

  public void addSection(final Section section) {
    InvariantChecks.checkNotNull(section);

    final String key = makeKey(section.getName(), section.isStandard());
    InvariantChecks.checkFalse(sections.containsKey(key));

    sections.put(key, section);
    sectionAddresses.put(section.getBaseVa(), section);
  }

  public Section getSection(final String name, final boolean standard) {
    InvariantChecks.checkNotNull(name);
    return sections.get(makeKey(name, standard));
  }

  private static String makeKey(final String name, final boolean standard) {
    return String.format("%s#%b", name, standard);
  }

  public Collection<Section> getSectionsOrderedByVa() {
    return sectionAddresses.values();
  }

  public void resetState() {
    for (final Section section : sections.values()) {
      section.resetState();
    }
  }

  public void setUseTempState(final boolean value) {
    for (final Section section : sections.values()) {
      section.setUseTempState(value);
    }
  }

  public BigInteger virtualToPhysical(final BigInteger va) {
    final Section section = findSection(va);
    return null != section ? section.virtualToPhysical(va) : va;
  }

  private Section findSection(final BigInteger va) {
    InvariantChecks.checkNotNull(va);
    final Map.Entry<BigInteger, Section> entry = sectionAddresses.floorEntry(va);
    return null != entry ? entry.getValue() : null;
  }
}
