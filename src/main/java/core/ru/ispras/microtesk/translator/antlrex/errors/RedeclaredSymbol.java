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
import ru.ispras.microtesk.translator.antlrex.symbols.Symbol;

public final class RedeclaredSymbol implements ISemanticError {
  private static final String FORMAT =
      "Symbol '%s' has been already defined: first defined in %s as %s";

  private final String FORMAT_KEYWORD =
      "Symbol '%s' is a reserved keyword of type %s";

  private final Symbol symbol;

  public RedeclaredSymbol(Symbol symbol) {
    this.symbol = symbol;
  }

  @Override
  public String getMessage() {
    if (symbol.isReserved()) {
      return String.format(FORMAT_KEYWORD, symbol.getName(), symbol.getKind().name());
    }

    return String.format(
        FORMAT,
        symbol.getName(),
        symbol.getWhere(),
        symbol.getKind().name());
  }
}
