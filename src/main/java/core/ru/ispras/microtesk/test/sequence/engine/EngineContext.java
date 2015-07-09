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

package ru.ispras.microtesk.test.sequence.engine;

import static ru.ispras.microtesk.test.sequence.engine.utils.EngineUtils.newTestBase;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.settings.DelaySlotSettings;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.test.template.PreparatorStore;
import ru.ispras.microtesk.test.template.StreamStore;
import ru.ispras.microtesk.translator.nml.coverage.TestBase;

/**
 * {@link EngineContext} contains information required by an {@link Engine} and an {@link Adapter}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class EngineContext {
  private final IModel model;
  private final PreparatorStore preparators;
  private final StreamStore streams;
  private final GeneratorSettings settings;
  private final TestBase testBase;
  private final int delaySlotSize;

  // Address to be used for allocations
  private long address; 

  // For some code sequence like exception handler no test data generation
  // (including randomization is not needed). This is to disable test data generation.
  private boolean generateData;

  public EngineContext(
      final IModel model,
      final PreparatorStore preparators,
      final StreamStore streams,
      final GeneratorSettings settings) {

    InvariantChecks.checkNotNull(model);
    InvariantChecks.checkNotNull(preparators);
    InvariantChecks.checkNotNull(streams);
    InvariantChecks.checkNotNull(settings);

    this.model = model;
    this.preparators = preparators;
    this.streams = streams;
    this.settings = settings;

    this.testBase = newTestBase(settings);

    final DelaySlotSettings delaySlotSettings = settings.getDelaySlot();
    this.delaySlotSize = delaySlotSettings != null ? delaySlotSettings.getSize() : 0;

    this.address = 0;
    this.generateData = true;
  }

  public IModel getModel() {
    return model;
  }

  public PreparatorStore getPreparators() {
    return preparators;
  }

  public StreamStore getStreams() {
    return streams;
  }

  public GeneratorSettings getSettings() {
    return settings;
  }

  public TestBase getTestBase() {
    return testBase;
  }

  public int getDelaySlotSize() {
    return delaySlotSize;
  }

  public long getAddress() {
    return address;
  }

  public void setAddress(final long value) {
    this.address = value;
  }

  public boolean isGenerateData() {
    return generateData;
  }

  public void setGenerateData(final boolean value) {
    this.generateData = value;
  }
}
