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

package ru.ispras.microtesk.mmu;

import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Plugin;
import ru.ispras.microtesk.SysUtils;
import ru.ispras.microtesk.mmu.test.sequence.engine.MemoryAdapter;
import ru.ispras.microtesk.mmu.test.sequence.engine.MemoryEngine;
import ru.ispras.microtesk.mmu.test.testbase.MmuDataGenerator;
import ru.ispras.microtesk.mmu.translator.MmuTranslator;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.test.TestEngine;
import ru.ispras.microtesk.test.sequence.engine.Adapter;
import ru.ispras.microtesk.test.sequence.engine.Engine;
import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.testbase.generator.DataGenerator;

/**
 * {@code MmuPlugin} is a MicroTESK plugin responsible for specifying and testing memory management
 * units (MMU) (address translation, caching, etc.).
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuPlugin implements Plugin {
  private static MmuSubsystem spec = null;

  public static MmuSubsystem getSpecification() {
    if (null != spec) {
      return spec;
    }

    final TestEngine testEngine = TestEngine.getInstance();
    if (null == testEngine) {
      throw new IllegalStateException("TestEngine is not initialized.");
    }

    final String modelName =
        testEngine.getModel().getName();

    final String specClassName = String.format(
        "%s.%s.mmu.Specification", PackageInfo.MODEL_PACKAGE, modelName);

    final MmuSubsystem.Holder specHolder =
        (MmuSubsystem.Holder) SysUtils.loadFromModel(specClassName);

    if (null == specHolder) {
      throw new IllegalStateException("Failed to load " + specClassName);
    }

    spec = specHolder.getSpecification();
    return spec;
  }

  public static void setSpecification(final MmuSubsystem mmu) {
    InvariantChecks.checkNotNull(mmu);
    spec = mmu;
  }

  @Override
  public Translator<?> getTranslator() {
    return new MmuTranslator();
  }

  @Override
  public Map<String, Engine<?>> getEngines() {
    final Map<String, Engine<?>> engines = new LinkedHashMap<>();

    engines.put("memory", new MemoryEngine());

    return engines;
  }

  @Override
  public Map<String, Adapter<?>> getAdapters() {
    final Map<String, Adapter<?>> adapters = new LinkedHashMap<>();

    adapters.put("memory", new MemoryAdapter());

    return adapters;
  }

  @Override
  public Map<String, DataGenerator> getDataGenerators() {
    final Map<String, DataGenerator> dataGenerators = new LinkedHashMap<>();

    dataGenerators.put("access", new MmuDataGenerator());

    return dataGenerators;
  }
}
