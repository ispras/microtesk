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

import org.antlr.runtime.Token;

import ru.ispras.fortress.util.InvariantChecks;

public class Symbol implements ISymbol {
  private final Token token;
  private final Enum<?> kind;
  private final IScope scope;

  public Symbol(final Token token, final Enum<?> kind, final IScope scope) {
    InvariantChecks.checkNotNull(token);
    InvariantChecks.checkNotNull(kind);
    InvariantChecks.checkNotNull(scope);

    this.token = token;
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
    return token.getText();
  }

  @Override
  public final Enum<?> getKind() {
    return kind;
  }

  @Override
  public final int getLine() {
    return token.getLine();
  }

  @Override
  public final int getPositionInLine() {
    return token.getCharPositionInLine();
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
