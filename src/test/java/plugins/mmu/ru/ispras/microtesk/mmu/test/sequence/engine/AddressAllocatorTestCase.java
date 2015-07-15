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

import org.junit.Test;

import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;

/**
 * Test for {@link AddressAllocator}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class AddressAllocatorTestCase {
  private final AddressAllocator addressAllocator = new AddressAllocator(MmuUnderTest.get().mmu);

  private void runTest(final MmuBuffer buffer) {
    for (int i = 0; i < 10; i++) {
      final long address1 = addressAllocator.allocateIndex(buffer, 0);
      final long address2 = addressAllocator.allocateTag(buffer, address1);

      System.out.format("%s.address1=%016x%n",
          buffer.getName(), address1);
      System.out.format("%s.tag.idx1=%x.%x%n",
          buffer.getName(), buffer.getTag(address1), buffer.getIndex(address1));
      System.out.format("%s.address2=%016x%n",
          buffer.getName(), address2);
      System.out.format("%s.tag.idx2=%x.%x%n",
          buffer.getName(), buffer.getTag(address2), buffer.getIndex(address2));
    }
  }

  @Test
  public void runTest() {
    runTest(MmuUnderTest.get().l1);
    runTest(MmuUnderTest.get().l2);
    runTest(MmuUnderTest.get().l1);
    runTest(MmuUnderTest.get().l2);
  }
}
