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

import java.util.Collections;
import java.util.List;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;

public final class SymbolTypeMismatch<Kind extends Enum<Kind>> implements ISemanticError {
  public static final String FORMAT =
    "The '%s' symbol uses a wrong type. It is %s while %s is expected in this expression.";

  private final String symbolName;
  private final Kind kind;
  private final List<Kind> expected;

  public SymbolTypeMismatch(String symbolName, Kind kind, Kind expected) {
    this(symbolName, kind, Collections.singletonList(expected));
  }

  public SymbolTypeMismatch(String symbolName, Kind kind, List<Kind> expected) {
    this.symbolName = symbolName;
    this.kind = kind;
    this.expected = expected;
  }

  private static <Kind extends Enum<Kind>> String kindsToString(List<Kind> kinds) {
    final StringBuffer sb = new StringBuffer();

    if (kinds.size() > 1) {
      sb.append('{');
    }

    for (Kind k : kinds) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(k.name());
    }

    if (kinds.size() > 1) {
      sb.append('}');
    }

    return sb.toString();
  }

  @Override
  public String getMessage() {
    return String.format(FORMAT, symbolName, kind, kindsToString(expected));
  }
}
