/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mips.test.branch;

import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestData;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * {@link MipsGtzDataGenerator} is a test data generator for BGTZ-family instructions.
 */
public final class MipsGtzDataGenerator extends MipsBranchDataGenerator {
  @Override
  public Iterator<TestData> generateThen(final TestBaseQuery query) {
    return generate(query, positiveValue());
  }

  @Override
  public Iterator<TestData> generateElse(final TestBaseQuery query) {
    return generate(query, nonPositiveValue());
  }
}
