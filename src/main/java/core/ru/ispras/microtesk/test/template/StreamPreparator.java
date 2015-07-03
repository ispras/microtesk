/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.template;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;

public final class StreamPreparator {
  private final MemoryMap memoryMap;

  private final List<Call> init;
  private final List<Call> read;
  private final List<Call> write;

  private final LazyPrimitive data;
  private final LazyPrimitive index;
  private final LabelValue startLabel;

  protected StreamPreparator(
      final MemoryMap memoryMap,
      final List<Call> init,
      final List<Call> read,
      final List<Call> write,
      final LazyPrimitive data,
      final LazyPrimitive index,
      final LabelValue startLabel) {
    InvariantChecks.checkNotNull(memoryMap);

    InvariantChecks.checkNotNull(init);
    InvariantChecks.checkNotNull(read);
    InvariantChecks.checkNotNull(write);

    InvariantChecks.checkNotNull(data);
    InvariantChecks.checkNotNull(index);
    InvariantChecks.checkNotNull(startLabel);

    this.memoryMap = memoryMap;

    this.init = Collections.unmodifiableList(init);
    this.read = Collections.unmodifiableList(read);
    this.write = Collections.unmodifiableList(write);

    this.data = data;
    this.index = index;
    this.startLabel = startLabel;
  }

  public Stream newStream(
      final Label label,
      final Primitive dataSource,
      final Primitive indexSource,
      final int length) {
    InvariantChecks.checkNotNull(label);
    InvariantChecks.checkNotNull(dataSource);
    InvariantChecks.checkNotNull(indexSource);
    InvariantChecks.checkGreaterThanZero(length);

    final BigInteger address = memoryMap.resolve(label.getName());

    startLabel.setLabel(label);
    startLabel.setAddress(address);

    data.setSource(dataSource);
    index.setSource(indexSource);

    return new Stream(
        label.getName(),
        Call.newCopy(init),
        Call.newCopy(read),
        Call.newCopy(write),
        length
        );
  }

  @Override
  public String toString() {
    return String.format("StreamPreparator [data_source=%s, index_source=%s]",
        data.getName(), index.getName());
  }

  public static String getId(final Primitive data, final Primitive index) {
    InvariantChecks.checkNotNull(data);
    InvariantChecks.checkNotNull(index);

    return String.format("%s_%s", data.getName(), index.getName());
  }

  public String getId() {
    return getId(data, index);
  }
}
