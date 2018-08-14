/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.memory.Section;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * The {@link ExceptionHandlerBuilder} class builds the template representation
 * of exception handlers.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class ExceptionHandlerBuilder {
  private final String id;
  private final Section section;
  private final boolean isDebugPrinting;
  private Set<Integer> instances;
  private final List<ExceptionHandler.EntryPoint> entryPoints;

  private BigInteger origin;
  private Set<String> exceptions;
  private List<AbstractCall> calls;

  public ExceptionHandlerBuilder(
      final String id,
      final Section section,
      final boolean isDebugPrinting) {
    InvariantChecks.checkNotNull(id);
    InvariantChecks.checkNotNull(section);

    this.id = id;
    this.section = section;
    this.isDebugPrinting = isDebugPrinting;
    this.instances = null;
    this.entryPoints = new ArrayList<>();
    this.exceptions = null;
    this.origin = null;
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

  public void beginEntryPoint(final BigInteger origin, final String exception) {
    InvariantChecks.checkNotNull(exception);
    beginEntryPoint(origin, Collections.singletonList(exception));
  }

  public void beginEntryPoint(final BigInteger origin, final Collection<String> exceptions) {
    InvariantChecks.checkNotNull(origin);
    InvariantChecks.checkGreaterThan(origin, BigInteger.ZERO);
    InvariantChecks.checkNotEmpty(exceptions);

    if (isDebugPrinting) {
      Logger.debug("Exception handler: .org 0x%x for %s", origin, exceptions);
    }

    InvariantChecks.checkTrue(this.origin == null);
    InvariantChecks.checkTrue(this.exceptions == null);
    InvariantChecks.checkTrue(this.calls == null);

    this.origin = origin;
    this.exceptions = new LinkedHashSet<>(exceptions);
    this.calls = new ArrayList<>();
  }

  public void endEntryPoint() {
    final ExceptionHandler.EntryPoint entryPoint =
        new ExceptionHandler.EntryPoint(origin, exceptions, calls);

    this.entryPoints.add(entryPoint);

    this.origin = null;
    this.exceptions = null;
    this.calls = null;
  }

  public void addCall(final AbstractCall call) {
    InvariantChecks.checkNotNull(call);
    this.calls.add(call);
  }

  public ExceptionHandler build() {
    InvariantChecks.checkTrue(this.origin == null);
    InvariantChecks.checkTrue(this.exceptions == null);
    InvariantChecks.checkTrue(this.calls == null);

    return new ExceptionHandler(id, section, instances, entryPoints);
  }
}
