/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * LetFactory.java, Apr 11, 2013 4:46:47 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.shared;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.ispras.microtesk.translator.antlrex.symbols.ISymbol;
import ru.ispras.microtesk.translator.simnml.ESymbolKind;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;

public final class LetFactory extends WalkerFactoryBase
{
    public LetFactory(WalkerContext context)
    {
        super(context);
    }

    public LetString createString(String name, String text)
    {
        return new LetString(name, text);
    }

    public LetConstant createConstant(String name, Expr value)
    {
        return new LetConstant(name, value);
    }

    public LetLabel createLabel(String name, String text)
    {
        final String    ID_REX = "[a-zA-Z][\\w]*";
        final String INDEX_REX = "[\\[][\\d]+[\\]]";
        final String LABEL_REX = String.format("^%s(%s)?$", ID_REX, INDEX_REX);

        final Matcher matcher = Pattern.compile(LABEL_REX).matcher(text);
        if (!matcher.matches())
            return null;

        final int indexPos = text.indexOf('[');
        final String memoryName = (-1 == indexPos) ? text : text.substring(0, indexPos);

        final ISymbol<ESymbolKind> symbol = getSymbols().resolve(memoryName);
        if ((null == symbol) || (symbol.getKind() != ESymbolKind.MEMORY))
            return null;

        if (-1 == indexPos)
            return new LetLabel(name, memoryName);

        final int memoryIndex =
            Integer.parseInt(text.substring(indexPos + 1, text.length() - 1));

        return new LetLabel(name, memoryName, memoryIndex);
    }
}
