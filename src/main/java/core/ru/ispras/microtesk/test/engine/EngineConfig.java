/*
 * Copyright 2013-2017 ISP RAS (http://www.ispras.ru)
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.engine.Adapter;
import ru.ispras.microtesk.test.engine.Engine;

/**
 * {@link EngineConfig} implements a test engine configuration.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class EngineConfig {
  private final Map<String, Engine> engines = new HashMap<>();
  private final Map<String, Adapter> adapters = new HashMap<>();
  private final Map<String, InitializerMaker> initializerMakers = new HashMap<>();

  private static final EngineConfig instance = new EngineConfig();

  public static EngineConfig get() {
    return (EngineConfig) instance;
  }

  private EngineConfig() {
    initializerMakers.put("default", new InitializerMakerDefault());
  }

  public Engine registerEngine(final String name, final Engine engine) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(engine);

    return engines.put(name.toLowerCase(), engine);
  }

  public Engine getEngine(final String name) {
    InvariantChecks.checkNotNull(name);
    return engines.get(name.toLowerCase());
  }

  public Collection<Engine> getEngines() {
    return engines.values();
  }

  public Adapter registerAdapter(final String name, final Adapter adapter) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(adapter);

    return adapters.put(name.toLowerCase(), adapter);
  }

  public Adapter getAdapter(final String name) {
    InvariantChecks.checkNotNull(name);
    return adapters.get(name.toLowerCase());
  }

  public Collection<Adapter> getAdapters() {
    return adapters.values();
  }

  public InitializerMaker registerEngine(final String name, final InitializerMaker initMaker) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(initMaker);
    return initializerMakers.put(name.toLowerCase(), initMaker);
  }

  public InitializerMaker getInitializerMaker(final String name) {
    InvariantChecks.checkNotNull(name);
    return initializerMakers.get(name.toLowerCase());
  }

  public Collection<InitializerMaker> getInitializerMakers() {
    return initializerMakers.values();
  }
}
