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
import ru.ispras.microtesk.test.sequence.internal.CompositeIterator;
import ru.ispras.microtesk.test.sequence.permutator.Permutator;
import ru.ispras.microtesk.test.sequence.rearranger.Rearranger;

import java.util.List;

/**
 * {@link GeneratorBuilder} implements the test sequence generator.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 *
 * @param <T> Sequence element type.
 */
public final class GeneratorBuilder<T> extends CompositeIterator<List<T>> {
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

  /** The combinator used in the generator. */
  private String combinator = null;
  /** The permutator used in the generator. */
  private String permutator = null;
  /** The compositor used in the generator. */
  private String compositor = null;
  /** The rearranger used in the generator. */
  private String rearranger = null;
  /** The modificator used in the generator. */
  private String obfuscator = null;

  /** Specifies whether a single sequence must be generated. */
  private final boolean isSequence;
  /** Specifies whether a collection of sequences returned by nested iterators must be generated.*/
  private final boolean isIterate;

  /**
   * Constructs a test sequence generator.
   *
   * @param isSequence Specifies whether a single sequence must be generated.
   * @param isIterate Specifies whether a collection of sequences returned by nested
   *        iterators must be generated.
   *
   * @throws IllegalArgumentException if both {@code isSequence} and {@code isIterate}
   *         are {@code true}.
   */
  public GeneratorBuilder(final boolean isSequence, final boolean isIterate) {
    InvariantChecks.checkFalse(isSequence && isIterate);

    this.isSequence = isSequence;
    this.isIterate = isIterate;
  }

  /**
   * Sets the combinator used in the generator.
   *
   * @param combinator the combinator name.
   */
  public void setCombinator(final String combinator) {
    this.combinator = combinator;
  }

  /**
   * Sets the permutator used in the generator.
   *
   * @param permutator the permutator name.
   */
  public void setPermutator(final String permutator) {
    this.permutator = permutator;
  }

  /**
   * Sets the compositor used in the generator.
   *
   * @param compositor the compositor name.
   */
  public void setCompositor(final String compositor) {
    this.compositor = compositor;
  }

  /**
   * Sets the rearranger used in the generator.
   *
   * @param rearranger the rearranger name.
   */
  public void setRearranger(final String rearranger) {
    this.rearranger = rearranger;
  }

  /**
   * Sets the obfuscator used in the generator.
   *
   * @param obfuscator the obfuscator name.
   */
  public void setObfuscator(final String obfuscator) {
    this.obfuscator = obfuscator;
  }

  /**
   * Returns the test sequence generator for the template block.
   *
   * @return the test sequence generator.
   */
  public Generator<T> getGenerator() {
    return applyObfuscator(newGenerator());
  }

  private Generator<T> newGenerator() {
    if (isSequence) {
      // Single sequence generator is returned.
      return new GeneratorSequence<>(getIterators());
    }

    final Generator<T> generator;
    if (isIterate) {
      // Generator will iterate over sequences of nested iterators.
      generator = new GeneratorIterate<>(getIterators());
    } else {
      generator = newBlockGenerator();
    }

    return applyRearranger(generator);
  }

  private Generator<T> newBlockGenerator() {
    final Combinator<List<T>> combinatorEngine = GeneratorConfig.<List<T>>get().getCombinator(
        combinator != null ? combinator : DEFAULT_COMBINATOR);

    final Permutator<List<T>> permutatorEngine = GeneratorConfig.<T>get().getPermutator(
        permutator != null ? permutator : DEFAULT_PERMUTATOR);

    final Compositor<T> compositorEngine = GeneratorConfig.<T>get().getCompositor(
        compositor != null ? compositor : DEFAULT_COMPOSITOR);

    return new GeneratorCompositor<T>(
        new CombinatorPermutator<>(combinatorEngine, permutatorEngine),
        compositorEngine,
        getIterators());
  }

  private Generator<T> applyObfuscator(final Generator<T> generator) {
    final Permutator<T> obfuscatorEngine = GeneratorConfig.<T>get().getModificator(
        obfuscator != null ? obfuscator : DEFAULT_OBFUSCATOR);

    return new GeneratorObfuscator<>(generator, obfuscatorEngine);
  }

  private Generator<T> applyRearranger(final Generator<T> generator) {
    final Rearranger<T> rearrangerEngine = GeneratorConfig.<T>get().getRearranger(
        rearranger != null ? rearranger : DEFAULT_REARRANGER);

    return new GeneratorRearranger<>(generator, rearrangerEngine);
  }
}
