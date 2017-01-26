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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.template.DataDirectiveFactory.TypeInfo;

public final class DataSectionBuilder {
  private final BlockId blockId;
  private final DataDirectiveFactory directiveFactory;

  private final boolean global;
  private final boolean separateFile;

  private final List<LabelValue> labelValues;
  private final List<DataDirective> directives;

  protected DataSectionBuilder(
      final BlockId blockId,
      final DataDirectiveFactory directiveFactory,
      final boolean isGlobal,
      final boolean isSeparateFile) {
    InvariantChecks.checkNotNull(blockId);
    InvariantChecks.checkNotNull(directiveFactory);

    this.blockId = blockId;
    this.directiveFactory = directiveFactory;

    this.global = isGlobal;
    this.separateFile = isSeparateFile;

    this.labelValues = new ArrayList<>();
    this.directives = new ArrayList<>();

    this.directives.add(directiveFactory.getHeader());
  }

  public boolean isGlobal() {
    return global;
  }

  public boolean isSeparateFile() {
    return separateFile;
  }

  private void addDirective(final DataDirective directive) {
    InvariantChecks.checkNotNull(directive);
    directives.add(directive);
  }

  /**
   * Sets allocation origin. Inserts the ".org" directive in the test program.
   */
  public void setOrigin(final BigInteger origin) {
    addDirective(directiveFactory.newOrigin(origin));
  }

  /**
   * Sets allocation origin related to the current address. The origin
   * value is calculated depending on the context. Inserts the ".org"
   * directive in the test program.
   */
  public void setRelativeOrigin(final BigInteger delta) {
    addDirective(directiveFactory.newOriginRelative(delta));
  }

  /**
   * @param value Alignment amount in addressable units.
   */
  public void align(final BigInteger value, final BigInteger valueInBytes) {
    addDirective(directiveFactory.newAlign(value, valueInBytes));
  }

  public void addLabel(final String id) {
    final Label label = new Label(id, blockId);
    final LabelValue labelValue = LabelValue.newUnknown(label);

    if (separateFile) {
      addDirective(directiveFactory.newGlobalLabel(labelValue));
    }

    addDirective(directiveFactory.newLabel(labelValue));
    labelValues.add(labelValue);
  }

  public void addText(final String text) {
    addDirective(directiveFactory.newText(text));
  }

  public void addComment(final String text) {
    addDirective(directiveFactory.newComment(text));
  }

  public DataValueBuilder addDataValues(final String typeName) {
    return new DataValueBuilder(typeName);
  }

  protected void addGeneratedData(
      final TypeInfo typeInfo, final DataGenerator generator, final int count) {
    addDirective(directiveFactory.newData(typeInfo, generator, count));
  }

  public void addSpace(final int length) {
    addDirective(directiveFactory.newSpace(length));
  }

  public void addAsciiStrings(final boolean zeroTerm, final String[] strings) {
    addDirective(directiveFactory.newAsciiStrings(zeroTerm, strings));
  }

  public DataSection build() {
    return new DataSection(labelValues, directives, global, separateFile);
  }

  public final class DataValueBuilder {
    private final String typeName;
    private final List<Value> values;

    private DataValueBuilder(final String typeName) {
      InvariantChecks.checkNotNull(typeName);

      this.typeName = typeName;
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

    public void build() {
      addDirective(directiveFactory.newDataValues(typeName, values));
    }
  }
}
