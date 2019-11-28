/*
 * Copyright 2016-2017 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.microtesk.model.memory.Section;
import ru.ispras.microtesk.test.template.directive.DirectiveFactory;
import ru.ispras.microtesk.test.template.directive.DirectiveTypeInfo;
import ru.ispras.microtesk.test.template.directive.Directive;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@link DataSectionBuilder} class builds data sections.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class DataSectionBuilder {
  private final BlockId blockId;
  private final DirectiveFactory directiveFactory;

  private final Section section;
  private BigInteger physicalAddress;
  private final boolean global;
  private final boolean separateFile;

  private final List<LabelValue> labelValues;
  private final List<Directive> directives;

  public DataSectionBuilder(
      final BlockId blockId,
      final DirectiveFactory directiveFactory,
      final Section section,
      final boolean isGlobal,
      final boolean isSeparateFile) {
    InvariantChecks.checkNotNull(blockId);
    InvariantChecks.checkNotNull(directiveFactory);
    InvariantChecks.checkNotNull(section);

    this.blockId = blockId;
    this.directiveFactory = directiveFactory;

    this.section = section;
    this.physicalAddress = null;

    this.global = isGlobal;
    this.separateFile = isSeparateFile;

    this.labelValues = new ArrayList<>();
    this.directives = new ArrayList<>();
  }

  public void setPhysicalAddress(final BigInteger value) {
    InvariantChecks.checkNotNull(value);
    this.physicalAddress = value;
  }

  public boolean isGlobal() {
    return global;
  }

  public boolean isSeparateFile() {
    return separateFile;
  }

  private void addDirective(final Directive directive) {
    InvariantChecks.checkNotNull(directive);
    directives.add(directive);
  }

  /**
   * Sets allocation origin. Inserts the ".org" directive in the test program.
   *
   * @param origin Origin value.
   */
  public void setOrigin(final BigInteger origin) {
    addDirective(directiveFactory.newOrigin(origin));
  }

  /**
   * Sets allocation origin related to the current address. The origin value is calculated
   * depending on the context. Inserts the ".org" directive in the test program.
   *
   * @param delta Relative origin value.
   */
  public void setRelativeOrigin(final BigInteger delta) {
    addDirective(directiveFactory.newOriginRelative(delta));
  }

  /**
   * Sets allocation origin that corresponds to the specified virtual address.
   * The origin value is calculated depending on the context. Inserts the ".org"
   * directive in the test program.
   *
   * @param address Virtual address.
   */
  public void setVirtualAddress(final BigInteger address) {
    final BigInteger origin = section.virtualToOrigin(address);
    addDirective(directiveFactory.newOrigin(origin));
  }

  /**
   * Adds an alignment directive.
   *
   * @param value Alignment amount in addressable units.
   * @param valueInBytes Alignment amount in bytes.
   */
  public void align(final BigInteger value, final BigInteger valueInBytes) {
    addDirective(directiveFactory.newAlign(value, valueInBytes));
  }

  public void addLabel(final String id, final boolean global) {
    final Label label = Label.newLabel(id, blockId);
    final LabelValue labelValue = LabelValue.newUnknown(label);

    if (global || separateFile) {
      addDirective(directiveFactory.newGlobalLabel(labelValue));
    }

    addDirective(directiveFactory.newLabel(section, labelValue));
    labelValues.add(labelValue);
  }

  public void addText(final String text) {
    addDirective(directiveFactory.newText(text));
  }

  public void addComment(final String text) {
    addDirective(directiveFactory.newComment(text));
  }

  public DataValueBuilder addDataValues(final String typeName) {
    final DirectiveTypeInfo type = directiveFactory.findTypeInfo(typeName);
    return new DataValueBuilder(type);
  }

  public DataValueBuilder addDataValuesForSize(final int typeBitSize) {
    final DirectiveTypeInfo type = directiveFactory.findTypeInfo(typeBitSize);
    return new DataValueBuilder(type);
  }

  protected void addGeneratedData(
      final DirectiveTypeInfo typeInfo, final DataGenerator generator, final int count) {
    addDirective(directiveFactory.newData(typeInfo, generator, count));
  }

  public void addSpace(final int length) {
    addDirective(directiveFactory.newSpace(length));
  }

  public void addAsciiStrings(final boolean zeroTerm, final String[] strings) {
    addDirective(directiveFactory.newAsciiStrings(zeroTerm, strings));
  }

  public DataSection build() {
    return new DataSection(
        labelValues, directives, physicalAddress, section, global, separateFile);
  }

  public final class DataValueBuilder {
    private final DirectiveTypeInfo type;
    private final List<Value> values;

    private DataValueBuilder(final DirectiveTypeInfo type) {
      InvariantChecks.checkNotNull(type);

      this.type = type;
      this.values = new ArrayList<>();
    }

    public void add(final BigInteger value) {
      InvariantChecks.checkNotNull(value);
      values.add(new FixedValue(value));
    }

    public void add(final Value value) {
      InvariantChecks.checkNotNull(value);
      values.add(value);
    }

    public void addDouble(final double value) {
      if (type.type.getBitSize() == 32) {
        add(BigInteger.valueOf(Float.floatToIntBits((float) value)));
      } else {
        add(BigInteger.valueOf(Double.doubleToLongBits(value)));
      }
    }

    public void build() {
      addDirective(directiveFactory.newDataValues(type, values));
    }
  }
}
