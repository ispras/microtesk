/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.model.api;

/**
 * This is a generic interface of a cache line matcher (hit checker).
 *
 * @param <D> the data type.
 * @param <A> the address type.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */

public interface Matcher<D, A extends Address> {

  /**
   * Checks whether the given data and the given address are matching each other.
   * 
   * @param data the data.
   * @param address the address.
   * @return <code>true</code> if the data and address are matching each other;
   *         <code>false</code> otherwise.
   */

  boolean areMatching(final D data, final A address);
}
