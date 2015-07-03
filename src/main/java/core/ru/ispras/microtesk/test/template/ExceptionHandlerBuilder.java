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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;

public final class ExceptionHandlerBuilder {
  private final List<ExceptionHandler.Section> sections;

  private BigInteger address;
  private Set<String> exceptions; 
  private List<Call> calls;

  public ExceptionHandlerBuilder() {
    this.sections = new ArrayList<>();
    this.exceptions = null;
    this.address = null;
    this.calls = null;
  }

  public void beginSection(final BigInteger address, final String exception) {
    InvariantChecks.checkNotNull(exception);
    beginSection(address, Collections.singletonList(exception));
  }

  public void beginSection(final BigInteger address, final Collection<String> exceptions) {
    InvariantChecks.checkNotNull(address);
    InvariantChecks.checkGreaterThan(address, BigInteger.ZERO);
    InvariantChecks.checkNotEmpty(exceptions);

    Logger.debug("Exception handler: .org 0x%x for %s", address, exceptions);

    InvariantChecks.checkTrue(this.address == null);
    InvariantChecks.checkTrue(this.exceptions == null);
    InvariantChecks.checkTrue(this.calls == null);

    this.address = address;
    this.exceptions = new LinkedHashSet<>(exceptions);
    this.calls = new ArrayList<>();
  }

  public void endSection() {
    final ExceptionHandler.Section section =
        new ExceptionHandler.Section(address, exceptions, calls);

    this.sections.add(section);

    this.address = null;
    this.exceptions = null;
    this.calls = null;
  }

  public void addCall(final Call call) {
    InvariantChecks.checkNotNull(call);
    this.calls.add(call);
  }

  public ExceptionHandler build() {
    InvariantChecks.checkTrue(this.address == null);
    InvariantChecks.checkTrue(this.exceptions == null);
    InvariantChecks.checkTrue(this.calls == null);

    return new ExceptionHandler(sections);
  }
}
