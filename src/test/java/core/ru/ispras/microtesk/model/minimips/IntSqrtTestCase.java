/*
 * Copyright 2016-2019 ISP RAS (http://www.ispras.ru)
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

public class IntSqrtTestCase extends MiniMipsTest {
  private Statistics test(final String file) {
    final Statistics statistics = run(file);
    Assert.assertNotNull(statistics);

    return statistics;
  }

  @Test
  public void testSqrt() {
    final Statistics statistics = test("int_sqrt.rb");

    Assert.assertEquals(1, statistics.getPrograms());
    Assert.assertEquals(0, statistics.getSequences());
    Assert.assertEquals(23, statistics.getInstructions());
  }

  @Test
  public void testSqrt4() {
    final Statistics statistics = test("int_sqrt4.rb");

    Assert.assertEquals(1, statistics.getPrograms());
    Assert.assertEquals(0, statistics.getSequences());
    Assert.assertEquals(24, statistics.getInstructions());
  }
}
