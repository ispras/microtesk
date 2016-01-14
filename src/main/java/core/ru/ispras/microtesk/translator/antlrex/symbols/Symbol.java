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
 * The {@link Symbol} class describes a record in a symbol table.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public class Symbol implements ISymbol {
  private final String name;
  private final Enum<?> kind;
  private final Where where;
  private final IScope scope;
  private final IScope innerScope;
  private Object tag;

  private Symbol(
      final String name,
      final Enum<?> kind,
      final Where where,
      final IScope scope,
      final boolean hasInnerScope,
      final Object tag) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(kind);
    InvariantChecks.checkNotNull(scope);

    this.name = name;
    this.kind = kind;
    this.where = where;
    this.scope = scope;
    this.innerScope = hasInnerScope ? new Scope(scope, this) : null;
    this.tag = tag;
  }

  public static Symbol newBuiltInSymbol(
      final String name,
      final Enum<?> kind,
      final IScope scope) {
    return new Symbol(
        name,
        kind,
        null,
        scope,
        false,
        name
        );
  }

  public static Symbol newSymbol(
      final String name,
      final Where where,
      final Enum<?> kind,
      final IScope scope,
      final boolean hasInnerScope) {
    InvariantChecks.checkNotNull(where);
    return new Symbol(
        name,
        kind,
        where,
        scope,
        hasInnerScope,
        null
        );
  }

  @Override
  public final String getName() {
    return name;
  }

  @Override
  public final Enum<?> getKind() {
    return kind;
  }

  @Override
  public final Where getWhere() {
    return where;
  }

  @Override
  public final IScope getOuterScope() {
    return scope;
  }

  @Override
  public IScope getInnerScope() {
    return innerScope;
  }

  @Override
  public Object getTag() {
    return tag;
  }

  @Override
  public void setTag(final Object tag) {
    InvariantChecks.checkTrue(this.tag == null, "Tag is already set.");
    this.tag = tag;
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
  public boolean isReserved() {
    return null == where;
  }
}
