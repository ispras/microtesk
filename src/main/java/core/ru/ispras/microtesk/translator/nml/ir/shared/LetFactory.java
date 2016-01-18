/*
 * Copyright 2013-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.ir.shared;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.microtesk.translator.antlrex.symbols.Symbol;
import ru.ispras.microtesk.translator.nml.NmlSymbolKind;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.nml.ir.expression.Expr;

public final class LetFactory extends WalkerFactoryBase {
  private static final String    ID_REX = "[a-zA-Z][\\w]*";
  private static final String INDEX_REX = "[\\[][\\d]+[\\]]";
  private static final String LABEL_REX = String.format("^%s(%s)?$", ID_REX, INDEX_REX);

  public LetFactory(final WalkerContext context) {
    super(context);
  }

  public void createString(final String name, final String text) {
    final LetConstant constant = new LetConstant(name, new Expr(NodeValue.newString(text)));
    getIR().add(name, constant);

    createLabel(name, text);
  }

  public void createConstant(final String name, final Expr value) {
    final LetConstant constant = new LetConstant(name, value);
    getIR().add(name, constant);
  }

  private void createLabel(final String name, final String text) {
    final Matcher matcher = Pattern.compile(LABEL_REX).matcher(text);
    if (!matcher.matches()) {
      return;
    }

    final int indexPos = text.indexOf('[');
    final String memoryName = (-1 == indexPos) ? text : text.substring(0, indexPos);

    final Symbol symbol = getSymbols().resolve(memoryName);
    if ((null == symbol) || (symbol.getKind() != NmlSymbolKind.MEMORY)) {
      return;
    }

    final LetLabel label;
    if (-1 == indexPos) {
      label = new LetLabel(name, memoryName);
    } else {
      final int memoryIndex = Integer.parseInt(text.substring(indexPos + 1, text.length() - 1));
      label = new LetLabel(name, memoryName, memoryIndex);
    }

    getIR().add(name, label);
  }
}
