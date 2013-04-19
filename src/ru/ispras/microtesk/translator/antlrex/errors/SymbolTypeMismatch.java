/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * SymbolTypeMismatch.java, Oct 22, 2012 1:22:33 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.antlrex.errors;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;

public final class SymbolTypeMismatch<Kind extends Enum<Kind>> implements ISemanticError
{
    public static final String FORMAT =
        "The '%s' symbol uses a wrong type. It is %s while %s is expected in this expression.";

    private final String symbolName;
    private final Kind         kind;
    private final Kind[]   expected;

    public SymbolTypeMismatch(String symbolName, Kind kind, Kind ... expected)
    {
        this.symbolName = symbolName;
        this.kind       = kind;
        this.expected   = expected;
    }

    private static <Kind extends Enum<Kind>> String kindsToString(Kind[] kinds)
    {
        final StringBuffer sb = new StringBuffer(); 

        if (kinds.length > 1) sb.append('{');
        for (Kind k : kinds)
        {
            if (sb.length() > 0) sb.append(", ");
            sb.append(k.name());
        }
        if (kinds.length > 1) sb.append('}');

        return sb.toString();
    }

    @Override
    public String getMessage()
    {
        return String.format(FORMAT, symbolName, kind, kindsToString(expected));
    }
}
