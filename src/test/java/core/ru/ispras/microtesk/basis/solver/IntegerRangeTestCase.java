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

package ru.ispras.microtesk.basis.solver;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import ru.ispras.microtesk.basis.solver.integer.IntegerRange;

/**
 * Test for {@link IntegerRange}.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */

public class IntegerRangeTestCase {
  private void printRanges(final List<IntegerRange> ranges, int index) {
    System.out.format(" ===== Range list %x ===== \n", index);
    for (final IntegerRange range : ranges) {
      System.out.format("%s\n", range);
    }
    System.out.format("\n");
  }

  /**
   * [0,1], [0,2], [1,1], [0,63], [12,12], [15,17], [16,25], [24,53].
   */
  @Test
  public void runTest0() {
    final Set<IntegerRange> inRanges = new HashSet<>();
    inRanges.add(new IntegerRange(16, 25));
    inRanges.add(new IntegerRange(0, 1));
    inRanges.add(new IntegerRange(24, 53));
    inRanges.add(new IntegerRange(12, 12));
    inRanges.add(new IntegerRange(0, 2));
    inRanges.add(new IntegerRange(1, 1));
    inRanges.add(new IntegerRange(0, 63));
    inRanges.add(new IntegerRange(15, 17));
    final List<IntegerRange> outRanges = IntegerRange.divide(inRanges);
    printRanges(outRanges, 0);

    final Set<IntegerRange> checkRanges = new HashSet<>();
    checkRanges.add(new IntegerRange(0, 0));
    checkRanges.add(new IntegerRange(1, 1));
    checkRanges.add(new IntegerRange(2, 2));
    checkRanges.add(new IntegerRange(3, 11));
    checkRanges.add(new IntegerRange(12, 12));
    checkRanges.add(new IntegerRange(13, 14));
    checkRanges.add(new IntegerRange(15, 15));
    checkRanges.add(new IntegerRange(16, 17));
    checkRanges.add(new IntegerRange(18, 23));
    checkRanges.add(new IntegerRange(24, 25));
    checkRanges.add(new IntegerRange(26, 53));
    checkRanges.add(new IntegerRange(54, 63));

    for (IntegerRange range : outRanges) {
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
    final Set<IntegerRange> inRanges = new HashSet<>();
    inRanges.add(new IntegerRange(0, 127));
    inRanges.add(new IntegerRange(0, 55));
    inRanges.add(new IntegerRange(3, 5));
    inRanges.add(new IntegerRange(27, 63));
    inRanges.add(new IntegerRange(12, 26));
    inRanges.add(new IntegerRange(15, 30));
    inRanges.add(new IntegerRange(31, 111));
    inRanges.add(new IntegerRange(100, 101));
    inRanges.add(new IntegerRange(91, 100));
    final List<IntegerRange> outRanges = IntegerRange.divide(inRanges);
    printRanges(outRanges, 1);
  }

  /**
   * [0,127], [0,55], [44,99].
   */
  @Test
  public void runTest2() {
    final Set<IntegerRange> inRanges = new HashSet<>();
    inRanges.add(new IntegerRange(0, 127));
    inRanges.add(new IntegerRange(0, 55));
    inRanges.add(new IntegerRange(44, 99));
    final List<IntegerRange> outRanges = IntegerRange.divide(inRanges);
    printRanges(outRanges, 2);
  }

  /**
   * [0,127], [0,55], [44,99], [55,111].
   */
  @Test
  public void runTest3() {
    final Set<IntegerRange> inRanges = new HashSet<>();
    inRanges.add(new IntegerRange(0, 127));
    inRanges.add(new IntegerRange(0, 55));
    inRanges.add(new IntegerRange(44, 99));
    inRanges.add(new IntegerRange(55, 111));
    final List<IntegerRange> outRanges = IntegerRange.divide(inRanges);
    printRanges(outRanges, 3);
  }

  /**
   * [0,127], [0,55], [58,99].
   */
  @Test
  public void runTest4() {
    final Set<IntegerRange> inRanges = new HashSet<>();
    inRanges.add(new IntegerRange(0, 127));
    inRanges.add(new IntegerRange(0, 55));
    inRanges.add(new IntegerRange(58, 99));
    final List<IntegerRange> outRanges = IntegerRange.divide(inRanges);
    printRanges(outRanges, 4);
  }

  /**
   * [23,127], [22,55], [44,99], [55,111].
   */
  @Test
  public void runTest5() {
    final Set<IntegerRange> inRanges = new HashSet<>();
    inRanges.add(new IntegerRange(23, 127));
    inRanges.add(new IntegerRange(22, 55));
    inRanges.add(new IntegerRange(44, 99));
    inRanges.add(new IntegerRange(55, 111));
    final List<IntegerRange> outRanges = IntegerRange.divide(inRanges);
    printRanges(outRanges, 5);
  }
}
