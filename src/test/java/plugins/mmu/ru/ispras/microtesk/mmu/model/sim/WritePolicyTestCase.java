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

import org.junit.Test;
import ru.ispras.microtesk.mmu.model.sim.model.Model;

public final class WritePolicyTestCase extends CacheUnitTestCase {

  private void test(final Model model) {
    test(model, 0x0000, 0xffff, true, 256, 16);
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
