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

package ru.ispras.microtesk.model.api.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.model.api.type.Type;

/**
 * This is a test to make sure that shifts work correctly for large shift amounts
 * (shift amount type is more 32-bits wide).
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public class BigShiftTestCase {
  private static final int LARGE_SIZE = 64;
  private static final int SHIFT_AMOUNT = 33;
  private static final Type LARGE = Type.CARD(LARGE_SIZE);

  @Test
  public void test() {
    final Data VALUE = 
        new Data(BitVector.valueOf(0xF0F10FF0DEADBEEFL, LARGE_SIZE), LARGE);

    final Data SHIFT = 
        new Data(BitVector.valueOf(SHIFT_AMOUNT, LARGE_SIZE), LARGE);

    System.out.println(VALUE);
    System.out.println(SHIFT);

    test("1011110101011011011111011101111000000000000000000000000000000000",
        EOperatorID.L_SHIFT, VALUE, SHIFT);

    test("0000000000000000000000000000000001111000011110001000011111111000",
        EOperatorID.R_SHIFT,  VALUE, SHIFT);

    test("1011110101011011011111011101111111100001111000100001111111100001",
        EOperatorID.L_ROTATE, VALUE, SHIFT);

    test("0110111101010110110111110111011111111000011110001000011111111000",
        EOperatorID.R_ROTATE, VALUE, SHIFT);
  }

  private static void test(String expectedStr, EOperatorID op, Data value, Data shift) {
    final Data expected = new Data(BitVector.valueOf(expectedStr, 2, LARGE_SIZE), LARGE);
    final Data result = DataEngine.execute(op, value, shift);

    System.out.println(result);
    assertEquals(expected, result);
  }
}
