/*
 * Copyright 2013-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test;

import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.test.sequence.CombinatorId;
import ru.ispras.microtesk.test.sequence.CompositorId;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.sequence.combinator.*;
import ru.ispras.microtesk.test.sequence.compositor.*;

/**
 * This class implements the configuration of the test sequence generator.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Configuration<T> {
  /** The map of available combinators. */
  private Map<String, Class<?>> combinators = new HashMap<String, Class<?>>();

  /** The map of available compositors. */
  private Map<String, Class<?>> compositors = new HashMap<String, Class<?>>();

  private final Map<String, Solver<?>> solvers = new HashMap<>();
  private final Map<String, Adapter<?>> adapters = new HashMap<>();

  /**
   * Constructs a configuration object.
   */
  public Configuration() {
    // Available combinators
    combinators.put(CombinatorId.PRODUCT.name(), ProductCombinator.class);
    combinators.put(CombinatorId.DIAGONAL.name(), DiagonalCombinator.class);
    combinators.put(CombinatorId.RANDOM.name(), RandomCombinator.class);

    // Available compositors
    compositors.put(CompositorId.CATENATION.name(), CatenationCompositor.class);
    compositors.put(CompositorId.ROTATION.name(), RotationCompositor.class);
    compositors.put(CompositorId.OVERLAPPING.name(), OverlappingCompositor.class);
    compositors.put(CompositorId.NESTING.name(), NestingCompositor.class);
    compositors.put(CompositorId.RANDOM.name(), RandomCompositor.class);
  }

  /**
   * Creates an instance of the combinator with the given name.
   * 
   * @param name the combinator's name.
   * @return a combinator instance.
   */

  @SuppressWarnings("unchecked")
  public Combinator<Sequence<T>> getCombinator(final String name) {
    return createInstance((Class<Combinator<Sequence<T>>>) combinators.get(name));
  }

  /**
   * Creates an instance of the combinator with the given id.
   * 
   * @return a combinator instance.
   * @param id the combinator's id.
   */
  public Combinator<Sequence<T>> getCombinator(final CombinatorId id) {
    return getCombinator(id.name());
  }

  /**
   * Creates an instance of the compositor with the given name.
   * 
   * @return a compositor instance.
   * @param name the compositor's name.
   */
  @SuppressWarnings("unchecked")
  public Compositor<T> getCompositor(final String name) {
    return createInstance((Class<Compositor<T>>) compositors.get(name));
  }

  /**
   * Creates an instance of the compositor with the given name.
   * 
   * @return a compositor instance.
   * @param id the compositor's id.
   */
  public Compositor<T> getCompositor(final CompositorId id) {
    return getCompositor(id.name());
  }

  public Solver<?> registerSolver(final String name, final Solver<?> solver) {
    return solvers.put(name, solver);
  }

  public Solver<?> getSolver(final String name) {
    return solvers.get(name);
  }

  public Adapter<?> registerAdapter(final String name, final Adapter<?> adapter) {
    return adapters.put(name, adapter);
  }

  public Adapter<?> getAdapter(final String name) {
    return adapters.get(name);
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
