/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * MemoryTestCase.java, Oct 1, 2014 11:46:05 AM Andrei Tatarnikov
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.model.api.mmu;

import org.junit.Assert;
import org.junit.Test;

public class MmuTestCase {
  public static final int ASSOCIATIVITY = 10;

  private void testPolicy(final PolicyId policyId) {
    final Policy policy = policyId.newPolicy(ASSOCIATIVITY);

    int victim = policy.chooseVictim();
    Assert.assertEquals(victim, 0);

    for (int i = 0; i < ASSOCIATIVITY; i++) {
      policy.accessLine(i);

      victim = policy.chooseVictim();
      Assert.assertTrue(victim != i);

      for (int j = 0; j < ASSOCIATIVITY; j++) {
        if (j != i) {
          policy.accessLine(j);
        }
      }

      victim = policy.chooseVictim();
      Assert.assertTrue(victim == i || victim == 0 /* for PLRU */);
    }
  }
  
  @Test
  public void testRandom() {
    final int count = 100;
    final Policy policy = PolicyId.RANDOM.newPolicy(ASSOCIATIVITY);

    for (int i = 0; i < count; i++) {
      final int victim = policy.chooseVictim();
      Assert.assertTrue(0 <= victim && victim < ASSOCIATIVITY);
    }
  }

  @Test
  public void testFIFO() {
   testPolicy(PolicyId.FIFO);
  }

  @Test
  public void testPLRU() {
    testPolicy(PolicyId.PLRU);
  }

  @Test
  public void testLRU() {
    testPolicy(PolicyId.LRU);
  }
}
