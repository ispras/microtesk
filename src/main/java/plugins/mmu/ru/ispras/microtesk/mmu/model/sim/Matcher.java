/*
 * Copyright 2014-2020 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.model.sim;

/**
 * {@link Matcher} is a generic interface of a cache line matcher (hit checker).
 *
 * @param <E> the entry type.
 * @param <A> the address type.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public interface Matcher<E extends Struct<?>, A extends Address<?>> {

  /**
   * Checks whether the given entry and the given address are matching each other.
   *
   * @param entry the entry.
   * @param address the address.
   * @return {@code true} iff the data and address are matching each other.
   */
  boolean areMatching(E entry, A address);

  /**
   * Extracts the tag from the given address and assigns it to the given entry.
   *
   * @param entry the entry.
   * @param address the address.
   * @return the input data w/ the assigned tag.
   */
  E assignTag(E entry, A address);
}
