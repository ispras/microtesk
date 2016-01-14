/*
 * Copyright 2012-2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.antlrex.symbols;

import ru.ispras.fortress.util.InvariantChecks;

public class Symbol implements ISymbol {
  private final String name;
  private final Where where;
  private final Enum<?> kind;
  private final IScope scope;

  public Symbol(
      final String name,
      final Where where,
      final Enum<?> kind,
      final IScope scope) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(where);
    InvariantChecks.checkNotNull(kind);
    InvariantChecks.checkNotNull(scope);

    this.name = name;
    this.where = where;
    this.kind = kind;
    this.scope = scope;
  }

  @Override
  public String toString() {
    return String.format(
        "Symbol [name=%s, kind=%s, scope=%s, innerScope=%s]",
         getName(),
         getKind(),
         getOuterScope(),
         getInnerScope()
         );
  }

  @Override
  public final String getName() {
    return name;
  }

  @Override
  public final Enum<?> getKind() {
    return kind;
  }

  @Override
  public final Where getWhere() {
    return where;
  }

  @Override
  public final IScope getOuterScope() {
    return scope;
  }

  @Override
  public IScope getInnerScope() {
    return null;
  }
}
