/*
 * Copyright 2012-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.antlrex;

import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.antlrex.errors.RedeclaredSymbol;
import ru.ispras.microtesk.translator.antlrex.errors.SymbolTypeMismatch;
import ru.ispras.microtesk.translator.antlrex.errors.UndeclaredSymbol;
import ru.ispras.microtesk.translator.antlrex.symbols.Symbol;
import ru.ispras.microtesk.translator.antlrex.symbols.SymbolScope;
import ru.ispras.microtesk.translator.antlrex.symbols.SymbolTable;

import java.util.Deque;
import java.util.List;
import java.util.Set;

/**
 * The {@link ParserBase} class is a base class for implementing ANTLR-based parsers.
 * It includes support for error reporting (inherited from {@link ParserEx})
 * and support for working with symbol tables.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public class ParserBase extends ParserEx {
  private SymbolTable symbols = null;
  private Set<String> revisions = null;
  private Deque<Boolean> revisionApplicable = null;

  public ParserBase(final TokenStream input, final RecognizerSharedState state) {
    super(input, state);
  }

  public final void assignSymbols(final SymbolTable symbols) {
    InvariantChecks.checkNotNull(symbols);
    this.symbols = symbols;
  }

  public final SymbolTable getSymbols() {
    return symbols;
  }

  public void assignRevisions(
      final Set<String> revisions,
      final Deque<Boolean> revisionApplicable) {
    InvariantChecks.checkNotNull(revisions);
    InvariantChecks.checkNotNull(revisionApplicable);

    this.revisions = revisions;
    this.revisionApplicable = revisionApplicable;
  }

  protected final void declare(
      final Token t,
      final Enum<?> kind,
      final boolean scoped) throws SemanticException {
    InvariantChecks.checkNotNull(symbols);

    if (!isRevisionApplicable()) {
      if (!isFakeScope()) {
        return;
      }

      // Symbol redefinitions within fake (throw-away) symbol scopes are ignored.
      final Symbol symbol = symbols.resolve(t.getText());
      if (null != symbol && symbol.getKind() == kind) {
        return;
      }
    }

    checkRedeclared(t);
    final Symbol symbol =
        Symbol.newSymbol(t.getText(), kind, where(t), symbols.peek(), scoped);

    symbols.define(symbol);
  }

  protected final void declareLocal(final Token t, final Enum<?> kind)
      throws SemanticException {
    if (isRevisionApplicable()) {
      final Symbol symbol = symbols.resolveMember(t.getText());
      if (null != symbol) {
        raiseError(where(t), new RedeclaredSymbol(symbol));
      }
      final Symbol local =
          Symbol.newSymbol(t.getText(), kind, where(t), symbols.peek(), false);
      symbols.define(local);
    }
  }

  protected final Symbol declareAndPushSymbolScope(
      final Token t,
      final Enum<?> kind) throws SemanticException {
    InvariantChecks.checkNotNull(symbols);

    final Symbol symbol =
        Symbol.newSymbol(t.getText(), kind, where(t), symbols.peek(), true);

    if (isRevisionApplicable()) {
      checkRedeclared(t);
      symbols.define(symbol);
    }

    symbols.push(symbol.getInnerScope());
    return symbol;
  }

  protected SymbolScope popSymbolScope() {
    final SymbolScope scope = symbols.peek();
    symbols.pop();
    return scope;
  }

  protected final void pushSymbolScopes(final List<SymbolScope> scopes) {
    InvariantChecks.checkNotNull(symbols);
    symbols.push(scopes);
  }

  private final void checkRedeclared(final Token t) throws SemanticException {
    InvariantChecks.checkNotNull(symbols);

    final Symbol symbol = symbols.resolve(t.getText());
    if (null == symbol) { // OK
      return;
    }

    raiseError(where(t), new RedeclaredSymbol(symbol));
  }

  protected final void checkDeclaration(
      final Token t,
      final Enum<?> expectedKind) throws SemanticException {
    InvariantChecks.checkNotNull(symbols);

    final Symbol symbol = symbols.resolve(t.getText());
    if (null == symbol) {
      raiseError(where(t), new UndeclaredSymbol(t.getText()));
    }

    if (expectedKind != symbol.getKind()) {
      raiseError(where(t), new SymbolTypeMismatch(t.getText(), symbol.getKind(), expectedKind));
    }
  }

  protected final boolean isDeclaredAs(final Token t, final Enum<?> expectedKind) {
    InvariantChecks.checkNotNull(symbols);

    final Symbol symbol = symbols.resolve(t.getText());
    if (null == symbol) {
      return false;
    }

    if (expectedKind != symbol.getKind()) {
      return false;
    }

    return true;
  }

  protected final boolean isRevisionApplicable(final Token revision) {
    InvariantChecks.checkNotNull(revisions);
    return isRevisionApplicable() && (null == revision || revisions.contains(revision.getText()));
  }

  private boolean isRevisionApplicable() {
    InvariantChecks.checkNotNull(revisionApplicable);
    return revisionApplicable.isEmpty() || revisionApplicable.peek();
  }

  protected final void pushRevisionApplicable(final boolean applicable) {
    InvariantChecks.checkNotNull(revisionApplicable);
    revisionApplicable.push(applicable);
  }

  protected final void popRevisionApplicable() {
    InvariantChecks.checkNotNull(revisionApplicable);
    revisionApplicable.pop();
  }

  private boolean isFakeScope() {
    final SymbolScope scope = symbols.peek();
    final Symbol symbol = scope.getAssociatedSymbol();

    return null != symbol && null == scope.resolve(symbol.getName());
  }
}
