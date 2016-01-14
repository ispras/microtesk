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

/**
 * The {@link SymbolTable} class implements a symbol table.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class SymbolTable {
  private final SymbolScope globalScope;
  private SymbolScope scope;

  /**
   * Creates a symbol table with a global scope. 
   */

  public SymbolTable() {
    this.globalScope = new SymbolScope(null);
    this.scope = globalScope;
  }

  /**
   * Defines reserved keywords by placing corresponding symbols
   * into the global scope.
   * 
   * @param kind Symbol kind for reserved keywords.
   * @param names Collection of keywords to be registered.
   */

  public void defineReserved(final Enum<?> kind, final String[] names) {
    for (final String s : names) {
      globalScope.define(Symbol.newReserved(s, kind, globalScope));
    }
  }

  /**
   * Checks whether the specified named was registered as reserved keyword.
   * 
   * @param name Name to be checked.
   * @return {@code true} if it is a reserved keyword of {@code false} otherwise.
   */

  public boolean isReserved(final String name) {
    final Symbol symbol = globalScope.resolveMember(name);
    return symbol != null && symbol.isReserved();
  }

  /**
   * Returns the current scope.
   * 
   * @return Current scope.
   */

  public SymbolScope peek() {
    return scope;
  }

  /**
   * Starts a new scope and sets it as the current scope. The new scope
   * will be nested into the current scope.
   */

  public void push() {
    this.scope = new SymbolScope(scope);
  }

  /**
   * Sets the specified scope as the current scope. The new scope must be
   * nested in the old scope.
   *  
   * @param scope Scope to be set as the current scope.
   * 
   * @throws IllegalArgumentException if {@code scope} is {@code null};
   *         if {@code scope} is not nested into the current scope.
   */

  public void push(final SymbolScope scope) {
    InvariantChecks.checkNotNull(scope);
    InvariantChecks.checkTrue(this.scope == scope.getOuterScope(), "Not a nested scope.");

    this.scope = scope;
  }

  /**
   * Discards the current scope and replaces it with its outer scope.
   * 
   * @throws IllegalStateException if the current scope is the global scope
   *         which cannot be discarded.
   */

  public void pop() {
    InvariantChecks.checkNotNull(scope);

    if (globalScope == scope) {
      throw new IllegalStateException("Cannot pop the global scope.");
    }

    scope = scope.getOuterScope();
  }

  /**
   * Defines the specified symbol in the current scope.
   * 
   * @param symbol Symbol to be defined.
   */

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
