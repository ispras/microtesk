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

package ru.ispras.microtesk.test.template;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.memory.MemoryAllocator;
import ru.ispras.microtesk.model.memory.Section;
import ru.ispras.microtesk.test.LabelManager;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@link DataSection} class describes data sections defined in
 * test templates or created by engines.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class DataSection {
  private final List<LabelValue> labelValues;
  private final List<DataDirective> directives;

  private final BigInteger physicalAddress;
  private final Section section;
  private final boolean global;
  private final boolean separateFile;

  private int sequenceIndex;
  private BigInteger allocationEndAddress;

  protected DataSection(
      final List<LabelValue> labelValues,
      final List<DataDirective> directives,
      final BigInteger physicalAddress,
      final Section section,
      final boolean global,
      final boolean separateFile) {
    InvariantChecks.checkNotNull(labelValues);
    InvariantChecks.checkNotNull(directives);
    InvariantChecks.checkNotNull(section);

    this.labelValues = Collections.unmodifiableList(labelValues);
    this.directives = Collections.unmodifiableList(directives);

    this.physicalAddress = physicalAddress;
    this.section = section;
    this.global = global;
    this.separateFile = separateFile;

    this.sequenceIndex = Label.NO_SEQUENCE_INDEX;
    this.allocationEndAddress = null;
  }

  protected DataSection(final DataSection other) {
    InvariantChecks.checkNotNull(other);

    try {
      this.labelValues = copyAllLabelValues(other.labelValues);
      this.directives = copyAllDirectives(other.directives);
    } catch (final Exception e) {
      Logger.error("Failed to copy %s", other);
      throw e;
    }

    this.physicalAddress = other.physicalAddress;
    this.section = other.section;
    this.global = other.global;
    this.separateFile = other.separateFile;

    this.sequenceIndex = other.sequenceIndex;
  }

  private static List<LabelValue> copyAllLabelValues(final List<LabelValue> labelValues) {
    InvariantChecks.checkNotNull(labelValues);

    if (labelValues.isEmpty()) {
      return Collections.emptyList();
    }

    final List<LabelValue> result = new ArrayList<>(labelValues.size());
    for (final LabelValue labelValue : labelValues) {
      result.add(new LabelValue(labelValue));
    }

    return result;
  }

  private static List<DataDirective> copyAllDirectives(final List<DataDirective> directives) {
    InvariantChecks.checkNotNull(directives);

    if (directives.isEmpty()) {
      return Collections.emptyList();
    }

    final List<DataDirective> result = new ArrayList<>(directives.size());
    for (final DataDirective directive : directives) {
      result.add(directive.copy());
    }

    return result;
  }

  public int getSequenceIndex() {
    InvariantChecks.checkTrue(global
        ? sequenceIndex == Label.NO_SEQUENCE_INDEX : sequenceIndex >= 0);
    return sequenceIndex;
  }

  public void setSequenceIndex(final int value) {
    if (global ? value == Label.NO_SEQUENCE_INDEX : value >= 0) {
      sequenceIndex = value;
    }
  }

  public List<Label> getLabels() {
    final List<Label> result = new ArrayList<>(labelValues.size());

    for (final LabelValue labelValue : labelValues) {
      final Label label = labelValue.getLabel();
      InvariantChecks.checkNotNull(label);
      result.add(label);
    }

    return result;
  }

  public List<DataDirective> getDirectives() {
    return directives;
  }

  public Section getSection() {
    return section;
  }

  public boolean isGlobal() {
    return global;
  }

  public boolean isSeparateFile() {
    return separateFile;
  }

  public BigInteger getAllocationEndAddress() {
    return allocationEndAddress;
  }

  public void allocate(final MemoryAllocator allocator) {
    InvariantChecks.checkNotNull(allocator);

    allocator.setBaseAddress(section.getBasePa());
    allocator.setCurrentAddress(section.getPa());

    if (null != physicalAddress) {
      allocator.setCurrentAddress(physicalAddress);
    }

    Logger.debugHeader("Allocating data");
    Logger.debug("Section: " + section.toString());
    Logger.debug("Allocation starts: 0x%016x%n", section.getPa());

    try {
      for (final DataDirective directive : directives) {
        final BigInteger address = allocator.getCurrentAddress();
        directive.apply(allocator);

        if (Logger.isDebug()) {
          if (!address.equals(allocator.getCurrentAddress())) {
            Logger.debug("0x%016x (PA): %s", address, directive.getText());
          } else {
            Logger.debug(directive.getText());
          }
        }
      }
    } finally {
      allocationEndAddress = allocator.getCurrentAddress();
      if (null == physicalAddress) {
        section.setPa(allocationEndAddress);
      }
    }
  }

  public void registerLabels(final LabelManager labelManager) {
    InvariantChecks.checkNotNull(labelManager);

    final int sequenceIndex = getSequenceIndex();
    for (final LabelValue labelValue : labelValues) {
      final Label label = labelValue.getLabel();
      InvariantChecks.checkNotNull(label);

      final BigInteger address = labelValue.getAddress();
      InvariantChecks.checkNotNull(address);

      label.setSequenceIndex(sequenceIndex);
      labelManager.addLabel(label, address.longValue());
    }
  }

  @Override
  public String toString() {
    return String.format(
        "DataSection [section=%s, global=%s, separateFile=%s, labelValues=%s, directives=%s]",
        section,
        global,
        separateFile,
        labelValues,
        directives
        );
  }
}
