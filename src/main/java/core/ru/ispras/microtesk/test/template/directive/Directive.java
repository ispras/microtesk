/*
 * Copyright 2016-2019 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.template.directive;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.memory.MemoryAllocator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link Directive} represents an assembly directive.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class Directive {
    /** Directive kind. */
    public enum Kind {
      ALIGN,
      ORIGIN,
      LABEL,
      DATA,
      TEXT
    }

  public static List<Directive> copyAll(final List<Directive> directives) {
    InvariantChecks.checkNotNull(directives);

    final List<Directive> result = new ArrayList<>(directives.size());
    for (final Directive directive : directives) {
      result.add(directive.copy());
    }

    return result;
  }

  /**
   * Returns the directive kind.
   *
   * @return the directive kind.
   */
  public abstract Kind getKind();

  /**
   * Returns the string representation of the directive.
   *
   * @return the directive text.
   */
  public abstract String getText();

  /**
   * Checks whether an indentation is required when printing the directive.
   *
   * @return {@code true} iff an indentation is required.
   */
  public boolean needsIndent() {
    return true;
  }

  /**
   * Applies the directive to the current address and the memory allocator.
   *
   * @param currentAddress the current address.
   * @param allocator the memory allocator.
   * @return the current address.
   */
  public BigInteger apply(final BigInteger currentAddress, final MemoryAllocator allocator) {
    return currentAddress;
  }

  /**
   * Copies the directive.
   *
   * @return a copy of the directive.
   */
  public Directive copy() {
    return this;
  }

  @Override
  public String toString() {
    return getText();
  }
}
