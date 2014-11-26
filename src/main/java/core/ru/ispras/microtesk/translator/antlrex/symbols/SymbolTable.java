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

package ru.ispras.microtesk.translator.antlrex.symbols;

public final class SymbolTable<Kind extends Enum<Kind>> implements IScope<Kind> {
  private final IScope<Kind> globalScope = new Scope<Kind>(null);
  private IScope<Kind> scope;

  public SymbolTable() {
    this.scope = globalScope;
  }

  public void defineReserved(Kind kind, String[] names) {
    for (String s : names) {
      globalScope.define(new BuiltInSymbol<Kind>(s, kind, globalScope));
    }
  }

  public void push() {
    this.scope = new Scope<Kind>(scope);
  }

  public void push(IScope<Kind> scope) {
    if (null == scope) {
      throw new NullPointerException();
    }

    if (globalScope == scope) {
      throw new IllegalStateException();
    }

    this.scope = scope;
  }

  public void pop() {
    if (null == scope) {
      throw new NullPointerException();
    }

    if (globalScope == scope) {
      throw new IllegalStateException();
    }

    scope = scope.getOuterScope();
  }

  public IScope<Kind> peek() {
    return scope;
  }

  @Override
  public void define(ISymbol<Kind> symbol) {
    peek().define(symbol);
  }

  @Override
  public ISymbol<Kind> resolve(String name) {
    return peek().resolve(name);
  }

  @Override
  public ISymbol<Kind> resolveMember(String name) {
    return peek().resolveMember(name);
  }

  @Override
  public ISymbol<Kind> resolveNested(String ... names) {
    return peek().resolveNested(names);
  }

  @Override
  public IScope<Kind> getOuterScope() {
    return peek().getOuterScope();
  }

  @Override
  public ISymbol<Kind> getAssociatedSymbol() {
    return peek().getAssociatedSymbol();
  }
}
