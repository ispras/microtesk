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

package ru.ispras.microtesk.test.mmu.testbase;

import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestDataProvider;
import ru.ispras.testbase.generator.DataGenerator;
import ru.ispras.testbase.stub.Utils;

/**
 * {@link MmuDataGenerator} is a test data generator for memory access instructions.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuDataGenerator implements DataGenerator {
  public static final String PARAM_PATH = "path";
  public static final String PARAM_DEPS = "deps";

  @Override
  public final boolean isSuitable(final TestBaseQuery query) {
    final Object path = Utils.getParameter(query, PARAM_PATH);
    final Object deps = Utils.getParameter(query, PARAM_DEPS);

    return path != null && deps != null;
  }

  @Override
  public final TestDataProvider generate(final TestBaseQuery query) {
    // TODO:
    //final ExecutionPath path = (ExecutionPath) Utils.getParameter(query, PARAM_PATH);
    //final List<Dependency> deps = (List<Dependency>) Utils.getParameter(query, PARAM_DEPS);

    return null;
  }
}
