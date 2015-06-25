/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru), UniTESK Lab (http://www.unitesk.com)
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

package ru.ispras.microtesk.test.sequence.engine.branch;

import org.junit.Test;

import ru.ispras.microtesk.test.sequence.iterator.Iterator;
import ru.ispras.microtesk.test.sequence.iterator.SingleValueIterator;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BranchExecutionIteratorTestCase {
  public BranchExecutionIteratorTestCase() {}

  private void runTest(final BranchStructure branchStructure, final int maxBranchExecution) {
    System.out.format("Branch structure: %s%n", branchStructure);
    System.out.format("Max trace length: %d%n", maxBranchExecution);

    final Iterator<BranchStructure> i =
        new SingleValueIterator<>(branchStructure);

    final BranchExecutionIterator j =
        new BranchExecutionIterator(i, maxBranchExecution);

    for (j.init(); j.hasValue(); j.next()) {
      final BranchStructure v = j.value();
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
    final BranchStructure branchStructure = new BranchStructure(9);

    final BranchEntry branchEntry0 = branchStructure.get(0);
    branchEntry0.setType(BranchEntry.Type.BASIC_BLOCK);

    final BranchEntry branchEntry1 = branchStructure.get(1);
    branchEntry1.setType(BranchEntry.Type.IF_THEN);
    branchEntry1.setBranchLabel(0);

    final BranchEntry branchEntry2 = branchStructure.get(2);
    branchEntry2.setType(BranchEntry.Type.BASIC_BLOCK);

    final BranchEntry branchEntry3 = branchStructure.get(3);
    branchEntry3.setType(BranchEntry.Type.IF_THEN);
    branchEntry3.setBranchLabel(2);

    final BranchEntry branchEntry4 = branchStructure.get(4);
    branchEntry4.setType(BranchEntry.Type.BASIC_BLOCK);

    final BranchEntry branchEntry5 = branchStructure.get(5);
    branchEntry5.setType(BranchEntry.Type.IF_THEN);
    branchEntry5.setBranchLabel(8);

    final BranchEntry branchEntry6 = branchStructure.get(6);
    branchEntry6.setType(BranchEntry.Type.BASIC_BLOCK);

    final BranchEntry branchEntry7 = branchStructure.get(7);
    branchEntry7.setType(BranchEntry.Type.GOTO);
    branchEntry7.setBranchLabel(0);

    final BranchEntry branchEntry8 = branchStructure.get(8);
    branchEntry8.setType(BranchEntry.Type.BASIC_BLOCK);

    runTest(branchStructure, 2);
  }
}
