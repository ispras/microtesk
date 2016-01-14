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

public final class BuiltInSymbol implements ISymbol {
  private final String name;
  private final Enum<?> kind;
  private final IScope scope;

  public BuiltInSymbol(
      final String name,
      final Enum<?> kind,
      final IScope scope) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(kind);
    InvariantChecks.checkNotNull(scope);

    this.name = name;
    this.kind = kind;
    this.scope = scope;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Enum<?> getKind() {
    return kind;
  }

  @Override
  public int getLine() {
    return -1;
  }

  @Override
  public int getPositionInLine() {
    return -1;
  }

  @Override
  public IScope getOuterScope() {
    return scope;
  }

  @Override
  public IScope getInnerScope() {
    return null;
  }
}
