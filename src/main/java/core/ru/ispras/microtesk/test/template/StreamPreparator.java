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

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.LabelManager;

public final class StreamPreparator {
  private final LabelManager memoryMap;

  private final List<AbstractCall> init;
  private final List<AbstractCall> read;
  private final List<AbstractCall> write;

  private final LazyPrimitive data;
  private final LazyPrimitive index;
  private final LabelValue startLabel;

  protected StreamPreparator(
      final LabelManager memoryMap,
      final List<AbstractCall> init,
      final List<AbstractCall> read,
      final List<AbstractCall> write,
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

    final LabelManager.Target target = memoryMap.resolve(label);
    if (null == target) {
      throw new GenerationAbortedException(
          String.format("The %s label is not defined.", label.getName()));
    }

    final BigInteger address =
        BitVector.valueOf(target.getAddress(), Long.SIZE).bigIntegerValue(false);

    startLabel.setLabel(label);
    startLabel.setAddress(address);

    data.setSource(dataSource);
    index.setSource(indexSource);

    return new Stream(
        label.getName(),
        dataSource,
        indexSource,
        AbstractCall.copyAll(init),
        AbstractCall.copyAll(read),
        AbstractCall.copyAll(write),
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
