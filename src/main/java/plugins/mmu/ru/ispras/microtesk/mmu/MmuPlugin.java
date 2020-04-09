/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Plugin;
import ru.ispras.microtesk.SysUtils;
import ru.ispras.microtesk.mmu.model.sim.MmuModel;
import ru.ispras.microtesk.mmu.test.engine.memory.AddressDataGenerator;
import ru.ispras.microtesk.mmu.test.engine.memory.MemoryDataGenerator;
import ru.ispras.microtesk.mmu.test.engine.memory.MemoryEngine;
import ru.ispras.microtesk.mmu.test.engine.memory.MemoryInitializerMaker;
import ru.ispras.microtesk.mmu.translator.MmuTranslator;
import ru.ispras.microtesk.mmu.model.spec.MmuSubsystem;
import ru.ispras.microtesk.model.Model;
import ru.ispras.microtesk.model.memory.MemoryDevice;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.test.TestEngine;
import ru.ispras.microtesk.test.engine.Engine;
import ru.ispras.microtesk.test.engine.InitializerMaker;
import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.translator.codegen.PackageInfo;
import ru.ispras.testbase.generator.DataGenerator;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code MmuPlugin} is a MicroTESK plugin responsible for specifying and testing memory management
 * units (MMU) (address translation, caching, etc.).
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuPlugin implements Plugin {
  private static MmuSubsystem spec = null;
  private static MmuModel model = null;

  public static MmuSubsystem getSpecification() {
    if (null != spec) {
      return spec;
    }

    final TestEngine testEngine = TestEngine.getInstance();
    InvariantChecks.checkNotNull(testEngine, "Test engine is uninitialized");

    final String modelName = testEngine.getModel().getName();

    final String specClassName =
        String.format("%s.%s.mmu.spec.Specification", PackageInfo.MODEL_PACKAGE, modelName);

    final MmuSubsystem.Holder specHolder =
        (MmuSubsystem.Holder) SysUtils.loadFromModel(specClassName);
    InvariantChecks.checkNotNull(specHolder, "Failed to load " + specClassName);

    spec = specHolder.getSpecification();

    final GeneratorSettings settings = GeneratorSettings.get();
    if (null != settings) {
      spec.setSettings(settings);
    }

    return spec;
  }

  public static void setSpecification(final MmuSubsystem mmu) {
    InvariantChecks.checkNotNull(mmu);
    spec = mmu;
  }

  public static MmuModel getMmuModel() {
    if (null != model) {
      return model;
    }

    final TestEngine testEngine = TestEngine.getInstance();
    InvariantChecks.checkNotNull(testEngine, "Test engine is uninitialized");

    final String modelName = testEngine.getModel().getName();

    final String modelClassName = String.format(
        "%s.%s.mmu.sim.Model", PackageInfo.MODEL_PACKAGE, modelName);

    final MmuModel mmuModel = (MmuModel) SysUtils.loadFromModel(modelClassName);

    model = mmuModel;
    return model;
  }

  private static Model getModel() {
    final TestEngine testEngine = TestEngine.getInstance();
    InvariantChecks.checkNotNull(testEngine, "Test engine is uninitialized");

    return testEngine.getModel();
  }

  @Override
  public Translator<?> getTranslator() {
    return new MmuTranslator();
  }

  @Override
  public Map<String, Engine> getEngines() {
    final Map<String, Engine> engines = new LinkedHashMap<>();
    engines.put(MemoryEngine.ID, new MemoryEngine());

    return engines;
  }

  @Override
  public Map<String, InitializerMaker> getInitializerMakers() {
    final Map<String, InitializerMaker> initializerMakers = new LinkedHashMap<>();
    initializerMakers.put(MemoryEngine.ID, new MemoryInitializerMaker());

    return initializerMakers;
  }

  @Override
  public Map<String, DataGenerator> getDataGenerators() {
    final Map<String, DataGenerator> dataGenerators = new LinkedHashMap<>();
    dataGenerators.put(AddressDataGenerator.ID, new AddressDataGenerator());
    dataGenerators.put(MemoryEngine.ID, new MemoryDataGenerator());

    return dataGenerators;
  }

  @Override
  public void initializeGenerationEnvironment() {
    final MmuModel mmuModel;
    try {
      mmuModel = getMmuModel();
    } catch (final Exception e) {
      Logger.warning("Failed to load the MMU model. The memory will be accessed directly.");
      e.printStackTrace();
      return;
    }

    final Model model = getModel();
    final MemoryDevice mmuDevice = mmuModel.getMmuDevice();
    final String memoryId = mmuModel.getStorageDeviceId();

    // TODO: The same handler is registered for all PEs though caches are usually local.
    final MemoryDevice storageDevice = model.setMemoryHandler(memoryId, mmuDevice);
    mmuModel.setStorageDevice(storageDevice);

    model.addStateManager(mmuModel);
  }
}
