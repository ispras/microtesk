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

import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.settings.DelaySlotSettings;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.test.template.BufferPreparatorStore;
import ru.ispras.microtesk.test.template.DataManager;
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
  private final DataManager dataManager;
  private final PreparatorStore preparators;
  private final BufferPreparatorStore bufferPreparators;
  private final StreamStore streams;
  private final GeneratorSettings settings;
  private final TestBase testBase;
  private final int delaySlotSize;

  // Address to be used for allocations
  private long address; 

  // For some code sequence like exception handler no test data generation
  // (including randomization is not needed). This is to disable test data generation.
  private boolean generateData;

  // TODO: temporal solution for extending the context for custom engines.
  private final Map<String, Object> contextExtensions = new HashMap<>();

  public EngineContext(
      final IModel model,
      final DataManager dataManager,
      final PreparatorStore preparators,
      final BufferPreparatorStore bufferPreparators,
      final StreamStore streams,
      final GeneratorSettings settings) {

    InvariantChecks.checkNotNull(model);
    InvariantChecks.checkNotNull(dataManager);
    InvariantChecks.checkNotNull(preparators);
    InvariantChecks.checkNotNull(bufferPreparators);
    InvariantChecks.checkNotNull(streams);
    InvariantChecks.checkNotNull(settings);

    this.model = model;
    this.dataManager = dataManager;
    this.preparators = preparators;
    this.bufferPreparators = bufferPreparators;
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

  public DataManager getDataManager() {
    return dataManager;
  }

  public PreparatorStore getPreparators() {
    return preparators;
  }

  public BufferPreparatorStore getBufferPreparators() {
    return bufferPreparators;
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

  public Object getCustomContext(final String id) {
    InvariantChecks.checkNotNull(id);
    return contextExtensions.get(id);
  }

  public void setCustomContext(final String id, final Object context) {
    InvariantChecks.checkNotNull(id);
    InvariantChecks.checkNotNull(context);

    contextExtensions.put(id, context);
  }
}
