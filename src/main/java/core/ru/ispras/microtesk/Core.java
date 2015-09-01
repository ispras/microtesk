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

package ru.ispras.microtesk;

import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.microtesk.test.sequence.engine.Adapter;
import ru.ispras.microtesk.test.sequence.engine.BranchAdapter;
import ru.ispras.microtesk.test.sequence.engine.BranchEngine;
import ru.ispras.microtesk.test.sequence.engine.DefaultAdapter;
import ru.ispras.microtesk.test.sequence.engine.DefaultEngine;
import ru.ispras.microtesk.test.sequence.engine.Engine;
import ru.ispras.microtesk.test.testbase.AddressDataGenerator;
import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.translator.nml.NmlTranslator;
import ru.ispras.testbase.generator.DataGenerator;

/**
 * MicroTESK {@link Core} is organized as a MicroTESK {@link Plugin}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
final class Core implements Plugin {
  @Override
  public Translator<?> getTranslator() {
    return new NmlTranslator();
  }

  @Override
  public Map<String, Engine<?>> getEngines() {
    final Map<String, Engine<?>> engines = new LinkedHashMap<>();

    engines.put("default", new DefaultEngine());
    engines.put("branch", new BranchEngine());

    return engines;
  }

  @Override
  public Map<String, Adapter<?>> getAdapters() {
    final Map<String, Adapter<?>> adapters = new LinkedHashMap<>();

    adapters.put("default", new DefaultAdapter());
    adapters.put("branch", new BranchAdapter());

    return adapters;
  }

  @Override
  public Map<String, DataGenerator> getDataGenerators() {
    final Map<String, DataGenerator> dataGenerators = new LinkedHashMap<>();

    dataGenerators.put("address", new AddressDataGenerator());

    return dataGenerators;
  }
}
