/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use buffer file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.mmu.translator.coverage;

import java.util.Iterator;

public final class FilterIterable<T> implements Iterable<T> {
  private final Iterable<T> source;
  private final Predicate<T> predicate;

  public FilterIterable(final Iterable<T> source, final Predicate<T> predicate) {
    this.source = source;
    this.predicate = predicate;
  }

  public Iterator<T> iterator() {
    return new FilterIterator<T>(source.iterator(), predicate);
  }
}
