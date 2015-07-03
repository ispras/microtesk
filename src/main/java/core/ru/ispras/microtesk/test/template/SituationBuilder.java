/*
 * Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.randomizer.Variate;

public final class SituationBuilder {
  private final String name;
  private Map<String, Object> attributes;

  SituationBuilder(final String name) {
    this.name = name;
    this.attributes = null;
  }

  public SituationBuilder setAttribute(final String attrName, final int value) {
    setAttributeCommon(attrName, value);
    return this;
  }

  public SituationBuilder setAttribute(final String attrName, final String value) {
    setAttributeCommon(attrName, value);
    return this;
  }

  public SituationBuilder setAttribute(final String attrName, final List<String> value) {
    setAttributeCommon(attrName, value);
    return this;
  }

  public SituationBuilder setAttribute(final String attrName, final Variate<?> value) {
    setAttributeCommon(attrName, value);
    return this;
  }

  private void setAttributeCommon(final String attrName, final Object value) {
    if (null == attributes) {
      attributes = new LinkedHashMap<String, Object>();
    }

    attributes.put(attrName, value);
  }

  public Situation build() {
    return new Situation(name, attributes);
  }
}
