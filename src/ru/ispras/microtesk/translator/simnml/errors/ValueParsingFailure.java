/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ValueParsingFailure.java, Oct 22, 2012 1:23:27 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.errors;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;

public class ValueParsingFailure implements ISemanticError
{
    private static final String FORMAT = "The '%s' token cannot be converted to the %s format."; 

    private final String valueText;
    private final String formatName;
    
    public ValueParsingFailure(String valueText, String formatName)
    {
        this.valueText  = valueText;
        this.formatName = formatName;
    }

    @Override
    public String getMessage()
    {
        return String.format(FORMAT, valueText, formatName);
    }
}
