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

package ru.ispras.microtesk.translator.antlrex.errors;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;

public class UndeclaredSymbol implements ISemanticError {
  private static final String FORMAT = "The '%s' symbol is not declared.";

  private final String symbolName;

  public UndeclaredSymbol(String symbolName) {
    this.symbolName = symbolName;
  }

  @Override
  public String getMessage() {
    return String.format(FORMAT, symbolName);
  }
}
