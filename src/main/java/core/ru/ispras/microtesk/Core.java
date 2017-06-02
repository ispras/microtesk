/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.microtesk.mmu.test.engine.memory.AddressDataGenerator;
import ru.ispras.microtesk.test.engine.DefaultEngine;
import ru.ispras.microtesk.test.engine.Engine;
import ru.ispras.microtesk.test.engine.InitializerMaker;
import ru.ispras.microtesk.test.engine.InitializerMakerDefault;
import ru.ispras.microtesk.test.engine.branch.BranchEngine;
import ru.ispras.microtesk.test.engine.branch.BranchInitializerMaker;
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
  public Map<String, Engine> getEngines() {
    final Map<String, Engine> engines = new LinkedHashMap<>();
    final Engine defaultEngine = new DefaultEngine();

    engines.put("default", defaultEngine);
    engines.put("trivial", defaultEngine);
    engines.put(BranchEngine.ID, new BranchEngine());

    return engines;
  }

  @Override
  public Map<String, InitializerMaker> getInitializerMakers() {
    final Map<String, InitializerMaker> result = new LinkedHashMap<>();

    result.put("default", new InitializerMakerDefault());
    result.put(BranchEngine.ID, new BranchInitializerMaker());

    return result;
  }

  @Override
  public Map<String, DataGenerator> getDataGenerators() {
    final Map<String, DataGenerator> dataGenerators = new LinkedHashMap<>();
    dataGenerators.put(AddressDataGenerator.ID, new AddressDataGenerator());

    return dataGenerators;
  }

  @Override
  public void initializeGenerationEnvironment() {
    // Empty. No special action is required.
  }
}
