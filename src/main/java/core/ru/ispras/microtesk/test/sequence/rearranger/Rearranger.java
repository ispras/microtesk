/*
 * Copyright 2016-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.sequence.rearranger;

import ru.ispras.testbase.knowledge.iterator.Iterator;

import java.util.List;

/**
 * The {@link Rearranger} interface is a base interface for objects
 * that rearrange a collection of sequences described by an iterator.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 *
 * @param <T> Sequence item type.
 */
public interface Rearranger<T> extends Iterator<List<T>> {
  void initialize(final Iterator<List<T>> original);
}
