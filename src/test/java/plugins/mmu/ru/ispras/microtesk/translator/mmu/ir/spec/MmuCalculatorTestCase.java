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

package ru.ispras.microtesk.translator.mmu.ir.spec;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.microtesk.test.sequence.solver.IntegerField;
import ru.ispras.microtesk.test.sequence.solver.IntegerVariable;
import ru.ispras.microtesk.translator.mmu.ir.spec.MmuCalculator;
import ru.ispras.microtesk.translator.mmu.ir.spec.MmuExpression;

/**
 * Test for {@link MmuCalculator}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class MmuCalculatorTestCase {
  private static final IntegerVariable VAR = new IntegerVariable("VAR", 64);

  private void runTest(int width, int count) {
    final MmuExpression expression = new MmuExpression();

    for (int i = 0; i < count; i++) {
      final IntegerField field = new IntegerField(VAR, i, (i + width) - 1);
      expression.addHiTerm(field);
    }

    System.out.format("Test: width=%d, count=%d\n", width, count);
    System.out.format("Expr: %s\n", expression);

    BigInteger result = MmuCalculator.eval(expression, VAR, BigInteger.ZERO);
    Assert.assertEquals(BigInteger.ZERO, result);

    long value = Randomizer.get().nextLong();
    System.out.format("Value: %x\n", value);

    result = MmuCalculator.eval(expression, VAR, BigInteger.valueOf(value));

    long reference = 0;

    for (int i = 0; i < count; i++) {
      reference |= ((value >> i) & (width == Long.SIZE ? -1L : (1L << width) - 1)) << width * i;
    }

    Assert.assertEquals(Long.toHexString(reference), Long.toHexString(result.longValue()));
  }

  @Test
  public void runTest() {
    runTest(1234, 0);

    for (int i = 1; i <= Long.SIZE; i++) {
      runTest(i, Long.SIZE / i);
    }
  }
}
