/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.vliw;

import org.junit.Assert;
import org.junit.Test;

import ru.ispras.microtesk.test.Statistics;

public class CombinationTestCase extends VliwTest {
  @Test
  public void test() {
    setVerbose(true);
    final Statistics statistics = run("combination.rb");
    Assert.assertNotNull(statistics);

    Assert.assertEquals(1,  statistics.getPrograms());
    Assert.assertEquals(4,  statistics.getSequences());
    Assert.assertEquals(56, statistics.getInstructions());
  }

  protected boolean isExpectedError(final String message) {
    return super.isExpectedError(message) || message.contains(
        "Error: Exception handler for IntegerOverflow is not found. " +
        "Have to continue to the next instruction"
         );
  }
}
