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
 * @author Andrei Tatarnikov
 */

public final class Label {
  private final String name;
  private final BlockId blockId;

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
   * Returns a unique name for the label based on its name and the identifier of the block where it
   * was defined.
   * 
   * @return Unique name based on the label name and the block identifier.
   */

  public String getUniqueName() {
    return name + blockId;
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
