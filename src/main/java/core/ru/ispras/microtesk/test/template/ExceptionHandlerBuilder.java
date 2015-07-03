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
import static ru.ispras.fortress.util.InvariantChecks.checkTrue;
import static ru.ispras.fortress.util.InvariantChecks.checkGreaterThan;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.Logger;

public final class ExceptionHandlerBuilder {
  private final Map<String, ExceptionHandler.Section> sections;

  private String exceptionType; 
  private BigInteger address;
  private List<Call> calls;

  public ExceptionHandlerBuilder() {
    this.sections = new LinkedHashMap<>();
    this.exceptionType = null;
    this.address = null;
    this.calls = null;
  }

  public void beginSection(final String exceptionType, final BigInteger address) {
    checkNotNull(exceptionType);
    checkNotNull(address);
    checkGreaterThan(address, BigInteger.ZERO);

    Logger.debug("Exception handler: %s at 0x%x", exceptionType, address);

    checkTrue(this.exceptionType == null);
    checkTrue(this.address == null);
    checkTrue(this.calls == null);

    this.exceptionType = exceptionType;
    this.address = address;
    this.calls = new ArrayList<>();
  }

  public void endSection() {
    final ExceptionHandler.Section section =
        new ExceptionHandler.Section(exceptionType, address, calls);

    if (null != this.sections.put(section.getExceptionType(), section)) {
      Logger.error("Handler for exception type %s is redefined.", exceptionType);
    }

    this.exceptionType = null;
    this.address = null;
    this.calls = null;
  }

  public void addCall(final Call call) {
    checkNotNull(call);
    this.calls.add(call);
  }

  public ExceptionHandler build() {
    checkTrue(this.exceptionType == null);
    checkTrue(this.address == null);
    checkTrue(this.calls == null);

    return new ExceptionHandler(sections);
  }
}
