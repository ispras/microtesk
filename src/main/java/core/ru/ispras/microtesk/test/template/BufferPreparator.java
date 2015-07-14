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

import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;

public final class BufferPreparator {
  private final String bufferId;
  private final LazyData address;
  private final Map<String, LazyData> entry;
  private final List<Call> calls;

  protected BufferPreparator(
      final String bufferId,
      final LazyData address,
      final Map<String, LazyData> entry,
      final List<Call> calls) {
    InvariantChecks.checkNotNull(bufferId);
    InvariantChecks.checkNotNull(address);
    InvariantChecks.checkNotNull(entry);
    InvariantChecks.checkNotNull(calls);

    this.bufferId = bufferId;
    this.address = address;
    this.entry = entry;
    this.calls = calls;
  }

  public String getBufferId() {
    return bufferId;
  }
}
