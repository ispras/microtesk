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
import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.microtesk.mmu.model.sim.model.Model;

public abstract class CacheUnitTestCase {

  protected final int nextCore() {
    return Randomizer.get().nextIntRange(0, Model.N1 - 1);
  }

  protected final int nextAddress(final int startAddress, final int endAddress) {
    final int l1tag = Randomizer.get().nextIntRange(startAddress >>> 12, endAddress >>> 12);
    final int l1idx = Randomizer.get().nextIntRange(0, 1);
    final int l1pos = Randomizer.get().nextIntRange(0, 7);

    return (l1tag << 12) | (l1idx << 5) | (l1pos << 2);
  }

  protected final int nextWord() {
    return Randomizer.get().nextInt();
  }

  protected final void test(
      final Model model,
      final int startAddress,
      final int endAddress,
      final boolean initMemory,
      final int numberOfTests,
      final int numberOfLoadsPerStore) {

    if (initMemory) {
      model.memset(startAddress, endAddress, 0xdeadbeef);
    }

    for (int i = 0; i < numberOfTests; i++) {
      final int storeCore = nextCore();
      final int storeAddress = nextAddress(startAddress, endAddress);
      final int storeWord = nextWord();

      model.sw(storeCore, storeAddress, storeWord);

      for (int j = 0; j < numberOfLoadsPerStore; j++) {
        final int loadCore = nextCore();
        final int loadAddress = nextAddress(startAddress, endAddress);

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
}
