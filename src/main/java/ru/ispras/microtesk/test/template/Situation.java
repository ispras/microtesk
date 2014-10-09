/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Situation.java, Sep 26, 2014 3:11:49 PM Andrei Tatarnikov
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
import java.util.Map;

public final class Situation {
  private final String name;
  private final Map<String, Object> attributes;

  Situation(String name, Map<String, Object> attributes) {
    if (null == name) {
      throw new NullPointerException();
    }

    this.name = name;
    this.attributes = (null != attributes) ?
      Collections.unmodifiableMap(attributes) :
      Collections.<String, Object>emptyMap();
  }

  public String getName() {
    return name;
  }

  public Object getAttribute(String attrName) {
    return attributes.get(attrName);
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public String toString() {
    if (attributes.isEmpty()) {
      return name;
    }

    final StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, Object> e : attributes.entrySet()) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(String.format("%s=%s", e.getKey(), e.getValue()));
    }
    return String.format("%s(%s)", name, sb);
  }
}
