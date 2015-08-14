/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.test.sequence.engine;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.allocator.AddressAllocator;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.utils.Range;

/**
 * Test for {@link AddressAllocator}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class AddressAllocatorTestCase {
  public static final boolean PRINT_ADDRESSES = false;

  private final AddressAllocator addressAllocator = new AddressAllocator(
      MmuUnderTest.get().mmu, new HashMap<MmuAddressType, Collection<? extends Range<Long>>>());

  private final Collection<Long> allAddresses =
      addressAllocator.getAllAddresses(MmuUnderTest.get().paAddr, null);

  private final Map<String, Set<Long>> allocatedIndices = new HashMap<>();

  private void update(final MmuBuffer buffer, final long address) {
    final long index = buffer.getIndex(address);

    Set<Long> indices = allocatedIndices.get(buffer.getName());
    if (indices == null) {
      allocatedIndices.put(buffer.getName(), indices = new HashSet<>());
    }

    Assert.assertFalse(indices.contains(index));
    indices.add(index);
  }

  private void updateIndices(final long address) {
    update(MmuUnderTest.get().l1, address);
    update(MmuUnderTest.get().l2, address);
  }

  private void runTest(final MmuBuffer buffer) {
    InvariantChecks.checkNotNull(buffer);

    for (int i = 0; i < 8; i++) {
      final long address1 = addressAllocator.allocateIndex(buffer, 0, null, false, null);
      final long address2 = addressAllocator.allocateTag(buffer, address1, null, false, null);

      Assert.assertTrue(
          String.format("Unexpected address: 0x%016x", address2),
          allAddresses.contains(address2));

      final long index1 = buffer.getIndex(address1);
      final long index2 = buffer.getIndex(address2);

      Assert.assertEquals(index1, index2);

      updateIndices(address2);
    }
  }

  @Test
  public void runTest() {
    System.out.println("All addresses: " + allAddresses.size());

    if (PRINT_ADDRESSES) {
      for (final long address : allAddresses) {
        System.out.format("0x%016x%n", address);
      }
    }

    runTest(MmuUnderTest.get().l1);
    runTest(MmuUnderTest.get().l2);
    runTest(MmuUnderTest.get().l1);
    runTest(MmuUnderTest.get().l2);
  }
}
