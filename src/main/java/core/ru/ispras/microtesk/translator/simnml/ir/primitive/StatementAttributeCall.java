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

package ru.ispras.microtesk.translator.simnml.ir.primitive;

public final class StatementAttributeCall extends Statement {
  private final Instance calleeInstance;
  private final String calleeName;
  private final String attributeName;

  static StatementAttributeCall newThisCall(String attributeName) {
    return new StatementAttributeCall(null, null, attributeName);
  }

  static StatementAttributeCall newArgumentCall(String calleeName, String attributeName) {
    if (null == calleeName) {
      throw new NullPointerException();
    }

    return new StatementAttributeCall(null, calleeName, attributeName);
  }
  
  static StatementAttributeCall newInstanceCall(Instance calleeInstance, String attributeName) {
    if (null == calleeInstance) {
      throw new NullPointerException();
    }

    return new StatementAttributeCall(calleeInstance, null, attributeName);
  }

  private StatementAttributeCall(Instance calleeInstance, String calleeName, String attributeName) {
    super(Kind.CALL);
    if (null == attributeName) {
      throw new NullPointerException();
    }

    this.calleeInstance = calleeInstance;
    this.calleeName = calleeName;
    this.attributeName = attributeName;
  }

  public String getCalleeName() {
    return calleeName;
  }

  public String getAttributeName() {
    return attributeName;
  }

  public Instance getCalleeInstance() {
    return calleeInstance;
  }
}
