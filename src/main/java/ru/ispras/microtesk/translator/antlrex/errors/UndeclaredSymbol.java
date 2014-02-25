/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * UndeclaredSymbol.java, Oct 22, 2012 1:22:46 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.antlrex.errors;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;

public class UndeclaredSymbol implements ISemanticError
{
    private static final String FORMAT = "The '%s' symbol is not declared.";

    private final String symbolName; 

    public UndeclaredSymbol(String symbolName)
    {
        this.symbolName = symbolName;
    }

    @Override
    public String getMessage()
    {
        return String.format(FORMAT, symbolName);
    }
}
