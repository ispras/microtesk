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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * The {@link ExceptionHandler} class holds template descriptions of
 * handers of certain exception types to be executed on certain processing
 * element instance.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class ExceptionHandler {

  public static final class Section {
    private final BigInteger origin;
    private final Set<String> exceptions;
    private final List<AbstractCall> calls;

    protected Section(
        final BigInteger origin,
        final Set<String> exceptions,
        final List<AbstractCall> calls) {
      InvariantChecks.checkNotNull(exceptions);
      InvariantChecks.checkNotNull(origin);
      InvariantChecks.checkGreaterThan(origin, BigInteger.ZERO);
      InvariantChecks.checkNotNull(calls);

      this.origin = origin;
      this.exceptions = Collections.unmodifiableSet(exceptions);
      this.calls = Collections.unmodifiableList(calls);
    }

    public BigInteger getOrigin() {
      return origin;
    }

    public Set<String> getExceptions() {
      return exceptions;
    }

    public List<AbstractCall> getCalls() {
      return calls;
    }
  }

  private final String id;
  private final Set<Integer> instances;
  private final List<Section> sections;

  protected ExceptionHandler(
      final String id,
      final Set<Integer> instances,
      final List<Section> sections) {
    InvariantChecks.checkNotNull(id);
    InvariantChecks.checkNotNull(instances);
    InvariantChecks.checkNotNull(sections);

    this.id = id;
    this.instances = Collections.unmodifiableSet(instances);
    this.sections = Collections.unmodifiableList(sections);
  }

  public String getId() {
    return id;
  }

  public Set<Integer> getInstances() {
    return instances;
  }

  public List<Section> getSections() {
    return sections;
  }
}
