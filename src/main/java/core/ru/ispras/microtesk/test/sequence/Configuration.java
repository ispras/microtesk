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
import java.util.Map;

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

/**
 * {@link Configuration} implements a test generator configuration.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class Configuration<T> {
  private final Map<String, Class<?>> combinators = new HashMap<String, Class<?>>();
  private final Map<String, Class<?>> compositors = new HashMap<String, Class<?>>();

  private final Map<String, Engine<?>> engines = new HashMap<>();
  private final Map<String, Adapter<?>> adapters = new HashMap<>();

  /**
   * Constructs a configuration object.
   */
  public Configuration() {
    combinators.put(CombinatorId.PRODUCT.name(), ProductCombinator.class);
    combinators.put(CombinatorId.DIAGONAL.name(), DiagonalCombinator.class);
    combinators.put(CombinatorId.RANDOM.name(), RandomCombinator.class);

    compositors.put(CompositorId.CATENATION.name(), CatenationCompositor.class);
    compositors.put(CompositorId.ROTATION.name(), RotationCompositor.class);
    compositors.put(CompositorId.OVERLAPPING.name(), OverlappingCompositor.class);
    compositors.put(CompositorId.NESTING.name(), NestingCompositor.class);
    compositors.put(CompositorId.RANDOM.name(), RandomCompositor.class);

    // engines.put("branch", new BranchTemplateSolver(0, 5));
    // adapters.put("branch", new BranchTemplateAdapter());
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
   * @param id the combinator's id.
   * @return a combinator instance.
   */
  public Combinator<Sequence<T>> getCombinator(final CombinatorId id) {
    return getCombinator(id.name());
  }

  /**
   * Creates an instance of the compositor with the given name.
   * 
   * @param name the compositor's name.
   * @return a compositor instance.
   */
  @SuppressWarnings("unchecked")
  public Compositor<T> getCompositor(final String name) {
    return createInstance((Class<Compositor<T>>) compositors.get(name));
  }

  /**
   * Creates an instance of the compositor with the given name.
   * 
   * @param id the compositor's id.
   * @return a compositor instance.
   */
  public Compositor<T> getCompositor(final CompositorId id) {
    return getCompositor(id.name());
  }

  public Engine<?> registerEngine(final String name, final Engine<?> engine) {
    return engines.put(name, engine);
  }

  public Engine<?> getEngine(final String name) {
    return engines.get(name);
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
