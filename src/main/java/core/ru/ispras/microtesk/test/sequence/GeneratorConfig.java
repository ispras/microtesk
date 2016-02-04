/*
 * Copyright 2013-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.sequence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.sequence.combinator.Combinator;
import ru.ispras.microtesk.test.sequence.combinator.DiagonalCombinator;
import ru.ispras.microtesk.test.sequence.combinator.ProductCombinator;
import ru.ispras.microtesk.test.sequence.combinator.RandomCombinator;
import ru.ispras.microtesk.test.sequence.compositor.CatenationCompositor;
import ru.ispras.microtesk.test.sequence.compositor.Compositor;
import ru.ispras.microtesk.test.sequence.compositor.NestingCompositor;
import ru.ispras.microtesk.test.sequence.compositor.OverlappingCompositor;
import ru.ispras.microtesk.test.sequence.compositor.RandomCompositor;
import ru.ispras.microtesk.test.sequence.compositor.RotationCompositor;
import ru.ispras.microtesk.test.sequence.engine.Adapter;
import ru.ispras.microtesk.test.sequence.engine.Engine;
import ru.ispras.microtesk.test.sequence.permutator.Permutator;
import ru.ispras.microtesk.test.sequence.permutator.RandomPermutator;
import ru.ispras.microtesk.test.sequence.permutator.TrivialPermutator;

/**
 * {@link GeneratorConfig} implements a test generator configuration.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class GeneratorConfig<T> {
  private final Map<String, Class<?>> combinators = new HashMap<String, Class<?>>();
  private final Map<String, Class<?>> compositors = new HashMap<String, Class<?>>();
  private final Map<String, Class<?>> permutators = new HashMap<String, Class<?>>();

  private final Map<String, Engine<?>> engines = new HashMap<>();
  private final Map<String, Adapter<?>> adapters = new HashMap<>();

  private static final GeneratorConfig<?> instance = new GeneratorConfig<>();

  @SuppressWarnings("unchecked")
  public static <T> GeneratorConfig<T> get() {
    return (GeneratorConfig<T>) instance;
  }

  private GeneratorConfig() {
    combinators.put("product", ProductCombinator.class);
    combinators.put("diagonal", DiagonalCombinator.class);
    combinators.put("random", RandomCombinator.class);

    compositors.put("catenation", CatenationCompositor.class);
    compositors.put("rotation", RotationCompositor.class);
    compositors.put("overlap", OverlappingCompositor.class);
    compositors.put("nesting", NestingCompositor.class);
    compositors.put("random", RandomCompositor.class);

    permutators.put("trivial", TrivialPermutator.class);
    permutators.put("random", RandomPermutator.class);
  }

  /**
   * Creates an instance of the combinator with the given name.
   * 
   * @param name the combinator's name.
   * @return a combinator instance.
   */
  @SuppressWarnings("unchecked")
  public Combinator<List<T>> getCombinator(final String name) {
    InvariantChecks.checkNotNull(name);
    final Class<?> combinatorClass = combinators.get(name.toLowerCase());
    return createInstance((Class<Combinator<List<T>>>) combinatorClass);
  }

  /**
   * Creates an instance of the compositor with the given name.
   * 
   * @param name the compositor's name.
   * @return a compositor instance.
   */
  @SuppressWarnings("unchecked")
  public Compositor<T> getCompositor(final String name) {
    InvariantChecks.checkNotNull(name);
    final Class<?> compositorClass = compositors.get(name.toLowerCase());
    return createInstance((Class<Compositor<T>>) compositorClass);
  }

  /**
   * Creates an instance of the permutator with the given name.
   * 
   * @param name the permutator name.
   * @return a permutator instance.
   */
  @SuppressWarnings("unchecked")
  public Permutator<T> getPermutator(final String name) {
    InvariantChecks.checkNotNull(name);
    final Class<?> permutatorClass = permutators.get(name.toLowerCase());
    return createInstance((Class<Permutator<T>>) permutatorClass);
  }

  public Engine<?> registerEngine(final String name, final Engine<?> engine) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(engine);

    return engines.put(name.toLowerCase(), engine);
  }

  public Engine<?> getEngine(final String name) {
    InvariantChecks.checkNotNull(name);
    return engines.get(name.toLowerCase());
  }

  public Adapter<?> registerAdapter(final String name, final Adapter<?> adapter) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(adapter);

    return adapters.put(name.toLowerCase(), adapter);
  }

  public Adapter<?> getAdapter(final String name) {
    InvariantChecks.checkNotNull(name);
    return adapters.get(name.toLowerCase());
  }

  /**
   * Creates an instance of the given type.
   * 
   * @param type the instance type.
   * @return the instance.
   */
  private static <I> I createInstance(final Class<I> type) {
    try {
      return type.newInstance();
    } catch (final Exception e) {
      throw new IllegalArgumentException();
    }
  }
}
