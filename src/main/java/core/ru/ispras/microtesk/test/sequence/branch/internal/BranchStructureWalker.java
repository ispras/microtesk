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

package ru.ispras.microtesk.test.sequence.branch.internal;

/**
 * {@link BranchStructureWalker} implements a branch structure walker.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
final class BranchStructureWalker {
  /** Flag that shows whether the walker is active or not. */
  private boolean active = true;

  /** Branch structure to be traversed. */
  private BranchStructure branchStructure;

  /** Branch entry visitor to be used. */
  private BranchEntryVisitor visitor;

  /** Entry execution counts. */
  private int[] count;

  /**
   * Constructs a branch structure walker.
   * 
   * @param branchStructure the branch structure to be traversed.
   * @param visitor the branch entry visitor to be used.
   */
  public BranchStructureWalker(
      final BranchStructure branchStructure, final BranchEntryVisitor visitor) {
    count = new int[branchStructure.size()];

    this.branchStructure = branchStructure;
    this.visitor = visitor;

    visitor.setBranchStructureWalker(this);
  }

  /**
   * Starts the traversal.
   * 
   * @param index the index of the initial branch entry.
   */
  public void start(final int index) {
    int addr = index;

    active = true;

    for (int i = 0; i < branchStructure.size(); i++) {
      count[i] = 0;
    }

    while (active && addr < branchStructure.size()) {
      final BranchEntry entry = branchStructure.get(addr);
      final BranchTrace trace = entry.getBranchTrace();

      if (entry.isBranch()) {
        BranchExecution execution = trace.get(count[addr]++);;

        visitor.onBranch(addr, entry, execution);

        if (addr < branchStructure.size() - 1) {
          final BranchEntry next = branchStructure.get(addr + 1);

          if (next.isDelaySlot()) {
            visitor.onDelaySlot(++addr, entry);
          }
        }

        addr = execution.value() ? entry.getBranchLabel() : addr + 1;
      } else if (entry.isDelaySlot()) {
        visitor.onDelaySlot(addr++, entry);
      } else {
        visitor.onBasicBlock(addr++, entry);
      }
    }
  }

  /** Starts the traversal. */
  public void start() {
    start(0);
  }

  /** Stops the traversal. */
  public void stop() {
    active = false;
  }
}
