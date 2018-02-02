/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

import java.util.List;

/**
 * The {@link SymbolScopeArray} class aggregates several scopes located at the same level.
 * It allows defining symbols in several scopes at the same time. This needed when
 * a single grammar rule defines several symbols.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class SymbolScopeArray implements SymbolScope {
  private final List<SymbolScope> scopes;

  public SymbolScopeArray(final List<SymbolScope> scopes) {
    InvariantChecks.checkNotEmpty(scopes);
    this.scopes = scopes;
  }

  @Override
  public void define(final Symbol symbol) {
    for (final SymbolScope symbolScope : scopes) {
      symbolScope.define(symbol);
    }
  }

  @Override
  public Symbol resolve(final String name) {
    for (final SymbolScope symbolScope : scopes) {
      final Symbol symbol = symbolScope.resolve(name);
      if (null != symbol) {
        return symbol;
      }
    }
    return null;
  }

  @Override
  public Symbol resolveMember(final String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Symbol resolveNested(final String... names) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SymbolScope getOuterScope() {
    return scopes.get(0).getOuterScope();
  }

  @Override
  public Symbol getAssociatedSymbol() {
    return scopes.get(0).getAssociatedSymbol();
  }

  @Override
  public String toString() {
    return String.format("SymbolScopeArray [scopes=%s]", scopes);
  }
}
