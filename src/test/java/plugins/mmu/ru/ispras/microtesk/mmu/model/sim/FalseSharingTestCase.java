/*
 * Copyright 2020 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.model.sim;

import org.junit.Assert;
import org.junit.Test;
import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.microtesk.mmu.model.sim.model.Model;

public final class FalseSharingTestCase extends CacheUnitTestCase {
  private final int sharedAddress = 0;
  private final int[] counter = new int[Model.N1];
  private final boolean isStarted[] = new boolean[Model.N1];

  private int getAddress(final int core) {
    return sharedAddress | (core << 2);
  }

  private void test(final Model model) {
    // Initialize the main memory.
    model.memset(0x0000, 0xffff, 0);

    final int numberOfTests = 256;

    for (int i = 0; i < numberOfTests; i++) {
      final int core = nextCore();
      final int address = getAddress(core);

      if (isStarted[core]) {
        model.sw(core, address, counter[core] + 1);
        isStarted[core] = false;
      } else {
        counter[core] = model.lw(core, address);
        isStarted[core] = true;
      }

      // Add some noise.
      test(model, 0x1000, 0xffff, false, 1, 8);
    }

    for (int core = 0; core < Model.N1; core++) {
      final int address = getAddress(core);

      final int received = model.lw(core, address);
      final int expected = counter[core] + (isStarted[core] ? 0 : 1);

      Assert.assertTrue(
          String.format("lw core=%d, address=%x: %x != %x", core, address, received, expected),
          received == expected
      );
    }
  }

  @Test
  public void testWriteBackInclusive() {
    final Model model = new Model(
        CachePolicy.create(
            EvictionPolicyId.FIFO,
            WritePolicyId.WB,
            InclusionPolicyId.INCLUSIVE,
            CoherenceProtocolId.MOESI
        )
    );

    test(model);
  }

  @Test
  public void testWriteBackExclusive() {
    final Model model = new Model(
        CachePolicy.create(
            EvictionPolicyId.FIFO,
            WritePolicyId.WB,
            InclusionPolicyId.EXCLUSIVE,
            CoherenceProtocolId.MOESI
        )
    );

    test(model);
  }

  @Test
  public void testWriteThroughInclusive() {
    final Model model = new Model(
        CachePolicy.create(
            EvictionPolicyId.FIFO,
            WritePolicyId.WT,
            InclusionPolicyId.INCLUSIVE,
            CoherenceProtocolId.MOESI
        )
    );

    test(model);
  }

  @Test
  public void testWriteTroughExclusive() {
    final Model model = new Model(
        CachePolicy.create(
            EvictionPolicyId.FIFO,
            WritePolicyId.WT,
            InclusionPolicyId.EXCLUSIVE,
            CoherenceProtocolId.MOESI
        )
    );

    test(model);
  }

  @Test
  public void testWriteThroughAllocationInclusive() {
    final Model model = new Model(
        CachePolicy.create(
            EvictionPolicyId.FIFO,
            WritePolicyId.WTA,
            InclusionPolicyId.INCLUSIVE,
            CoherenceProtocolId.MOESI
        )
    );

    test(model);
  }

  @Test
  public void testWriteTroughAllocationExclusive() {
    final Model model = new Model(
        CachePolicy.create(
            EvictionPolicyId.FIFO,
            WritePolicyId.WTA,
            InclusionPolicyId.EXCLUSIVE,
            CoherenceProtocolId.MOESI
        )
    );

    test(model);
  }
}
