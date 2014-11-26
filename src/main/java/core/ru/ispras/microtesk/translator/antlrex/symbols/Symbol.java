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

import org.antlr.runtime.Token;

public class Symbol<Kind extends Enum<Kind>> implements ISymbol<Kind> {
  private final Token token;
  private final Kind kind;
  private final IScope<Kind> scope;

  public Symbol(Token token, Kind kind, IScope<Kind> scope) {
    if (null == token) {
      throw new NullPointerException();
    }

    if (null == kind) {
      throw new NullPointerException();
    }

    if (null == scope) {
      throw new NullPointerException();
    }

    this.token = token;
    this.kind = kind;
    this.scope = scope;
  }

  @Override
  public String toString() {
    return String.format("Symbol [name=%s, kind=%s, scope=%s, innerScope=%s]",
      getName(), getKind(), getOuterScope(), getInnerScope());
  }

  @Override
  public final String getName() {
    return token.getText();
  }

  @Override
  public final Kind getKind() {
    return kind;
  }

  @Override
  public final int getTokenIndex() {
    return token.getTokenIndex();
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
  public final IScope<Kind> getOuterScope() {
    return scope;
  }

  @Override
  public IScope<Kind> getInnerScope() {
    return null;
  }
}
