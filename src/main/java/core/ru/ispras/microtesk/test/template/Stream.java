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

import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;

public final class Stream {
  private final String startLabelName;
  private final int length;
  private final List<Call> init;
  private final List<Call> read;
  private final List<Call> write;
  private boolean isInit;
  private int index;

  protected Stream(
      final String startLabelName,
      final List<Call> init,
      final List<Call> read,
      final List<Call> write,
      final int length) {
    InvariantChecks.checkNotNull(startLabelName);
    InvariantChecks.checkGreaterThanZero(length);
    InvariantChecks.checkNotNull(init);
    InvariantChecks.checkNotNull(read);
    InvariantChecks.checkNotNull(write);

    this.startLabelName = startLabelName;
    this.length = length;

    this.init = Collections.unmodifiableList(init);
    this.read = Collections.unmodifiableList(read);
    this.write = Collections.unmodifiableList(write);

    this.isInit = false;
    this.index = 0;
  }

  public String getStartLabelName() {
    return startLabelName;
  }

  public List<Call> getInit() {
    index = 0;
    isInit = true;
    return init;
  }

  public List<Call> getRead() {
    InvariantChecks.checkTrue(isInit, "Stream is never initialized: " + toString());
    InvariantChecks.checkBounds(index++, length);
    return read;
  }

  public List<Call> getWrite() {
    InvariantChecks.checkTrue(isInit, "Stream is never initialized: " + toString());
    InvariantChecks.checkBounds(index++, length);
    return write;
  }

  public boolean hasNext() {
    return index < length;
  }

  @Override
  public String toString() {
    return String.format(
        "Stream [label=%s, length=%d, index=%d]", startLabelName, length, index);
  }
}
