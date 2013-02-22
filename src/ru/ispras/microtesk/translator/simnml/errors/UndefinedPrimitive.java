/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * UndefinedPrimitive.java, Dec 27, 2012 6:44:35 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.errors;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;
import ru.ispras.microtesk.translator.simnml.ESymbolKind;

public final class UndefinedPrimitive implements ISemanticError
{
    private static final String FORMAT =
        "The '%s' primitive is not defined or does not have the '%s' type.";

    private final String name;
    private final ESymbolKind type;
    
    public UndefinedPrimitive(String name, ESymbolKind type)
    {
        this.name = name;
        this.type = type;
    }

    @Override
    public String getMessage()
    {
        return String.format(FORMAT, name, type.name());
    }
}
