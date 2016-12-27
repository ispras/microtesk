/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.engine;

import ru.ispras.fortress.util.InvariantChecks;

public final class ThreadState {
  public static enum Kind {
    /** Ready to start execution */
    START,

    /** Executing code */
    EXECUTING,

    /** Waiting for a synchronization event */
    WAIT_SYNC,

    /** Waiting for an address to be allocated */
    WAIT_ADDR,

    /** Waiting for a label to be allocated */
    WAIT_LABEL
  }

  private final Kind kind;
  private final long address;
  private final Object object;

  public ThreadState(
      final Kind kind,
      final long address,
      final Object object) {
    InvariantChecks.checkNotNull(kind);

    this.kind = kind;
    this.address = address;
    this.object = object;
  }

  public Kind getKind() {
    return kind;
  }

  public long getAddress() {
    return address;
  }

  public Object getObject() {
    return object;
  }
}
