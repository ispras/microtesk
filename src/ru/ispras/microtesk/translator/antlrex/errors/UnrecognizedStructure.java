/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * UnrecognizedStructure.java, Oct 22, 2012 1:22:58 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.antlrex.errors;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;

public final class UnrecognizedStructure implements ISemanticError
{
    private static final String MESSAGE =
        "Failed to recognize the grammar structure. It will be ignored";

    private final String what;

    public UnrecognizedStructure(String what)
    {
        this.what = what;
    }

    public UnrecognizedStructure()
    {
        this.what = null;
    }

    @Override
    public String getMessage()
    {
        if (null == what)
            return MESSAGE + "."; 

        return String.format("%s: '%s'.", MESSAGE, what);
    }
}
