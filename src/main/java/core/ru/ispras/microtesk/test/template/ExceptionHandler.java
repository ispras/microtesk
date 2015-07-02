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

public final class ExceptionHandler {

  public static final class Section {
    private final BigInteger address;
    private final List<Call> calls;

    protected Section(final BigInteger address, final List<Call> calls) {
      InvariantChecks.checkNotNull(address);
      InvariantChecks.checkGreaterThan(address, BigInteger.ZERO);
      InvariantChecks.checkNotNull(calls);

      this.address = address;
      this.calls = Collections.unmodifiableList(calls);
    }

    public List<Call> getCalls() {
      return calls;
    }

    public BigInteger getAddress() {
      return address;
    }
  }

  private final List<Section> sections;

  protected ExceptionHandler(final List<Section> sections) {
    InvariantChecks.checkNotNull(sections);
    this.sections = Collections.unmodifiableList(sections);
  }

  public List<Section> getSections() {
    return sections;
  }

  public int getSectionCount() {
    return sections.size();
  }
}
