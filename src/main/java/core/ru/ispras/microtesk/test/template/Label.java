/*
 * Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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

/**
 * The Label class describes a label set in test templates and symbolic test programs.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class Label {
  private final String name;
  private final BlockId blockId;

  public static final int NO_SEQUENCE_INDEX = -1;
  private int sequenceIndex;

  /**
   * Constructs a label object.  
   * 
   * @param name The name of the label.
   * @param blockId The identifier of the block where the label is defined.
   * 
   * @throws IllegalStateException if any of the parameters equals {@code null}.
   */

  public Label(final String name, final BlockId blockId) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(blockId);

    this.name = name;
    this.blockId = blockId;
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
    this.sequenceIndex = other.sequenceIndex;
  }

  /**
   * Assigns index that identifies the instruction sequence where the label is defined.
   * This is required to avoid situations when different sequences produced by the same 
   * block refer to the same labels.
   * 
   * @param value Index that identifies the instruction sequence where the label is defined.
   */

  public void setSequenceIndex(final int value) {
    InvariantChecks.checkGreaterOrEqZero(value);
    this.sequenceIndex = value;
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
   * Returns a unique name for the label based on its name, the identifier of
   * the block where it was defined and the sequence index if it has been set up.
   * 
   * @return Unique name based on the label name, the block identifier and the
   *         sequence index.
   */

  public String getUniqueName() {
    // For root blocks (that have no parent), we do not add the block
    // identifier as a suffix to the label name. 

    final String blockSuffix = blockId.parentId() != null ?
        blockId.toString() : "";

    if (sequenceIndex == NO_SEQUENCE_INDEX) {
      return String.format("%s%s", name, blockSuffix);
    }

    return String.format("%s%s_%04d", name, blockSuffix, sequenceIndex);
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

    return result;
  }

  @Override
  public boolean equals(Object obj) {
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
    return name.equals(other.name) && blockId.equals(other.blockId);
  }
}
