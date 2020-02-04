/*
 * Copyright 2020 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.x86.nasm;

import org.junit.Assert;
import org.junit.Test;

import ru.ispras.microtesk.test.Statistics;

public final class TemplatesTestCase extends X86NasmTest {
  private void runTemplate(final String file,
      final int numberOfPrograms,
      final int numberOfSequences) {
    final Statistics statistics = runTemplate(file);

    Assert.assertEquals(numberOfPrograms, statistics.getPrograms());
    Assert.assertEquals(numberOfSequences, statistics.getSequences());
  }

  private Statistics runTemplate(final String file) {
    final Statistics statistics = run(file);

    Assert.assertNotNull(statistics);

    return statistics;
  }

  @Test
  public void testBlockRandom() {
    runTemplate("block_random.rb");
  }

  @Test
  public void testBlock() {
    runTemplate("block.rb");
  }

  @Test
  public void testBubbleSort386() {
    runTemplate("bubble_sort_386.rb");
  }

  @Test
  public void testBubbleSort() {
    runTemplate("bubble_sort.rb");
  }

  @Test
  public void testDebug00() {
    runTemplate("debud00.rb");
  }

  @Test
  public void testDebug01() {
    runTemplate("debug01.rb", 1, 0);
  }

  @Test
  public void testEuclid() {
    runTemplate("euclid.rb");
  }

  @Test
  public void testRandomImmediate() {
    runTemplate("random_immediate.rb");
  }

  @Test
  public void testRandomRegisters() {
    runTemplate("random_registers.rb");
  }

  @Test
  public void testRandom() {
    runTemplate("random.rb");
  }
}
