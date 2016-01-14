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

import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;

public final class SymbolScope {
  private final SymbolScope outerScope;
  private final Map<String, Symbol> memberSymbols;
  private final Symbol associatedSymbol;

  public SymbolScope(final SymbolScope scope, final Symbol associatedSymbol) {
    this.outerScope = scope;
    this.memberSymbols = new HashMap<>();
    this.associatedSymbol = associatedSymbol;
  }

  public SymbolScope(final SymbolScope scope) {
    this(scope, null);
  }

  public void define(final Symbol symbol) {
    InvariantChecks.checkNotNull(symbol);

    if (memberSymbols.containsKey(symbol.getName())) {
      throw new IllegalAccessError(
          String.format("Symbol %s is already defined.", symbol.getName()));
    }

    if (!memberSymbols.containsKey(symbol.getName())) {
      memberSymbols.put(symbol.getName(), symbol);
    }
  }

  public Symbol resolve(final String name) {
    if (memberSymbols.containsKey(name)) {
      return memberSymbols.get(name);
    }

    if (null != outerScope) {
      return outerScope.resolve(name);
    }

    return null;
  }

  public Symbol resolveMember(final String name) {
    return memberSymbols.get(name);
  }

  public Symbol resolveNested(final String... names) {
    if (names.length == 0) {
      throw new IllegalArgumentException("No arguments.");
    }

    Symbol symbol = resolve(names[0]);
    for (int index = 1; index < names.length; ++index) {
      if (null == symbol) {
        return null;
      }

      final SymbolScope scope = symbol.getInnerScope();
      if (null == scope) {
        return null;
      }

      symbol = scope.resolveMember(names[index]);
    }

    return symbol;
  }

  public SymbolScope getOuterScope() {
    return outerScope;
  }

  public Symbol getAssociatedSymbol() {
    return associatedSymbol;
  }

  @Override
  public String toString() {
    return String.format(
        "SymbolScope [symbol=%s, outerScope=%s, members=%d]",
        null != associatedSymbol ? associatedSymbol.getName() : "null",
        null != outerScope ? "YES" : "NO",
        memberSymbols.size()
        );
  }

}
