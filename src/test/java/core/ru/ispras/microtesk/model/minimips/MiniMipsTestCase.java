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

package ru.ispras.microtesk.model.minimips;

import org.junit.Assert;
import org.junit.Test;

import ru.ispras.microtesk.test.Statistics;

public class MiniMipsTestCase extends MiniMipsTest {
  private void test(
      final String file,
      final int numberOfPrograms,
      final int numberOfSequences) {
    final Statistics statistics = run(file);

    Assert.assertNotNull(statistics);
    Assert.assertEquals(numberOfPrograms, statistics.getPrograms());
    Assert.assertEquals(numberOfSequences, statistics.getSequences());
  }

  @Test
  public void testSqrt() {
    test("int_sqrt.rb", 1, 0);
  }

  @Test
  public void testSqrt4() {
    test("int_sqrt4.rb", 1, 0);
  }

  @Test
  public void testMinMax() {
    test("min_max.rb", 1, 0);
  }

  @Test
  public void testMultipleEngines() {
    test("multiple_engines.rb", 1, 10);
  }

  @Test
  public void testPageTable() {
    test("page_table.rb", 1, 0);
  }
}
