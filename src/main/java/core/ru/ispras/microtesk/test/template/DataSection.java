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

import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.memory.MemoryAllocator;
import ru.ispras.microtesk.model.memory.Section;
import ru.ispras.microtesk.test.LabelManager;
import ru.ispras.microtesk.test.template.directive.Directive;
import ru.ispras.microtesk.test.template.directive.DirectiveLabel;

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
  private final List<Directive> directives;

  private final BigInteger physicalAddress;
  private final Section section;
  private final boolean global;
  private final boolean separateFile;

  private int sequenceIndex;
  private BigInteger allocationEndAddress;

  protected DataSection(
      final List<Directive> directives,
      final BigInteger physicalAddress,
      final Section section,
      final boolean global,
      final boolean separateFile) {
    InvariantChecks.checkNotNull(directives);
    InvariantChecks.checkNotNull(section);

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
      this.directives = Directive.copyAll(other.directives);
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

  public List<Directive> getDirectives() {
    return directives;
  }

  public List<Label> getLabels() {
    final ArrayList<Label> labels = new ArrayList<>();
    for (final Directive directive : directives) {
      if (directive.getKind() == Directive.Kind.LABEL) {
        final DirectiveLabel label = (DirectiveLabel) directive;
        labels.add(label.getLabel());
      }
    }
    return Collections.unmodifiableList(labels);
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

  public void allocateDataAndRegisterLabels(
      final MemoryAllocator allocator, final LabelManager labelManager) {
    InvariantChecks.checkNotNull(allocator);

    allocator.setBaseAddress(section.getBasePa());

    BigInteger currentPa = section.getPa();
    if (null != physicalAddress) {
      currentPa = physicalAddress;
    }
    BigInteger currentVa = section.physicalToVirtual(currentPa);

    Logger.debugHeader("Allocating data");
    Logger.debug("Section: " + section.toString());
    Logger.debug("Allocation starts: 0x%016x", section.getPa());
    Logger.debug("Sequence index: %d%n", getSequenceIndex());

    try {
      BigInteger nextPa = currentPa;
      BigInteger nextVa = currentVa;

      for (final Directive directive : directives) {
        // Register labels.
        if (directive.getKind() == Directive.Kind.LABEL) {
          final DirectiveLabel labelDirective = (DirectiveLabel) directive;

          if (labelDirective.isRealLabel()) {
            final Label label = labelDirective.getLabel();
            labelManager.addLabel(label, nextVa.longValue(), getSequenceIndex());
          }
        }

        nextPa = directive.apply(nextPa, allocator);
        nextVa = section.physicalToVirtual(nextPa);

        if (Logger.isDebug()) {
          if (!nextPa.equals(currentPa)) {
            Logger.debug("0x%016x (PA): %s", currentPa, directive.getText());
          } else {
            Logger.debug(directive.getText());
          }
        }

        currentPa = nextPa;
        currentVa = nextVa;
      }
    } finally {
      allocationEndAddress = currentPa;
      if (null == physicalAddress) {
        section.setPa(currentPa);
      }
    }
  }

  @Override
  public String toString() {
    return String.format(
        "DataSection [section=%s, global=%s, separateFile=%s, labelValues=%s, directives=%s]",
        section,
        global,
        separateFile,
        directives
        );
  }
}
