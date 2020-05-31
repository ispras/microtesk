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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.metadata.MetaAddressingMode;
import ru.ispras.microtesk.test.LabelManager;

import java.util.ArrayList;
import java.util.List;

public final class StreamPreparatorBuilder
    implements CodeBlockBuilder<StreamPreparator> {
  private final LabelManager memoryMap;
  private final LazyPrimitive data;
  private final LazyPrimitive index;
  private final LabelValue startLabel;

  private final List<AbstractCall> init;
  private final List<AbstractCall> read;
  private final List<AbstractCall> write;
  private List<AbstractCall> currentMethod;

  protected StreamPreparatorBuilder(
      final LabelManager memoryMap,
      final MetaAddressingMode metaData,
      final MetaAddressingMode metaIndex) {

    InvariantChecks.checkNotNull(memoryMap);
    InvariantChecks.checkNotNull(metaData);
    InvariantChecks.checkNotNull(metaIndex);

    this.memoryMap = memoryMap;

    this.data = new LazyPrimitive(
        Primitive.Kind.MODE, metaData.getName(), metaData.getName());

    this.index = new LazyPrimitive(
        Primitive.Kind.MODE, metaIndex.getName(), metaIndex.getName());

    this.startLabel = LabelValue.newLazy();

    this.init = new ArrayList<>();
    this.read = new ArrayList<>();
    this.write = new ArrayList<>();
    this.currentMethod = null;
  }

  public Primitive getDataSource() {
    return data;
  }

  public Primitive getIndexSource() {
    return index;
  }

  public LabelValue getStartLabel() {
    return startLabel;
  }

  public void beginInitMethod() {
    currentMethod = init;
  }

  public void beginReadMethod() {
    currentMethod = read;
  }

  public void beginWriteMethod() {
    currentMethod = write;
  }

  public void endMethod() {
    currentMethod = null;
  }

  @Override
  public void addCall(final AbstractCall call) {
    InvariantChecks.checkNotNull(call);

    if (null == currentMethod) {
      throw new IllegalStateException(
          "The instruction call is specified outside any data_stream method.");
    }

    currentMethod.add(call);
  }

  @Override
  public StreamPreparator build() {
    return new StreamPreparator(
        memoryMap,
        init,
        read,
        write,
        data,
        index,
        startLabel
        );
  }
}
