/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * SizeExpressionTypeMismatch.java, Oct 22, 2012 1:21:35 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.errors;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;

public class SizeExpressionTypeMismatch implements ISemanticError
{
    public static final String FORMAT = "The evaluated expression has a wrong type: %s. An integer type is expected for size expressions.";

    private final Class<?> type;

    public SizeExpressionTypeMismatch(Class<?> type)
    {
        this.type = type;
    } 
    
    @Override
    public String getMessage()
    {
        return String.format(FORMAT, type.getSimpleName());
    }   
}
