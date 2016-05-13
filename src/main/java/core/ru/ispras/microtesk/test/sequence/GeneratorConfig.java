/*
 * Copyright 2013-2016 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.sequence.combinator.Combinator;
import ru.ispras.microtesk.test.sequence.combinator.CombinatorDiagonal;
import ru.ispras.microtesk.test.sequence.combinator.CombinatorProduct;
import ru.ispras.microtesk.test.sequence.combinator.CombinatorRandom;
import ru.ispras.microtesk.test.sequence.compositor.Compositor;
import ru.ispras.microtesk.test.sequence.compositor.CompositorCatenation;
import ru.ispras.microtesk.test.sequence.compositor.CompositorNesting;
import ru.ispras.microtesk.test.sequence.compositor.CompositorOverlapping;
import ru.ispras.microtesk.test.sequence.compositor.CompositorRandom;
import ru.ispras.microtesk.test.sequence.compositor.CompositorRotation;
import ru.ispras.microtesk.test.sequence.engine.Adapter;
import ru.ispras.microtesk.test.sequence.engine.Engine;
import ru.ispras.microtesk.test.sequence.permutator.Permutator;
import ru.ispras.microtesk.test.sequence.permutator.PermutatorRandom;
import ru.ispras.microtesk.test.sequence.permutator.PermutatorTrivial;
import ru.ispras.microtesk.test.sequence.rearranger.Rearranger;
import ru.ispras.microtesk.test.sequence.rearranger.RearrangerExpand;
import ru.ispras.microtesk.test.sequence.rearranger.RearrangerTrivial;

/**
 * {@link GeneratorConfig} implements a test generator configuration.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class GeneratorConfig<T> {
  private final Map<String, Class<?>> combinators = new HashMap<>();
  private final Map<String, Class<?>> compositors = new HashMap<>();
  private final Map<String, Class<?>> permutators = new HashMap<>();
  private final Map<String, Class<?>> rearrangers = new HashMap<>();

  private final Map<String, Engine<?>> engines = new HashMap<>();
  private final Map<String, Adapter<?>> adapters = new HashMap<>();

  private static final GeneratorConfig<?> instance = new GeneratorConfig<>();

  @SuppressWarnings("unchecked")
  public static <T> GeneratorConfig<T> get() {
    return (GeneratorConfig<T>) instance;
  }

  private GeneratorConfig() {
    combinators.put("product", CombinatorProduct.class);
    combinators.put("diagonal", CombinatorDiagonal.class);
    combinators.put("random", CombinatorRandom.class);

    compositors.put("catenation", CompositorCatenation.class);
    compositors.put("rotation", CompositorRotation.class);
    compositors.put("overlap", CompositorOverlapping.class);
    compositors.put("nesting", CompositorNesting.class);
    compositors.put("random", CompositorRandom.class);

    permutators.put("trivial", PermutatorTrivial.class);
    permutators.put("random", PermutatorRandom.class);

    rearrangers.put("trivial", RearrangerTrivial.class);
    rearrangers.put("expand", RearrangerExpand.class);
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
    if (null == combinatorClass) {
      throw new GenerationAbortedException("Combinator is not defined: " + name);
    }

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
    if (null == compositorClass) {
      throw new GenerationAbortedException("Compositor is not defined: " + name);
    }

    return createInstance((Class<Compositor<T>>) compositorClass);
  }

  /**
   * Creates an instance of the permutator with the given name.
   * 
   * @param name the permutator name.
   * @return a permutator instance.
   */
  @SuppressWarnings("unchecked")
  public Permutator<List<T>> getPermutator(final String name) {
    InvariantChecks.checkNotNull(name);

    final Class<?> permutatorClass = permutators.get(name.toLowerCase());
    if (null == permutatorClass) {
      throw new GenerationAbortedException("Permutator is not defined: " + name);
    }

    return createInstance((Class<Permutator<List<T>>>) permutatorClass);
  }

  /**
   * Creates an instance of the modificator with the given name.
   * 
   * @param name the modificator name.
   * @return a modificator instance.
   */
  @SuppressWarnings("unchecked")
  public Permutator<T> getModificator(final String name) {
    InvariantChecks.checkNotNull(name);

    final Class<?> permutatorClass = permutators.get(name.toLowerCase());
    if (null == permutatorClass) {
      throw new GenerationAbortedException("Permutator is not defined: " + name);
    }

    return createInstance((Class<Permutator<T>>) permutatorClass);
  }

  /**
   * Creates an instance of the rearranger with the given name.
   * 
   * @param name the rearranger name.
   * @return a rearranger instance.
   */
  @SuppressWarnings("unchecked")
  public Rearranger<T> getRearranger(final String name) {
    InvariantChecks.checkNotNull(name);

    final Class<?> rearrangerClass = rearrangers.get(name.toLowerCase());
    if (null == rearrangerClass) {
      throw new GenerationAbortedException("Rearranger is not defined: " + name);
    }

    return createInstance((Class<Rearranger<T>>) rearrangerClass);
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
      throw new IllegalArgumentException("Cannot instantiate " + type.getName());
    }
  }
}
