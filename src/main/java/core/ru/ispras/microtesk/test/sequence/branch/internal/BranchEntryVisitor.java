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
 * {@link BranchEntryVisitor} is an abstract class for branch entry visitors.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
abstract class BranchEntryVisitor {
  /** Branch structure walker. */
  private BranchStructureWalker walker = null;

  /**
   * Returns the branch structure walker.
   * 
   * @return the branch structure walker.
   */
  public BranchStructureWalker getBranchStructureWalker() {
    return walker;
  }

  /**
   * Sets the branch structure walker.
   * 
   * @param walker the branch structure walker to be set.
   */
  public void setBranchStructureWalker(final BranchStructureWalker walker) {
    this.walker = walker;
  }

  /** Stops traversal. */
  public void stop() {
    if (walker != null) {
      walker.stop();
    }
  }

  /**
   * Process the branch entry (conditional or unconditional).
   * 
   * @param index the entry index.
   * @param entry the entry.
   * @param execution the execution of the branch entry.
   */
  public abstract void onBranch(
      final int index, final BranchEntry entry, final BranchExecution execution);

  /**
   * Process the delay slot entry.
   * 
   * @param index the entry index.
   * @param entry the entry.
   */
  public abstract void onDelaySlot(final int index, final BranchEntry entry);

  /**
   * Process the basic block entry.
   * 
   * @param index the entry index.
   * @param entry the entry.
   */
  public abstract void onBasicBlock(final int index, final BranchEntry entry);
}
