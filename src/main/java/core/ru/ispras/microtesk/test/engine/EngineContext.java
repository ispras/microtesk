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

package ru.ispras.microtesk.test.engine;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.Model;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.settings.DelaySlotSettings;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.test.LabelManager;
import ru.ispras.microtesk.test.Statistics;
import ru.ispras.microtesk.test.template.BufferPreparatorStore;
import ru.ispras.microtesk.test.template.DataDirectiveFactory;
import ru.ispras.microtesk.test.template.MemoryPreparatorStore;
import ru.ispras.microtesk.test.template.PreparatorStore;
import ru.ispras.microtesk.test.template.StreamStore;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link EngineContext} contains information required by an {@link Engine}.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class EngineContext {
  private final Options options;
  private final Model model;
  private final LabelManager labelManager;
  private final PreparatorStore preparators;
  private final BufferPreparatorStore bufferPreparators;
  private final MemoryPreparatorStore memoryPreparators;
  private final StreamStore streams;
  private final Statistics statistics;
  private final int delaySlotSize;
  private long codeAllocationAddress;
  private DataDirectiveFactory dataDirectiveFactory;

  // TODO: temporal solution for extending the context for custom engines.
  private final Map<String, Object> contextExtensions = new HashMap<>();

  public EngineContext(
      final Options options,
      final Model model,
      final Statistics statistics) {
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkNotNull(model);
    InvariantChecks.checkNotNull(statistics);

    this.options = options;
    this.model = model;
    this.labelManager = new LabelManager();
    this.preparators = new PreparatorStore();
    this.bufferPreparators = new BufferPreparatorStore();
    this.memoryPreparators = new MemoryPreparatorStore();
    this.streams = new StreamStore();

    final GeneratorSettings settings = GeneratorSettings.get();
    InvariantChecks.checkNotNull(settings, "Settings were not loaded.");

    this.statistics = statistics;

    final DelaySlotSettings delaySlotSettings = settings.getDelaySlot();
    this.delaySlotSize = delaySlotSettings != null ? delaySlotSettings.getSize() : 0;

    this.codeAllocationAddress = 0;
    this.dataDirectiveFactory = null;
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

  public PreparatorStore getPreparators() {
    return preparators;
  }

  public BufferPreparatorStore getBufferPreparators() {
    return bufferPreparators;
  }

  public MemoryPreparatorStore getMemoryPreparators() {
    return memoryPreparators;
  }

  public StreamStore getStreams() {
    return streams;
  }

  public Statistics getStatistics() {
    return statistics;
  }

  public int getDelaySlotSize() {
    return delaySlotSize;
  }

  public long getCodeAllocationAddress() {
    return codeAllocationAddress;
  }

  public void setCodeAllocationAddress(final long value) {
    this.codeAllocationAddress = value;
  }

  public DataDirectiveFactory getDataDirectiveFactory() {
    return dataDirectiveFactory;
  }

  public void setDataDirectiveFactory(final DataDirectiveFactory value) {
    InvariantChecks.checkNotNull(value);
    InvariantChecks.checkTrue(null == dataDirectiveFactory);
    dataDirectiveFactory = value;
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
