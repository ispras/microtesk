/*
 * Copyright 2014-2018 ISP RAS (http://www.ispras.ru)
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

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link Label} class describes a label set in test templates and symbolic test programs.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Label {
  private final String name;
  private final BlockId blockId;

  private final boolean global;
  private final boolean numeric;

  public static final int NO_REFERENCE_NUMBER = -1;
  private int referenceNumber;

  public static final int NO_SEQUENCE_INDEX = -1;
  private int sequenceIndex;

  /**
   * Constructs a label object. The label is considered non-global and non-numeric.
   *
   * @param name The name of the label.
   * @param blockId The identifier of the block where the label is defined.
   *
   * @throws IllegalStateException if any of the parameters equals {@code null}.
   */
  public Label(final String name, final BlockId blockId) {
    this(name, blockId, false, false);
  }

  /**
   * Constructs a label object.
   *
   * @param name The name of the label.
   * @param blockId The identifier of the block where the label is defined.
   * @param global Specifies whether the label is global.
   * @param numeric Specifies whether the label is numeric.
   *
   * @throws IllegalStateException if any of the arguments equals {@code null};
   *                               if the label both global and numeric.
   */
  public Label(
      final String name,
      final BlockId blockId,
      final boolean global,
      final boolean numeric) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(blockId);

    // A label cannot be both global and numeric.
    InvariantChecks.checkTrue(global ? !numeric : true);

    this.name = name;
    this.blockId = blockId;

    this.global = global;
    this.numeric = numeric;

    this.referenceNumber = NO_REFERENCE_NUMBER;
    this.sequenceIndex = NO_SEQUENCE_INDEX;
  }

  /**
   * Constructs a copy of the specified label.
   *
   * @param other Label to be copied.
   */
  public Label(final Label other) {
    InvariantChecks.checkNotNull(other);

    this.name = other.name;
    this.blockId = other.blockId;

    this.global = other.global;
    this.numeric = other.numeric;

    this.referenceNumber = other.referenceNumber;
    this.sequenceIndex = other.sequenceIndex;
  }

  /**
   * Creates a deep copy of the specified label list.
   *
   * @param labels List of labels to be copied.
   * @return Copy of the list.
   *
   * @throws IllegalArgumentException if the parameter is {@code null}.
   */
  public static List<Label> copyAll(final List<Label> labels) {
    InvariantChecks.checkNotNull(labels);

    final List<Label> result = new ArrayList<>(labels.size());
    for (final Label label : labels) {
      result.add(new Label(label));
    }

    return result;
  }

  /**
   * Assigns a number that uniquely identifies the label which is duplicated within an
   * instruction sequence. This is required to resolve name conflicts that occur when
   * a subsequence containing labels (e.g. a preparator) is inserted multiple times
   * into the same sequence.
   *
   * @param value Number that uniquely identifies a label which is duplicated within a sequence.
   */
  public void setReferenceNumber(final int value) {
    InvariantChecks.checkGreaterOrEqZero(value);
    this.referenceNumber = value;
  }

  /**
   * Returns a number that uniquely identifies the label among labels that have the same name.
   *
   * @return Number that uniquely identifies the label among labels that have the same name.
   */
  public int getReferenceNumber() {
    return referenceNumber;
  }

  /**
   * Assigns an index that identifies the instruction sequence where the label is defined.
   * This is required to resolve name conflicts that occur when different sequences
   * produced by the same block use the same labels.
   *
   * @param value Index that identifies the instruction sequence where the label is defined.
   */
  public void setSequenceIndex(final int value) {
    InvariantChecks.checkTrue(value >= 0 || value == NO_SEQUENCE_INDEX);
    this.sequenceIndex = value;
  }

  /**
   * Returns an index that identifies the instruction sequence where the label is defined.
   *
   * @return Index that identifies the instruction sequence where the label is defined.
   */
  public int getSequenceIndex() {
    return sequenceIndex;
  }

  /**
   * Returns the name of the label as it was defined in a test template.
   *
   * @return The name of the label.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns a unique name for the label based on:
   * <ol>
   * <li>Label name</li>
   * <li>Block identifier if it is not a root (no parent) block</li>
   * <li>Reference number if it set</li>
   * <li>Sequence index if it set</li>
   * </ol>
   * N.B. Numeric labels do not have unique names. For such labels, a unique name includes
   * only label name.
   *
   * @return Unique name based on the label name and the context in which it is defined.
   */
  public String getUniqueName() {
    if (numeric) {
      return name;
    }

    final StringBuilder sb = new StringBuilder(name);

    if (blockId.parentId() != null) {
      sb.append(blockId.toString());
    }

    if (referenceNumber != NO_REFERENCE_NUMBER) {
      sb.append(String.format("_n%02d", referenceNumber));
    }

    if (sequenceIndex != NO_SEQUENCE_INDEX) {
      sb.append(String.format("_%04d", sequenceIndex));
    }

    return sb.toString();
  }

  /**
   * Returns the identifier of the block where the label was defined.
   *
   * @return Block identifier.
   */
  public BlockId getBlockId() {
    return blockId;
  }

  /**
   * Checks whether the label is global.
   *
   * @return {@code true} if the label is global or {@code false} otherwise.
   */
  public boolean isGlobal() {
    return global;
  }

  /**
   * Checks whether the label is numeric.
   *
   * @return {@code true} if the label is numeric or {@code false} otherwise.
   */
  public boolean isNumeric() {
    return numeric;
  }

  /**
   * Returns textual representation of the label based on its unique name.
   *
   * @return Textual representation based on the unique name.
   */
  @Override
  public String toString() {
    return getUniqueName();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;

    result = prime * result + name.hashCode();
    result = prime * result + blockId.hashCode();
    result = prime * result + referenceNumber;
    result = prime * result + sequenceIndex;

    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final Label other = (Label) obj;
    return this.name.equals(other.name)
        && this.blockId.equals(other.blockId)
        && this.referenceNumber == other.referenceNumber
        && this.sequenceIndex == other.sequenceIndex;
  }
}
