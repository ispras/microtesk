/*
 * Copyright 2017-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.memory;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

public final class MemoryTrackerTestCase {
  @Test
  public void test() {
    final MemoryTracker tracker = new MemoryTracker();

    use(tracker, 0x00000100, 0x00000200);

    checkUnused(tracker, 0x000000FF);
    checkUsed(tracker, 0x00000100);
    checkUsed(tracker, 0x000001FF);
    checkUnused(tracker, 0x00000200);
    checkUnused(tracker, 0x00000201);

    use(tracker, 0x00000300, 0x00000388);
    use(tracker, 0x00000000, 0x00000100);
    use(tracker, 0x00000400, 0x00000500);
    use(tracker, 0x00000388, 0x00000400);
    use(tracker, 0x00000200, 0x00000250);
    use(tracker, 0x00000250, 0x00000300);

    final MemoryTracker.Region overlapping =
        tracker.use(BigInteger.valueOf(0x00000000), BigInteger.valueOf(0x00000500));

    Assert.assertEquals(
        new MemoryTracker.Region(BigInteger.valueOf(0x00000000), BigInteger.valueOf(0x00000500)),
        overlapping
    );
  }

  private static void use(final MemoryTracker tracker, final long start, final long end) {
    tracker.use(BigInteger.valueOf(start), BigInteger.valueOf(end));
  }

  private static void checkUsed(final MemoryTracker tracker, final long address) {
    Assert.assertTrue(
        String.format("0x%016x must be used", address),
        tracker.isUsed(BigInteger.valueOf(address))
    );
  }

  private static void checkUnused(final MemoryTracker tracker, final long address) {
    Assert.assertFalse(
        String.format("0x%016x must be unused", address),
        tracker.isUsed(BigInteger.valueOf(address))
    );
  }
}
