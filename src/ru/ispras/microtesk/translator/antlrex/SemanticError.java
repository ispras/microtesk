/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * SemanticError.java, Oct 10, 2013 9:02:58 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.antlrex;

public class SemanticError implements ISemanticError
{
    private final String message; 

    public SemanticError(String message)
    {
        assert null != message;
        this.message = message;
    }

    @Override
    public String getMessage()
    {
        return message;
    }
}
