/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.data;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

public class SignificantBitsLostTestCase {

  // Checks whether the loss of significant bits occurs when
  // a 32-bit integer value is cast to an N-bit bit vector.

  @Test
  public void test() {
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Size is Greater

    Assert.assertFalse(check(Type.CARD(64), 0xFFFFFFFF));
    Assert.assertFalse(check(Type.CARD(64), 0x00000000));
    Assert.assertFalse(check(Type.CARD(64), Integer.MAX_VALUE));
    Assert.assertFalse(check(Type.CARD(64), Integer.MIN_VALUE));

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Size is Equal

    Assert.assertFalse(check(Type.CARD(32), 0xFFFFFFFF));
    Assert.assertFalse(check(Type.CARD(32), 0x00000000));
    Assert.assertFalse(check(Type.CARD(32), Integer.MAX_VALUE));
    Assert.assertFalse(check(Type.CARD(32), Integer.MIN_VALUE));

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Size is Smaller (a multiple of byte (8-bits))

    Assert.assertFalse(check(Type.CARD(16), 0x00000001));
    Assert.assertFalse(check(Type.CARD(16), 0x0000FFFF));
    Assert.assertFalse(check(Type.CARD(16), 0xFFFFFFFF));
    Assert.assertFalse(check(Type.CARD(16), 0xFFFF8000));

    Assert.assertTrue(check(Type.CARD(16),  0x00010000));
    Assert.assertTrue(check(Type.CARD(16),  0xFFFF0000));
    Assert.assertTrue(check(Type.CARD(16),  0xF7FFFFFF));
    Assert.assertTrue(check(Type.CARD(16),  0xFFFF7000));
    Assert.assertTrue(check(Type.CARD(16),  0xFFFE8000));

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Size is Smaller (not a multiple of byte (8-bits))

    Assert.assertFalse(check(Type.CARD(11), 0x00000001));
    Assert.assertFalse(check(Type.CARD(11), 0x000007FF));
    Assert.assertFalse(check(Type.CARD(11), 0xFFFFFFFF));
    Assert.assertFalse(check(Type.CARD(11), 0xFFFFFD00));

    Assert.assertTrue(check(Type.CARD(11),  0x00000800));
    Assert.assertTrue(check(Type.CARD(11),  0xFFFFF800));
    Assert.assertTrue(check(Type.CARD(11),  0xFFF7FFFF));
    Assert.assertTrue(check(Type.CARD(11),  0x7FFFFFFF));
    Assert.assertTrue(check(Type.CARD(11),  0xFFFFF3FF));
    Assert.assertTrue(check(Type.CARD(11),  0xFFFFECFF));
  }

  private static boolean check(Type type, long value) {
    return Data.isLossOfSignificantBits(type, BigInteger.valueOf(value));
  }
}
