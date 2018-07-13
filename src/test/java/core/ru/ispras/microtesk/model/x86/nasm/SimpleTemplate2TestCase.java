/*
 * Copyright 2018 ISP RAS (http://www.ispras.ru)
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

public final class SimpleTemplate2TestCase extends X86NasmTest {
  @Test
  public void test() {
    // To enable debug printing:
    // setCommandLineOption(Option.VERBOSE);

    final Statistics statistics = run("simple_template2.rb");
    Assert.assertNotNull(statistics);

    /*
    Assert.assertEquals(1, statistics.getPrograms());
    Assert.assertEquals(0, statistics.getSequences());
    Assert.assertEquals(22, statistics.getInstructions());
    */
  }
}
