/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.situation;

import ru.ispras.microtesk.model.api.metadata.MetaSituation;

public abstract class Situation implements ISituation {
  private final IInfo info;

  public Situation(IInfo info) {
    assert null != info;
    this.info = info;
  }

  protected final IInfo getInfo() {
    return info;
  }

  public static final class Info implements IInfo {
    private final String name;
    private final IFactory factory;
    private final MetaSituation metaData;

    public Info(String name, IFactory factory) {
      assert null != factory;

      this.name = name;
      this.factory = factory;
      this.metaData = new MetaSituation(name);
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public MetaSituation getMetaData() {
      return metaData;
    }

    @Override
    public ISituation createSituation() {
      return factory.create();
    }
  }
}
