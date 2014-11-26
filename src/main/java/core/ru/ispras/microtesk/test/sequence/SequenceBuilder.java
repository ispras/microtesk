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

package ru.ispras.microtesk.test.sequence;

import java.util.ArrayList;
import java.util.List;

public final class SequenceBuilder<T> {
  private final List<T> prologue;
  private final List<T> body;

  public SequenceBuilder() {
    this.prologue = new ArrayList<T>();
    this.body = new ArrayList<T>();
  }

  public void addToPrologue(T entry) {
    checkNotNull(entry);
    prologue.add(entry);
  }

  public void addToPrologue(List<T> entries) {
    checkNotNull(entries);
    prologue.addAll(entries);
  }

  public void add(T entry) {
    checkNotNull(entry);
    body.add(entry);
  }

  public void add(List<T> entries) {
    checkNotNull(entries);
    entries.addAll(entries);
  }

  public Sequence<T> build() {
    final Sequence<T> result = new Sequence<T>();

    result.addAll(prologue);
    result.addAll(body);

    return result;
  }

  private static void checkNotNull(Object o) {
    if (null == o) {
      throw new NullPointerException();
    }
  }
}
