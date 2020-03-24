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

public final class CacheUnitTestCase {

  public static Model model = new Model(
      CachePolicy.create(
          EvictionPolicyId.FIFO,
          WritePolicyId.WB,
          InclusionPolicyId.INCLUSIVE,
          CoherenceProtocolId.MOESI
      )
  );

  private int nextCore() {
    return Randomizer.get().nextIntRange(0, Model.N1 - 1);
  }

  private int nextAddress(final int start, final int end) {
    final int l1tag = Randomizer.get().nextIntRange(start >>> 12, end >>> 12);
    final int l1idx = Randomizer.get().nextIntRange(0, 2);
    final int l1pos = Randomizer.get().nextIntRange(0, 7);

    return (l1tag << 12) | (l1idx << 5) | (l1pos << 2);
  }

  private int nextWord() {
    return Randomizer.get().nextInt();
  }

  private void test(final Model model, final int start, final int end) {
    // Initialize the main memory.
    model.memset(start, end, 0xdeadbeef);

    final int numberOfTests = 100;
    final int numberOfLoadsPerStore = 10;

    for (int i = 0; i < numberOfTests; i++) {
      final int storeCore = nextCore();
      final int storeAddress = nextAddress(start, end);
      final int storeWord = nextWord();

      model.sw(storeCore, storeAddress, storeWord);

      for (int j = 0; j < numberOfLoadsPerStore; j++) {
        final int loadCore = nextCore();
        final int loadAddress = nextAddress(start, end);

        final int loadReceived = model.lw(loadCore, loadAddress);
        final int loadExpected = model.lookup(loadAddress);

        Assert.assertTrue(
            String.format("lw core=%d, address=%x: %x != %x",
                loadCore, loadAddress, loadReceived, loadExpected
            ),
            loadReceived == loadExpected
        );
      }
    }
  }

  @Test
  public void test() {
    test(model, 0x0000, 0xffff);
  }
}
