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

import java.util.List;

import ru.ispras.microtesk.test.sequence.internal.CompositeIterator;
import ru.ispras.microtesk.test.sequence.modificator.Modificator;

/**
 * {@link GeneratorBuilder} implements the test sequence generator.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class GeneratorBuilder<T> extends CompositeIterator<List<T>> {
  /** The default combinator. */
  public static final String DEFAULT_COMBINATOR = "diagonal";
  /** The default compositor. */
  public static final String DEFAULT_COMPOSITOR = "catenation";
  /** The default modificator. */
  public static final String DEFAULT_MODIFICATOR = "trivial";

  /** The combinator used in the generator. */
  private String combinator = null;
  /** The compositor used in the generator. */
  private String compositor = null;
  /** The modificator used in the generator. */
  private String modificator = null;

  /**
   * Specifies whether a single sequence must be generated 
   * (all sequences returned by all iterators are united into a single sequence).
   */
  private boolean isSingle = false;

  /**
   * Constructs a test sequence generator.
   */
  public GeneratorBuilder() {}

  /**
   * Sets the combinator used in the generator.
   * 
   * @param combinator the combinator name.
   */

  public void setCombinator(final String combinator) {
    this.combinator = combinator;
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
   * Sets the modificator used in the generator.
   * 
   * @param modificator the modificator name.
   */

  public void setModificator(final String modificator) {
    this.modificator = modificator;
  }

  /**
   * Sets the isSingle flag (whether a single sequence must be generated).
   * 
   * @param isSingle {@code true} to generate a single sequence or {@code false} to prevent this.
   */

  public void setSingle(boolean isSingle) {
    this.isSingle = isSingle;
  }

  /**
   * Returns the test sequence generator for the template block.
   * 
   * @return the test sequence generator.
   */

  public Generator<T> getGenerator() {
    final GeneratorConfig<T> config = GeneratorConfig.get();

    final Modificator<T> modificatorEngine =
        config.getPermutator(modificator != null ? modificator : DEFAULT_MODIFICATOR);

    // If the isSingle flag is set, the single sequence generator is returned.
    if (isSingle) {
      return new GeneratorModificator<>(new GeneratorSingle<>(getIterators()), modificatorEngine);
    }

    if ((null == combinator) && (null == compositor)) {
      return new GeneratorModificator<>(new GeneratorSequence<>(getIterators()), modificatorEngine);
    }

    if (null == combinator) { 
      combinator = DEFAULT_COMBINATOR;
    }

    if (null == compositor) {
      compositor = DEFAULT_COMPOSITOR;
    }

    return new GeneratorModificator<>(new GeneratorMerge<T>(config.getCombinator(combinator),
        config.getCompositor(compositor), getIterators()), modificatorEngine);
  }
}
