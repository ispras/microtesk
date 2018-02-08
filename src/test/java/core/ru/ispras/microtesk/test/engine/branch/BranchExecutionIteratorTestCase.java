/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru), UniTESK Lab (http://www.unitesk.com)
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

package ru.ispras.microtesk.test.engine.branch;

import org.junit.Test;

import ru.ispras.testbase.knowledge.iterator.Iterator;
import ru.ispras.testbase.knowledge.iterator.SingleValueIterator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BranchExecutionIteratorTestCase {
  public BranchExecutionIteratorTestCase() {}

  private void runTest(
      final List<BranchEntry> branchStructure,
      final int maxBranchExecution,
      final int maxBlockExecution) {
    System.out.format("Branch structure: %s%n", branchStructure);
    System.out.format("Max branch execution: %d%n", maxBranchExecution);
    System.out.format("Max block execution: %d%n", maxBlockExecution);

    final Iterator<List<BranchEntry>> i =
        new SingleValueIterator<>(branchStructure);

    final BranchExecutionIterator j =
        new BranchExecutionIterator(i, maxBranchExecution, maxBlockExecution, -1);

    for (j.init(); j.hasValue(); j.next()) {
      final List<BranchEntry> v = j.value();
      System.out.println(v);
    }
  }

  /**
   * 0: BASIC_BLOCK
   * 1: IF_THEN: Target=0
   * 2: BASIC_BLOCK
   * 3: IF_THEN: Target=2
   * 4: BASIC_BLOCK
   * 5: IF_THEN: Target=8
   * 6: BASIC_BLOCK
   * 7: GOTO: Target=0
   * 8: BASIC_BLOCK
   */
  @Test
  public void runTest1() {
    final int size = 9;
    final List<BranchEntry> branchStructure = new ArrayList<>(size);

    int regId = 0;
    final BranchEntry branchEntry0 = new BranchEntry(BranchEntry.Type.BASIC_BLOCK, -1, 0, -1);
    branchStructure.add(branchEntry0);

    final BranchEntry branchEntry1 = new BranchEntry(BranchEntry.Type.IF_THEN, regId++, 0, 0);
    branchStructure.add(branchEntry1);

    final BranchEntry branchEntry2 = new BranchEntry(BranchEntry.Type.BASIC_BLOCK, -1, 0, -1);
    branchStructure.add(branchEntry2);

    final BranchEntry branchEntry3 = new BranchEntry(BranchEntry.Type.IF_THEN, regId++, 0, 2);
    branchStructure.add(branchEntry3);

    final BranchEntry branchEntry4 = new BranchEntry(BranchEntry.Type.BASIC_BLOCK, -1, 0, -1);
    branchStructure.add(branchEntry4);

    final BranchEntry branchEntry5 = new BranchEntry(BranchEntry.Type.IF_THEN, regId++, 0, 8);
    branchStructure.add(branchEntry5);

    final BranchEntry branchEntry6 = new BranchEntry(BranchEntry.Type.BASIC_BLOCK, -1, 0, -1);
    branchStructure.add(branchEntry6);

    final BranchEntry branchEntry7 = new BranchEntry(BranchEntry.Type.GOTO, -1, 0, 0);
    branchStructure.add(branchEntry7);

    final BranchEntry branchEntry8 = new BranchEntry(BranchEntry.Type.BASIC_BLOCK, -1, 0, -1);
    branchStructure.add(branchEntry8);

    runTest(branchStructure, 2, 2);
  }
}
