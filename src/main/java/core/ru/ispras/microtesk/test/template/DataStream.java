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

/*
data_stream(reg_data, reg_index, start_label, array_size) {
  init {
    reg_index = start_label   // User-defined code
  }
  read {
    reg_data = mem[reg_index] // User-defined code
    reg_index++
  }
  write {
    mem[reg_index] = reg_data // User-defined code
    reg_index++
  }
}
*/

public final class DataStream {
  private final List<Call> initCalls;
  private final List<Call> readCalls;
  private final List<Call> writeCalls;

  private final LazyPrimitive regData;
  private final LazyPrimitive regIndex;
  private String startLabel;

  private int arrayLength;
  private int arrayIndex;
  private boolean isInitialized;

  protected DataStream(
      final List<Call> initCalls,
      final List<Call> readCalls,
      final List<Call> writeCalls,
      final LazyPrimitive regData,
      final LazyPrimitive regIndex,
      final String startLabel) {
    InvariantChecks.checkNotNull(initCalls);
    InvariantChecks.checkNotNull(readCalls);
    InvariantChecks.checkNotNull(writeCalls);

    InvariantChecks.checkNotNull(regData);
    InvariantChecks.checkNotNull(regIndex);
    InvariantChecks.checkNotNull(startLabel);

    this.initCalls = Collections.unmodifiableList(initCalls);
    this.readCalls = Collections.unmodifiableList(readCalls);
    this.writeCalls = Collections.unmodifiableList(writeCalls);

    this.regData = regData;
    this.regIndex = regIndex;
    this.startLabel = startLabel;

    this.arrayLength = 0;
    this.arrayIndex = 0;
    this.isInitialized = false;
  }

  public void initialize(
      final Primitive regDataSource,
      final Primitive regIndexSource,
      final String startLabel,
      final int arrayLength) {
    InvariantChecks.checkNotNull(regDataSource);
    InvariantChecks.checkNotNull(regIndexSource);
    InvariantChecks.checkNotNull(startLabel);
    InvariantChecks.checkGreaterThanZero(arrayLength);

    this.regData.setSource(regDataSource);
    this.regIndex.setSource(regIndexSource);
    this.startLabel = startLabel;

    this.arrayLength = arrayLength;
    this.arrayIndex = 0;
    this.isInitialized = true;
  }

  public boolean isInitialized() {
    return isInitialized;
  }

  public List<Call> getInit() {
    InvariantChecks.checkTrue(isInitialized);

    return initCalls;
  }

  public List<Call> getRead() {
    InvariantChecks.checkTrue(isInitialized);
    InvariantChecks.checkBounds(arrayIndex++, arrayLength);

    return readCalls;
  }

  public List<Call> getWrite() {
    InvariantChecks.checkTrue(isInitialized);
    InvariantChecks.checkBounds(arrayIndex++, arrayLength);

    return writeCalls;
  }
}
