/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

/**
 * The {@link SymbolScope} interface is to be implemented by symbol scopes.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public interface SymbolScope {
  /**
   * Defines the specified symbol in the current scope.
   *
   * @param symbol Symbol to be defined.
   *
   * @throws IllegalArgumentException if {@code symbol} is {@code null};
   *         if a symbol with such a name is already defined in the current scope.
   */
  void define(final Symbol symbol);

  /**
   * Searches for a symbol by its name in the current scope and its outer scopes.
   * If no symbol is found, {@code null} is returned.
   *
   * @param name Symbol name.
   * @return Symbol or {@code null} if it is not defined.
   */
  Symbol resolve(final String name);

  /**
   * Searches for a symbol by its name in the current scope only. Outer
   * scopes are not searched. If no such symbol is found, {@code null} is returned.
   *
   * @param name Symbol name.
   * @return Symbol or {@code null} if it is not defined.
   */
  Symbol resolveMember(final String name);

  /**
   * Searches for a symbol described by an array containing its name 
   * preceded with names of the scopes the symbol is nested into. Search
   * starts in the current scope and goes to outer scopes until the first
   * nesting scope is found. Then the search is continued in that scope.
   * If no such symbol is found, {@code null} is returned.
   *
   * @param names Array of names.
   * @return Symbol or {@code null} if it is not defined.
   */
  Symbol resolveNested(final String... names);

  /**
   * Returns the outer scope for the current scope or {@code null} if
   * there is no outer scope.
   *
   * @return Outer scope or {@code null} if there is no outer scope.
   */
  SymbolScope getOuterScope();

  /**
   * Returns symbol associated with the scope (or containing the scope)
   * or {@code null} it there is not such symbol.
   *
   * @return Associated symbol or {@code null} it there is not such symbol.
   */
  Symbol getAssociatedSymbol();
}
