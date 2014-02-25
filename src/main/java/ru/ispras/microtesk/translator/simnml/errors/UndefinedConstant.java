/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * UndefinedConstant.java, Jan 22, 2013 5:29:50 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.errors;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;

public final class UndefinedConstant implements ISemanticError
{
    private static final String FORMAT =
        "The '%s' symbol is not defined or is not a constant.";

    private final String symbolName; 

    public UndefinedConstant(String symbolName)
    {
        this.symbolName = symbolName;
    }

    @Override
    public String getMessage()
    {
        return String.format(FORMAT, symbolName);
    }
}