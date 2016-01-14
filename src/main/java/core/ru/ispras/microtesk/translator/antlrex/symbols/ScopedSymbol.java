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

public final class ScopedSymbol extends Symbol {
  private final IScope innerScope;

  public ScopedSymbol(
      final String name,
      final Where where,
      final Enum<?> kind,
      final IScope scope) {
    super(name, where, kind, scope);
    this.innerScope = new Scope(scope, this);
  }

  @Override
  public final IScope getInnerScope() {
    return innerScope;
  }
}
