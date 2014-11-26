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
  private final String calleeName;
  private final String attributeName;

  StatementAttributeCall(String calleeName, String attributeName) {
    super(Kind.CALL);
    if (null == attributeName) {
      throw new NullPointerException();
    }

    this.calleeName = calleeName;
    this.attributeName = attributeName;
  }

  public String getCalleeName() {
    return calleeName;
  }

  public String getAttributeName() {
    return attributeName;
  }
}
