/*
 * Copyright 2013-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.simnml.ir.shared;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.ispras.microtesk.translator.antlrex.symbols.ISymbol;
import ru.ispras.microtesk.translator.simnml.ESymbolKind;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;

public final class LetFactory extends WalkerFactoryBase {
  public LetFactory(WalkerContext context) {
    super(context);
  }

  public LetString createString(String name, String text) {
    return new LetString(name, text);
  }

  public LetConstant createConstant(String name, Expr value) {
    return new LetConstant(name, value);
  }

  public LetLabel createLabel(String name, String text) {
    final String ID_REX = "[a-zA-Z][\\w]*";
    final String INDEX_REX = "[\\[][\\d]+[\\]]";
    final String LABEL_REX = String.format("^%s(%s)?$", ID_REX, INDEX_REX);

    final Matcher matcher = Pattern.compile(LABEL_REX).matcher(text);
    if (!matcher.matches()) {
      return null;
    }

    final int indexPos = text.indexOf('[');
    final String memoryName = (-1 == indexPos) ? text : text.substring(0, indexPos);

    final ISymbol<ESymbolKind> symbol = getSymbols().resolve(memoryName);
    if ((null == symbol) || (symbol.getKind() != ESymbolKind.MEMORY)) {
      return null;
    }

    if (-1 == indexPos) {
      return new LetLabel(name, memoryName);
    }

    final int memoryIndex = Integer.parseInt(text.substring(indexPos + 1, text.length() - 1));
    return new LetLabel(name, memoryName, memoryIndex);
  }
}
