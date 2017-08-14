/*
 * Copyright 2012-2015 ISP RAS (http://www.ispras.ru)
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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

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
  private final Deque<Boolean> revisionApplicable;

  public ParserBase(final TokenStream input, final RecognizerSharedState state) {
    super(input, state);
    this.revisionApplicable = new ArrayDeque<>();
  }

  public final void assignSymbols(final SymbolTable symbols) {
    InvariantChecks.checkNotNull(symbols);
    this.symbols = symbols;
  }

  public final SymbolTable getSymbols() {
    return symbols;
  }

  public void assignRevisions(final Set<String> revisions) {
    InvariantChecks.checkNotNull(revisions);
    this.revisions = revisions;
  }

  protected final void declare(
      final Token t,
      final Enum<?> kind,
      final boolean scoped) throws SemanticException {
    InvariantChecks.checkNotNull(symbols);

    if (!isRevisionApplicable() && !isFakeScope()) {
      return;
    }

    checkRedeclared(t);
    final Symbol symbol =
        Symbol.newSymbol(t.getText(), kind, where(t), symbols.peek(), scoped);

    symbols.define(symbol);
  }

  protected final void declareAndPushSymbolScope(
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
  }

  protected void popSymbolScope() {
    symbols.pop();
  }

  private final void checkRedeclared(final Token t) throws SemanticException {
    InvariantChecks.checkNotNull(symbols);

    final Symbol symbol = symbols.resolve(t.getText());
    if (null == symbol) {// OK
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
    return null == revision || revisions.contains(revision.getText());
  }

  protected final void pushRevisionApplicable(final boolean applicable) {
    revisionApplicable.push(isRevisionApplicable() ? applicable : false);
  }

  protected final void popRevisionApplicable() {
    revisionApplicable.pop();
  }

  private boolean isRevisionApplicable() {
    return revisionApplicable.isEmpty() || revisionApplicable.peek();
  }

  private boolean isFakeScope() {
    final SymbolScope scope = symbols.peek();
    final Symbol symbol = scope.getAssociatedSymbol();

    return null != symbol && null == scope.resolve(symbol.getName());
  }
}
