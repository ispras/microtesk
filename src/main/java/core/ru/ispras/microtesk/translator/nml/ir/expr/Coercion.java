/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.ir.expr;

/**
 * The {@link Coercion} enumeration describes coercion types
 * applied in nML.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public enum Coercion {
  IMPLICIT("valueOf"),

  SIGN_EXTEND("signExtend"),
  ZERO_EXTEND("zeroExtend"),

  COERCE("coerce"),
  CAST("cast"),

  INT_TO_FLOAT("intToFloat"),
  FLOAT_TO_INT("floatToInt"),
  FLOAT_TO_FLOAT("floatToFloat");

  private final String methodName;

  private Coercion(final String methodName) {
    this.methodName = methodName;
  }

  public String getMethodName() {
    return methodName;
  }
}
