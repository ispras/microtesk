/*
 * Copyright 2013-2014 ISP RAS (http://www.ispras.ru)
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

import java.util.Map;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.metadata.MetaSituation;

public interface ISituation {
  public boolean setInput(String name, Data value);
  public boolean setOutput(String name);
  public Map<String, Data> solve() throws ConfigurationException;

  public interface IInfo {
    public MetaSituation getMetaData();
    public String getName();
    public ISituation createSituation();
  }

  public interface IFactory {
    public ISituation create();
  }
}
