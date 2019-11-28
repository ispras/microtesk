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

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.model.memory.MemoryAllocator;

import java.math.BigInteger;
import java.util.List;

/**
 * {@link Directive} is to be supported by all data directives.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public interface Directive {
  enum Kind {
    /** Address modification directives ({@code .align}, {@code .org}, etc.). */
    ADDR,
    /** Data declaration directives ({@code .word}, {@code .ascii}, etc.). */
    DATA,
    /** Text and comment printing directives. */
    TEXT,
    /** Label definition directives. */
    LABEL
  }

  /**
   * Returns the string representation of the directive.
   *
   * @return the directive text.
   */
  String getText();

  /**
   * Checks whether an indentation is required when printing the directive.
   *
   * @return {@code true} iff an indentation is required.
   */
  boolean needsIndent();

  /**
   * Applies the directive to the current address and the memory allocator.
   *
   * @param currentAddress the current address.
   * @param allocator the memory allocator.
   * @return the current address.
   */
  BigInteger apply(BigInteger currentAddress, MemoryAllocator allocator);

  /**
   * Copies the directive.
   *
   * @return a copy of the directive.
   */
  Directive copy();
}
