/*
 * Copyright 2009-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.sequence.branch;

import java.util.ArrayList;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link BranchStructure} implements an internal representation of branch structures (control flow
 * graphs).
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BranchStructure {
  private final ArrayList<BranchEntry> entries = new ArrayList<>();

  /**
   * Constructs a branch structure.
   * 
   * @param size the branch structure size.
   */
  public BranchStructure(final int size) {
    InvariantChecks.checkGreaterThanZero(size);

    for (int i = 0; i < size; i++) {
      entries.add(new BranchEntry(BranchEntry.Type.BASIC_BLOCK, -1, -1));
    }
  }

  private BranchStructure(final BranchStructure r) {
    for(final BranchEntry entry : r.entries) {
      entries.add(entry.clone());
    }
  }

  public int size() {
    return entries.size();
  }

  public boolean isEmpty() {
    return entries.isEmpty();
  }

  public BranchEntry get(final int i) {
    InvariantChecks.checkBounds(i, entries.size());
    return entries.get(i);
  }

  @Override
  public String toString() {
    final StringBuffer buffer = new StringBuffer();

    boolean delimiter = false;
    for (int i = 0; i < entries.size(); i++) {
      final BranchEntry entry = entries.get(i);

      if (delimiter) {
        buffer.append(", ");
      }

      buffer.append(String.format("%d: ", i));
      buffer.append(entry);
      delimiter = true;
    }

    return buffer.toString();
  }

  @Override
  public BranchStructure clone() {
      return new BranchStructure(this);
  }
}
