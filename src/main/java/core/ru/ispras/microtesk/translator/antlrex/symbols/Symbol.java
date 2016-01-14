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

public final class Symbol {
  private final String name;
  private final Enum<?> kind;
  private final Where where;
  private final SymbolScope scope;
  private final SymbolScope innerScope;
  private Object tag;

  /**
   * Constructs a symbol.
   *  
   * @param name Symbol name.
   * @param kind Symbol kind.
   * @param where Location of the symbol in the source code.
   * @param scope Scope the symbol will be placed to.
   * @param hasInnerScope Specifies whether the symbol has an inner scope.
   * @param tag Tag object associated with the symbol.
   * 
   * @throws IllegalArgumentException if {@code name}, {@code kind} or
   *         {@code scope} equals {@code null}.
   */

  private Symbol(
      final String name,
      final Enum<?> kind,
      final Where where,
      final SymbolScope scope,
      final boolean hasInnerScope,
      final Object tag) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(kind);
    InvariantChecks.checkNotNull(scope);

    this.name = name;
    this.kind = kind;
    this.where = where;
    this.scope = scope;
    this.innerScope = hasInnerScope ? new SymbolScope(scope, this) : null;
    this.tag = tag;
  }

  /**
   * Creates a new symbol for a reserved keyword.
   * 
   * Note: Tag object for the symbol is its name.
   * 
   * @param name Keyword name.
   * @param kind Symbol kind for the keyword.
   * @param scope Scope the symbol will be placed to.
   * 
   * @return New symbol for a reserved keyword.
   * 
   * @throws IllegalArgumentException if any of the parameters equals {@code null}. 
   */

  public static Symbol newReserved(
      final String name,
      final Enum<?> kind,
      final SymbolScope scope) {
    return new Symbol(
        name,
        kind,
        null,
        scope,
        false,
        name
        );
  }

  /**
   * Creates a new symbol.
   * 
   * @param name Symbol name.
   * @param kind Symbol kind.
   * @param where Location of the symbol in the source code.
   * @param scope Scope the symbol will be placed to.
   * @param hasInnerScope Specifies whether the symbol has an inner scope.
   * 
   * @return New symbol.
   * 
   * @throws IllegalArgumentException if any of the first four parameters
   *         equals {@code null}.
   */

  public static Symbol newSymbol(
      final String name,
      final Enum<?> kind,
      final Where where,
      final SymbolScope scope,
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

  /**
   * Returns the symbol name.
   * 
   * @return Symbol name.
   */

  public String getName() {
    return name;
  }

  /**
   * Returns the symbol kind.
   * 
   * @return Symbol kind.
   */

  public Enum<?> getKind() {
    return kind;
  }

  /**
   * Returns information on symbol location in source code or {@code null}
   * if the symbol is a reserved keyword. 
   * 
   * @return {@link Where} object or {@code null} for reserved keywords. 
   */

  public Where getWhere() {
    return where;
  }

  /**
   * Returns the scope where the symbol is defined.
   * 
   * @return Scope where the symbol is defined.
   */

  public SymbolScope getOuterScope() {
    return scope;
  }

  /**
   * Returns the scope nested into the symbol or {@code null} if
   * the symbol has no nested scope. 
   * 
   * @return Nested scope or {@code null} if the symbol has no nested scope.
   */

  public SymbolScope getInnerScope() {
    return innerScope;
  }

  /**
   * Returns the tag object associated with the symbol or {@code null} if
   * it is not assigned.
   * 
   * @return Tag object or {@code null} if it is not assigned.
   */

  public Object getTag() {
    return tag;
  }

  /**
   * Links the specified tag object the symbol. Links cannot be reassigned.
   * 
   * @param tag Tag object to be associated with the current symbol.
   * 
   * @throws IllegalArgumentException if {@code tag} is {@code null} or tag
   *         object is already assigned.
   */

  public void setTag(final Object tag) {
    InvariantChecks.checkNotNull(tag);
    InvariantChecks.checkTrue(this.tag == null, "Tag is already assigned.");
    this.tag = tag;
  }

  /**
   * Checks whether this symbol is a registered keyword.
   *  
   * @return {@code true} if it is a registered keyword or {@code false} otherwise.
   */

  public boolean isReserved() {
    return null == where;
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
}
