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

public final class SymbolTable {
  private final SymbolScope globalScope = new SymbolScope(null);
  private SymbolScope scope;

  public SymbolTable() {
    this.scope = globalScope;
  }

  public void defineReserved(final Enum<?> kind, final String[] names) {
    for (final String s : names) {
      globalScope.define(Symbol.newBuiltInSymbol(s, kind, globalScope));
    }
  }

  public boolean isReserved(final String name) {
    final Symbol symbol = globalScope.resolve(name);
    return symbol != null && symbol.isReserved();
  }

  public void push() {
    this.scope = new SymbolScope(scope);
  }

  public void push(final SymbolScope scope) {
    InvariantChecks.checkNotNull(scope);
    InvariantChecks.checkTrue(globalScope != scope);

    this.scope = scope;
  }

  public void pop() {
    InvariantChecks.checkNotNull(scope);

    if (globalScope == scope) {
      throw new IllegalStateException("Cannot pop global scope.");
    }

    scope = scope.getOuterScope();
  }

  public SymbolScope peek() {
    return scope;
  }

  public void define(final Symbol symbol) {
    peek().define(symbol);
  }

  public Symbol resolve(final String name) {
    return peek().resolve(name);
  }

  public Symbol resolveMember(final String name) {
    return peek().resolveMember(name);
  }

  public Symbol resolveNested(final String... names) {
    return peek().resolveNested(names);
  }
}
