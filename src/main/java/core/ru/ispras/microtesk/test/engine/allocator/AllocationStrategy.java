/*
 * Copyright 2014-2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.engine.allocator;

import ru.ispras.microtesk.utils.function.Supplier;

import java.util.Collection;
import java.util.Map;

/**
 * {@link AllocationStrategy} defines an interface of resource allocation strategies.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public interface AllocationStrategy {

  /**
   * Chooses an object.
   *
   * @param <T> type of objects.
   * @param domain the set of all available objects. 
   * @param exclude the set of objects to be excluded. 
   * @param used the of used objects.
   * @param attributes the allocation parameters.
   * @return the chosen object or {@code null}.
   */
  <T> T next(
      final Collection<T> domain,
      final Collection<T> exclude,
      final Collection<T> used,
      final Map<String, String> attributes);

  /**
   * Generates an object.
   *
   * @param <T> type of objects.
   * @param supplier the object generator. 
   * @param exclude the set of objects to be excluded. 
   * @param used the set of used objects.
   * @param attributes the allocation parameters parameters.
   * @return the chosen object or {@code null}.
   */
  <T> T next(
      final Supplier<T> supplier,
      final Collection<T> exclude,
      final Collection<T> used,
      final Map<String, String> attributes);
}
