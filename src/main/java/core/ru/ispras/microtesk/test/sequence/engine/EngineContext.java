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

import static ru.ispras.microtesk.test.sequence.engine.common.EngineUtils.newTestBase;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.test.template.DataStreamStore;
import ru.ispras.microtesk.test.template.PreparatorStore;
import ru.ispras.microtesk.translator.nml.coverage.TestBase;

/**
 * {@link EngineContext} contains information required by an {@link Engine} and an {@link Adapter}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class EngineContext {
  private final IModel model;
  private final PreparatorStore preparators;
  private final DataStreamStore dataStreams;
  private final GeneratorSettings settings;
  private final TestBase testBase;

  // TODO:
  private final int delaySlotSize; 

  public EngineContext(
      final IModel model,
      final PreparatorStore preparators,
      final DataStreamStore dataStreams,
      final GeneratorSettings settings) {

    InvariantChecks.checkNotNull(model);
    InvariantChecks.checkNotNull(preparators);
    InvariantChecks.checkNotNull(dataStreams);
    InvariantChecks.checkNotNull(settings);

    this.model = model;
    this.preparators = preparators;
    this.dataStreams = dataStreams;
    this.settings = settings;

    this.testBase = newTestBase(settings);

    // TODO:
    this.delaySlotSize = 0;
  }

  public IModel getModel() {
    return model;
  }

  public PreparatorStore getPreparators() {
    return preparators;
  }

  public DataStreamStore getDataStreams() {
    return dataStreams;
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
}
