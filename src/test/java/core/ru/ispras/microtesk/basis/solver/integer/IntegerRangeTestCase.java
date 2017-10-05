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

package ru.ispras.microtesk.basis.solver.integer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import ru.ispras.fortress.data.types.bitvector.BitVector;

/**
 * Test for {@link BitVectorRange}.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */

public class IntegerRangeTestCase {
  private void printRanges(final List<BitVectorRange> ranges, int index) {
    System.out.format(" ===== Range list %x ===== \n", index);
    for (final BitVectorRange range : ranges) {
      System.out.format("%s\n", range);
    }
    System.out.format("\n");
  }

  /**
   * [0,1], [0,2], [1,1], [0,63], [12,12], [15,17], [16,25], [24,53].
   */
  @Test
  public void runTest0() {
    final Set<BitVectorRange> inRanges = new HashSet<>();
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(16, 32),
        BitVector.valueOf(25, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(0, 32),
        BitVector.valueOf(1, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(24, 32),
        BitVector.valueOf(53, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(12, 32),
        BitVector.valueOf(12, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(0, 32),
        BitVector.valueOf(2, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(1, 32),
        BitVector.valueOf(1, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(0, 32),
        BitVector.valueOf(63, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(15, 32),
        BitVector.valueOf(17, 32)));
    final List<BitVectorRange> outRanges = BitVectorRange.divide(inRanges);
    printRanges(outRanges, 0);

    final Set<BitVectorRange> checkRanges = new HashSet<>();
    checkRanges.add(new BitVectorRange(
        BitVector.valueOf(0, 32),
        BitVector.valueOf(0, 32)));
    checkRanges.add(new BitVectorRange(
        BitVector.valueOf(1, 32),
        BitVector.valueOf(1, 32)));
    checkRanges.add(new BitVectorRange(
        BitVector.valueOf(2, 32),
        BitVector.valueOf(2, 32)));
    checkRanges.add(new BitVectorRange(
        BitVector.valueOf(3, 32),
        BitVector.valueOf(11, 32)));
    checkRanges.add(new BitVectorRange(
        BitVector.valueOf(12, 32),
        BitVector.valueOf(12, 32)));
    checkRanges.add(new BitVectorRange(
        BitVector.valueOf(13, 32),
        BitVector.valueOf(14, 32)));
    checkRanges.add(new BitVectorRange(
        BitVector.valueOf(15, 32),
        BitVector.valueOf(15, 32)));
    checkRanges.add(new BitVectorRange(
        BitVector.valueOf(16, 32),
        BitVector.valueOf(17, 32)));
    checkRanges.add(new BitVectorRange(
        BitVector.valueOf(18, 32),
        BitVector.valueOf(23, 32)));
    checkRanges.add(new BitVectorRange(
        BitVector.valueOf(24, 32),
        BitVector.valueOf(25, 32)));
    checkRanges.add(new BitVectorRange(
        BitVector.valueOf(26, 32),
        BitVector.valueOf(53, 32)));
    checkRanges.add(new BitVectorRange(
        BitVector.valueOf(54, 32),
        BitVector.valueOf(63, 32)));

    for (BitVectorRange range : outRanges) {
      if (!checkRanges.contains(range)) {
        throw new IllegalStateException("Error: invalid range " + range);
      }
    }
  }

  /**
   * [0,127], [0,55], [3,5], [27,63], [12,26], [15,30], [31,111], [100,101], [91,100].
   */
  @Test
  public void runTest1() {
    final Set<BitVectorRange> inRanges = new HashSet<>();
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(0, 32),
        BitVector.valueOf(127, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(0, 32),
        BitVector.valueOf(55, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(3, 32),
        BitVector.valueOf(5, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(27, 32),
        BitVector.valueOf(63, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(12, 32),
        BitVector.valueOf(26, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(15, 32),
        BitVector.valueOf(30, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(31, 32),
        BitVector.valueOf(111, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(100, 32),
        BitVector.valueOf(101, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(91, 32),
        BitVector.valueOf(100, 32)));

    final List<BitVectorRange> outRanges = BitVectorRange.divide(inRanges);
    printRanges(outRanges, 1);
  }

  /**
   * [0,127], [0,55], [44,99].
   */
  @Test
  public void runTest2() {
    final Set<BitVectorRange> inRanges = new HashSet<>();
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(0, 32),
        BitVector.valueOf(127, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(0, 32),
        BitVector.valueOf(55, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(44, 32),
        BitVector.valueOf(99, 32)));

    final List<BitVectorRange> outRanges = BitVectorRange.divide(inRanges);
    printRanges(outRanges, 2);
  }

  /**
   * [0,127], [0,55], [44,99], [55,111].
   */
  @Test
  public void runTest3() {
    final Set<BitVectorRange> inRanges = new HashSet<>();
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(0, 32),
        BitVector.valueOf(127, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(0, 32),
        BitVector.valueOf(55, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(44, 32),
        BitVector.valueOf(99, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(55, 32),
        BitVector.valueOf(111, 32)));

    final List<BitVectorRange> outRanges = BitVectorRange.divide(inRanges);
    printRanges(outRanges, 3);
  }

  /**
   * [0,127], [0,55], [58,99].
   */
  @Test
  public void runTest4() {
    final Set<BitVectorRange> inRanges = new HashSet<>();
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(0, 32),
        BitVector.valueOf(127, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(0, 32),
        BitVector.valueOf(55, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(58, 32),
        BitVector.valueOf(99, 32)));

    final List<BitVectorRange> outRanges = BitVectorRange.divide(inRanges);
    printRanges(outRanges, 4);
  }

  /**
   * [23,127], [22,55], [44,99], [55,111].
   */
  @Test
  public void runTest5() {
    final Set<BitVectorRange> inRanges = new HashSet<>();
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(23, 32),
        BitVector.valueOf(127, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(22, 32),
        BitVector.valueOf(55, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(44, 32),
        BitVector.valueOf(99, 32)));
    inRanges.add(new BitVectorRange(
        BitVector.valueOf(55, 32),
        BitVector.valueOf(111, 32)));

    final List<BitVectorRange> outRanges = BitVectorRange.divide(inRanges);
    printRanges(outRanges, 5);
  }
}
