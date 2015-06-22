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

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.metadata.MetaAddressingMode;

public final class DataStreamBuilder {
  private final LazyPrimitive data;
  private final LazyPrimitive index;
  private final LazyLabel startLabel;

  private final List<Call> init;
  private final List<Call> read;
  private final List<Call> write;

  protected DataStreamBuilder(
      final MetaAddressingMode metaData,
      final MetaAddressingMode metaIndex) {

    InvariantChecks.checkNotNull(metaData);
    InvariantChecks.checkNotNull(metaIndex);

    this.data = new LazyPrimitive(
        Primitive.Kind.MODE, metaData.getName(), metaData.getName());

    this.index = new LazyPrimitive(
        Primitive.Kind.MODE, metaIndex.getName(), metaIndex.getName());

    this.startLabel = new LazyLabel(); 

    this.init = new ArrayList<>();
    this.read = new ArrayList<>();
    this.write = new ArrayList<>();
  }

  public Primitive getDataSource() {
    return data;
  }

  public Primitive getIndexSource() {
    return index;
  }

  public LazyLabel getStartLabel() {
    return startLabel;
  }

  public void addCallToInit(final Call call) {
    checkNotNull(call);
    init.add(call);
  }

  public void addCallToRead(final Call call) {
    checkNotNull(call);
    read.add(call);
  }

  public void addCallToWrite(final Call call) {
    checkNotNull(call);
    write.add(call);
  }

  public DataStream build() {
    return new DataStream(
        init,
        read,
        write,
        data,
        index,
        startLabel
        );
  }
}
