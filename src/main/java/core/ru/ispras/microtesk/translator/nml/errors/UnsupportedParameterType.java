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

package ru.ispras.microtesk.translator.nml.errors;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;

public final class UnsupportedParameterType implements ISemanticError {
  private static final String FORMAT =
    "The '%s' parameter has unsupported type (%s). This construction supports only %s.";

  private final String name;
  private final String kind;
  private final String expectedKinds;

  public UnsupportedParameterType(String name, String kind, String expectedKinds) {
    this.name = name;
    this.kind = kind;
    this.expectedKinds = expectedKinds;
  }

  @Override
  public String getMessage() {
    return String.format(FORMAT, name, kind, expectedKinds);
  }
}
