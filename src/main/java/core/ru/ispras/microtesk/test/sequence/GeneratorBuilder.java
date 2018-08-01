/*
 * Copyright 2013-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.sequence.combinator.Combinator;
import ru.ispras.microtesk.test.sequence.combinator.CombinatorPermutator;
import ru.ispras.microtesk.test.sequence.compositor.Compositor;
import ru.ispras.microtesk.test.sequence.permutator.Permutator;
import ru.ispras.microtesk.test.sequence.rearranger.Rearranger;
import ru.ispras.testbase.knowledge.iterator.Iterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@link GeneratorBuilder} implements the test sequence generator.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 *
 * @param <T> Sequence element type.
 */
public final class GeneratorBuilder<T> {
  /** The default combinator. */
  public static final String DEFAULT_COMBINATOR = "diagonal";
  /** The default permutator. */
  public static final String DEFAULT_PERMUTATOR = "trivial";
  /** The default compositor. */
  public static final String DEFAULT_COMPOSITOR = "catenation";
  /** The default rearranger. */
  public static final String DEFAULT_REARRANGER  = "trivial";
  /** The default obfuscator. */
  public static final String DEFAULT_OBFUSCATOR = "trivial";

  /** Specifies whether a single sequence must be generated. */
  private final boolean isSequence;
  /** Specifies whether a collection of sequences returned by nested iterators must be generated.*/
  private final boolean isIterate;
  /** Attributes describing the properties of engines to be applied by the generator. */
  private final Map<String, Object> attributes;
  /** Iterators to be used by the generator. */
  private final List<Iterator<List<T>>> iterators;

  /**
   * Constructs a test sequence generator.
   *
   * @param isSequence Specifies whether a single sequence must be generated.
   * @param isIterate Specifies whether a collection of sequences returned by nested
   *        iterators must be generated.
   * @param attributes Attributes describing the properties of engines
   *        to be applied by the generator.
   *
   * @throws IllegalArgumentException if both {@code isSequence} and {@code isIterate}
   *         are {@code true}; if the {@code attributes} argument is {@code null}.
   */
  public GeneratorBuilder(
      final boolean isSequence,
      final boolean isIterate,
      final Map<String, Object> attributes) {
    InvariantChecks.checkFalse(isSequence && isIterate);
    InvariantChecks.checkNotNull(attributes);

    this.isSequence = isSequence;
    this.isIterate = isIterate;
    this.iterators = new ArrayList<>();
    this.attributes = attributes;
  }

  /**
   * Adds an iterator into the list.
   *
   * @param iterator the sub-iterator to be added to the list.
   */
  public void addIterator(final Iterator<List<T>> iterator) {
    iterators.add(iterator);
  }

  /**
   * Builds and returns the test sequence generator.
   *
   * @return the test sequence generator.
   */
  public Generator<T> build() {
    Generator<T> generator = newGenerator();

    generator = applyObfuscator(generator);
    generator = applyNitems(generator);

    return generator;
  }

  private Generator<T> newGenerator() {
    if (isSequence) {
      // Single sequence generator is returned.
      return new GeneratorSequence<>(iterators);
    }

    final Generator<T> generator;
    if (isIterate) {
      // Generator will iterate over sequences of nested iterators.
      generator = new GeneratorIterate<>(iterators);
    } else {
      generator = newBlockGenerator();
    }

    return applyRearranger(generator);
  }

  private Generator<T> newBlockGenerator() {
    final String combinator = (String) attributes.get("combinator");
    final Combinator<List<T>> combinatorEngine = GeneratorConfig.<List<T>>get().getCombinator(
        combinator != null ? combinator : DEFAULT_COMBINATOR);

    final String permutator = (String) attributes.get("permutator");
    final Permutator<List<T>> permutatorEngine = GeneratorConfig.<T>get().getPermutator(
        permutator != null ? permutator : DEFAULT_PERMUTATOR);

    final String compositor = (String) attributes.get("compositor");
    final Compositor<T> compositorEngine = GeneratorConfig.<T>get().getCompositor(
        compositor != null ? compositor : DEFAULT_COMPOSITOR);

    return new GeneratorCompositor<T>(
        new CombinatorPermutator<>(combinatorEngine, permutatorEngine),
        compositorEngine,
        iterators);
  }

  private Generator<T> applyNitems(final Generator<T> generator) {
    final Object nitems = attributes.get("nitems");
    return nitems instanceof Number ?
        new GeneratorNitems<>(generator, ((Number) nitems).intValue()) : generator;
  }

  private Generator<T> applyObfuscator(final Generator<T> generator) {
    final String obfuscator = (String) attributes.get("obfuscator");
    final Permutator<T> obfuscatorEngine = GeneratorConfig.<T>get().getModificator(
        obfuscator != null ? obfuscator : DEFAULT_OBFUSCATOR);

    return new GeneratorObfuscator<>(generator, obfuscatorEngine);
  }

  private Generator<T> applyRearranger(final Generator<T> generator) {
    final String rearranger = (String) attributes.get("rearranger");
    final Rearranger<T> rearrangerEngine = GeneratorConfig.<T>get().getRearranger(
        rearranger != null ? rearranger : DEFAULT_REARRANGER);

    return new GeneratorRearranger<>(generator, rearrangerEngine);
  }
}
