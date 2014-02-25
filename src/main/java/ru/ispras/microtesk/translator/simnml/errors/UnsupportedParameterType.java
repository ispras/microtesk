/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * UnsupportedParameterType.java, Dec 26, 2012 3:26:51 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.errors;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;

public final class UnsupportedParameterType implements ISemanticError
{
    private static final String FORMAT = 
        "The '%s' parameter has unsupported type (%s). This construction supports only %s.";
    
    private final String name;
    private final String kind;
    private final String expectedKinds;

    public UnsupportedParameterType(
        String name,
        String kind,
        String expectedKinds)
    {
        this.name = name;
        this.kind = kind;
        this.expectedKinds = expectedKinds;
    }

    @Override
    public String getMessage()
    {
        return String.format(FORMAT, name, kind, expectedKinds);
    }        
}
