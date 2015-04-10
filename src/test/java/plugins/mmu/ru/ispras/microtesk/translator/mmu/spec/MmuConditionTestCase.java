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

package ru.ispras.microtesk.translator.mmu.spec;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

import ru.ispras.unitesk.processor.test.basis.IntegerVariable;

/**
 * Test for {@link MmuCondition}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class MmuConditionTestCase {
  private static final IntegerVariable VAR = new IntegerVariable("VAR", 64);

  public void runTest(long min, long max) {
    System.out.format("Range: min=%x, max=%x\n", min, max);

    final MmuCondition condition = MmuCondition.RANGE(VAR,
        BigInteger.valueOf(min), BigInteger.valueOf(max));

    System.out.println(condition);
    Assert.assertNotNull(condition);
  }

  @Test
  public void runTest() {
    runTest(0x000L, 0xfffL);
    runTest(0x0000000000000000L, 0x000000007FFFffffL);
    runTest(0x08L, 0xffL);
    runTest(0x0000000080000000L, 0x000000ffFFFFffffL);
    runTest(0x4000000000000000L, 0x400000ffFFFFffffL);
  }
}
