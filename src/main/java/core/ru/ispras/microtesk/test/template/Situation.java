/*
 * Copyright 2014-2019 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.testbase.TestData;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link Situation} represents a situation, a test data provider, or an allocation constraint.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class Situation {
  public static enum Kind {
    SITUATION,
    TESTDATA,
    ALLOCATION
  }

  public static final class Builder {
    private final String name;
    private final Kind kind;
    private Map<String, Object> attributes;

    Builder(final String name) {
      this(name, Kind.SITUATION);
    }

    Builder(final String name, final Kind kind) {
      InvariantChecks.checkNotNull(name);
      InvariantChecks.checkNotNull(kind);

      this.name = name;
      this.kind = kind;
      this.attributes = null;
    }

    public Builder setAttribute(final String attrName, final Object value) {
      if (null == attributes) {
        attributes = new LinkedHashMap<String, Object>();
      }

      attributes.put(attrName, value);
      return this;
    }

    public Situation build() {
      return new Situation(name, kind, attributes);
    }
  }

  private final String name;
  private final Kind kind;
  private final Map<String, Object> attributes;
  private TestData testData;

  public Situation(final String name, final Map<String, Object> attributes) {
    this(name, Kind.SITUATION, attributes);
  }

  public Situation(final String name, final Kind kind, final Map<String, Object> attributes) {
    InvariantChecks.checkNotNull(name);

    this.name = name;
    this.kind = kind;
    this.attributes = null != attributes ? attributes : new LinkedHashMap<String, Object>();
    this.testData = null;
  }

  public String getName() {
    return name;
  }

  public Kind getKind() {
    return kind;
  }

  public Object getAttribute(final String attrName) {
    return attributes.get(attrName);
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public TestData getTestData() {
    return testData;
  }

  public void setTestData(final TestData testData) {
    this.testData = testData;
  }

  @Override
  public String toString() {
    if (attributes.isEmpty()) {
      return name;
    }

    final StringBuilder sb = new StringBuilder();
    for (final Map.Entry<String, Object> e : attributes.entrySet()) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(String.format("%s=%s", e.getKey(), e.getValue()));
    }

    return String.format(
        "%s %s(%s)", kind.name().toLowerCase(), name, sb);
  }
}
