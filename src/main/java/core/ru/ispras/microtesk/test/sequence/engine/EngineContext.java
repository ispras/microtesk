/*
 * Copyright 2015-2016 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.microtesk.model.api.Model;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.settings.DelaySlotSettings;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.test.LabelManager;
import ru.ispras.microtesk.test.Statistics;
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
  private final Options options;
  private final Model model;
  private final LabelManager labelManager;
  private final DataManager dataManager;
  private final PreparatorStore preparators;
  private final BufferPreparatorStore bufferPreparators;
  private final StreamStore streams;
  private final GeneratorSettings settings;
  private final TestBase testBase;
  private final Statistics statistics;
  private final int delaySlotSize;

  // Address to be used for allocations
  private long address; 

  // TODO: temporal solution for extending the context for custom engines.
  private final Map<String, Object> contextExtensions = new HashMap<>();

  public EngineContext(
      final Options options,
      final Model model,
      final DataManager dataManager,
      final GeneratorSettings settings,
      final Statistics statistics) {
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkNotNull(model);
    InvariantChecks.checkNotNull(dataManager);
    InvariantChecks.checkNotNull(settings, "Settings were not loaded.");
    InvariantChecks.checkNotNull(statistics);

    this.options = options;
    this.model = model;
    this.labelManager = new LabelManager();
    this.dataManager = dataManager;
    this.preparators = new PreparatorStore();
    this.bufferPreparators = new BufferPreparatorStore();
    this.streams = new StreamStore();
    this.settings = settings;

    this.testBase = newTestBase(settings);
    this.statistics = statistics;

    final DelaySlotSettings delaySlotSettings = settings.getDelaySlot();
    this.delaySlotSize = delaySlotSettings != null ? delaySlotSettings.getSize() : 0;

    this.address = options.getValueAsBigInteger(Option.BASE_VA).longValue();
  }

  public Options getOptions() {
    return options;
  }

  public Model getModel() {
    return model;
  }

  public LabelManager getLabelManager() {
    return labelManager;
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

  public Statistics getStatistics() {
    return statistics;
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
