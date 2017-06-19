/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import ru.ispras.fortress.util.InvariantChecks;

public final class Sections {
  private static Sections instance = null;

  private final Section dataSection;
  private final Section textSection;

  private final HashMap<String, Section> sections;
  private final TreeMap<BigInteger, Section> sectionAddresses;

  private Sections(final Section dataSection, final Section textSection) {
    InvariantChecks.checkNotNull(dataSection);
    InvariantChecks.checkNotNull(textSection);

    this.dataSection = dataSection;
    this.textSection = textSection;

    this.sections = new HashMap<>();
    this.sectionAddresses = new TreeMap<>();

    addSection(dataSection);
    addSection(textSection);
  }

  public static void initialize(final Section dataSection, final Section textSection) {
    InvariantChecks.checkTrue(null == instance, "Already initialized!");
    instance = new Sections(dataSection, textSection);
  }

  public Sections get() {
    InvariantChecks.checkNotNull(instance, "Not initialized!");
    return instance;
  }

  public Section getDataSection() {
    return dataSection;
  }

  public Section getTextSection() {
    return textSection;
  }

  public void addSection(final Section section) {
    InvariantChecks.checkNotNull(section);
    InvariantChecks.checkFalse(sections.containsKey(section.getText()));

    sections.put(section.getText(), section);
    sectionAddresses.put(section.getBaseVa(), section);
  }

  public Section getSection(final String text) {
    InvariantChecks.checkNotNull(text);
    return sections.get(text);
  }

  public Section getSection(final BigInteger va) {
    InvariantChecks.checkNotNull(va);
    final Map.Entry<BigInteger, Section> entry = sectionAddresses.floorEntry(va);
    return null != entry ? entry.getValue() : null;
  }
}
