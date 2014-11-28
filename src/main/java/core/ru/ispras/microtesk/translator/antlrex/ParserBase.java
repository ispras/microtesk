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

package ru.ispras.microtesk.translator.antlrex;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

import ru.ispras.microtesk.translator.antlrex.symbols.*;
import ru.ispras.microtesk.translator.antlrex.errors.RedeclaredSymbol;
import ru.ispras.microtesk.translator.antlrex.errors.SymbolTypeMismatch;
import ru.ispras.microtesk.translator.antlrex.errors.UndeclaredSymbol;
import ru.ispras.microtesk.translator.antlrex.errors.UnrecognizedStructure;

public class ParserBase extends ParserEx {
  private SymbolTable symbols = null;

  public ParserBase(TokenStream input, RecognizerSharedState state) {
    super(input, state);
  }

  public final void assignSymbols(SymbolTable symbols) {
    this.symbols = symbols;
  }

  protected final void declare(Token t, Enum<?> kind, boolean scoped) throws SemanticException {
    if (null == symbols) {
      throw new NullPointerException();
    }

    checkRedeclared(t);

    final ISymbol symbol = scoped ?
        new ScopedSymbol(t, kind, symbols.peek()) :
        new Symbol(t, kind, symbols.peek());

    symbols.define(symbol);
  }

  protected final void declareAndPushSymbolScope(Token t, Enum<?> kind)
      throws SemanticException {
    if (null == symbols) {
      throw new NullPointerException();
    }

    checkRedeclared(t);
    final ISymbol symbol = new ScopedSymbol(t, kind, symbols.peek());

    symbols.define(symbol);
    symbols.push(symbol.getInnerScope());
  }

  protected void popSymbolScope() {
    symbols.pop();
  }

  private final void checkRedeclared(final Token t) throws SemanticException {
    if (null == symbols) {
      throw new NullPointerException();
    }

    final ISymbol symbol = symbols.resolve(t.getText());
    if (null == symbol) {// OK
      return;
    }

    raiseError(where(t), new RedeclaredSymbol(symbol));
  }

  protected final void checkDeclaration(Token t, Enum<?> expectedKind) throws SemanticException {
    if (null == symbols) {
      throw new NullPointerException();
    }

    final ISymbol symbol = symbols.resolve(t.getText());
    if (null == symbol) {
      raiseError(new UndeclaredSymbol(t.getText()));
    }

    if (expectedKind != symbol.getKind()) {
      raiseError(new SymbolTypeMismatch(t.getText(), symbol.getKind(), expectedKind));
    }
  }

  protected final boolean isDeclaredAs(Token t, Enum<?> expectedKind) {
    if (null == symbols) {
      throw new NullPointerException();
    }

    final ISymbol symbol = symbols.resolve(t.getText());
    if (null == symbol) {
      return false;
    }

    if (expectedKind != symbol.getKind()) {
      return false;
    }

    return true;
  }

  protected void checkNotNull(Token t, Object obj) throws RecognitionException {
    if (null == obj) {
      raiseError(where(t), new UnrecognizedStructure());
    }
  }
}
