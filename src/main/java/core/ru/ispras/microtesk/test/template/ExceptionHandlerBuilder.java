/*
 * Copyright 2015-2016 ISP RAS (http://www.ispras.ru)
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
import java.util.TreeSet;
import java.util.List;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;

/**
 * The {@link ExceptionHandlerBuilder} class builds the template representation
 * of exception handlers.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class ExceptionHandlerBuilder {
  private final String id;
  private Set<Integer> instances;
  private final List<ExceptionHandler.Section> sections;

  private BigInteger address;
  private Set<String> exceptions; 
  private List<Call> calls;

  public ExceptionHandlerBuilder(final String id) {
    InvariantChecks.checkNotNull(id);

    this.id = id;
    this.instances = null;
    this.sections = new ArrayList<>();
    this.exceptions = null;
    this.address = null;
    this.calls = null;
  }

  public void setInstances(final int index) {
    InvariantChecks.checkGreaterOrEqZero(index);
    instances = Collections.singleton(index);
  }

  public void setInstances(final int indexMin, final int indexMax) {
    InvariantChecks.checkGreaterOrEqZero(indexMin);
    InvariantChecks.checkTrue(indexMin <= indexMax);

    instances = new TreeSet<>();
    for (int index = indexMin; index <= indexMax; index++) {
      instances.add(index);
    }
  }

  public void setInstances(final Collection<Integer> indices) {
    InvariantChecks.checkNotNull(indices);
    instances = new TreeSet<>(indices);
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

    return new ExceptionHandler(id, instances, sections);
  }
}
