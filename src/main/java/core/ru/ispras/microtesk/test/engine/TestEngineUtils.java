/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.TestSequence;
import ru.ispras.microtesk.test.sequence.GeneratorConfig;
import ru.ispras.microtesk.test.sequence.engine.Adapter;
import ru.ispras.microtesk.test.sequence.engine.AdapterResult;
import ru.ispras.microtesk.test.sequence.engine.Engine;
import ru.ispras.microtesk.test.sequence.engine.EngineContext;
import ru.ispras.microtesk.test.sequence.engine.TestSequenceEngine;
import ru.ispras.microtesk.test.template.Block;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.testbase.knowledge.iterator.Iterator;

public final class TestEngineUtils {
  private TestEngineUtils() {}

  public static TestSequenceEngine getEngine(final Block block) {
    InvariantChecks.checkNotNull(block);

    final String engineName;
    final String adapterName;

    if (block.isExternal()) {
      engineName = "trivial";
      adapterName = engineName;
    } else {
      engineName = block.getAttribute("engine", "default");
      adapterName = block.getAttribute("adapter", engineName);
    }

    final Engine<?> engine = GeneratorConfig.get().getEngine(engineName);
    InvariantChecks.checkNotNull(engine);

    final Adapter<?> adapter = GeneratorConfig.get().getAdapter(adapterName);
    InvariantChecks.checkNotNull(adapter);

    if (!adapter.getSolutionClass().isAssignableFrom(engine.getSolutionClass())) {
      throw new IllegalStateException("Mismatched solver/adapter pair");
    }

    final TestSequenceEngine testSequenceEngine = new TestSequenceEngine(engine, adapter);
    testSequenceEngine.configure(block.getAttributes());

    return testSequenceEngine;
  }

  public static List<Call> getSingleSequence(final Block block) {
    InvariantChecks.checkNotNull(block);

    final Iterator<List<Call>> iterator = block.getIterator();
    iterator.init();

    if (!iterator.hasValue()) {
      return Collections.emptyList();
    }

    final List<Call> result = iterator.value();

    iterator.next();
    InvariantChecks.checkFalse(iterator.hasValue(), "A single sequence is expected.");

    return result;
  }

  public static TestSequence getTestSequence(final AdapterResult adapterResult) {
    InvariantChecks.checkNotNull(adapterResult);

    if (adapterResult.getStatus() != AdapterResult.Status.OK) {
      throw new GenerationAbortedException(String.format(
          "Adapter Error: %s", adapterResult.getErrors()));
    }

    final TestSequence result = adapterResult.getResult();
    InvariantChecks.checkNotNull(result);

    return result;
  }

  public static TestSequence getSingleTestSequence(final Iterator<AdapterResult> iterator) {
    InvariantChecks.checkNotNull(iterator);

    iterator.init();
    InvariantChecks.checkTrue(iterator.hasValue());

    final TestSequence result = getTestSequence(iterator.value());

    iterator.next();
    InvariantChecks.checkFalse(iterator.hasValue(), "A single sequence is expected.");

    return result;
  }

  public static TestSequence makeTestSequenceForExternalBlock(
      final EngineContext engineContext,
      final Block block) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(block);
    InvariantChecks.checkTrue(block.isExternal());

    final TestSequenceEngine engine = getEngine(block);
    final List<Call> abstractSequence = getSingleSequence(block);

    final Iterator<AdapterResult> iterator = engine.process(engineContext, abstractSequence);
    return getSingleTestSequence(iterator);
  }
}
