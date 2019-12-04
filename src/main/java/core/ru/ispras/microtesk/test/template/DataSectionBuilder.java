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

  /**
   * Adds the directive to the data section.
   *
   * @param directive Directive to be added.
   */
  public void addDirective(final Directive directive) {
    InvariantChecks.checkNotNull(directive);
    directives.add(directive);
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

  protected void addGeneratedData(
      final DirectiveTypeInfo typeInfo,
      final DataGenerator generator,
      final int count,
      final boolean align) {
    addDirective(directiveFactory.newData(typeInfo, generator, count, align));
  }

  public DataSection build() {
    return new DataSection(
        labelValues, directives, physicalAddress, section, global, separateFile);
  }

}
