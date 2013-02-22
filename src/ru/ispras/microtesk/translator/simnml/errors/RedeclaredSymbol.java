/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * RedeclaredSymbol.java, Oct 22, 2012 1:15:36 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.errors;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;
import ru.ispras.microtesk.translator.antlrex.symbols.BuiltInSymbol;
import ru.ispras.microtesk.translator.antlrex.symbols.ISymbol;

public final class RedeclaredSymbol implements ISemanticError
{
    private static final String FORMAT =
        "The '%s' name is already used to declare another symbol of type %s (position %d:%d).";
    private final String FORMAT_KEYWORD =
        "The '%s' name is already used a reserved keyword (the %s type).";

    private final ISymbol<?> symbol;

    public RedeclaredSymbol(ISymbol<?> symbol)
    {
        this.symbol = symbol;
    }

    @Override
    public String getMessage()
    {
        if (symbol instanceof BuiltInSymbol)
            return String.format(FORMAT_KEYWORD, symbol.getName(), symbol.getKind().name());

        return String.format(
            FORMAT,
            symbol.getName(),
            symbol.getKind().name(),
            symbol.getLine(),
            symbol.getPositionInLine()
            );
    }
}
