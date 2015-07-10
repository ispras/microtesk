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

public final class SymbolTable implements IScope {
  private final IScope globalScope = new Scope(null);
  private IScope scope;

  public SymbolTable() {
    this.scope = globalScope;
  }

  public void defineReserved(final Enum<?> kind, final String[] names) {
    for (final String s : names) {
      globalScope.define(new BuiltInSymbol(s, kind, globalScope));
    }
  }

  public boolean isReserved(final String name) {
    return resolve(name) instanceof BuiltInSymbol;
  }

  public void push() {
    this.scope = new Scope(scope);
  }

  public void push(IScope scope) {
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

  public IScope peek() {
    return scope;
  }

  @Override
  public void define(ISymbol symbol) {
    peek().define(symbol);
  }

  @Override
  public ISymbol resolve(String name) {
    return peek().resolve(name);
  }

  @Override
  public ISymbol resolveMember(String name) {
    return peek().resolveMember(name);
  }

  @Override
  public ISymbol resolveNested(String ... names) {
    return peek().resolveNested(names);
  }

  @Override
  public IScope getOuterScope() {
    return peek().getOuterScope();
  }

  @Override
  public ISymbol getAssociatedSymbol() {
    return peek().getAssociatedSymbol();
  }
}
