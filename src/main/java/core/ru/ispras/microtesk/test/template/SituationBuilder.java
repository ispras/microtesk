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
import java.util.Map;

import ru.ispras.fortress.randomizer.Variate;

public final class SituationBuilder {
  private final String name;
  private Map<String, Object> attributes;

  SituationBuilder(String name) {
    this.name = name;
    this.attributes = null;
  }

  public SituationBuilder setAttribute(String attrName, int value) {
    setAttributeCommon(attrName, value);
    return this;
  }

  public SituationBuilder setAttribute(String attrName, String value) {
    setAttributeCommon(attrName, value);
    return this;
  }

  public SituationBuilder setAttribute(String attrName, Variate<?> value) {
    setAttributeCommon(attrName, value);
    return this;
  }

  private void setAttributeCommon(String attrName, Object value) {
    if (null == attributes) {
      attributes = new LinkedHashMap<String, Object>();
    }

    attributes.put(attrName, value);
  }

  public Situation build() {
    return new Situation(name, attributes);
  }
}
