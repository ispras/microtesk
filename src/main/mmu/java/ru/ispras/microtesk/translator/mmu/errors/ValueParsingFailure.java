package ru.ispras.microtesk.translator.mmu.errors;

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
