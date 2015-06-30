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

import ru.ispras.microtesk.test.sequence.solver.IntegerVariable;

/**
 * Test for {@link MmuCondition}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class MmuConditionTestCase {
  private static final IntegerVariable VAR = new IntegerVariable("VAR", 64);

  public void runTest(final BigInteger min, final BigInteger max) {
    System.out.format("Range: min=%x, max=%x\n", min, max);

    final MmuCondition condition = MmuCondition.RANGE(VAR, min, max);

    System.out.println(condition);
    Assert.assertNotNull(condition);
  }

  @Test
  public void runTest() {
    runTest(new BigInteger("0000000000000000", 16), new BigInteger("000000007FFFffff", 16));
    runTest(new BigInteger("0000000080000000", 16), new BigInteger("000000ffFFFFffff", 16));
    runTest(new BigInteger("4000000000000000", 16), new BigInteger("400000ffFFFFffff", 16));
    runTest(new BigInteger("FFFFffffc0000000", 16), new BigInteger("FFFFffffdFFFffff", 16));
    runTest(new BigInteger("FFFFffffe0000000", 16), new BigInteger("FFFFffffFFFFffff", 16));
    runTest(new BigInteger("c000000000000000", 16), new BigInteger("c00000ff7FFFffff", 16));
  }
}
