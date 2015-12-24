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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;


public class SignificantBitsLostTestCase {

  // Checks whether the loss of significant bits occurs when 
  // a 32-bit integer value is cast to an N-bit bit vector. 

  @Test
  public void test() {
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Size is Greater

    assertFalse(check(Type.CARD(64), 0xFFFFFFFF));
    assertFalse(check(Type.CARD(64), 0x00000000));
    assertFalse(check(Type.CARD(64), Integer.MAX_VALUE));
    assertFalse(check(Type.CARD(64), Integer.MIN_VALUE));

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Size is Equal

    assertFalse(check(Type.CARD(32), 0xFFFFFFFF));
    assertFalse(check(Type.CARD(32), 0x00000000));
    assertFalse(check(Type.CARD(32), Integer.MAX_VALUE));
    assertFalse(check(Type.CARD(32), Integer.MIN_VALUE));
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Size is Smaller (a multiple of byte (8-bits))

    assertFalse(check(Type.CARD(16), 0x00000001));
    assertFalse(check(Type.CARD(16), 0x0000FFFF));
    assertFalse(check(Type.CARD(16), 0xFFFFFFFF));
    assertFalse(check(Type.CARD(16), 0xFFFF8000));

    assertTrue(check(Type.CARD(16),  0xFFFF0000));
    assertTrue(check(Type.CARD(16),  0xF7FFFFFF));
    assertTrue(check(Type.CARD(16),  0xFFFF7000));
    assertTrue(check(Type.CARD(16),  0xFFFE8000));

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Size is Smaller (not a multiple of byte (8-bits))

    assertFalse(check(Type.CARD(11), 0x00000001));
    assertFalse(check(Type.CARD(11), 0x000007FF));
    assertFalse(check(Type.CARD(11), 0xFFFFFFFF));
    assertFalse(check(Type.CARD(11), 0xFFFFFD00));

    assertTrue(check(Type.CARD(11),  0xFFFFF800));
    assertTrue(check(Type.CARD(11),  0xFFF7FFFF));
    assertTrue(check(Type.CARD(11),  0x7FFFFFFF));
    assertTrue(check(Type.CARD(11),  0xFFFFF3FF));
    assertTrue(check(Type.CARD(11),  0xFFFFECFF));
  }

  private static boolean check(Type type, long value) {
    return DataEngine.isLossOfSignificantBits(type, BigInteger.valueOf(value));
  }
}
