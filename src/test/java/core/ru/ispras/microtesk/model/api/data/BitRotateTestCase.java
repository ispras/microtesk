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
import ru.ispras.microtesk.model.api.memory.Location;

public class BitRotateTestCase {

  @Test
  public void test() {
    final Location lhs = Location.newLocationForConst(
        new Data(BitVector.valueOf(0xDEADBEEF, 32), Type.CARD(32)));

    final Location rhs = Location.newLocationForConst(
        new Data(BitVector.valueOf(4, 32), Type.CARD(32)));

    final Data result1 = DataEngine.execute(
        EOperatorID.R_ROTATE, lhs.load(), rhs.load());
    assertEquals(BitVector.valueOf(0xFDEADBEE, 32), result1.getRawData());

    final Data result2 = DataEngine.execute(
        EOperatorID.R_ROTATE, lhs.bitField(0, 15).load(), rhs.load());
    assertEquals(BitVector.valueOf(0xFBEE, 16), result2.getRawData());
  }
}
