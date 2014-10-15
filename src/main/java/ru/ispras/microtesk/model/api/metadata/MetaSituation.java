/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.metadata;

/**
 * The MetaSituation class describes test situations.
 * 
 * @author Andrei Tatarnikov
 */

public final class MetaSituation implements MetaData {
  private final String name;

  /**
   * Constructs a metadata object for a situation that has the specified name,
   * 
   * @param name Situation name.
   * 
   * @throws NullPointerException if the parameter is {@code null}.
   */

  public MetaSituation(String name) {
    if (null == name) {
      throw new NullPointerException();
    }

    this.name = name;
  }

  /**
   * Returns the name of the test situation.
   * 
   * @return Situation name.
   */

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return String.format("MetaSituation %s", name);
  }
}
