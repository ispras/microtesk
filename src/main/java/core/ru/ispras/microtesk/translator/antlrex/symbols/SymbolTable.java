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

public final class SymbolTable implements IScope {
  private final IScope globalScope = new Scope(null);
  private IScope scope;

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
    this.scope = new Scope(scope);
  }

  public void push(final IScope scope) {
    InvariantChecks.checkNotNull(scope);

    if (globalScope == scope) {
      throw new IllegalStateException();
    }

    this.scope = scope;
  }

  public void pop() {
    InvariantChecks.checkNotNull(scope);

    if (globalScope == scope) {
      throw new IllegalStateException();
    }

    scope = scope.getOuterScope();
  }

  public IScope peek() {
    return scope;
  }

  @Override
  public void define(final Symbol symbol) {
    peek().define(symbol);
  }

  @Override
  public Symbol resolve(final String name) {
    return peek().resolve(name);
  }

  @Override
  public Symbol resolveMember(final String name) {
    return peek().resolveMember(name);
  }

  @Override
  public Symbol resolveNested(final String... names) {
    return peek().resolveNested(names);
  }

  @Override
  public IScope getOuterScope() {
    return peek().getOuterScope();
  }

  @Override
  public Symbol getAssociatedSymbol() {
    return peek().getAssociatedSymbol();
  }
}
