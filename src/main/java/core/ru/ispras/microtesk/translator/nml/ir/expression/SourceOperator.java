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

package ru.ispras.microtesk.translator.nml.ir.expression;

import ru.ispras.microtesk.translator.nml.ir.valueinfo.ValueInfo;

public final class SourceOperator {
  private final Operator operator;
  private final ValueInfo castValueInfo;
  private final ValueInfo resultValueInfo;

  SourceOperator(Operator operator, ValueInfo castValueInfo, ValueInfo resultValueInfo) {
    if (null == operator) {
      throw new NullPointerException();
    }

    if (null == castValueInfo) {
      throw new NullPointerException();
    }

    if (null == resultValueInfo) {
      throw new NullPointerException();
    }

    this.operator = operator;
    this.castValueInfo = castValueInfo;
    this.resultValueInfo = resultValueInfo;
  }

  public Operator getOperator() {
    return operator;
  }

  public ValueInfo getCastValueInfo() {
    return castValueInfo;
  }

  public ValueInfo getResultValueInfo() {
    return resultValueInfo;
  }
}
