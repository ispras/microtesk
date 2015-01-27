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

import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.TreeNodeStream;

import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.TreeParserEx;
import ru.ispras.microtesk.translator.antlrex.symbols.ISymbol;
import ru.ispras.microtesk.translator.antlrex.symbols.ScopedSymbol;
import ru.ispras.microtesk.translator.antlrex.symbols.Symbol;
import ru.ispras.microtesk.translator.antlrex.symbols.SymbolTable;

import ru.ispras.microtesk.translator.antlrex.errors.RedeclaredSymbol;
import ru.ispras.microtesk.translator.antlrex.errors.SymbolTypeMismatch;
import ru.ispras.microtesk.translator.antlrex.errors.UndeclaredSymbol;

/**
 * The TreeParserBase class is a base class for implementing ANTLR-based tree walkers.
 * It includes support for error reporting (inherited from {@link TreeParserEx})
 * and support for working with symbol tables.
 * 
 * @author Andrei Tatarnikov
 */

public class TreeParserBase extends TreeParserEx {
  private SymbolTable symbols;

  public TreeParserBase(TreeNodeStream input, RecognizerSharedState state) {
    super(input, state);
    this.symbols = null;
  }

  public final void assignSymbols(SymbolTable symbols) {
    InvariantChecks.checkNotNull(symbols);
    this.symbols = symbols;
  }

  public final SymbolTable getSymbols() {
    return symbols;
  }

  protected final void checkRedeclared(CommonTree current) throws SemanticException {
    InvariantChecks.checkNotNull(symbols);

    final ISymbol symbol = symbols.resolve(current.getText());

    if (null != symbol) {
      raiseError(where(current), new RedeclaredSymbol(symbol));
    }
  }

  protected final boolean isDeclaredAs(CommonTree t, Enum<?> expectedKind) {
    InvariantChecks.checkNotNull(symbols);

    final ISymbol symbol = symbols.resolve(t.getText());
    if (null == symbol) {
      return false;
    }

    if (expectedKind != symbol.getKind()) {
      return false;
    }

    return true;
  }

  protected final void declare(
      CommonTree t, Enum<?> kind, boolean scoped) throws SemanticException {

    InvariantChecks.checkNotNull(symbols);

    checkRedeclared(t);

    final ISymbol symbol = scoped ?
        new ScopedSymbol(t.getToken(), kind, symbols.peek()) :
        new Symbol(t.getToken(), kind, symbols.peek());

    symbols.define(symbol);
  }
  
  protected final void declareAndPushSymbolScope(
      CommonTree t, Enum<?> kind) throws SemanticException {
    InvariantChecks.checkNotNull(symbols);

    checkRedeclared(t);
    final ISymbol symbol = new ScopedSymbol(t.getToken(), kind, symbols.peek());

    symbols.define(symbol);
    symbols.push(symbol.getInnerScope());
  }

  protected final void checkMemberDeclared(
      CommonTree t, Enum<?> expectedKind) throws SemanticException {

    InvariantChecks.checkNotNull(symbols);

    final ISymbol symbol = symbols.resolveMember(t.getText());
    if (null == symbol) {
      raiseError(where(t), new UndeclaredSymbol(t.getText()));
    }

    if (expectedKind != symbol.getKind()) {
      raiseError(
        where(t),
        new SymbolTypeMismatch(t.getText(),
        symbol.getKind(),
        expectedKind)
      );
    }
  }

  protected final void pushSymbolScope(CommonTree scopeID) {
    InvariantChecks.checkNotNull(symbols);

    final ISymbol scopeSymbol = symbols.resolve(scopeID.getText());

    if (null == scopeSymbol) {
      throw new IllegalStateException(String.format(
         "The %s symbol must be registered in the symbol table.", scopeID.getText()));
    }

    if (null == scopeSymbol.getInnerScope()) { 
      throw new IllegalStateException(String.format(
          "The %s symbol must be a scoped symbol.", scopeID.getText()));
    }

    symbols.push(scopeSymbol.getInnerScope());
  }

  protected final void popSymbolScope() {
    symbols.pop();
  }
}
