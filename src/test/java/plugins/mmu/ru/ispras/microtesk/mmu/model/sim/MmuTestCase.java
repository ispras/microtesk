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

package ru.ispras.microtesk.mmu.model.sim;

import org.junit.Assert;
import org.junit.Test;

public class MmuTestCase {
  public static final int ASSOCIATIVITY = 10;

  private void testPolicy(final EvictionPolicyId evictionPolicyId) {
    final EvictionPolicy evictionPolicy = evictionPolicyId.newPolicy(ASSOCIATIVITY);

    int victim = evictionPolicy.getVictim();
    Assert.assertEquals(victim, 0);

    for (int i = 0; i < ASSOCIATIVITY; i++) {
      evictionPolicy.onAccess(i);

      victim = evictionPolicy.getVictim();
      Assert.assertTrue(victim != i);

      for (int j = 0; j < ASSOCIATIVITY; j++) {
        if (j != i) {
          evictionPolicy.onAccess(j);
        }
      }

      victim = evictionPolicy.getVictim();
      Assert.assertTrue(victim == i || victim == 0 /* for PLRU */);
    }
  }

  @Test
  public void testRandom() {
    final int count = 100;
    final EvictionPolicy evictionPolicy = EvictionPolicyId.RANDOM.newPolicy(ASSOCIATIVITY);

    for (int i = 0; i < count; i++) {
      final int victim = evictionPolicy.getVictim();
      Assert.assertTrue(0 <= victim && victim < ASSOCIATIVITY);
    }
  }

  @Test
  public void testFIFO() {
    testPolicy(EvictionPolicyId.FIFO);
  }

  @Test
  public void testPLRU() {
    testPolicy(EvictionPolicyId.PLRU);
  }

  @Test
  public void testLRU() {
    testPolicy(EvictionPolicyId.LRU);
  }
}
