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
import ru.ispras.microtesk.translator.nml.NmlSymbolKind;

public final class UndefinedPrimitive implements ISemanticError {
  private static final String FORMAT =
    "The '%s' primitive is not defined or does not have the '%s' type.";

  private final String name;
  private final NmlSymbolKind type;

  public UndefinedPrimitive(String name, NmlSymbolKind type) {
    this.name = name;
    this.type = type;
  }

  @Override
  public String getMessage() {
    return String.format(FORMAT, name, type.name());
  }
}
